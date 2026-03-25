package com.wearsync.app.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wearsync.app.data.PhonePackageRepository
import com.wearsync.app.data.PlayStoreChecker
import com.wearsync.app.data.WatchConnectionException
import com.wearsync.app.data.WatchPackageRepository
import com.wearsync.app.data.WearAppDatabase
import com.wearsync.app.data.WearCheckResult
import com.wearsync.app.domain.AppComparisonEngine
import com.wearsync.app.domain.AppInfo
import com.wearsync.app.domain.ComparisonResult
import com.wearsync.app.playstore.PlayStoreIntentBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UiState {
    data object Loading : UiState
    data class Success(
        val result: ComparisonResult,
        val showSystemApps: Boolean = false,
        val isRefreshing: Boolean = false,
        val batchInstallProgress: BatchInstallProgress? = null,
        val isCheckingPlayStore: Boolean = false,
        val autoCheckPlayStore: Boolean = false,
        val hasCheckedPlayStore: Boolean = false,
        val uncheckedCount: Int = 0
    ) : UiState
    data class Error(val message: String) : UiState
}

data class BatchInstallProgress(
    val current: Int,
    val total: Int
)

class MainViewModel(
    private val phoneRepo: PhonePackageRepository,
    private val watchRepo: WatchPackageRepository,
    private val applicationContext: Context,
    private val playStoreChecker: PlayStoreChecker = PlayStoreChecker(applicationContext)
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var showSystemApps = false
    private var lastPhoneApps: List<AppInfo> = emptyList()
    private var lastWatchPackages: Set<String> = emptySet()

    private val settingsPrefs: SharedPreferences =
        applicationContext.getSharedPreferences("wearsync_settings", Context.MODE_PRIVATE)

    init {
        WearAppDatabase.load(applicationContext)
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is UiState.Success) {
                _uiState.value = currentState.copy(isRefreshing = true)
            } else {
                _uiState.value = UiState.Loading
            }

            try {
                val phoneApps = phoneRepo.getInstalledApps(includeSystemApps = showSystemApps)
                val watchPackages = watchRepo.getWatchPackages()
                lastPhoneApps = phoneApps
                lastWatchPackages = watchPackages

                val result = AppComparisonEngine.compare(phoneApps, watchPackages)
                val unchecked = AppComparisonEngine.uncheckedApps(phoneApps, watchPackages)
                val autoCheck = settingsPrefs.getBoolean("auto_check_play_store", false)

                _uiState.value = UiState.Success(
                    result = result,
                    showSystemApps = showSystemApps,
                    autoCheckPlayStore = autoCheck,
                    uncheckedCount = unchecked.size
                )

                if (autoCheck && unchecked.isNotEmpty()) {
                    checkPlayStore()
                }
            } catch (e: WatchConnectionException) {
                _uiState.value = UiState.Error(
                    message = e.message ?: "Failed to connect to watch"
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = "An unexpected error occurred: ${e.message}"
                )
            }
        }
    }

    fun toggleSystemApps() {
        showSystemApps = !showSystemApps
        loadData()
    }

    fun toggleAutoCheckPlayStore() {
        val currentState = _uiState.value
        if (currentState !is UiState.Success) return
        val newValue = !currentState.autoCheckPlayStore
        settingsPrefs.edit().putBoolean("auto_check_play_store", newValue).apply()
        _uiState.value = currentState.copy(autoCheckPlayStore = newValue)
    }

    fun checkPlayStore() {
        val currentState = _uiState.value
        if (currentState !is UiState.Success) return
        if (currentState.isCheckingPlayStore) return

        val unchecked = AppComparisonEngine.uncheckedApps(lastPhoneApps, lastWatchPackages)
        if (unchecked.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isCheckingPlayStore = true)

            try {
                val results = playStoreChecker.checkPackages(unchecked.map { it.packageName })
                val discovered = unchecked.filter { app ->
                    results[app.packageName] == WearCheckResult.HAS_WEAR_VERSION
                }.sortedBy { it.appLabel.lowercase() }

                val current = _uiState.value
                if (current is UiState.Success) {
                    _uiState.value = current.copy(
                        result = current.result.copy(discoveredWearApps = discovered),
                        isCheckingPlayStore = false,
                        hasCheckedPlayStore = true,
                        uncheckedCount = unchecked.size - discovered.size
                    )
                }
            } catch (_: Exception) {
                val current = _uiState.value
                if (current is UiState.Success) {
                    _uiState.value = current.copy(
                        isCheckingPlayStore = false,
                        hasCheckedPlayStore = true
                    )
                }
            }
        }
    }

    fun openPlayStore(packageName: String) {
        PlayStoreIntentBuilder.openPlayStoreListing(applicationContext, packageName)
    }

    fun batchInstall() {
        val currentState = _uiState.value
        if (currentState !is UiState.Success) return

        val apps = currentState.result.notOnWatch + currentState.result.discoveredWearApps
        if (apps.isEmpty()) return

        viewModelScope.launch {
            apps.forEachIndexed { index, app ->
                _uiState.value = currentState.copy(
                    batchInstallProgress = BatchInstallProgress(
                        current = index + 1,
                        total = apps.size
                    )
                )
                PlayStoreIntentBuilder.openPlayStoreListing(applicationContext, app.packageName)
                if (index < apps.size - 1) {
                    delay(1500L)
                }
            }
            _uiState.value = currentState.copy(batchInstallProgress = null)
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val appContext = context.applicationContext
            return MainViewModel(
                phoneRepo = PhonePackageRepository(appContext),
                watchRepo = WatchPackageRepository(appContext),
                applicationContext = appContext
            ) as T
        }
    }
}
