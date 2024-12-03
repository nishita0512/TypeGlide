package com.typeglide.typeglide.services

import android.content.Intent
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.typeglide.typeglide.Constants
import com.typeglide.typeglide.EnglishComposeKeyboardView
import com.typeglide.typeglide.HinglishComposeKeyboardView

class IMEService : LifeCycleInputMethodService(),
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    override fun onCreateInputView(): View {
        val preferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        val language = preferences.getString("language", "English")
        Log.d("OnCreateInputView", "$language keyboard")
        val view = if(language.equals("English")){
            EnglishComposeKeyboardView(this)
        }
        else{
            HinglishComposeKeyboardView(this)
        }
        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }
        return view
    }

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(editorInfo, restarting)
        setInputView(onCreateInputView())
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
    }

    override val viewModelStore: ViewModelStore
        get() = store
    override val lifecycle: Lifecycle
        get() = dispatcher.lifecycle

    //ViewModelStore Methods
    private val store = ViewModelStore()

    //SaveStateRegestry Methods

    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
}