package com.kontenery.service

import com.kontenery.model.Client
import com.kontenery.model.ClientEvent
import com.kontenery.model.ClientPersonalData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClientEventHandlerTest {
    @Test
    fun personalEventsUpdateNestedPersonalData() {
        var client = Client(clientPrivate = ClientPersonalData())
        val handler = ClientEventHandler(
            updateClient = { update -> client = update(client) },
            saveClient = {},
            updateExistingClient = {},
        )

        handler.handle(ClientEvent.Personal.FirstNameChanged("Jan"))
        handler.handle(ClientEvent.Personal.LastNameChanged("Kowalski"))

        assertEquals("Jan", client.clientPrivate?.firstName)
        assertEquals("Kowalski", client.clientPrivate?.lastName)
    }

    @Test
    fun bankEventsModifyBankAccountList() {
        var client = Client(bankAccounts = listOf("111"))
        val handler = ClientEventHandler(
            updateClient = { update -> client = update(client) },
            saveClient = {},
            updateExistingClient = {},
        )

        handler.handle(ClientEvent.Bank.Add("222"))
        handler.handle(ClientEvent.Bank.Update(index = 0, value = "333"))
        handler.handle(ClientEvent.Bank.Remove("222"))

        assertEquals(listOf("333"), client.bankAccounts)
    }

    @Test
    fun metaAndActionEventsUseProvidedCallbacks() {
        var client = Client(isActive = true)
        var saveCalled = false
        var updateCalled = false
        val handler = ClientEventHandler(
            updateClient = { update -> client = update(client) },
            saveClient = { saveCalled = true },
            updateExistingClient = { updateCalled = true },
        )

        handler.handle(ClientEvent.ToggleActive)
        handler.handle(ClientEvent.Save)
        handler.handle(ClientEvent.Update)

        assertFalse(client.isActive ?: true)
        assertTrue(saveCalled)
        assertTrue(updateCalled)
    }
}
