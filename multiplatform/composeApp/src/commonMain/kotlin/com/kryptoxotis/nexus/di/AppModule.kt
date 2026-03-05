package com.kryptoxotis.nexus.di

import com.kryptoxotis.nexus.data.local.BusinessPassLocalDataSource
import com.kryptoxotis.nexus.data.local.NexusDatabase
import com.kryptoxotis.nexus.data.local.PersonalCardLocalDataSource
import com.kryptoxotis.nexus.data.local.ReceivedCardLocalDataSource
import com.kryptoxotis.nexus.data.remote.AuthManager
import com.kryptoxotis.nexus.data.repository.BusinessPassRepository
import com.kryptoxotis.nexus.data.repository.FileRepository
import com.kryptoxotis.nexus.data.repository.OrganizationRepository
import com.kryptoxotis.nexus.data.repository.PersonalCardRepository
import com.kryptoxotis.nexus.data.repository.ReceivedCardRepository
import com.kryptoxotis.nexus.platform.DatabaseDriverFactory
import com.kryptoxotis.nexus.presentation.admin.AdminViewModel
import com.kryptoxotis.nexus.presentation.auth.AuthViewModel
import com.kryptoxotis.nexus.presentation.business.BusinessViewModel
import com.kryptoxotis.nexus.presentation.cards.PersonalCardViewModel
import com.kryptoxotis.nexus.presentation.cards.ReceivedCardViewModel

object AppModule {

    private lateinit var database: NexusDatabase

    private val authManager by lazy { AuthManager() }
    private val personalCardLocalDataSource by lazy { PersonalCardLocalDataSource(database) }
    private val receivedCardLocalDataSource by lazy { ReceivedCardLocalDataSource(database) }
    private val businessPassLocalDataSource by lazy { BusinessPassLocalDataSource(database) }

    private val personalCardRepository by lazy { PersonalCardRepository(personalCardLocalDataSource) }
    private val fileRepository by lazy { FileRepository() }
    private val receivedCardRepository by lazy { ReceivedCardRepository(receivedCardLocalDataSource) }
    private val businessPassRepository by lazy { BusinessPassRepository(businessPassLocalDataSource) }
    private val organizationRepository by lazy { OrganizationRepository() }

    val authViewModel by lazy { AuthViewModel(authManager) }
    val cardViewModel by lazy { PersonalCardViewModel(personalCardRepository, fileRepository) }
    val receivedCardViewModel by lazy { ReceivedCardViewModel(receivedCardRepository) }
    val businessViewModel by lazy { BusinessViewModel(businessPassRepository, organizationRepository) }
    val adminViewModel by lazy { AdminViewModel() }

    fun init(driverFactory: DatabaseDriverFactory) {
        database = NexusDatabase(driverFactory.createDriver())
    }

    fun onAppStart() {
        authViewModel.checkSession()
    }
}
