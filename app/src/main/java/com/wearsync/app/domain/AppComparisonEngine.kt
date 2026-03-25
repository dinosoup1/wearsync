package com.wearsync.app.domain

object AppComparisonEngine {

    fun compare(phoneApps: List<AppInfo>, watchPackages: Set<String>): ComparisonResult {
        val notOnWatch = mutableListOf<AppInfo>()
        val alreadyOnWatch = mutableListOf<AppInfo>()

        for (app in phoneApps) {
            if (app.packageName in watchPackages) {
                alreadyOnWatch.add(app)
            } else {
                notOnWatch.add(app)
            }
        }

        return ComparisonResult(
            notOnWatch = notOnWatch.sortedBy { it.appLabel.lowercase() },
            alreadyOnWatch = alreadyOnWatch.sortedBy { it.appLabel.lowercase() }
        )
    }
}
