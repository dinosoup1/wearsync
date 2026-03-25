package com.wearsync.app.playstore

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class PlayStoreIntentBuilderTest {

    @Test
    @DisplayName("builds correct market URI for package name")
    fun correctMarketUri() {
        val intent = PlayStoreIntentBuilder.buildIntent("com.example.app")

        assertEquals("market://details?id=com.example.app", intent.data.toString())
    }

    @Test
    @DisplayName("targets Play Store package")
    fun targetsPlayStore() {
        val intent = PlayStoreIntentBuilder.buildIntent("com.example.app")

        assertEquals("com.android.vending", intent.`package`)
    }

    @Test
    @DisplayName("intent has NEW_TASK flag")
    fun hasNewTaskFlag() {
        val intent = PlayStoreIntentBuilder.buildIntent("com.example.app")

        assertEquals(
            android.content.Intent.FLAG_ACTIVITY_NEW_TASK,
            intent.flags and android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        )
    }
}
