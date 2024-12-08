package com.typeglide.typeglide.models

data class KeyboardButton(
    val center: String,
    val right: String?,
    val left: String?,
    val top: String?,
    val bottom: String?,
    val keySize: Int = 1
)