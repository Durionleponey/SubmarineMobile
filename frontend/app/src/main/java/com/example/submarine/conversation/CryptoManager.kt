package com.example.submarine.conversation

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val ALIAS = "Submarine_E2EE_KeyPair"

    // Initialisation du Keystore (coffre-fort Android)
    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    /**
     * 1. GESTION DES CLÉS
     * Vérifie si des clés existent, sinon les crée.
     * Retourne la CLÉ PUBLIQUE en format String (Base64) prête à être envoyée au serveur.
     */
    fun getMyPublicKey(): String {
        if (!keyStore.containsAlias(ALIAS)) {
            generateKeyPair()
        }
        val entry = keyStore.getEntry(ALIAS, null) as? KeyStore.PrivateKeyEntry
        return if (entry != null) {
            Base64.encodeToString(entry.certificate.publicKey.encoded, Base64.NO_WRAP)
        } else {
            ""
        }
    }

    private fun generateKeyPair() {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA,
            ANDROID_KEYSTORE
        )
        val spec = KeyGenParameterSpec.Builder(
            ALIAS,
            KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT
        )
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
            .setKeySize(2048)
            .build()

        keyPairGenerator.initialize(spec)
        keyPairGenerator.generateKeyPair()
    }

    /**
     * 2. UTILITAIRE
     * Convertit une String (reçue du serveur) en objet PublicKey utilisable
     */
    fun stringToPublicKey(base64PublicKey: String): PublicKey {
        val decoded = Base64.decode(base64PublicKey, Base64.DEFAULT)
        val spec = X509EncodedKeySpec(decoded)
        val factory = KeyFactory.getInstance("RSA")
        return factory.generatePublic(spec)
    }

    /**
     * 3. CHIFFREMENT (Envoi)
     * Utilise une méthode hybride :
     * - Crée une clé AES unique (rapide) pour chiffrer le message.
     * - Chiffre cette clé AES avec la clé RSA Publique du destinataire.
     * Retourne : "CléAES_Chiffrée|IV|Message_Chiffré"
     */
    fun encrypt(message: String, recipientPublicKey: PublicKey): String {
        // A. Générer une clé AES jetable (Session Key)
        val aesKey = generateAesKey()

        // B. Chiffrer le message avec AES
        val cipherAes = Cipher.getInstance("AES/GCM/NoPadding")
        cipherAes.init(Cipher.ENCRYPT_MODE, aesKey)
        val encryptedMessageBytes = cipherAes.doFinal(message.toByteArray(Charsets.UTF_8))
        val iv = cipherAes.iv // Vecteur d'initialisation (indispensable pour déchiffrer)

        // C. Chiffrer la clé AES avec la clé publique RSA du destinataire
        val cipherRsa = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipherRsa.init(Cipher.ENCRYPT_MODE, recipientPublicKey)
        val encryptedAesKey = cipherRsa.doFinal(aesKey.encoded)

        // D. Encoder le tout en Base64 pour le transport
        val encKeyB64 = Base64.encodeToString(encryptedAesKey, Base64.NO_WRAP)
        val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val encMsgB64 = Base64.encodeToString(encryptedMessageBytes, Base64.NO_WRAP)

        // On sépare les éléments par des barres verticales "|"
        return "$encKeyB64|$ivB64|$encMsgB64"
    }

    /**
     * 4. DÉCHIFFREMENT (Réception)
     * Lit le format "CléAES|IV|Message" et utilise NOTRE clé privée pour lire.
     */
    fun decrypt(fullContent: String): String {
        try {
            val parts = fullContent.split("|")
            if (parts.size != 3) return "Message non chiffré ou format invalide"

            val encKeyB64 = parts[0]
            val ivB64 = parts[1]
            val encMsgB64 = parts[2]

            // A. Récupérer MA clé privée dans le Keystore
            val privateKeyEntry = keyStore.getEntry(ALIAS, null) as? KeyStore.PrivateKeyEntry
                ?: return "Erreur: Clé privée introuvable"

            // B. Déchiffrer la clé AES avec ma clé RSA privée
            val cipherRsa = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipherRsa.init(Cipher.DECRYPT_MODE, privateKeyEntry.privateKey)
            val aesKeyBytes = cipherRsa.doFinal(Base64.decode(encKeyB64, Base64.DEFAULT))

            // Reconstitution de la clé AES
            val aesKey = SecretKeySpec(aesKeyBytes, "AES")

            // C. Déchiffrer le message avec la clé AES
            val cipherAes = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, Base64.decode(ivB64, Base64.DEFAULT))
            cipherAes.init(Cipher.DECRYPT_MODE, aesKey, spec)

            val decryptedBytes = cipherAes.doFinal(Base64.decode(encMsgB64, Base64.DEFAULT))
            return String(decryptedBytes, Charsets.UTF_8)

        } catch (e: Exception) {
            e.printStackTrace()
            return "Échec déchiffrement (Message illisible)"
        }
    }

    private fun generateAesKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        return keyGen.generateKey()
    }
    fun isEncrypted(content: String): Boolean {
        // "base64key|base64iv|base64msg"
        return content.count { it == '|' } == 2
    }
}