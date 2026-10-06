package com.example.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthRepository
import com.example.data.auth.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<UserProfile?> = authRepository.currentUser
    val isAuthenticated: StateFlow<Boolean> = authRepository.isAuthenticated

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true, errorMessage = null)
            val result = authRepository.signInWithGoogle(context)
            result.fold(
                onSuccess = {
                    _uiState.value = AuthUiState(isLoading = false, errorMessage = null)
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Sign-in cancelled or failed"
                    )
                }
            )
        }
    }

    fun signInWithDemo() {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true, errorMessage = null)
            authRepository.signInWithDemo()
            _uiState.value = AuthUiState(isLoading = false, errorMessage = null)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
