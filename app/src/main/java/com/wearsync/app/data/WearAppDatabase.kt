package com.wearsync.app.data

import android.content.Context
import org.json.JSONObject

object WearAppDatabase {

    @Volatile
    private var knownPackages: Set<String>? = null

    fun hasWearVersion(packageName: String): Boolean {
        return knownPackages?.contains(packageName) ?: false
    }

    fun load(context: Context) {
        if (knownPackages != null) return
        synchronized(this) {
            if (knownPackages != null) return
            val json = context.assets.open("wear_apps.json")
                .bufferedReader()
                .use { it.readText() }
            val root = JSONObject(json)
            val apps = root.getJSONArray("apps")
            val packages = HashSet<String>(apps.length())
            for (i in 0 until apps.length()) {
                packages.add(apps.getJSONObject(i).getString("package"))
            }
            knownPackages = packages
        }
    }

    fun allKnownPackages(): Set<String> = knownPackages ?: emptySet()

    /** Visible for testing only. */
    fun setPackagesForTesting(packages: Set<String>?) {
        knownPackages = packages
    }
}
