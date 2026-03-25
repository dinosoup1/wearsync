package com.wearsync.app.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AppComparisonEngineTest {

    private fun appInfo(packageName: String, label: String = packageName) =
        AppInfo(packageName = packageName, appLabel = label, icon = null)

    @Nested
    @DisplayName("compare()")
    inner class Compare {

        @Test
        @DisplayName("returns all phone apps as notOnWatch when watch has no apps")
        fun allAppsNotOnWatch() {
            val phoneApps = listOf(
                appInfo("com.example.app1", "App One"),
                appInfo("com.example.app2", "App Two")
            )
            val watchPackages = emptySet<String>()

            val result = AppComparisonEngine.compare(phoneApps, watchPackages)

            assertEquals(2, result.notOnWatch.size)
            assertTrue(result.alreadyOnWatch.isEmpty())
            assertEquals("com.example.app1", result.notOnWatch[0].packageName)
            assertEquals("com.example.app2", result.notOnWatch[1].packageName)
        }

        @Test
        @DisplayName("returns all phone apps as alreadyOnWatch when watch has all apps")
        fun allAppsOnWatch() {
            val phoneApps = listOf(
                appInfo("com.example.app1", "App One"),
                appInfo("com.example.app2", "App Two")
            )
            val watchPackages = setOf("com.example.app1", "com.example.app2")

            val result = AppComparisonEngine.compare(phoneApps, watchPackages)

            assertTrue(result.notOnWatch.isEmpty())
            assertEquals(2, result.alreadyOnWatch.size)
        }

        @Test
        @DisplayName("correctly splits apps between watch and not-on-watch")
        fun mixedResults() {
            val phoneApps = listOf(
                appInfo("com.example.app1", "App One"),
                appInfo("com.example.app2", "App Two"),
                appInfo("com.example.app3", "App Three")
            )
            val watchPackages = setOf("com.example.app2")

            val result = AppComparisonEngine.compare(phoneApps, watchPackages)

            assertEquals(2, result.notOnWatch.size)
            assertEquals(1, result.alreadyOnWatch.size)
            assertEquals("com.example.app2", result.alreadyOnWatch[0].packageName)
        }

        @Test
        @DisplayName("handles empty phone app list")
        fun emptyPhoneApps() {
            val result = AppComparisonEngine.compare(emptyList(), setOf("com.example.app1"))

            assertTrue(result.notOnWatch.isEmpty())
            assertTrue(result.alreadyOnWatch.isEmpty())
        }

        @Test
        @DisplayName("handles both lists empty")
        fun bothEmpty() {
            val result = AppComparisonEngine.compare(emptyList(), emptySet())

            assertTrue(result.notOnWatch.isEmpty())
            assertTrue(result.alreadyOnWatch.isEmpty())
        }

        @Test
        @DisplayName("sorts results alphabetically by app label")
        fun sortsByLabel() {
            val phoneApps = listOf(
                appInfo("com.c", "Charlie"),
                appInfo("com.a", "Alpha"),
                appInfo("com.b", "Bravo")
            )

            val result = AppComparisonEngine.compare(phoneApps, emptySet())

            assertEquals("Alpha", result.notOnWatch[0].appLabel)
            assertEquals("Bravo", result.notOnWatch[1].appLabel)
            assertEquals("Charlie", result.notOnWatch[2].appLabel)
        }

        @Test
        @DisplayName("sorting is case-insensitive")
        fun caseInsensitiveSorting() {
            val phoneApps = listOf(
                appInfo("com.b", "banana"),
                appInfo("com.a", "Apple")
            )

            val result = AppComparisonEngine.compare(phoneApps, emptySet())

            assertEquals("Apple", result.notOnWatch[0].appLabel)
            assertEquals("banana", result.notOnWatch[1].appLabel)
        }

        @Test
        @DisplayName("watch packages not on phone are ignored")
        fun extraWatchPackagesIgnored() {
            val phoneApps = listOf(appInfo("com.phone.only", "Phone App"))
            val watchPackages = setOf("com.phone.only", "com.watch.only1", "com.watch.only2")

            val result = AppComparisonEngine.compare(phoneApps, watchPackages)

            assertTrue(result.notOnWatch.isEmpty())
            assertEquals(1, result.alreadyOnWatch.size)
        }

        @Test
        @DisplayName("handles large number of apps")
        fun largeAppList() {
            val phoneApps = (1..500).map { appInfo("com.app$it", "App $it") }
            val watchPackages = (1..250).map { "com.app$it" }.toSet()

            val result = AppComparisonEngine.compare(phoneApps, watchPackages)

            assertEquals(250, result.notOnWatch.size)
            assertEquals(250, result.alreadyOnWatch.size)
        }
    }
}
