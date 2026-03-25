package com.wearsync.app.ui

import android.content.Context
import app.cash.turbine.test
import com.wearsync.app.data.PhonePackageRepository
import com.wearsync.app.data.WatchConnectionException
import com.wearsync.app.data.WatchPackageRepository
import com.wearsync.app.domain.AppInfo
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val phoneRepo = mockk<PhonePackageRepository>()
    private val watchRepo = mockk<WatchPackageRepository>()
    private val context = mockk<Context>(relaxed = true) {
        every { applicationContext } returns this
    }

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MainViewModel {
        return MainViewModel(phoneRepo, watchRepo, context)
    }

    @Test
    @DisplayName("initial state is Loading")
    fun initialStateIsLoading() = runTest {
        coEvery { phoneRepo.getInstalledApps(any()) } returns emptyList()
        coEvery { watchRepo.getWatchPackages() } returns emptySet()

        val vm = createViewModel()

        vm.uiState.test {
            val first = awaitItem()
            assertTrue(first is UiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    @DisplayName("emits Success after loading data")
    fun emitsSuccessState() = runTest {
        val apps = listOf(
            AppInfo("com.test", "Test App", null)
        )
        coEvery { phoneRepo.getInstalledApps(any()) } returns apps
        coEvery { watchRepo.getWatchPackages() } returns emptySet()

        val vm = createViewModel()

        vm.uiState.test {
            // Skip loading
            awaitItem()
            testDispatcher.scheduler.advanceUntilIdle()
            val success = awaitItem()
            assertTrue(success is UiState.Success)
            assertEquals(1, (success as UiState.Success).result.notOnWatch.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    @DisplayName("emits Error when watch connection fails")
    fun emitsErrorOnWatchFailure() = runTest {
        coEvery { phoneRepo.getInstalledApps(any()) } returns emptyList()
        coEvery { watchRepo.getWatchPackages() } throws WatchConnectionException("No watch")

        val vm = createViewModel()

        vm.uiState.test {
            awaitItem() // Loading
            testDispatcher.scheduler.advanceUntilIdle()
            val error = awaitItem()
            assertTrue(error is UiState.Error)
            assertEquals("No watch", (error as UiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
