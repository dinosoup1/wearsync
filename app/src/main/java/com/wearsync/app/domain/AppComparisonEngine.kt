package com.wearsync.app.domain

import com.wearsync.app.data.WearAppDatabase

object AppComparisonEngine {

    fun compare(phoneApps: List<AppInfo>, watchPackages: Set<String>): ComparisonResult {
        val notOnWatch = mutableListOf<AppInfo>()
        val alreadyOnWatch = mutableListOf<AppInfo>()

        for (app in phoneApps) {
            if (app.packageName in watchPackages) {
                alreadyOnWatch.add(app)
            } else if (WearAppDatabase.hasWearVersion(app.packageName)) {
                notOnWatch.add(app)
            }
        }

        return ComparisonResult(
            notOnWatch = notOnWatch.sortedBy { it.appLabel.lowercase() },
            alreadyOnWatch = alreadyOnWatch.sortedBy { it.appLabel.lowercase() }
        )
    }

    /**
     * Returns phone apps that are not on the watch AND not in the bundled Wear database.
     * These are candidates for Play Store checking.
     */
    fun uncheckedApps(phoneApps: List<AppInfo>, watchPackages: Set<String>): List<AppInfo> {
        return phoneApps.filter { app ->
            app.packageName !in watchPackages && !WearAppDatabase.hasWearVersion(app.packageName)
        }
    }
}
