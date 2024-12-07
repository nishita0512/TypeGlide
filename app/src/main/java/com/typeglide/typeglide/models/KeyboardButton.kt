package com.typeglide.typeglide.models

data class KeyboardButton(
    val center: Char,
    val right: Char?,
    val left: Char?,
    val top: Char?,
    val bottom: Char?,
    val keySize: Int
)