package com.example.submarine.listeContact

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.submarine.bio.EditBioActivity
import com.example.submarine.conversation.ConversationActivity
import com.example.submarine.graphql.GetFriendsListQuery
import com.example.submarine.graphql.RemoveFriendMutation
import com.example.submarine.listeContact.API.FriendsApi
import com.example.submarine.network.Apollo
import com.example.submarine.network.GraphQLRequest
import com.example.submarine.network.RetrofitInstance
import com.example.submarine.network.TokenProvider
import com.example.submarine.ui.theme.SubmarineTheme
import kotlinx.coroutines.launch

class ContactsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SubmarineTheme {
                ContactsScreen(
                    onBack = { finish() },
                    onAddFriendClick = {
                        val intent = Intent(this, AddContactActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    onBack: () -> Unit,
    onAddFriendClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    // --- GESTION DU RETOUR ET DU DRAWER ---
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var lastBackPressTime by remember { mutableLongStateOf(0L) }


    BackHandler(enabled = true) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000) {
                (context as? Activity)?.moveTaskToBack(true)
            } else {
                lastBackPressTime = currentTime
                Toast.makeText(context, "Appuyez à nouveau pour quitter", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- ÉTATS UTILISATEUR ---
    var myPseudo by remember { mutableStateOf("Chargement...") }
    var myBio by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val token = TokenProvider.token

    LaunchedEffect(token) {
        Log.d("TOKEN_CHECK", "Token: ${token?.take(10)}... | Est-il vide: ${token.isNullOrEmpty()}")
    }
    // États pour la modification du pseudo utilisateur
    var showEditPseudoDialog by remember { mutableStateOf(false) }
    var newPseudoInput by remember { mutableStateOf("") }
    var isUpdatingPseudo by remember { mutableStateOf(false) }

    // États pour la personnalisation des contacts
    var showActionsDialog by remember { mutableStateOf(false) }
    var showBioDialog by remember { mutableStateOf(false) }
    var showEditContactNameDialog by remember { mutableStateOf(false) }
    var contactNameInput by remember { mutableStateOf("") }

    // --- LISTE AMIS ---
    var searchQuery by remember { mutableStateOf("") }
    var contacts by remember { mutableStateOf<List<GetFriendsListQuery.FriendsList>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedContact by remember { mutableStateOf<GetFriendsListQuery.FriendsList?>(null) }

    // --- FONCTION : CHARGER LE PROFIL ---
    fun fetchUserData() {
        if (token.isNullOrEmpty()) return

        scope.launch {
            try {
                val query = """
                    query {
                        meAll {
                            pseudo
                            bio
                        }
                    }
                """
                val request = GraphQLRequest(query = query)
                val response = RetrofitInstance.graphqlApi.executeGraphQL<Map<String, Any>>(
                    token = "Bearer $token",
                    request = request
                )

                if (response.isSuccessful && response.body()?.data != null) {
                    val dataMap = response.body()?.data as? Map<*, *>
                    val meData = dataMap?.get("meAll") as? Map<*, *>

                    if (meData != null) {
                        myPseudo = meData["pseudo"] as? String ?: "Utilisateur"
                        myBio = meData["bio"] as? String ?: ""
                    }
                }
            } catch (e: Exception) {
                Log.e("ContactsActivity", "Erreur chargement profil", e)
            }
        }
    }

    // --- FONCTION : METTRE À JOUR LE PSEUDO UTILISATEUR ---
    fun updateMyPseudo(newPseudo: String) {
        if (token.isNullOrEmpty()) return
        isUpdatingPseudo = true

        scope.launch {
            try {
                val query = """
                    mutation UpdateMyPseudo(${"$"}pseudoVal: String!) {
                      updatePseudo(updateUserPseudo: { pseudo: ${"$"}pseudoVal }) {
                        pseudo
                      }
                    }
                """

                val variables = mapOf("pseudoVal" to newPseudo)
                val request = GraphQLRequest(query = query, variables = variables)

                val response = RetrofitInstance.graphqlApi.executeGraphQL<Map<String, Any>>(
                    token = "Bearer $token",
                    request = request
                )

                val errors = response.body()?.errors

                if (errors != null && errors.isNotEmpty()) {
                    val firstError = errors[0] as? Map<*, *>
                    val errorMsg = firstError?.get("message")?.toString() ?: "Erreur inconnue"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                } else if (response.isSuccessful && response.body()?.data != null) {
                    myPseudo = newPseudo
                    showEditPseudoDialog = false
                    Toast.makeText(context, "Pseudo modifié avec succès !", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Erreur inconnue lors de la modification", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("ContactsActivity", "Erreur update pseudo", e)
                Toast.makeText(context, "Erreur réseau : ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isUpdatingPseudo = false
            }
        }
    }

    // --- FONCTION : SUPPRIMER UN AMI ---
    fun removeFriend(relationId: String) {
        if (token.isNullOrEmpty()) return

        scope.launch {
            try {
                val client = Apollo.apolloClient
                val response = client.mutation(RemoveFriendMutation(relationId)).execute()

                if (response.hasErrors()) {
                    snackbarHostState.showSnackbar(response.errors!!.first().message)
                    return@launch
                }

                contacts = contacts.filterNot { it.relationId == relationId }

                showDeleteConfirmDialog = false
                showActionsDialog = false
                snackbarHostState.showSnackbar("Ami supprimé")

            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Erreur réseau : ${e.message}")
            }
        }
    }

    // --- CYCLE DE VIE ---
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                fetchUserData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        if (!token.isNullOrEmpty()) {
            try {
                contacts = FriendsApi.getFriendsList(token!!)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Erreur lors du chargement"
            }
        }
    }

    // --- LISTE FILTRÉE & NOM PERSONNALISÉ ---
    val filtered = contacts.map { contact ->
        val userId = contact.user._id
        val customName = ContactPrefs.getCustomName(context, userId)
        val displayName = customName ?: contact.user.pseudo
        Triple(contact, displayName, customName)
    }.filter { (contact, displayName, _) ->
        (displayName + " " + (contact.user.email ?: "")).contains(searchQuery, ignoreCase = true)
    }

    // --- UI ---
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.clickable {
                            newPseudoInput = myPseudo
                            showEditPseudoDialog = true
                        }
                    ) {
                        Text(
                            text = myPseudo,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modifier pseudo",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        val displayBio = if (myBio.isNotEmpty()) {
                            if (myBio.length > 35) myBio.take(35) + "..." else myBio
                        } else {
                            "Ajouter une bio..."
                        }

                        Text(
                            text = displayBio,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = if (myBio.isEmpty()) FontStyle.Italic else FontStyle.Normal,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                val intent = Intent(context, EditBioActivity::class.java)
                                context.startActivity(intent)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier la bio",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            TokenProvider.token = null
                            (context as? Activity)?.finish()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Se déconnecter")
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Mes Contacts") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Ouvrir le menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = onAddFriendClick) {
                            Icon(Icons.Filled.PersonAdd, contentDescription = "Ajouter un contact")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Rechercher un ami") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(filtered) { (contact, displayName, _) ->
                        ContactRow(
                            name = displayName,
                            onClick = {
                                val intent = Intent(context, ConversationActivity::class.java).apply {
                                    putExtra("contactId", contact.user._id)
                                }
                                context.startActivity(intent)
                            },
                            onLongPress = {
                                selectedContact = contact
                                Log.d("FRIEND_ITEM", "Contact = $contact")
                                showActionsDialog = true
                            },
                            isMuted = ContactPrefs.isMuted(context, contact.user._id)
                        )
                    }
                }
            }
        }
    }

    // --- POPUP: MODIFICATION PSEUDO UTILISATEUR ---
    if (showEditPseudoDialog) {
        AlertDialog(
            onDismissRequest = { if (!isUpdatingPseudo) showEditPseudoDialog = false },
            title = { Text("Changer votre pseudo") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPseudoInput,
                        onValueChange = { newPseudoInput = it },
                        label = { Text("Nouveau pseudo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Le pseudo doit être unique.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { updateMyPseudo(newPseudoInput) },
                    enabled = !isUpdatingPseudo && newPseudoInput.isNotBlank() && newPseudoInput != myPseudo
                ) {
                    if (isUpdatingPseudo) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Enregistrer")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditPseudoDialog = false },
                    enabled = !isUpdatingPseudo
                ) {
                    Text("Annuler")
                }
            }
        )
    }

    // --- POPUP: ACTIONS CONTACT ---
    if (showActionsDialog && selectedContact != null) {
        val contact = selectedContact!!
        AlertDialog(
            onDismissRequest = { showActionsDialog = false },
            title = { Text("Options pour ${contact.user.pseudo}") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showActionsDialog = false
                            showBioDialog = true
                        }
                    ) { Text("Voir la bio") }
                    // Renommer le contact
                    TextButton(
                        onClick = {
                            showActionsDialog = false
                            contactNameInput = ContactPrefs
                                .getCustomName(context, contact.user._id)
                                ?: contact.user.pseudo
                            showEditContactNameDialog = true
                        }
                    ) { Text("Renommer ce contact") }

                    TextButton(
                        onClick = {
                            showActionsDialog = false
                            showDeleteConfirmDialog = true
                        }
                    ) {
                        Text(
                            "❌ Supprimer cet ami",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    // --- POPUP: RENOMMER UN CONTACT ---
    if (showEditContactNameDialog && selectedContact != null) {
        val contact = selectedContact!!
        val userId = contact.user._id

        AlertDialog(
            onDismissRequest = { showEditContactNameDialog = false },
            title = { Text("Renommer ${contact.user.pseudo}") },
            text = {
                OutlinedTextField(
                    value = contactNameInput,
                    onValueChange = { contactNameInput = it },
                    label = { Text("Nom personnalisé") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalName = contactNameInput.trim()
                        ContactPrefs.setCustomName(
                            context,
                            userId,
                            if (finalName == contact.user.pseudo) null else finalName
                        )
                        showEditContactNameDialog = false
                    },
                    enabled = contactNameInput.isNotBlank()
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditContactNameDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // --- POPUP: CONFIRMATION SUPPRESSION ---
    if (showDeleteConfirmDialog && selectedContact != null) {
        val contact = selectedContact!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Supprimer ${contact.user.pseudo} ?") },
            text = { Text("Voulez-vous vraiment supprimer cet ami de votre liste ?") },
            confirmButton = {
                Button(
                    onClick = { removeFriend(contact.relationId) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // --- POPUP: BIO CONTACT ---
    if (showBioDialog && selectedContact != null) {
        val contact = selectedContact!!
        AlertDialog(
            onDismissRequest = { showBioDialog = false },
            title = { Text("Bio de ${contact.user.pseudo}") },
            text = { Text(contact.user.bio ?: "Aucune bio", fontSize = 16.sp) },
            confirmButton = {
                TextButton(onClick = { showBioDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }
}

@Composable
private fun ContactRow(
    name: String,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    isMuted: Boolean
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.03f else 1f,
        label = "press-scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .combinedClickable(
                onClick = { isPressed = false; onClick() },
                onLongClick = {
                    isPressed = true
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                    isPressed = false
                }
            )
            .padding(16.dp)
    ) {
        Text(
            text = name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (isMuted)
                MaterialTheme.colorScheme.onSurfaceVariant
            else
                MaterialTheme.colorScheme.onSurface
        )
        if (isMuted) {
            Text(
                text = "En sourdine",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    HorizontalDivider()
}

// --- PREFS POUR PERSONNALISER CONTACTS ---
object ContactPrefs {
    private const val PREFS_NAME = "contact_prefs"
    private const val KEY_PREFIX_NAME = "contact_name_"
    private const val KEY_PREFIX_MUTED = "contact_muted_"

    private fun prefs(context: android.content.Context) =
        context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)

    fun getCustomName(context: android.content.Context, userId: String): String? =
        prefs(context).getString(KEY_PREFIX_NAME + userId, null)

    fun setCustomName(context: android.content.Context, userId: String, name: String?) {
        prefs(context).edit().apply {
            if (name.isNullOrBlank()) remove(KEY_PREFIX_NAME + userId)
            else putString(KEY_PREFIX_NAME + userId, name)
        }.apply()
    }

    fun isMuted(context: android.content.Context, userId: String): Boolean =
        prefs(context).getBoolean(KEY_PREFIX_MUTED + userId, false)

    fun setMuted(context: android.content.Context, userId: String, muted: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_PREFIX_MUTED + userId, muted)
            .apply()
    }
}
