package com.pokemonai.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pokemonai.client.auth.LoginViewModel
import com.pokemonai.client.core.AnswerInput
import com.pokemonai.client.core.UiState
import com.pokemonai.client.questionnaire.QuestionnaireViewModel
import com.pokemonai.client.recommendation.RecommendationViewModel
import org.koin.compose.koinInject

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val vm = koinInject<LoginViewModel>()
    val state by vm.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        if (state is UiState.Success) onLoginSuccess()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Pokémon AI", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        Button(onClick = { vm.login(email, password) }, modifier = Modifier.fillMaxWidth(), enabled = state !is UiState.Loading) {
            Text(if (state is UiState.Loading) "Signing in…" else "Sign in")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { vm.register(email, password) }, modifier = Modifier.fillMaxWidth(), enabled = state !is UiState.Loading) {
            Text("Create account")
        }
        if (state is UiState.Error) {
            Spacer(Modifier.height(12.dp))
            Text((state as UiState.Error).message, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun QuestionnaireScreen(onSubmitted: () -> Unit) {
    val vm = koinInject<QuestionnaireViewModel>()
    val questions by vm.questions.collectAsState()
    val submitState by vm.submitState.collectAsState()
    val answers = remember { mutableStateMapOf<String, Int>() }

    LaunchedEffect(Unit) { vm.loadQuestions() }
    LaunchedEffect(submitState) { if (submitState is UiState.Success) onSubmitted() }

    when (questions) {
        is UiState.Loading, UiState.Idle -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        is UiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text((questions as UiState.Error).message) }
        is UiState.Success -> {
            val list = (questions as UiState.Success).data
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text("Find your Pokémon", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(list) { q ->
                        Text(q.text, style = MaterialTheme.typography.bodyLarge)
                        Slider(
                            value = (answers[q.id] ?: 3).toFloat(),
                            onValueChange = { answers[q.id] = it.toInt() },
                            valueRange = 1f..5f, steps = 3,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                    }
                }
                Button(
                    onClick = {
                        vm.submitAnswers(list.map { AnswerInput(it.id, answers[it.id] ?: 3) })
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = submitState !is UiState.Loading,
                ) { Text("See my matches") }
            }
        }
    }
}

@Composable
fun ProcessingScreen(onGenerated: () -> Unit) {
    val vm = koinInject<RecommendationViewModel>()
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.generate() }
    LaunchedEffect(state) { if (state is UiState.Success) onGenerated() }

    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("Finding your Pokémon…")
        }
    }
}

@Composable
fun ResultScreen(onViewHistory: () -> Unit) {
    val vm = koinInject<RecommendationViewModel>()
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { if (state is UiState.Idle) vm.load() }

    when (state) {
        is UiState.Loading, UiState.Idle -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        is UiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text((state as UiState.Error).message) }
        is UiState.Success -> {
            val recs = (state as UiState.Success).data
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text("Your Pokémon", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(recs) { rec ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Text("#${rec.rank} ${rec.pokemon.name}", style = MaterialTheme.typography.titleMedium)
                                Text("Match: ${"%.0f".format(rec.matchScore * 100)}%")
                                rec.explanation?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                    }
                }
                OutlinedButton(onClick = onViewHistory, Modifier.fillMaxWidth()) { Text("View history") }
            }
        }
    }
}

@Composable
fun HistoryScreen() {
    val vm = koinInject<RecommendationViewModel>()
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    when (state) {
        is UiState.Loading, UiState.Idle -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        is UiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text((state as UiState.Error).message) }
        is UiState.Success -> {
            val recs = (state as UiState.Success).data
            LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                item { Text("History", style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(8.dp)) }
                items(recs) { rec ->
                    ListItem(
                        headlineContent = { Text(rec.pokemon.name) },
                        supportingContent = { Text("Match: ${"%.0f".format(rec.matchScore * 100)}%") },
                        trailingContent = { Text("#${rec.rank}") },
                    )
                }
            }
        }
    }
}
