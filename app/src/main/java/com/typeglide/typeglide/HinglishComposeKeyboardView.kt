package com.typeglide.typeglide

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AbstractComposeView
import com.typeglide.typeglide.composables.KeyboardScreen
import com.typeglide.typeglide.models.KeyboardButton

class HinglishComposeKeyboardView(context: Context) : AbstractComposeView(context) {
    @Composable
    override fun Content() {
        KeyboardScreen(
            arrayOf(
                arrayOf(
                    KeyboardButton('d', '~', null, null, '\n', 1),
                    KeyboardButton('e', '$', '<', null, 'z', 1),
                    KeyboardButton('h', '"', '?', null, 'y', 1),
                    KeyboardButton('i', ':', '[', null, 'p', 1),
                    KeyboardButton('a', ']', ';', null, 'j', 1),
                    KeyboardButton('t', '%', '/', null, 'b', 1),
                    KeyboardButton('k', '>', '&', null, 'v', 1),
                    KeyboardButton('l', null, '\\', null, '#', 1),
                ),
                arrayOf(
                    KeyboardButton('g', '*', null, '^', '{', 1),
                    KeyboardButton('s', 'u', '(', '.', '\'', 1),
                    KeyboardButton(' ', null, null, null, null, 2),
                    KeyboardButton('r', ')', 'c', ',', '_', 1),
                    KeyboardButton('m', null, '@', '|', '}', 1),
                ),
                arrayOf(
                    KeyboardButton('o', '-', null, 'f', null, 1),
                    KeyboardButton('w', '=', '+', 'x', null, 1),
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
