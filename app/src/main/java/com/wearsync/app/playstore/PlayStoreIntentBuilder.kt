package com.wearsync.app.playstore

import android.content.Context
import android.content.Intent
import android.net.Uri

object PlayStoreIntentBuilder {

    fun buildIntent(packageName: String): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=$packageName")
            setPackage("com.android.vending")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun openPlayStoreListing(context: Context, packageName: String) {
        val intent = buildIntent(packageName)
        try {
            context.startActivity(intent)
        } catch (_: android.content.ActivityNotFoundException) {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webIntent)
        }
    }
}
