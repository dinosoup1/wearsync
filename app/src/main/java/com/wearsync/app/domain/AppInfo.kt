package com.wearsync.app.domain

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appLabel: String,
    val icon: Drawable?
)
