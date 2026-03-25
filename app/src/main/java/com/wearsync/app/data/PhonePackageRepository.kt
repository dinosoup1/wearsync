package com.wearsync.app.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.wearsync.app.domain.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PhonePackageRepository(private val context: Context) {

    suspend fun getInstalledApps(includeSystemApps: Boolean = false): List<AppInfo> =
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

            packages
                .filter { appInfo ->
                    includeSystemApps || (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                }
                .mapNotNull { appInfo ->
                    try {
                        AppInfo(
                            packageName = appInfo.packageName,
                            appLabel = pm.getApplicationLabel(appInfo).toString(),
                            icon = pm.getApplicationIcon(appInfo)
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                .sortedBy { it.appLabel.lowercase() }
        }
}
