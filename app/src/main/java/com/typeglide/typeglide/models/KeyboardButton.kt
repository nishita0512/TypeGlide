package com.typeglide.typeglide.models

import android.adservices.topics.GetTopicsRequest

data class KeyboardButton(
    val center: Char,
    val right: Char?,
    val left: Char?,
    val top: Char?,
    val bottom: Char?
)
