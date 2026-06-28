package com.pokemonai.client.auth

import com.pokemonai.client.core.AuthUser
import com.pokemonai.client.core.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @Test
    fun `login transitions Loading then Success`() = runTest(UnconfinedTestDispatcher()) {
        val fakeUser = AuthUser("1", "ash@pokemon.com")
        val repo = FakeAuthRepository(loginResult = Result.success(fakeUser))
        val vm = LoginViewModel(repo, this)

        vm.login("ash@pokemon.com", "pikachu")

        assertIs<UiState.Success<AuthUser>>(vm.state.value)
        assertEquals(fakeUser, (vm.state.value as UiState.Success).data)
    }

    @Test
    fun `login transitions Loading then Error on failure`() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeAuthRepository(loginResult = Result.failure(RuntimeException("Invalid credentials")))
        val vm = LoginViewModel(repo, this)

        vm.login("bad@email.com", "wrong")

        assertIs<UiState.Error>(vm.state.value)
        assertEquals("Invalid credentials", (vm.state.value as UiState.Error).message)
    }

    @Test
    fun `register transitions Loading then Success`() = runTest(UnconfinedTestDispatcher()) {
        val fakeUser = AuthUser("2", "misty@pokemon.com")
        val repo = FakeAuthRepository(registerResult = Result.success(fakeUser))
        val vm = LoginViewModel(repo, this)

        vm.register("misty@pokemon.com", "starmie")

        assertIs<UiState.Success<AuthUser>>(vm.state.value)
        assertEquals(fakeUser, (vm.state.value as UiState.Success).data)
    }

    @Test
    fun `register transitions Loading then Error on failure`() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeAuthRepository(registerResult = Result.failure(RuntimeException("Email taken")))
        val vm = LoginViewModel(repo, this)

        vm.register("taken@email.com", "pass")

        assertIs<UiState.Error>(vm.state.value)
        assertEquals("Email taken", (vm.state.value as UiState.Error).message)
    }
}

private class FakeAuthRepository(
    private val loginResult: Result<AuthUser> = Result.success(AuthUser("1", "test@test.com")),
    private val registerResult: Result<AuthUser> = Result.success(AuthUser("1", "test@test.com")),
) : AuthRepository {
    override suspend fun login(email: String, password: String): AuthUser = loginResult.getOrThrow()
    override suspend fun register(email: String, password: String): AuthUser = registerResult.getOrThrow()
}
