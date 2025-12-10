package com.example.submarine

data class SimpleUser(val id: String, val pseudo: String, val email: String?)
data class SimpleFriend(val user: SimpleUser)

fun filterContacts(
    contacts: List<SimpleFriend>,
    search: String,
    nameResolver: (String, String) -> String // (userId, pseudo) -> displayName
): List<SimpleFriend> {
    return contacts.filter { friend ->
        val name = nameResolver(friend.user.id, friend.user.pseudo)
        val full = (name + " " + (friend.user.email ?: ""))
        full.contains(search, ignoreCase = true)
    }
}
