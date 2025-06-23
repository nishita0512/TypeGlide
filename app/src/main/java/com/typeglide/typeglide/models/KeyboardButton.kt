package com.typeglide.typeglide.models

data class KeyboardButton(
    var center: String,
    var right: String?,
    var left: String?,
    var top: String?,
    var bottom: String?,
    var keySize: Int = 1
)