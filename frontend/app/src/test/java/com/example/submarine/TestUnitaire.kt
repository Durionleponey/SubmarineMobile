package com.example.submarine

import org.junit.Assert.*
import org.junit.Test

class ContactsUtilsTest {

    private val friend1 = SimpleFriend(SimpleUser("1", "Alice", "alice@mail.com"))
    private val friend2 = SimpleFriend(SimpleUser("2", "Bob", "bob@mail.com"))
    private val friend3 = SimpleFriend(SimpleUser("3", "Charlie", null))

    private val contacts = listOf(friend1, friend2, friend3)

    @Test
    fun `filter contacts by pseudo`() {
        val result = filterContacts(contacts, "Alice") { _, pseudo -> pseudo }
        assertEquals(1, result.size)
        assertEquals("Alice", result.first().user.pseudo)
    }

    @Test
    fun `filter contacts by email`() {
        val result = filterContacts(contacts, "bob@") { _, pseudo -> pseudo }
        assertEquals(1, result.size)
        assertEquals("Bob", result.first().user.pseudo)
    }

    @Test
    fun `filter contacts returns multiple results`() {
        val result = filterContacts(contacts, "a") { _, pseudo -> pseudo }
        assertEquals(2, result.size) // Alice + Charlie
    }

    @Test
    fun `filter contacts with custom name`() {
        val result = filterContacts(contacts, "SuperBob") { id, pseudo ->
            if (id == "2") "SuperBob" else pseudo
        }

        assertEquals(1, result.size)
        assertEquals("Bob", result.first().user.pseudo)
    }

    @Test
    fun `filter returns empty when no match`() {
        val result = filterContacts(contacts, "ZZZ") { _, pseudo -> pseudo }
        assertTrue(result.isEmpty())
    }
}
