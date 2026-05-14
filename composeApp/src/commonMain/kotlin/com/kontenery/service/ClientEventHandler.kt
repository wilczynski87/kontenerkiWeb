package com.kontenery.service

import com.kontenery.model.Client
import com.kontenery.model.ClientCompanyData
import com.kontenery.model.ClientEvent
import com.kontenery.model.ClientPersonalData

internal class ClientEventHandler(
    private val updateClient: ((Client) -> Client) -> Unit,
    private val saveClient: () -> Unit,
    private val updateExistingClient: () -> Unit,
) {
    fun handle(event: ClientEvent) {
        when (event) {
            is ClientEvent.Personal -> handlePersonal(event)
            is ClientEvent.Company -> handleCompany(event)
            is ClientEvent.AddressEvent -> handleAddress(event)
            is ClientEvent.Bank -> handleBank(event)

            is ClientEvent.InvoiceTitleChanged -> updateClient {
                it.copy(invoiceTitle = event.value)
            }

            ClientEvent.ToggleActive -> updateClient {
                it.copy(isActive = !(it.isActive ?: false))
            }

            ClientEvent.Save -> saveClient()
            ClientEvent.Update -> updateExistingClient()
        }
    }

    private fun handlePersonal(event: ClientEvent.Personal) {
        updateClient { client ->
            val personalData = client.clientPrivate ?: ClientPersonalData()

            client.copy(
                clientPrivate = when (event) {
                    is ClientEvent.Personal.FirstNameChanged -> personalData.copy(firstName = event.value)
                    is ClientEvent.Personal.LastNameChanged -> personalData.copy(lastName = event.value)
                    is ClientEvent.Personal.PeselChanged -> personalData.copy(pesel = event.value)
                    is ClientEvent.Personal.PassportChanged -> personalData.copy(passport = event.value)
                    is ClientEvent.Personal.PhoneChanged -> personalData.copy(phone = event.value)
                    is ClientEvent.Personal.EmailChanged -> personalData.copy(email = event.value)
                    is ClientEvent.Personal.SalutationChanged -> personalData.copy(salutation = event.value)
                }
            )
        }
    }

    private fun handleCompany(event: ClientEvent.Company) {
        updateClient { client ->
            val companyData = client.clientCompany ?: ClientCompanyData()

            client.copy(
                clientCompany = when (event) {
                    is ClientEvent.Company.NameChanged -> companyData.copy(name = event.value)
                    is ClientEvent.Company.NipChanged -> companyData.copy(nip = event.value)
                    is ClientEvent.Company.KrsChanged -> companyData.copy(krs = event.value)
                    is ClientEvent.Company.PhoneChanged -> companyData.copy(phone = event.value)
                    is ClientEvent.Company.EmailChanged -> companyData.copy(email = event.value)

                    ClientEvent.Company.ToggleInvoice ->
                        companyData.copy(needInvoice = !(companyData.needInvoice ?: false))
                }
            )
        }
    }

    private fun handleAddress(event: ClientEvent.AddressEvent) {
        updateClient { client ->
            when (event) {
                is ClientEvent.AddressEvent.PersonalAddressChanged -> {
                    val personalData = client.clientPrivate ?: ClientPersonalData()
                    client.copy(clientPrivate = personalData.copy(address = event.address))
                }

                is ClientEvent.AddressEvent.CompanyAddressChanged -> {
                    val companyData = client.clientCompany ?: ClientCompanyData()
                    client.copy(clientCompany = companyData.copy(address = event.address))
                }
            }
        }
    }

    private fun handleBank(event: ClientEvent.Bank) {
        updateClient { client ->
            val accounts = client.bankAccounts.orEmpty().toMutableList()

            when (event) {
                is ClientEvent.Bank.Add -> accounts.add(event.account)
                is ClientEvent.Bank.Remove -> accounts.remove(event.account)
                is ClientEvent.Bank.Update -> {
                    if (event.index in accounts.indices) {
                        accounts[event.index] = event.value
                    }
                }
            }

            client.copy(bankAccounts = accounts)
        }
    }
}
