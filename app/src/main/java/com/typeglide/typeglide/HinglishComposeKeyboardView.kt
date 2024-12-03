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
                    KeyboardButton('d', '~', null, null, '\n'),
                    KeyboardButton('e', '$', '<', null, 'z'),
                    KeyboardButton('h', '"', '?', null, 'y'),
                    KeyboardButton('i', ':', '[', null, 'p'),
                    KeyboardButton('a', ']', ';', null, 'j'),
                    KeyboardButton('t', '%', '/', null, 'b'),
                    KeyboardButton('k', '>', '&', null, 'v'),
                    KeyboardButton('l', null, '\\', null, '#'),
                ),
                arrayOf(
                    KeyboardButton('g', '*', null, '^', '{'),
                    KeyboardButton('s', 'u', '(', '.', '\''),
                    KeyboardButton(' ', null, null, null, null),
                    KeyboardButton('r', ')', 'c', ',', '_'),
                    KeyboardButton('m', null, '@', '|', '}'),
                ),
                arrayOf(
                    KeyboardButton('o', '-', null, 'f', null),
                    KeyboardButton('w', '=', '+', 'x', null),
                    KeyboardButton('n', '`', '!', 'q', null),
                    KeyboardButton('0', null, null, null, null),
                ),
                arrayOf(
                    KeyboardButton('1', null, null, null, null)
                )
            )
        )
    }
}
