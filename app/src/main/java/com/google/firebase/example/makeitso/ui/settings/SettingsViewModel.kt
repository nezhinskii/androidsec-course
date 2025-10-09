package com.google.firebase.example.makeitso.ui.settings

import android.util.Log
import com.google.firebase.example.makeitso.MainViewModel
import com.google.firebase.example.makeitso.data.model.User
import com.google.firebase.example.makeitso.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : MainViewModel() {
    private val _shouldRestartApp = MutableStateFlow(false)
    val shouldRestartApp: StateFlow<Boolean>
        get() = _shouldRestartApp.asStateFlow()

    private val _user = MutableStateFlow(User())
    val user: StateFlow<User>
        get() = _user.asStateFlow()

    fun loadCurrentUser() {
        launchCatching {
            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                val authMethod = when {
                    currentUser.providerData.any { it.providerId == "google.com" } -> "Google"
                    currentUser.providerData.any { it.providerId == "password" } -> "Email/Password"
                    else -> "Unknown"
                }
                _user.value = User(
                    id = currentUser.uid,
                    email = currentUser.email ?: "",
                    displayName = currentUser.displayName ?: "",
                    authMethod = authMethod
                )
            } else {
                _shouldRestartApp.value = true
            }
        }
    }

    fun signOut() {
        launchCatching {
            authRepository.signOut()
            _shouldRestartApp.value = true
        }
    }

    fun deleteAccount() {
        launchCatching {
            authRepository.deleteAccount()
            _shouldRestartApp.value = true
        }
    }

    fun resetRestart() {
        _shouldRestartApp.value = false
    }
}