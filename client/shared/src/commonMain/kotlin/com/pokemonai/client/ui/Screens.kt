package com.pokemonai.client.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.pokemonai.client.core.PersonalityProfile
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.pokemonai.client.auth.LoginViewModel
import com.pokemonai.client.core.AnswerInput
import com.pokemonai.client.core.Recommendation
import com.pokemonai.client.core.UiState
import com.pokemonai.client.questionnaire.QuestionnaireViewModel
import com.pokemonai.client.recommendation.RecommendationViewModel
import org.koin.compose.koinInject

// ── Palette ───────────────────────────────────────────────────────────────────
private val White        = Color(0xFFFFFFFF)
private val Surface      = Color(0xFFF2F4F7)
private val Border       = Color(0xFFE8ECF2)
private val Ink          = Color(0xFF1A1A2E)
private val CardBlue     = Color(0xFF3D5A99)
private val PokeRed      = Color(0xFFE63946)
private val BodyGrey     = Color(0xFF6B7280)
private val ErrorBg      = Color(0xFFFFF0F0)
private val ImageBg      = Color(0xFFF7F9FC)
private val HeroWarm     = Color(0xFFFFF5F5)  // barely-there red warmth for the #1 hero

// ── Type styles ───────────────────────────────────────────────────────────────
private val LogoStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 15.sp,
    letterSpacing = 2.sp,
    color = Ink,
)
private val HeadingStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    color = Ink,
)
private val CardNameStyle = TextStyle(
    fontFamily = FontFamily.Serif,
    fontWeight = FontWeight.Bold,
    fontSize = 17.sp,
    color = Ink,
)
private val DetailNameStyle = TextStyle(
    fontFamily = FontFamily.Serif,
    fontWeight = FontWeight.Bold,
    fontSize = 26.sp,
    color = Ink,
)
private val BodyStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    color = BodyGrey,
    lineHeight = 19.sp,
)
private val LabelStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    color = BodyGrey,
)
private val MatchStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp,
    color = CardBlue,
)
private val SectionLabelStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 11.sp,
    letterSpacing = 1.sp,
    color = BodyGrey,
)

// ── Shared composables ────────────────────────────────────────────────────────

@Composable
private fun AppLogo(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(50)).background(PokeRed))
        Text("POKÉMON AI", style = LogoStyle)
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PokeRed,
            contentColor = White,
            disabledContainerColor = PokeRed.copy(alpha = 0.3f),
            disabledContentColor = White.copy(alpha = 0.5f),
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
private fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(6.dp),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = SolidColor(Border)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = BodyGrey),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Text(text, fontSize = 14.sp)
    }
}

@Composable
private fun ScreenShell(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(White)) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 24.dp),
            content = content,
        )
    }
}

@Composable
private fun ShellDivider() {
    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Border)
}

@Composable
private fun TopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    action: Pair<String, () -> Unit>? = null,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (onBack != null) {
                TextButton(onClick = onBack, contentPadding = PaddingValues(horizontal = 0.dp)) {
                    Text("←", style = TextStyle(fontSize = 18.sp, color = BodyGrey))
                }
            }
            Text(title, style = HeadingStyle)
        }
        if (action != null) {
            TextButton(onClick = action.second) {
                Text(action.first, style = LabelStyle)
            }
        }
    }
}

@Composable
private fun InlineError(message: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(ErrorBg)
            .border(1.dp, PokeRed.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text("!", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PokeRed))
        Text(message, style = BodyStyle.copy(color = PokeRed.copy(alpha = 0.85f)))
    }
}

// ── Login ─────────────────────────────────────────────────────────────────────

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val vm = koinInject<LoginViewModel>()
    val state by vm.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loading = state is UiState.Loading

    LaunchedEffect(state) {
        if (state is UiState.Success) onLoginSuccess()
    }

    Box(Modifier.fillMaxSize().background(White), contentAlignment = Alignment.Center) {
        Column(Modifier.width(340.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AppLogo()
            Spacer(Modifier.height(6.dp))
            Text("Find your Pokémon match", style = LabelStyle)
            Spacer(Modifier.height(36.dp))
            LightTextField(value = email, onValueChange = { email = it }, label = "Email", enabled = !loading)
            Spacer(Modifier.height(10.dp))
            LightTextField(value = password, onValueChange = { password = it }, label = "Password", enabled = !loading, isPassword = true)
            if (state is UiState.Error) {
                Spacer(Modifier.height(14.dp))
                InlineError((state as UiState.Error).message)
            }
            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = if (loading) "Signing in…" else "Sign in",
                onClick = { vm.login(email, password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading && email.isNotBlank() && password.isNotBlank(),
            )
            Spacer(Modifier.height(8.dp))
            SecondaryButton(
                text = "Create account",
                onClick = { vm.register(email, password) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LightTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean = true,
    isPassword: Boolean = false,
) {
    val visualTransformation = if (isPassword)
        androidx.compose.ui.text.input.PasswordVisualTransformation()
    else
        androidx.compose.ui.text.input.VisualTransformation.None

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = LabelStyle) },
        enabled = enabled,
        singleLine = true,
        visualTransformation = visualTransformation,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CardBlue,
            unfocusedBorderColor = Border,
            focusedLabelColor = CardBlue,
            unfocusedLabelColor = BodyGrey,
            focusedTextColor = Ink,
            unfocusedTextColor = Ink,
            cursorColor = CardBlue,
            focusedContainerColor = White,
            unfocusedContainerColor = White,
        ),
    )
}

// ── Questionnaire ─────────────────────────────────────────────────────────────

@Composable
fun QuestionnaireScreen(onSubmitted: () -> Unit) {
    val vm = koinInject<QuestionnaireViewModel>()
    val questions by vm.questions.collectAsState()
    val submitState by vm.submitState.collectAsState()
    val answers = remember { mutableStateMapOf<String, Int>() }

    LaunchedEffect(Unit) { vm.loadQuestions() }
    LaunchedEffect(submitState) { if (submitState is UiState.Success) onSubmitted() }

    ScreenShell {
        TopBar("Find your Pokémon")
        ShellDivider()
        when (questions) {
            is UiState.Loading, UiState.Idle -> Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                CircularProgressIndicator(color = PokeRed, strokeWidth = 2.dp)
            }
            is UiState.Error -> Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                InlineError((questions as UiState.Error).message)
            }
            is UiState.Success -> {
                val list = (questions as UiState.Success).data
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(list) { _, q ->
                        QuestionCard(
                            text = q.text,
                            value = answers[q.id] ?: 3,
                            onValueChange = { answers[q.id] = it },
                        )
                    }
                    item { Spacer(Modifier.height(4.dp)) }
                }
                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    text = if (submitState is UiState.Loading) "Matching…" else "See my matches",
                    onClick = { vm.submitAnswers(list.map { AnswerInput(it.id, answers[it.id] ?: 3) }) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = submitState !is UiState.Loading,
                )
            }
        }
    }
}

@Composable
private fun QuestionCard(text: String, value: Int, onValueChange: (Int) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(text, style = BodyStyle.copy(color = Ink, fontSize = 14.sp, lineHeight = 20.sp))
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Not me", style = LabelStyle)
            Text("Totally me", style = LabelStyle)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = PokeRed,
                activeTrackColor = PokeRed.copy(alpha = 0.7f),
                inactiveTrackColor = Border,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ── Processing ────────────────────────────────────────────────────────────────

@Composable
fun ProcessingScreen(onGenerated: () -> Unit) {
    val vm = koinInject<RecommendationViewModel>()
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.generate() }
    LaunchedEffect(state) { if (state is UiState.Success) onGenerated() }

    Box(Modifier.fillMaxSize().background(White), Alignment.Center) {
        when (state) {
            is UiState.Error -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp).width(320.dp),
            ) {
                InlineError((state as UiState.Error).message)
                PrimaryButton("Try again", onClick = { vm.generate() }, modifier = Modifier.fillMaxWidth())
            }
            else -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CircularProgressIndicator(color = PokeRed, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                Text("Finding your Pokémon…", style = BodyStyle)
            }
        }
    }
}

// ── Result list ───────────────────────────────────────────────────────────────

@Composable
fun ResultScreen(
    onSelectRecommendation: (Recommendation) -> Unit,
    onTakeQuiz: () -> Unit,
    onViewHistory: () -> Unit,
    onSignOut: () -> Unit,
) {
    val vm = koinInject<RecommendationViewModel>()
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { if (state is UiState.Idle) vm.load() }

    when (state) {
        is UiState.Loading, UiState.Idle -> Box(
            Modifier.fillMaxSize().background(White),
            Alignment.Center,
        ) {
            CircularProgressIndicator(color = PokeRed, strokeWidth = 2.dp)
        }

        is UiState.Error -> Box(
            Modifier.fillMaxSize().background(White).padding(32.dp),
            Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.width(320.dp),
            ) {
                InlineError((state as UiState.Error).message)
                SecondaryButton("Sign out", onClick = onSignOut, modifier = Modifier.fillMaxWidth())
            }
        }

        is UiState.Success -> {
            val recs = (state as UiState.Success).data
            if (recs.isEmpty()) {
                Box(Modifier.fillMaxSize().background(White).padding(32.dp), Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.width(280.dp),
                    ) {
                        Text("No matches yet", style = HeadingStyle)
                        Text(
                            "Answer a few questions and we'll find your Pokémon.",
                            style = BodyStyle,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(4.dp))
                        PrimaryButton("Take the quiz", onClick = onTakeQuiz, modifier = Modifier.fillMaxWidth())
                    }
                }
            } else {
                val top = recs.first()
                val others = recs.drop(1)
                LazyColumn(Modifier.fillMaxSize().background(White)) {
                    // Sign-out row
                    item {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(onClick = onSignOut) {
                                Text("Sign out", style = LabelStyle)
                            }
                        }
                    }

                    // ── Hero card for #1 match ──
                    item {
                        TopMatchHero(rec = top, onClick = { onSelectRecommendation(top) })
                    }

                    // ── Section label ──
                    if (others.isNotEmpty()) {
                        item {
                            Text(
                                "OTHER MATCHES",
                                style = SectionLabelStyle,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            )
                        }
                        itemsIndexed(others) { _, rec ->
                            SecondaryMatchRow(
                                rec = rec,
                                onClick = { onSelectRecommendation(rec) },
                            )
                        }
                    }

                    item {
                        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                            SecondaryButton(
                                "View history",
                                onClick = onViewHistory,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

// The hero card — full-width panel for the #1 match
@Composable
private fun TopMatchHero(rec: Recommendation, onClick: () -> Unit) {
    val matchPct = "${"%.0f".format(rec.matchScore * 100)}%"
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(HeroWarm)
            .border(1.dp, PokeRed.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        // Large watermark percentage behind the artwork
        Text(
            matchPct,
            style = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = 110.sp,
                color = PokeRed.copy(alpha = 0.055f),
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 0.dp),
        )

        Column(Modifier.fillMaxWidth()) {
            // Top label
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PokeRed)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        "YOUR POKÉMON",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp,
                            color = White,
                        ),
                    )
                }
                Text(
                    matchPct,
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PokeRed,
                    ),
                )
            }

            // Artwork centered
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (rec.pokemon.imageUrl != null) {
                    AsyncImage(
                        model = rec.pokemon.imageUrl,
                        contentDescription = rec.pokemon.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(165.dp),
                    )
                } else {
                    Text("?", style = TextStyle(fontSize = 64.sp, color = BodyGrey))
                }
            }

            // Name + snippet
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    rec.pokemon.name.replaceFirstChar { it.uppercase() },
                    style = DetailNameStyle.copy(fontSize = 28.sp),
                )
                rec.explanation?.let {
                    Text(
                        it.replace("**", ""),
                        style = BodyStyle.copy(fontSize = 13.sp, lineHeight = 19.sp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "Tap to learn more →",
                    style = LabelStyle.copy(color = PokeRed, fontSize = 12.sp),
                )
            }
        }
    }
}

// Compact row for #2–5
@Composable
private fun SecondaryMatchRow(rec: Recommendation, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Small sprite
        Box(
            Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)).background(White),
            contentAlignment = Alignment.Center,
        ) {
            if (rec.pokemon.imageUrl != null) {
                AsyncImage(
                    model = rec.pokemon.imageUrl,
                    contentDescription = rec.pokemon.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(44.dp),
                )
            } else {
                Text("?", style = TextStyle(fontSize = 18.sp, color = BodyGrey))
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                rec.pokemon.name.replaceFirstChar { it.uppercase() },
                style = CardNameStyle.copy(fontSize = 15.sp),
            )
            rec.explanation?.let {
                Text(
                    it.replace("**", ""),
                    style = BodyStyle.copy(fontSize = 12.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "${"%.0f".format(rec.matchScore * 100)}%",
                style = MatchStyle.copy(color = CardBlue, fontSize = 13.sp),
            )
            Text("→", style = LabelStyle.copy(color = CardBlue))
        }
    }
}

// ── Pokemon Detail ────────────────────────────────────────────────────────────

@Composable
fun PokemonDetailScreen(recommendation: Recommendation, profile: PersonalityProfile?, onBack: () -> Unit) {
    val pokemon = recommendation.pokemon
    val matchPct = "${"%.0f".format(recommendation.matchScore * 100)}%"
    val accentColor = if (recommendation.rank == 1) PokeRed else CardBlue
    val heroBackground = if (recommendation.rank == 1) HeroWarm else ImageBg

    Box(Modifier.fillMaxSize().background(White)) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp),
        ) {
            // Back nav
            item {
                TextButton(
                    onClick = onBack,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    Text("← Back to matches", style = LabelStyle.copy(color = CardBlue, fontSize = 13.sp))
                }
            }

            // Hero — artwork on tinted background with watermark %
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(heroBackground),
                ) {
                    // Watermark
                    Text(
                        matchPct,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = 130.sp,
                            color = accentColor.copy(alpha = 0.06f),
                        ),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 8.dp),
                    )
                    // Artwork
                    if (pokemon.imageUrl != null) {
                        AsyncImage(
                            model = pokemon.imageUrl,
                            contentDescription = pokemon.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(210.dp).align(Alignment.Center),
                        )
                    } else {
                        Text(
                            "?",
                            style = TextStyle(fontSize = 64.sp, color = BodyGrey),
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }

            // Name + match score
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        pokemon.name.replaceFirstChar { it.uppercase() },
                        style = DetailNameStyle,
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            matchPct,
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = accentColor,
                            ),
                        )
                        Text("match", style = LabelStyle)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Border)
            }

            // Why you match — the personal section, visually primary
            recommendation.explanation?.let { explanation ->
                item {
                    Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                Modifier
                                    .width(3.dp)
                                    .height(14.dp)
                                    .background(accentColor),
                            )
                            Text("WHY YOU MATCH", style = SectionLabelStyle)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            explanation.replace("**", ""),
                            style = BodyStyle.copy(
                                fontSize = 15.sp,
                                lineHeight = 23.sp,
                                color = Ink,
                            ),
                        )
                    }
                }
            }

            // Personality trait breakdown
            if (profile != null) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Border)
                    Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(Modifier.width(3.dp).height(14.dp).background(accentColor))
                            Text("YOUR PERSONALITY", style = SectionLabelStyle)
                        }
                        Spacer(Modifier.height(14.dp))
                        val traits = listOf(
                            "Energy"     to profile.energy,
                            "Curiosity"  to profile.curiosity,
                            "Leadership" to profile.leadership,
                            "Loyalty"    to profile.loyalty,
                            "Risk-taking" to profile.risk,
                            "Creativity" to profile.creativity,
                        )
                        traits.forEach { (label, value) ->
                            TraitBar(label = label, value = value, accentColor = accentColor)
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }

            // About the Pokémon
            pokemon.description?.let { desc ->
                item {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Border)
                    Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                Modifier
                                    .width(3.dp)
                                    .height(14.dp)
                                    .background(Border),
                            )
                            Text("ABOUT ${pokemon.name.uppercase()}", style = SectionLabelStyle)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            desc,
                            style = BodyStyle.copy(fontSize = 14.sp, lineHeight = 21.sp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TraitBar(label: String, value: Double, accentColor: Color) {
    val pct = (value * 100).toInt()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = LabelStyle.copy(color = Ink, fontSize = 12.sp))
            Text("$pct%", style = LabelStyle.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = if (value >= 0.7) accentColor else BodyGrey,
            ))
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Border),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(value.toFloat().coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (value >= 0.7) accentColor else CardBlue.copy(alpha = 0.45f)),
            )
        }
    }
}

// ── History ───────────────────────────────────────────────────────────────────

@Composable
fun HistoryScreen(onBack: () -> Unit, onSignOut: () -> Unit) {
    val vm = koinInject<RecommendationViewModel>()
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    ScreenShell {
        when (state) {
            is UiState.Loading, UiState.Idle -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = PokeRed, strokeWidth = 2.dp)
            }
            is UiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.width(320.dp),
                ) {
                    InlineError((state as UiState.Error).message)
                    SecondaryButton("Sign out", onClick = onSignOut, modifier = Modifier.fillMaxWidth())
                }
            }
            is UiState.Success -> {
                val recs = (state as UiState.Success).data
                TopBar("History", onBack = onBack, action = "Sign out" to onSignOut)
                ShellDivider()
                if (recs.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("No matches yet", style = HeadingStyle.copy(fontSize = 17.sp))
                            Text(
                                "Complete the questionnaire to find your Pokémon.",
                                style = BodyStyle,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        itemsIndexed(recs) { _, rec ->
                            HistoryRow(rec)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(rec: Recommendation) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Surface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (rec.pokemon.imageUrl != null) {
                AsyncImage(
                    model = rec.pokemon.imageUrl,
                    contentDescription = rec.pokemon.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(40.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    rec.pokemon.name.replaceFirstChar { it.uppercase() },
                    style = CardNameStyle.copy(fontSize = 15.sp),
                )
                Text(rec.generatedAt.take(10), style = LabelStyle)
            }
        }
        Text("${"%.0f".format(rec.matchScore * 100)}%", style = MatchStyle)
    }
}
