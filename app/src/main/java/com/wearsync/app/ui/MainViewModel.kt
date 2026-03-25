package com.wearsync.app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wearsync.app.data.PhonePackageRepository
import com.wearsync.app.data.PlayStoreChecker
import com.wearsync.app.data.WatchConnectionException
import com.wearsync.app.data.WatchPackageRepository
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
        val hasCheckedPlayStore: Boolean = false
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

    init {
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

                // Show initial state while we check Play Store
                val result = ComparisonResult(
                    notOnWatch = emptyList(),
                    alreadyOnWatch = phoneApps.filter { it.packageName in watchPackages }
                        .sortedBy { it.appLabel.lowercase() },
                    discoveredWearApps = emptyList()
                )

                _uiState.value = UiState.Success(
                    result = result,
                    showSystemApps = showSystemApps,
                    isCheckingPlayStore = true
                )

                // Automatically check Play Store for all apps not on watch
                checkPlayStore()

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

    fun checkPlayStore() {
        val currentState = _uiState.value
        if (currentState !is UiState.Success) return

        val candidates = AppComparisonEngine.uncheckedApps(lastPhoneApps, lastWatchPackages)
        if (candidates.isEmpty()) {
            _uiState.value = currentState.copy(
                isCheckingPlayStore = false,
                hasCheckedPlayStore = true
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isCheckingPlayStore = true)

            try {
                // First check cache for instant results
                val cached = mutableListOf<AppInfo>()
                val needsCheck = mutableListOf<AppInfo>()

                for (app in candidates) {
                    val cachedResult = playStoreChecker.getCachedResult(app.packageName)
                    if (cachedResult == WearCheckResult.HAS_WEAR_VERSION) {
                        cached.add(app)
                    } else if (cachedResult == null) {
                        needsCheck.add(app)
                    }
                    // NO_WEAR_VERSION and CHECK_FAILED are skipped (already checked)
                }

                // Show cached results immediately
                if (cached.isNotEmpty()) {
                    val current = _uiState.value
                    if (current is UiState.Success) {
                        _uiState.value = current.copy(
                            result = current.result.copy(
                                notOnWatch = cached.sortedBy { it.appLabel.lowercase() }
                            )
                        )
                    }
                }

                // Check remaining apps via network
                if (needsCheck.isNotEmpty()) {
                    val results = playStoreChecker.checkPackages(needsCheck.map { it.packageName })
                    val discovered = needsCheck.filter { app ->
                        results[app.packageName] == WearCheckResult.HAS_WEAR_VERSION
                    }

                    val allWearApps = (cached + discovered).sortedBy { it.appLabel.lowercase() }

                    val current = _uiState.value
                    if (current is UiState.Success) {
                        _uiState.value = current.copy(
                            result = current.result.copy(
                                notOnWatch = allWearApps
                            ),
                            isCheckingPlayStore = false,
                            hasCheckedPlayStore = true
                        )
                    }
                } else {
                    val current = _uiState.value
                    if (current is UiState.Success) {
                        _uiState.value = current.copy(
                            isCheckingPlayStore = false,
                            hasCheckedPlayStore = true
                        )
                    }
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

        val apps = currentState.result.notOnWatch
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
