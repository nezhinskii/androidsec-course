package com.google.firebase.example.makeitso.ui.signin

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.example.makeitso.MainViewModel
import com.google.firebase.example.makeitso.data.model.ErrorMessage
import com.google.firebase.example.makeitso.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val webClientId: String
) : MainViewModel() {
    private val _shouldRestartApp = MutableStateFlow(false)
    val shouldRestartApp: StateFlow<Boolean>
        get() = _shouldRestartApp.asStateFlow()
    fun resetRestart() {
        _shouldRestartApp.value = false
    }

    fun signIn(
        email: String,
        password: String,
        showErrorSnackbar: (ErrorMessage) -> Unit
    ) {
        launchCatching(showErrorSnackbar) {
            authRepository.signIn(email, password)
            _shouldRestartApp.value = true
        }
    }


    private fun createGoogleSignInRequest(webClientId: String): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false)
            .build()
        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    suspend fun signInWithGoogle(
        context: Context,
        showErrorSnackbar: (ErrorMessage) -> Unit
    ) {
        try {
            val request = createGoogleSignInRequest(webClientId)
            val credentialManager = CredentialManager.create(context)
            val result = credentialManager.getCredential(context, request)
            if (result.credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                authRepository.signInWithGoogle(googleIdTokenCredential.idToken)
                _shouldRestartApp.value = true
            } else {
                showErrorSnackbar(ErrorMessage.StringError("Invalid credential type"))
            }
        } catch (e: Exception) {
            showErrorSnackbar(ErrorMessage.StringError("Google sign-in failed: ${e.message}"))
        }
    }
}