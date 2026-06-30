package com.pokemonai.client.auth

import com.pokemonai.client.core.AuthUser
import com.pokemonai.client.core.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow<UiState<AuthUser>>(UiState.Idle)
    val state: StateFlow<UiState<AuthUser>> = _state

    fun login(email: String, password: String) {
        _state.value = UiState.Loading
        scope.launch {
            _state.value = try {
                UiState.Success(authRepository.login(email, password))
            } catch (e: Throwable) {
                UiState.Error(e.message ?: e::class.simpleName ?: "Login failed")
            }
        }
    }

    fun register(email: String, password: String) {
        _state.value = UiState.Loading
        scope.launch {
            _state.value = try {
                UiState.Success(authRepository.register(email, password))
            } catch (e: Throwable) {
                UiState.Error(e.message ?: e::class.simpleName ?: "Registration failed")
            }
        }
    }
}
