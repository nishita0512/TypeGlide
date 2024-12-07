package com.typeglide.typeglide

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AbstractComposeView
import com.typeglide.typeglide.composables.KeyboardScreen
import com.typeglide.typeglide.models.KeyboardButton

class EnglishComposeKeyboardView(context: Context) : AbstractComposeView(context) {

    @Composable
    override fun Content() {
        KeyboardScreen(
            arrayOf(
                arrayOf(
                    KeyboardButton('m', '~', null, null, '\n', 1),
                    KeyboardButton('t', '$', '<', null, 'j', 1),
                    KeyboardButton('i', '"', '?', null, 'c', 1),
                    KeyboardButton('a', ':', '[', null, 'u', 1),
                    KeyboardButton('e', ']', ';', null, 'b', 1),
                    KeyboardButton('h', '%', '/', null, 'g', 1),
                    KeyboardButton('o', '>', '&', null, 'v', 1),
                    KeyboardButton('p', null, '\\', null, '#', 1),
                ),
                arrayOf(
                    KeyboardButton('y', '*', null, '^', '{', 1),
                    KeyboardButton('r', 'f', '(', '.', '\'', 1),
                    KeyboardButton(' ', null, null, null, null, 2),
                    KeyboardButton('s', ')', 'w', ',', '_', 1),
                    KeyboardButton('d', null, '@', '|', '}', 1),
                ),
                arrayOf(
                    KeyboardButton('l', '-', null, 'x', null, 1),
                    KeyboardButton('k', '=', '+', 'z', null, 1),
                    KeyboardButton('n', '`', '!', 'q', null, 1),
                    KeyboardButton('0', null, null, null, null, 1),
                ),
                arrayOf(
                    KeyboardButton('1', null, null, null, null, 1)
                )
            )
        )
    }
}