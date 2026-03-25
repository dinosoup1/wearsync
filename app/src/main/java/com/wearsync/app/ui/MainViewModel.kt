package com.wearsync.app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wearsync.app.data.PhonePackageRepository
import com.wearsync.app.data.WatchConnectionException
import com.wearsync.app.data.WatchPackageRepository
import com.wearsync.app.domain.AppComparisonEngine
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
        val batchInstallProgress: BatchInstallProgress? = null
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
    private val applicationContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var showSystemApps = false

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
                val result = AppComparisonEngine.compare(phoneApps, watchPackages)

                _uiState.value = UiState.Success(
                    result = result,
                    showSystemApps = showSystemApps
                )
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
