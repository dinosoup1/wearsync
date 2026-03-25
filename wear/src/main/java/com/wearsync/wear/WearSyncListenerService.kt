package com.wearsync.wear

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WearSyncListenerService : WearableListenerService() {

    companion object {
        private const val REQUEST_PATH = "/wearsync/request"
        private const val RESPONSE_PATH = "/wearsync/response"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == REQUEST_PATH) {
            scope.launch {
                val packageList = getInstalledPackages()
                sendResponse(messageEvent.sourceNodeId, packageList)
            }
        }
    }

    private fun getInstalledPackages(): String {
        val pm = packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
            .joinToString("\n") { it.packageName }
    }

    private suspend fun sendResponse(nodeId: String, packageList: String) {
        try {
            Wearable.getMessageClient(this)
                .sendMessage(nodeId, RESPONSE_PATH, packageList.toByteArray())
                .await()
        } catch (_: Exception) {
            // Best effort - if watch can't respond, phone side will timeout
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
