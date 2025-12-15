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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox // Important pour le refresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import com.example.submarine.conversation.CryptoManager
import com.example.submarine.conversation.UserService
import com.example.submarine.graphql.GetFriendsListQuery
import com.example.submarine.graphql.RemoveFriendMutation
import com.example.submarine.listeContact.API.FriendsApi
import com.example.submarine.network.Apollo
import com.example.submarine.network.GraphQLRequest
import com.example.submarine.network.RetrofitInstance
import com.example.submarine.network.TokenProvider
import com.example.submarine.ui.theme.SubmarineTheme
import kotlinx.coroutines.delay
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

    // --- ÉTATS GLOBAUX ---
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val token = TokenProvider.token

    // États pour le double retour arrière
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    // États Données
    var myPseudo by remember { mutableStateOf("Chargement...") }
    var myBio by remember { mutableStateOf("") }
    var contacts by remember { mutableStateOf<List<GetFriendsListQuery.FriendsList>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // État Refresh
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()

    // États UI Divers (Dialogs)
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showEditPseudoDialog by remember { mutableStateOf(false) }
    var newPseudoInput by remember { mutableStateOf("") }
    var isUpdatingPseudo by remember { mutableStateOf(false) }
    var showActionsDialog by remember { mutableStateOf(false) }
    var showBioDialog by remember { mutableStateOf(false) }
    var showEditContactNameDialog by remember { mutableStateOf(false) }
    var contactNameInput by remember { mutableStateOf("") }
    var selectedContact by remember { mutableStateOf<GetFriendsListQuery.FriendsList?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // --- FONCTION : CHARGER TOUT (PROFIL + CONTACTS) ---
    // On regroupe tout ici pour faciliter le "Refresh"
    fun fetchAllData() {
        if (token.isNullOrEmpty()) return
        scope.launch {
            isRefreshing = true
            errorMessage = null

            // 1. Charger Profil
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
                Log.e("ContactsActivity", "Erreur profil", e)
            }

            // 2. Charger Contacts
            try {
                contacts = FriendsApi.getFriendsList(token!!)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Erreur chargement contacts"
            }

            // 3. Envoyer clés (optionnel à chaque refresh mais utile)
            try {
                val myPublicKey = CryptoManager.getMyPublicKey()
                UserService.sendPublicKey(myPublicKey)
            } catch (e: Exception) {
                Log.e("CryptoKey", "Échec envoi clé", e)
            }

            // Petit délai pour l'UX si c'est trop rapide
            delay(500)
            isRefreshing = false
        }
    }

    // --- GESTION DU RETOUR (DOUBLE TAP) ---
    BackHandler(enabled = true) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000) {
                // Si moins de 2 secondes : Retour à l'écran d'accueil du téléphone
                (context as? Activity)?.moveTaskToBack(true)
            } else {
                // Premier appui : Toast
                lastBackPressTime = currentTime
                Toast.makeText(context, "Appuyez à nouveau pour quitter", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- FONCTIONS ACTIONS (Pseudo, Remove...) ---
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
                    Toast.makeText(context, "Pseudo modifié !", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erreur réseau", Toast.LENGTH_SHORT).show()
            } finally {
                isUpdatingPseudo = false
            }
        }
    }

    fun removeFriend(relationId: String) {
        if (token.isNullOrEmpty()) return
        scope.launch {
            try {
                val client = Apollo.apolloClient
                val response = client.mutation(RemoveFriendMutation(relationId)).execute()
                if (response.hasErrors()) {
                    snackbarHostState.showSnackbar(response.errors!!.first().message)
                } else {
                    contacts = contacts.filterNot { it.relationId == relationId }
                    showDeleteConfirmDialog = false
                    showActionsDialog = false
                    snackbarHostState.showSnackbar("Ami supprimé")
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Erreur réseau")
            }
        }
    }

    // --- CYCLE DE VIE ---
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // On recharge si on revient sur l'écran
                if (contacts.isEmpty()) fetchAllData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        fetchAllData()
    }

    // --- LISTE FILTRÉE ---
    val filtered = contacts.map { contact ->
        val userId = contact.user._id
        val customName = ContactPrefs.getCustomName(context, userId)
        val displayName = customName ?: contact.user.pseudo
        Triple(contact, displayName, customName)
    }.filter { (contact, displayName, _) ->
        (displayName + " " + (contact.user.email ?: "")).contains(searchQuery, ignoreCase = true)
    }

    // --- UI PRINCIPALE ---
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Icon(Icons.Default.AccountCircle, "Avatar", Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.clickable { newPseudoInput = myPseudo; showEditPseudoDialog = true }
                    ) {
                        Text(myPseudo, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Edit, "Edit", Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        val displayBio = if (myBio.isNotEmpty()) if (myBio.length > 35) myBio.take(35) + "..." else myBio else "Ajouter une bio..."
                        Text(displayBio, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, false))
                        IconButton(onClick = { context.startActivity(Intent(context, EditBioActivity::class.java)) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Edit, "Edit Bio", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { TokenProvider.token = null; (context as? Activity)?.finish() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ExitToApp, null)
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
                            Icon(Icons.Default.Menu, "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = onAddFriendClick) {
                            Icon(Icons.Filled.PersonAdd, "Ajouter")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { padding ->

            // --- ZONE DE REFRESH ---
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { fetchAllData() },
                state = pullRefreshState,
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Rechercher un ami") },
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    )

                    if (errorMessage != null) {
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                        // Permet de swiper même si la liste est vide ou petite
                        verticalArrangement = if (filtered.isEmpty()) Arrangement.Center else Arrangement.Top
                    ) {
                        if (filtered.isEmpty() && !isRefreshing) {
                            item {
                                Text(
                                    text = "Aucun contact trouvé",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
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
                                    showActionsDialog = true
                                },
                                isMuted = ContactPrefs.isMuted(context, contact.user._id)
                            )
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS (Reste inchangé ou presque) ---
    // (J'ai gardé la logique des Dialogs identique à ton code original pour la lisibilité)

    if (showEditPseudoDialog) {
        AlertDialog(
            onDismissRequest = { if (!isUpdatingPseudo) showEditPseudoDialog = false },
            title = { Text("Changer votre pseudo") },
            text = {
                Column {
                    OutlinedTextField(value = newPseudoInput, onValueChange = { newPseudoInput = it }, label = { Text("Nouveau pseudo") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text("Le pseudo doit être unique.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = { updateMyPseudo(newPseudoInput) }, enabled = !isUpdatingPseudo && newPseudoInput.isNotBlank() && newPseudoInput != myPseudo) {
                    if (isUpdatingPseudo) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Enregistrer")
                }
            },
            dismissButton = { TextButton(onClick = { showEditPseudoDialog = false }, enabled = !isUpdatingPseudo) { Text("Annuler") } }
        )
    }

    if (showActionsDialog && selectedContact != null) {
        val contact = selectedContact!!
        AlertDialog(
            onDismissRequest = { showActionsDialog = false },
            title = { Text("Options pour ${contact.user.pseudo}") },
            text = {
                Column {
                    TextButton(onClick = { showActionsDialog = false; showBioDialog = true }) { Text("Voir la bio") }
                    TextButton(onClick = { showActionsDialog = false; contactNameInput = ContactPrefs.getCustomName(context, contact.user._id) ?: contact.user.pseudo; showEditContactNameDialog = true }) { Text("Renommer ce contact") }
                    TextButton(onClick = { showActionsDialog = false; showDeleteConfirmDialog = true }) { Text("❌ Supprimer cet ami", color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {}, dismissButton = {}
        )
    }

    if (showEditContactNameDialog && selectedContact != null) {
        val contact = selectedContact!!
        AlertDialog(
            onDismissRequest = { showEditContactNameDialog = false },
            title = { Text("Renommer ${contact.user.pseudo}") },
            text = { OutlinedTextField(value = contactNameInput, onValueChange = { contactNameInput = it }, label = { Text("Nom personnalisé") }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                Button(onClick = { ContactPrefs.setCustomName(context, contact.user._id, if (contactNameInput.trim() == contact.user.pseudo) null else contactNameInput.trim()); showEditContactNameDialog = false }, enabled = contactNameInput.isNotBlank()) { Text("Enregistrer") }
            },
            dismissButton = { TextButton(onClick = { showEditContactNameDialog = false }) { Text("Annuler") } }
        )
    }

    if (showDeleteConfirmDialog && selectedContact != null) {
        val contact = selectedContact!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Supprimer ${contact.user.pseudo} ?") },
            text = { Text("Voulez-vous vraiment supprimer cet ami ?") },
            confirmButton = { Button(onClick = { removeFriend(contact.relationId) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Supprimer") } },
            dismissButton = { TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Annuler") } }
        )
    }

    if (showBioDialog && selectedContact != null) {
        AlertDialog(
            onDismissRequest = { showBioDialog = false },
            title = { Text("Bio de ${selectedContact!!.user.pseudo}") },
            text = { Text(selectedContact!!.user.bio ?: "Aucune bio", fontSize = 16.sp) },
            confirmButton = { TextButton(onClick = { showBioDialog = false }) { Text("Fermer") } }
        )
    }
}

// ... (La classe ContactRow et ContactPrefs reste inchangée en bas du fichier)
@Composable
private fun ContactRow(name: String, onClick: () -> Unit, onLongPress: () -> Unit, isMuted: Boolean) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 1.03f else 1f, label = "press-scale")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .combinedClickable(
                onClick = { isPressed = false; onClick() },
                onLongClick = { isPressed = true; haptic.performHapticFeedback(HapticFeedbackType.LongPress); onLongPress(); isPressed = false }
            )
            .padding(16.dp)
    ) {
        Text(text = name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (isMuted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
        if (isMuted) Text("En sourdine", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider()
}

object ContactPrefs {
    private const val PREFS_NAME = "contact_prefs"
    private const val KEY_PREFIX_NAME = "contact_name_"
    private const val KEY_PREFIX_MUTED = "contact_muted_"
    private fun prefs(context: android.content.Context) = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
    fun getCustomName(context: android.content.Context, userId: String): String? = prefs(context).getString(KEY_PREFIX_NAME + userId, null)
    fun setCustomName(context: android.content.Context, userId: String, name: String?) { prefs(context).edit().apply { if (name.isNullOrBlank()) remove(KEY_PREFIX_NAME + userId) else putString(KEY_PREFIX_NAME + userId, name) }.apply() }
    fun isMuted(context: android.content.Context, userId: String): Boolean = prefs(context).getBoolean(KEY_PREFIX_MUTED + userId, false)
    fun setMuted(context: android.content.Context, userId: String, muted: Boolean) { prefs(context).edit().putBoolean(KEY_PREFIX_MUTED + userId, muted).apply() }
}