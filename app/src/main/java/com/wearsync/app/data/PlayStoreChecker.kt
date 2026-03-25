package com.wearsync.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

enum class WearCheckResult {
    HAS_WEAR_VERSION,
    NO_WEAR_VERSION,
    CHECK_FAILED
}

class PlayStoreChecker(context: Context) {

    companion object {
        private const val PREFS_NAME = "play_store_cache"
        private const val CONNECT_TIMEOUT = 10_000
        private const val READ_TIMEOUT = 10_000
        private const val MAX_CONCURRENT = 5
    }

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getCachedResult(packageName: String): WearCheckResult? {
        val value = prefs.getString(packageName, null) ?: return null
        return try {
            WearCheckResult.valueOf(value)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    suspend fun checkPackages(packageNames: List<String>): Map<String, WearCheckResult> =
        coroutineScope {
            val semaphore = Semaphore(MAX_CONCURRENT)
            val results = packageNames.map { pkg ->
                async {
                    semaphore.withPermit {
                        pkg to checkSingle(pkg)
                    }
                }
            }.awaitAll().toMap()

            val editor = prefs.edit()
            for ((pkg, result) in results) {
                editor.putString(pkg, result.name)
            }
            editor.apply()

            results
        }

    internal suspend fun checkSingle(packageName: String): WearCheckResult =
        withContext(Dispatchers.IO) {
            try {
                val url = URL("https://play.google.com/store/apps/details?id=$packageName")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = CONNECT_TIMEOUT
                conn.readTimeout = READ_TIMEOUT
                conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                conn.instanceFollowRedirects = true

                try {
                    val code = conn.responseCode
                    if (code != 200) return@withContext WearCheckResult.NO_WEAR_VERSION

                    val body = conn.inputStream.bufferedReader().use { it.readText() }

                    // The Play Store shows device tabs like "Phone", "Watch", "Tablet", etc.
                    // Only apps with actual Wear OS versions have a "Watch" device tab.
                    // We look for this specific indicator, NOT generic "Wear OS" text mentions
                    // which can appear in descriptions of apps that don't have watch versions.
                    //
                    // The device tabs appear in the page as elements containing device type names.
                    // Common patterns in the rendered HTML:
                    // - "Watch" as a device filter/tab option
                    // - "Wear OS" in the device compatibility section (not description)
                    val hasWatchDeviceTab = body.contains(">Watch<") ||
                        body.contains(">Watch •") ||
                        body.contains("\"Watch\"") ||
                        // Google encodes device types in structured data
                        body.contains("WATCH_TYPE") ||
                        body.contains("watch_type") ||
                        // Form factor indicators in Play Store metadata
                        body.contains("FORM_FACTOR_WATCH") ||
                        body.contains("form_factor_watch")

                    if (hasWatchDeviceTab) {
                        WearCheckResult.HAS_WEAR_VERSION
                    } else {
                        WearCheckResult.NO_WEAR_VERSION
                    }
                } finally {
                    conn.disconnect()
                }
            } catch (_: Exception) {
                WearCheckResult.CHECK_FAILED
            }
        }
}
