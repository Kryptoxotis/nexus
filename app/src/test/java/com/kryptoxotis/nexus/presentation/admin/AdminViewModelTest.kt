package com.kryptoxotis.nexus.presentation.admin

import com.kryptoxotis.nexus.data.remote.dto.BusinessRequestDto
import com.kryptoxotis.nexus.data.remote.dto.AllowedEmailDto
import com.kryptoxotis.nexus.data.remote.dto.OrganizationDto
import com.kryptoxotis.nexus.data.remote.dto.ProfileDto
import com.kryptoxotis.nexus.data.repository.AdminRepository
import com.kryptoxotis.nexus.domain.model.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AdminViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: AdminRepository
    private lateinit var vm: AdminViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk(relaxed = true)
        vm = AdminViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Validation guards ──

    @Test
    fun `approveRequest with blank ID sets error`() {
        val request = BusinessRequestDto(id = "", userId = "u1", businessName = "Biz")
        vm.approveRequest(request)
        assertTrue(vm.uiState.value is AdminUiState.Error)
        assertEquals("Request ID is missing", (vm.uiState.value as AdminUiState.Error).message)
    }

    @Test
    fun `approveRequest with null ID sets error`() {
        val request = BusinessRequestDto(id = null, userId = "u1", businessName = "Biz")
        vm.approveRequest(request)
        assertTrue(vm.uiState.value is AdminUiState.Error)
    }

    @Test
    fun `rejectRequest with blank ID sets error`() {
        vm.rejectRequest("")
        assertTrue(vm.uiState.value is AdminUiState.Error)
        assertEquals("Request ID is missing", (vm.uiState.value as AdminUiState.Error).message)
    }

    @Test
    fun `updateUserStatus with blank userId sets error`() {
        vm.updateUserStatus("", "active")
        assertTrue(vm.uiState.value is AdminUiState.Error)
        assertEquals("User ID is missing", (vm.uiState.value as AdminUiState.Error).message)
    }

    @Test
    fun `updateUserStatus with invalid status delegates to repo`() = runTest {
        coEvery { repo.updateUserStatus("user-1", "invalid") } returns Result.Error("Invalid status: invalid")

        vm.updateUserStatus("user-1", "invalid")
        advanceUntilIdle()

        assertTrue(vm.uiState.value is AdminUiState.Error)
    }

    @Test
    fun `changeAccountType with blank userId sets error`() {
        vm.changeAccountType("", "business")
        assertTrue(vm.uiState.value is AdminUiState.Error)
        assertEquals("User ID is missing", (vm.uiState.value as AdminUiState.Error).message)
    }

    @Test
    fun `createUser with blank email sets error`() {
        vm.createUser("", "Alice", "individual")
        assertTrue(vm.uiState.value is AdminUiState.Error)
        assertEquals("Invalid email address", (vm.uiState.value as AdminUiState.Error).message)
    }

    @Test
    fun `createUser with blank name sets error`() {
        vm.createUser("a@b.com", "  ", "individual")
        assertTrue(vm.uiState.value is AdminUiState.Error)
        assertEquals("Name must be 1-200 characters", (vm.uiState.value as AdminUiState.Error).message)
    }

    @Test
    fun `createUser with invalid account type delegates to repo`() = runTest {
        coEvery { repo.createUser("a@b.com", "Alice", "root") } returns Result.Error("Invalid account type: root")

        vm.createUser("a@b.com", "Alice", "root")
        advanceUntilIdle()

        assertTrue(vm.uiState.value is AdminUiState.Error)
    }

    @Test
    fun `deleteAllowedEmail with blank ID sets error`() {
        vm.deleteAllowedEmail("")
        assertTrue(vm.uiState.value is AdminUiState.Error)
        assertEquals("Email ID is missing", (vm.uiState.value as AdminUiState.Error).message)
    }

    @Test
    fun `deleteUser with blank ID sets error`() {
        vm.deleteUser("")
        assertTrue(vm.uiState.value is AdminUiState.Error)
        assertEquals("User ID is missing", (vm.uiState.value as AdminUiState.Error).message)
    }

    @Test
    fun `toggleOrganizationActive with blank ID sets error`() {
        vm.toggleOrganizationActive("", true)
        assertTrue(vm.uiState.value is AdminUiState.Error)
        assertEquals("Organization ID is missing", (vm.uiState.value as AdminUiState.Error).message)
    }

    @Test
    fun `createOrganization with blank name sets error`() {
        vm.createOrganization("  ", null, null, "owner-1", "open")
        assertTrue(vm.uiState.value is AdminUiState.Error)
        assertEquals("Organization name is required", (vm.uiState.value as AdminUiState.Error).message)
    }

    @Test
    fun `createOrganization with blank owner sets error`() {
        vm.createOrganization("Acme", null, null, "", "open")
        assertTrue(vm.uiState.value is AdminUiState.Error)
        assertEquals("Owner is required", (vm.uiState.value as AdminUiState.Error).message)
    }

    @Test
    fun `createOrganization with invalid enrollment mode delegates to repo`() = runTest {
        coEvery { repo.createOrganization("Acme", null, null, "owner-1", "invalid") } returns Result.Error("Invalid enrollment mode: invalid")

        vm.createOrganization("Acme", null, null, "owner-1", "invalid")
        advanceUntilIdle()

        assertTrue(vm.uiState.value is AdminUiState.Error)
    }

    @Test
    fun `deleteOrganization with blank ID sets error`() {
        vm.deleteOrganization("")
        assertTrue(vm.uiState.value is AdminUiState.Error)
        assertEquals("Organization ID is missing", (vm.uiState.value as AdminUiState.Error).message)
    }

    // ── Repo delegation (success path) ──

    @Test
    fun `loadPendingRequests populates list on success`() = runTest {
        val requests = listOf(BusinessRequestDto(id = "r1", userId = "u1", businessName = "Biz"))
        coEvery { repo.loadPendingRequests() } returns Result.Success(requests)

        vm.loadPendingRequests()
        advanceUntilIdle()

        assertEquals(requests, vm.pendingRequests.value)
    }

    @Test
    fun `loadUsers populates list on success`() = runTest {
        val profiles = listOf(ProfileDto(id = "p1", email = "a@b.com"))
        coEvery { repo.loadUsers() } returns Result.Success(profiles)

        vm.loadUsers()
        advanceUntilIdle()

        assertEquals(profiles, vm.users.value)
    }

    @Test
    fun `loadOrganizations populates list on success`() = runTest {
        val orgs = listOf(OrganizationDto(name = "Acme", ownerId = "o1"))
        coEvery { repo.loadOrganizations() } returns Result.Success(orgs)

        vm.loadOrganizations()
        advanceUntilIdle()

        assertEquals(orgs, vm.organizations.value)
    }

    // ── Repo delegation (error path) ──

    @Test
    fun `loadPendingRequests sets error on failure`() = runTest {
        coEvery { repo.loadPendingRequests() } returns Result.Error("Network error")

        vm.loadPendingRequests()
        // runCurrent() runs the repo call but not the auto-dismiss delay
        testScheduler.runCurrent()

        assertTrue(vm.uiState.value is AdminUiState.Error)
        assertEquals("Network error", (vm.uiState.value as AdminUiState.Error).message)
    }

    @Test
    fun `loadUsers sets error on failure`() = runTest {
        coEvery { repo.loadUsers() } returns Result.Error("Network error")

        vm.loadUsers()
        testScheduler.runCurrent()

        assertTrue(vm.uiState.value is AdminUiState.Error)
    }

    @Test
    fun `loadAllowedEmails sets error on failure`() = runTest {
        coEvery { repo.loadAllowedEmails() } returns Result.Error("Network error")

        vm.loadAllowedEmails()
        testScheduler.runCurrent()

        assertTrue(vm.uiState.value is AdminUiState.Error)
        assertEquals("Network error", (vm.uiState.value as AdminUiState.Error).message)
    }

    @Test
    fun `resetState sets Idle`() {
        vm.deleteUser("")  // triggers error
        assertTrue(vm.uiState.value is AdminUiState.Error)
        vm.resetState()
        assertTrue(vm.uiState.value is AdminUiState.Idle)
    }

    // ── Loading state on load functions ──

    @Test
    fun `loadPendingRequests sets Loading then Idle on success`() = runTest {
        val requests = listOf(BusinessRequestDto(id = "r1", userId = "u1", businessName = "Biz"))
        coEvery { repo.loadPendingRequests() } returns Result.Success(requests)

        vm.loadPendingRequests()
        // Before coroutine runs, state should be Loading
        assertTrue(vm.uiState.value is AdminUiState.Loading)

        testScheduler.runCurrent()
        assertTrue(vm.uiState.value is AdminUiState.Idle)
        assertEquals(requests, vm.pendingRequests.value)
    }

    // ── Reload-after-mutation tests ──

    @Test
    fun `approveRequest success reloads pending requests`() = runTest {
        val request = BusinessRequestDto(id = "r1", userId = "u1", businessName = "Biz")
        coEvery { repo.approveRequest(request) } returns Result.Success("Approved")
        val reloadedList = listOf(BusinessRequestDto(id = "r2", userId = "u2", businessName = "Other"))
        coEvery { repo.loadPendingRequests() } returns Result.Success(reloadedList)

        vm.approveRequest(request)
        advanceUntilIdle()

        assertTrue(vm.uiState.value is AdminUiState.Success || vm.uiState.value is AdminUiState.Idle)
        assertEquals(reloadedList, vm.pendingRequests.value)
    }

    @Test
    fun `rejectRequest success reloads pending requests`() = runTest {
        coEvery { repo.rejectRequest("r1") } returns Result.Success("Rejected")
        val reloadedList = emptyList<BusinessRequestDto>()
        coEvery { repo.loadPendingRequests() } returns Result.Success(reloadedList)

        vm.rejectRequest("r1")
        advanceUntilIdle()

        assertEquals(reloadedList, vm.pendingRequests.value)
    }

    @Test
    fun `createUser success reloads users and allowed emails`() = runTest {
        coEvery { repo.createUser("a@b.com", "Alice", "individual") } returns Result.Success("Added")
        val users = listOf(ProfileDto(id = "p1", email = "a@b.com"))
        val emails = emptyList<AllowedEmailDto>()
        coEvery { repo.loadUsers() } returns Result.Success(users)
        coEvery { repo.loadAllowedEmails() } returns Result.Success(emails)

        vm.createUser("a@b.com", "Alice", "individual")
        advanceUntilIdle()

        assertEquals(users, vm.users.value)
    }

    // ── Auto-dismiss timing ──

    @Test
    fun `auto-dismiss resets success state to Idle after 3 seconds`() = runTest {
        coEvery { repo.deleteUser("u1") } returns Result.Success("User deleted")
        coEvery { repo.loadUsers() } returns Result.Success(emptyList())

        vm.deleteUser("u1")
        advanceUntilIdle()

        assertTrue(vm.uiState.value is AdminUiState.Idle)
    }

    @Test
    fun `error state persists longer than success state`() = runTest {
        coEvery { repo.deleteUser("u1") } returns Result.Error("Network error")

        vm.deleteUser("u1")
        testScheduler.runCurrent()

        assertTrue(vm.uiState.value is AdminUiState.Error)

        // Advance 3 seconds — error should still be showing (6s timeout)
        testScheduler.advanceTimeBy(3500)
        assertTrue(vm.uiState.value is AdminUiState.Error)

        // Advance to 6 seconds — should auto-dismiss
        testScheduler.advanceTimeBy(3000)
        assertTrue(vm.uiState.value is AdminUiState.Idle)
    }
}
