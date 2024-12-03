package com.typeglide.typeglide.activities

import android.content.Intent
import android.inputmethodservice.InputMethodService.MODE_PRIVATE
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getSystemService
import com.typeglide.typeglide.Constants
import com.typeglide.typeglide.R
import com.typeglide.typeglide.services.IMEService
import com.typeglide.typeglide.ui.theme.TypeGlideTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TypeGlideTheme{
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column {
                        Options()
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            isSystemInDarkTheme()
        }
    }

}

@Composable
fun Options() {
    Column(
        Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        val ctx = LocalContext.current
        Text(text = "Compose Keyboard")
        val (text, setValue) = remember { mutableStateOf(TextFieldValue("Try here")) }
        Spacer(modifier = Modifier.height(16.dp))
        Button(modifier = Modifier.fillMaxWidth(), onClick = {
            ctx.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }) {
            Text(text = "Enable IME")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(modifier = Modifier.fillMaxWidth(), onClick = {
            getSystemService(ctx,InputMethodManager::class.java)?.showInputMethodPicker()
        }) {
            Text(text = "Select IME")
        }
        Spacer(modifier = Modifier.height(16.dp))

        val context = LocalContext.current
        val sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        val currentLanguage = remember { mutableStateOf(sharedPreferences.getString("language", "English") ?: "English")}
        Log.d("CurrentLanguageDropdownMenu", currentLanguage.value)

        LanguageDropdownMenu(selectedLanguage = currentLanguage.value) { selectedLanguage ->
            Log.d("SelectedLanguageDropdownMenu",selectedLanguage)
            val editor = sharedPreferences.edit()
            editor.putString("language", selectedLanguage)
            editor.apply()
            currentLanguage.value = selectedLanguage
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextField(value = text, onValueChange = setValue, modifier = Modifier.fillMaxWidth())
    }
}

//@Composable
//fun LanguageDropdownMenu(
//    selectedLanguage: String,
//    onLanguageSelected: (String) -> Unit
//) {
//    val expanded = remember { mutableStateOf(false) }
//    val languages = listOf("English", "Hinglish")
//
//    Box(
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Log.d("LanguageDropdownMenu", selectedLanguage)
//        TextButton(onClick = { expanded.value = true }) {
//            Text(text = selectedLanguage)
//        }
//        DropdownMenu(
//            modifier = Modifier.fillMaxWidth(),
//            expanded = expanded.value,
//            onDismissRequest = { expanded.value = false }
//        ) {
//            languages.forEach { language ->
//                DropdownMenuItem(
//                    text = { Text(text = language) },
//                    onClick = {
//                        Log.d("DropdownMenuItemClicked", language)
//                        expanded.value = false
//                        onLanguageSelected(language)
//                    }
//                )
//            }
//        }
//    }
//}

@Composable
fun LanguageDropdownMenu(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    val languages = listOf("English", "Hinglish")
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    Box(
        modifier = Modifier.fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, if (isDarkTheme) Color.LightGray else Color.DarkGray)
            .clickable {
                expanded.value = true
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                modifier = Modifier.weight(9f),
                onClick = { expanded.value = true }
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    text = selectedLanguage
                )
            }
            // Add arrow button
            IconButton(
                modifier = Modifier.weight(1f),
                onClick = { expanded.value = !expanded.value }
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = if (expanded.value) "Collapse" else "Expand",
                    tint = if (isDarkTheme) Color.White else Color.Black // Dynamic color
                )
            }
        }

        DropdownMenu(
            modifier = Modifier.fillMaxWidth(0.9f),
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            languages.forEach { language ->
                DropdownMenuItem(
                    text = { Text(text = language) },
                    onClick = {
                        expanded.value = false
                        onLanguageSelected(language)
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun OptionsPreview(){
    TypeGlideTheme {
        Options()
    }
}