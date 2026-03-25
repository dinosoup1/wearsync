package com.wearsync.app.domain

data class ComparisonResult(
    val notOnWatch: List<AppInfo>,
    val alreadyOnWatch: List<AppInfo>
)
