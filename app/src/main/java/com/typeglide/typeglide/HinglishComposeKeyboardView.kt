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
                    KeyboardButton('m', '~', null, null, '\n'),
                    KeyboardButton('t', '$', '<', null, 'j'),
                    KeyboardButton('i', '"', '?', null, 'c'),
                    KeyboardButton('a', ':', '[', null, 'u'),
                    KeyboardButton('e', ']', ';', null, 'b'),
                    KeyboardButton('h', '%', '/', null, 'g'),
                    KeyboardButton('o', '>', '&', null, 'v'),
                    KeyboardButton('p', null, '\\', null, '#'),
                ),
                arrayOf(
                    KeyboardButton('y', '*', null, '^', '{'),
                    KeyboardButton('r', 'f', '(', '.', '\''),
                    KeyboardButton(' ', null, null, null, null),
                    KeyboardButton('s', ')', 'w', ',', '_'),
                    KeyboardButton('d', null, '@', '|', '}'),
                ),
                arrayOf(
                    KeyboardButton('l', '-', null, 'x', null),
                    KeyboardButton('k', '=', '+', 'z', null),
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
