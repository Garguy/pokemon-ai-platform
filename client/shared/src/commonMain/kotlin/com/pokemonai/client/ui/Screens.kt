package com.pokemonai.client.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.composables.ui.components.Button
import com.composables.ui.components.ButtonSize
import com.composables.ui.components.ButtonStyle
import com.composables.ui.components.IndeterminateProgressIndicator
import com.composables.ui.components.Slider
import com.composables.ui.components.TextField
import com.pokemonai.client.auth.LoginViewModel
import com.pokemonai.client.core.AnswerInput
import com.pokemonai.client.core.PersonalityProfile
import com.pokemonai.client.core.Recommendation
import com.pokemonai.client.core.UiState
import com.pokemonai.client.questionnaire.QuestionnaireViewModel
import com.pokemonai.client.recommendation.RecommendationViewModel
import org.koin.compose.koinInject

// ── Typography ────────────────────────────────────────────────────────────────

private val DisplayStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 30.sp, color = PokeColors.Ink, lineHeight = 36.sp)
private val HeroName     = TextStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp, color = PokeColors.Ink)
private val TitleStyle   = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = PokeColors.Ink)
private val CardName     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = PokeColors.Ink)
private val BodyStyle    = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, color = PokeColors.Muted, lineHeight = 22.sp)
private val LabelStyle   = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, color = PokeColors.Muted)
private val CapStyle     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.4.sp, color = PokeColors.MutedLight)
private val MonoStyle    = TextStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.Monospace, color = PokeColors.Ink)

// ── Pokémon type lookup (Gen 1) ───────────────────────────────────────────────

private val gen1PrimaryType: Map<String, String> = mapOf(
    "bulbasaur" to "grass", "ivysaur" to "grass", "venusaur" to "grass",
    "charmander" to "fire", "charmeleon" to "fire", "charizard" to "fire",
    "squirtle" to "water", "wartortle" to "water", "blastoise" to "water",
    "caterpie" to "bug", "metapod" to "bug", "butterfree" to "bug",
    "weedle" to "bug", "kakuna" to "bug", "beedrill" to "bug",
    "pidgey" to "normal", "pidgeotto" to "normal", "pidgeot" to "normal",
    "rattata" to "normal", "raticate" to "normal",
    "spearow" to "normal", "fearow" to "normal",
    "ekans" to "poison", "arbok" to "poison",
    "pikachu" to "electric", "raichu" to "electric",
    "sandshrew" to "ground", "sandslash" to "ground",
    "nidoran-f" to "poison", "nidorina" to "poison", "nidoqueen" to "poison",
    "nidoran-m" to "poison", "nidorino" to "poison", "nidoking" to "poison",
    "clefairy" to "normal", "clefable" to "normal",
    "vulpix" to "fire", "ninetales" to "fire",
    "jigglypuff" to "normal", "wigglytuff" to "normal",
    "zubat" to "poison", "golbat" to "poison",
    "oddish" to "grass", "gloom" to "grass", "vileplume" to "grass",
    "paras" to "bug", "parasect" to "bug",
    "venonat" to "bug", "venomoth" to "bug",
    "diglett" to "ground", "dugtrio" to "ground",
    "meowth" to "normal", "persian" to "normal",
    "psyduck" to "water", "golduck" to "water",
    "mankey" to "fighting", "primeape" to "fighting",
    "growlithe" to "fire", "arcanine" to "fire",
    "poliwag" to "water", "poliwhirl" to "water", "poliwrath" to "water",
    "abra" to "psychic", "kadabra" to "psychic", "alakazam" to "psychic",
    "machop" to "fighting", "machoke" to "fighting", "machamp" to "fighting",
    "bellsprout" to "grass", "weepinbell" to "grass", "victreebel" to "grass",
    "tentacool" to "water", "tentacruel" to "water",
    "geodude" to "rock", "graveler" to "rock", "golem" to "rock",
    "ponyta" to "fire", "rapidash" to "fire",
    "slowpoke" to "water", "slowbro" to "water",
    "magnemite" to "electric", "magneton" to "electric",
    "farfetchd" to "normal", "doduo" to "normal", "dodrio" to "normal",
    "seel" to "water", "dewgong" to "water",
    "grimer" to "poison", "muk" to "poison",
    "shellder" to "water", "cloyster" to "water",
    "gastly" to "ghost", "haunter" to "ghost", "gengar" to "ghost",
    "onix" to "rock",
    "drowzee" to "psychic", "hypno" to "psychic",
    "krabby" to "water", "kingler" to "water",
    "voltorb" to "electric", "electrode" to "electric",
    "exeggcute" to "grass", "exeggutor" to "grass",
    "cubone" to "ground", "marowak" to "ground",
    "hitmonlee" to "fighting", "hitmonchan" to "fighting",
    "lickitung" to "normal",
    "koffing" to "poison", "weezing" to "poison",
    "rhyhorn" to "ground", "rhydon" to "ground",
    "chansey" to "normal",
    "tangela" to "grass",
    "kangaskhan" to "normal",
    "horsea" to "water", "seadra" to "water",
    "goldeen" to "water", "seaking" to "water",
    "staryu" to "water", "starmie" to "water",
    "mrmime" to "psychic",
    "scyther" to "bug",
    "jynx" to "psychic",
    "electabuzz" to "electric",
    "magmar" to "fire",
    "pinsir" to "bug",
    "tauros" to "normal",
    "magikarp" to "water", "gyarados" to "water",
    "lapras" to "water",
    "ditto" to "normal",
    "eevee" to "normal",
    "vaporeon" to "water", "jolteon" to "electric", "flareon" to "fire",
    "porygon" to "normal",
    "omanyte" to "rock", "omastar" to "rock",
    "kabuto" to "rock", "kabutops" to "rock",
    "aerodactyl" to "rock",
    "snorlax" to "normal",
    "articuno" to "ice", "zapdos" to "electric", "moltres" to "fire",
    "dratini" to "dragon", "dragonair" to "dragon", "dragonite" to "dragon",
    "mewtwo" to "psychic", "mew" to "psychic",
)

fun pokemonType(name: String): String =
    gen1PrimaryType[name.lowercase().replace(" ", "-").replace(".", "")] ?: "normal"

// ── Animated sprite with type effect ─────────────────────────────────────────

@Composable
fun AnimatedPokemonSprite(name: String, imageUrl: String?, modifier: Modifier = Modifier) {
    val type = pokemonType(name)
    val transition = rememberInfiniteTransition()

    // Universal bob — all Pokémon float gently
    val bob by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse)
    )
    val offsetY = (bob - 0.5f) * 12f  // ±6dp range

    // Type-specific ambient value (0→1 looping)
    val ambient by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart)
    )

    Box(modifier, contentAlignment = Alignment.Center) {
        // Type-specific background effect
        when (type) {
            "electric" -> ElectricEffect(ambient)
            "water"    -> RainEffect(ambient)
            "fire"     -> FireGlowEffect(ambient)
            "grass", "bug" -> LeafEffect(ambient)
            "psychic"  -> PsychicRingsEffect(ambient)
            "ghost"    -> GhostMistEffect(ambient)
            else       -> PulseGlowEffect(typeColor(type), ambient)
        }

        // Sprite with bob
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize(0.75f)
                    .offset(y = offsetY.dp),
            )
        }
    }
}

@Composable
private fun ElectricEffect(t: Float) {
    // Flickering lightning bolts drawn on canvas
    val flash1 by rememberInfiniteTransition().animateFloat(
        0f, 1f, infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Restart)
    )
    val flash2 by rememberInfiniteTransition().animateFloat(
        0f, 1f, infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Restart)
    )
    val alpha1 = if (flash1 < 0.15f) flash1 / 0.15f else if (flash1 < 0.25f) 1f - (flash1 - 0.15f) / 0.1f else 0f
    val alpha2 = if (flash2 < 0.1f) flash2 / 0.1f else if (flash2 < 0.2f) 1f - (flash2 - 0.1f) / 0.1f else 0f

    Box(
        Modifier.fillMaxSize().drawBehind {
            // Radial yellow glow
            val cx = size.width / 2f
            val cy = size.height / 2f
            drawCircle(
                color = PokeColors.ElectricYellow.copy(alpha = 0.18f + alpha1 * 0.12f),
                radius = size.minDimension * 0.45f,
                center = Offset(cx, cy),
            )
            // Bolt 1 — upper left to center
            drawLightningBolt(
                cx - size.width * 0.28f, cy - size.height * 0.35f,
                cx - size.width * 0.08f, cy,
                PokeColors.ElectricYellow.copy(alpha = alpha1 * 0.9f), strokeWidth = 3f
            )
            // Bolt 2 — upper right to center
            drawLightningBolt(
                cx + size.width * 0.25f, cy - size.height * 0.30f,
                cx + size.width * 0.06f, cy + size.height * 0.05f,
                PokeColors.ElectricYellow.copy(alpha = alpha2 * 0.85f), strokeWidth = 2.5f
            )
        }
    )
}

private fun DrawScope.drawLightningBolt(
    x1: Float, y1: Float, x2: Float, y2: Float,
    color: androidx.compose.ui.graphics.Color, strokeWidth: Float
) {
    val mx = (x1 + x2) / 2f + (y2 - y1) * 0.25f
    val my = (y1 + y2) / 2f - (x2 - x1) * 0.25f
    drawLine(color, Offset(x1, y1), Offset(mx, my), strokeWidth)
    drawLine(color, Offset(mx, my), Offset(x2, y2), strokeWidth)
}

@Composable
private fun RainEffect(t: Float) {
    // 12 rain drops at different phases falling through the frame
    val drops = remember {
        List(12) { i ->
            Triple(
                0.05f + (i * 0.083f) % 0.9f,   // x fraction
                (i * 0.19f) % 1f,               // phase offset
                0.4f + (i % 3) * 0.2f           // opacity base
            )
        }
    }
    val progress by rememberInfiniteTransition().animateFloat(
        0f, 1f, infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart)
    )
    Box(
        Modifier.fillMaxSize().drawBehind {
            val w = size.width; val h = size.height
            // Soft blue background tint
            drawRect(color = PokeColors.WaterBlue.copy(alpha = 0.06f))
            drops.forEach { (xFrac, phase, alphaBase) ->
                val y = ((progress + phase) % 1f) * h * 1.3f - h * 0.15f
                val x = xFrac * w
                drawLine(
                    color = PokeColors.WaterBlue.copy(alpha = alphaBase * 0.7f),
                    start = Offset(x, y),
                    end = Offset(x - 2f, y + 14f),
                    strokeWidth = 1.5f,
                )
            }
        }
    )
}

@Composable
private fun FireGlowEffect(t: Float) {
    val flicker by rememberInfiniteTransition().animateFloat(
        0.7f, 1f, infiniteRepeatable(tween(300, easing = FastOutSlowInEasing), RepeatMode.Reverse)
    )
    Box(
        Modifier.fillMaxSize().drawBehind {
            val cx = size.width / 2f
            val cy = size.height * 0.65f
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        PokeColors.FireOrange.copy(alpha = 0.35f * flicker),
                        PokeColors.ElectricYellow.copy(alpha = 0.15f * flicker),
                        Color.Transparent,
                    ),
                    center = Offset(cx, cy),
                    radius = size.minDimension * 0.5f,
                ),
                radius = size.minDimension * 0.5f,
                center = Offset(cx, cy),
            )
        }
    )
}

@Composable
private fun LeafEffect(t: Float) {
    val leaves = remember { List(8) { i -> Pair(i * 0.137f, i * 45f) } }
    val progress by rememberInfiniteTransition().animateFloat(
        0f, 1f, infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart)
    )
    Box(
        Modifier.fillMaxSize().drawBehind {
            drawRect(color = PokeColors.GrassGreen.copy(alpha = 0.04f))
            val cx = size.width / 2f; val cy = size.height / 2f
            leaves.forEach { (phase, angleDeg) ->
                val p = (progress + phase) % 1f
                val radius = size.minDimension * 0.42f * p
                val angle = Math.toRadians((angleDeg + p * 120).toDouble())
                val x = cx + radius * cos(angle).toFloat()
                val y = cy + radius * sin(angle).toFloat()
                val alpha = if (p < 0.2f) p / 0.2f else if (p > 0.8f) (1f - p) / 0.2f else 1f
                drawCircle(PokeColors.GrassGreen.copy(alpha = alpha * 0.55f), radius = 5f, center = Offset(x, y))
            }
        }
    )
}

@Composable
private fun PsychicRingsEffect(t: Float) {
    val ring1 by rememberInfiniteTransition().animateFloat(
        0f, 1f, infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart)
    )
    val ring2 by rememberInfiniteTransition().animateFloat(
        0f, 1f, infiniteRepeatable(tween(1600, easing = LinearEasing, delayMillis = 533), RepeatMode.Restart)
    )
    val ring3 by rememberInfiniteTransition().animateFloat(
        0f, 1f, infiniteRepeatable(tween(1600, easing = LinearEasing, delayMillis = 1066), RepeatMode.Restart)
    )
    Box(
        Modifier.fillMaxSize().drawBehind {
            val cx = size.width / 2f; val cy = size.height / 2f
            val maxR = size.minDimension * 0.52f
            listOf(ring1, ring2, ring3).forEach { r ->
                val alpha = (1f - r) * 0.55f
                drawCircle(
                    color = PokeColors.PsychicPink.copy(alpha = alpha),
                    radius = r * maxR,
                    center = Offset(cx, cy),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f),
                )
            }
        }
    )
}

@Composable
private fun GhostMistEffect(t: Float) {
    val mist by rememberInfiniteTransition().animateFloat(
        0f, 1f, infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Restart)
    )
    Box(
        Modifier.fillMaxSize().drawBehind {
            val cx = size.width / 2f; val cy = size.height / 2f
            // Slow pulsing dark mist circles at corners
            val alpha = (sin(mist * Math.PI * 2).toFloat() * 0.5f + 0.5f) * 0.2f
            drawCircle(PokeColors.GhostIndigo.copy(alpha = alpha + 0.05f), radius = size.minDimension * 0.48f, center = Offset(cx, cy))
            drawCircle(PokeColors.GhostIndigo.copy(alpha = alpha * 0.6f), radius = size.minDimension * 0.3f, center = Offset(cx * 0.5f, cy * 0.7f))
            drawCircle(PokeColors.GhostIndigo.copy(alpha = alpha * 0.5f), radius = size.minDimension * 0.25f, center = Offset(cx * 1.5f, cy * 1.2f))
        }
    )
}

@Composable
private fun PulseGlowEffect(color: Color, t: Float) {
    val pulse by rememberInfiniteTransition().animateFloat(
        0.6f, 1f, infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse)
    )
    Box(
        Modifier.fillMaxSize().drawBehind {
            drawCircle(
                color = color.copy(alpha = 0.12f * pulse),
                radius = size.minDimension * 0.46f,
                center = Offset(size.width / 2f, size.height / 2f),
            )
        }
    )
}

// ── Shared primitives ─────────────────────────────────────────────────────────

@Composable
private fun PokeScreen(content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(PokeColors.Cream), content = content)
}

@Composable
private fun PokeCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val base = modifier
        .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp),
            ambientColor = PokeColors.Ink.copy(alpha = 0.05f),
            spotColor = PokeColors.Ink.copy(alpha = 0.08f))
        .clip(RoundedCornerShape(20.dp))
        .background(PokeColors.White)
    Column(if (onClick != null) base.clickable(onClick = onClick) else base, content = content)
}

@Composable
private fun TypeBadge(type: String) {
    val bg = typeColor(type)
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(bg.copy(alpha = 0.12f))
            .border(1.dp, bg.copy(alpha = 0.35f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
        androidx.compose.material3.Text(
            type.replaceFirstChar { it.uppercase() },
            style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = bg)
        )
    }
}

@Composable
private fun SectionLabel(text: String, accent: Color = PokeColors.PokeRed) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.width(3.dp).height(14.dp).clip(RoundedCornerShape(2.dp)).background(accent))
        androidx.compose.material3.Text(text.uppercase(), style = CapStyle)
    }
}

@Composable
private fun InlineError(message: String) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PokeColors.ErrorBg)
            .border(1.dp, PokeColors.ErrorRed.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        androidx.compose.material3.Text("!", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PokeColors.ErrorRed))
        androidx.compose.material3.Text(message, style = BodyStyle.copy(color = PokeColors.ErrorRed.copy(alpha = 0.85f)))
    }
}

// ── Login ─────────────────────────────────────────────────────────────────────

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val vm = koinInject<LoginViewModel>()
    val state by vm.state.collectAsState()
    val emailState = rememberTextFieldState()
    val passwordState = rememberTextFieldState()
    val loading = state is UiState.Loading

    LaunchedEffect(state) { if (state is UiState.Success) onLoginSuccess() }

    PokemonTheme {
        PokeScreen {
            Column(
                Modifier.fillMaxSize().padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Pokéball logo
                Box(
                    Modifier.size(72.dp).clip(CircleShape)
                        .background(PokeColors.PokeRedLight)
                        .border(1.5.dp, PokeColors.Border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.size(32.dp).clip(CircleShape).background(PokeColors.PokeRed))
                    Box(Modifier.fillMaxWidth().height(1.5.dp).background(PokeColors.PokeRed.copy(alpha = 0.4f)))
                }
                Spacer(Modifier.height(20.dp))
                androidx.compose.material3.Text("Pokémon AI", style = DisplayStyle)
                Spacer(Modifier.height(4.dp))
                androidx.compose.material3.Text("Discover your Pokémon match", style = LabelStyle)
                Spacer(Modifier.height(40.dp))

                PokeCard(Modifier.fillMaxWidth().widthIn(max = 400.dp)) {
                    Column(Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        androidx.compose.material3.Text("Sign in", style = TitleStyle)
                        Spacer(Modifier.height(2.dp))
                        TextField(
                            state = emailState,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading,
                            placeholder = {
                                androidx.compose.material3.Text("Email", style = LabelStyle)
                            },
                        )
                        TextField(
                            state = passwordState,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading,
                            placeholder = {
                                androidx.compose.material3.Text("Password", style = LabelStyle)
                            },
                            outputTransformation = androidx.compose.foundation.text.input.OutputTransformation {
                                val len = originalText.length
                                replace(0, len, "•".repeat(len))
                            },
                        )
                        AnimatedVisibility(state is UiState.Error, enter = fadeIn(), exit = fadeOut()) {
                            InlineError((state as? UiState.Error)?.message ?: "")
                        }
                        Spacer(Modifier.height(2.dp))
                        Button(
                            onClick = { vm.login(emailState.text.toString(), passwordState.text.toString()) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading && emailState.text.isNotBlank() && passwordState.text.isNotBlank(),
                            style = ButtonStyle.Primary,
                            buttonSize = ButtonSize.Large,
                        ) {
                            androidx.compose.material3.Text(if (loading) "Signing in…" else "Sign in")
                        }
                        Button(
                            onClick = { vm.register(emailState.text.toString(), passwordState.text.toString()) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading && emailState.text.isNotBlank() && passwordState.text.isNotBlank(),
                            style = ButtonStyle.Outlined,
                            buttonSize = ButtonSize.Large,
                        ) {
                            androidx.compose.material3.Text("Create account")
                        }
                    }
                }
            }
        }
    }
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

    PokemonTheme {
        PokeScreen {
            Column(Modifier.fillMaxSize()) {
                // Top bar
                Row(
                    Modifier.fillMaxWidth()
                        .background(PokeColors.White)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(PokeColors.PokeRed))
                        androidx.compose.material3.Text("Personality Quiz", style = TitleStyle)
                    }
                    androidx.compose.material3.Text("5-question match", style = LabelStyle)
                }
                androidx.compose.material3.HorizontalDivider(color = PokeColors.Border, thickness = 1.dp)

                when (questions) {
                    is UiState.Loading, UiState.Idle -> Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                        IndeterminateProgressIndicator(
                            modifier = Modifier.width(120.dp),
                            indicatorColor = PokeColors.PokeRed,
                            trackColor = PokeColors.CreamDeep,
                        )
                    }
                    is UiState.Error -> Box(Modifier.weight(1f).fillMaxWidth().padding(24.dp), Alignment.Center) {
                        InlineError((questions as UiState.Error).message)
                    }
                    is UiState.Success -> {
                        val list = (questions as UiState.Success).data
                        LazyColumn(
                            Modifier.weight(1f).padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            itemsIndexed(list) { index, q ->
                                QuestionCard(
                                    index = index + 1,
                                    total = list.size,
                                    text = q.text,
                                    value = answers[q.id] ?: 3,
                                    onValueChange = { answers[q.id] = it },
                                    category = q.traitCategory,
                                )
                            }
                        }
                        Column(Modifier.background(PokeColors.White).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (submitState is UiState.Error) {
                                InlineError((submitState as UiState.Error).message)
                            }
                            Button(
                                onClick = { vm.submitAnswers(list.map { AnswerInput(it.id, answers[it.id] ?: 3) }) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = submitState !is UiState.Loading,
                                style = ButtonStyle.Primary,
                                buttonSize = ButtonSize.Large,
                            ) {
                                androidx.compose.material3.Text(if (submitState is UiState.Loading) "Matching…" else "Find my Pokémon")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionCard(index: Int, total: Int, text: String, value: Int, onValueChange: (Int) -> Unit, category: String) {
    PokeCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TypeBadge(category)
                androidx.compose.material3.Text("$index / $total", style = MonoStyle.copy(color = PokeColors.MutedLight, fontSize = 11.sp))
            }
            androidx.compose.material3.Text(text, style = BodyStyle.copy(color = PokeColors.Ink, fontSize = 15.sp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                androidx.compose.material3.Text("Not me", style = LabelStyle)
                androidx.compose.material3.Text(
                    value.toString(),
                    style = MonoStyle.copy(color = PokeColors.PokeRed, fontSize = 16.sp)
                )
                androidx.compose.material3.Text("Totally me", style = LabelStyle)
            }
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 1f..5f,
                steps = 3,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Processing ────────────────────────────────────────────────────────────────

@Composable
fun ProcessingScreen(onGenerated: () -> Unit) {
    val vm = koinInject<RecommendationViewModel>()
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.generate() }
    LaunchedEffect(state) { if (state is UiState.Success) onGenerated() }

    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart)
    )

    PokemonTheme {
        PokeScreen {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                when (state) {
                    is UiState.Error -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp).widthIn(max = 360.dp),
                    ) {
                        InlineError((state as UiState.Error).message)
                        Button(
                            onClick = { vm.generate() },
                            modifier = Modifier.fillMaxWidth(),
                            style = ButtonStyle.Primary,
                        ) {
                            androidx.compose.material3.Text("Try again")
                        }
                    }
                    else -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(28.dp),
                        modifier = Modifier.padding(32.dp),
                    ) {
                        // Spinning Pokéball
                        Box(
                            Modifier.size(80.dp).clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(PokeColors.PokeRedLight, PokeColors.Cream)
                                    )
                                )
                                .border(1.5.dp, PokeColors.Border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                Modifier.size(40.dp).clip(CircleShape)
                                    .background(PokeColors.PokeRed.copy(alpha = 0.15f))
                                    .border(2.dp, PokeColors.PokeRed.copy(alpha = 0.5f), CircleShape)
                                    .rotate(rotation)
                            )
                            Box(Modifier.size(12.dp).clip(CircleShape).background(PokeColors.PokeRed))
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            androidx.compose.material3.Text("Finding your Pokémon", style = TitleStyle)
                            androidx.compose.material3.Text("Analysing your personality traits…", style = BodyStyle)
                        }
                        IndeterminateProgressIndicator(
                            modifier = Modifier.width(160.dp),
                            indicatorColor = PokeColors.PokeRed,
                            trackColor = PokeColors.CreamDeep,
                        )
                    }
                }
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

    PokemonTheme {
        PokeScreen {
            when (state) {
                is UiState.Loading, UiState.Idle -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    IndeterminateProgressIndicator(
                        modifier = Modifier.width(120.dp),
                        indicatorColor = PokeColors.PokeRed,
                        trackColor = PokeColors.CreamDeep,
                    )
                }
                is UiState.Error -> Box(Modifier.fillMaxSize().padding(32.dp), Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.widthIn(max = 360.dp)
                    ) {
                        InlineError((state as UiState.Error).message)
                        Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth(), style = ButtonStyle.Outlined) {
                            androidx.compose.material3.Text("Sign out")
                        }
                    }
                }
                is UiState.Success -> {
                    val recs = (state as UiState.Success).data
                    if (recs.isEmpty()) EmptyResultState(onTakeQuiz, onSignOut)
                    else ResultList(recs, onSelectRecommendation, onTakeQuiz, onViewHistory, onSignOut)
                }
            }
        }
    }
}

@Composable
private fun EmptyResultState(onTakeQuiz: () -> Unit, onSignOut: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(32.dp), Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            androidx.compose.material3.Text("No matches yet", style = TitleStyle)
            androidx.compose.material3.Text(
                "Complete the personality quiz to discover which Pokémon you are.",
                style = BodyStyle, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onTakeQuiz, modifier = Modifier.fillMaxWidth(), style = ButtonStyle.Primary, buttonSize = ButtonSize.Large) {
                androidx.compose.material3.Text("Take the quiz")
            }
            Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth(), style = ButtonStyle.Ghost) {
                androidx.compose.material3.Text("Sign out")
            }
        }
    }
}

@Composable
private fun ResultList(
    recs: List<Recommendation>,
    onSelect: (Recommendation) -> Unit,
    onTakeQuiz: () -> Unit,
    onViewHistory: () -> Unit,
    onSignOut: () -> Unit,
) {
    val top = recs.first()
    val others = recs.drop(1)

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
        // Top bar
        item {
            Row(
                Modifier.fillMaxWidth()
                    .background(PokeColors.White)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(PokeColors.PokeRed))
                    androidx.compose.material3.Text("Your Matches", style = TitleStyle)
                }
                Button(onClick = onSignOut, style = ButtonStyle.Ghost, buttonSize = ButtonSize.Small) {
                    androidx.compose.material3.Text("Sign out", style = LabelStyle)
                }
            }
            androidx.compose.material3.HorizontalDivider(color = PokeColors.Border, thickness = 1.dp)
        }

        // Hero card — full-bleed artwork with radial glow
        item {
            Spacer(Modifier.height(16.dp))
            var heroVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { heroVisible = true }
            AnimatedVisibility(
                heroVisible,
                enter = fadeIn(tween(400)) + scaleIn(tween(400, easing = { it }), initialScale = 0.96f),
            ) {
                HeroCard(rec = top, onClick = { onSelect(top) })
            }
            Spacer(Modifier.height(20.dp))
        }

        // Other matches label
        if (others.isNotEmpty()) {
            item {
                Row(Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    SectionLabel("Other matches")
                }
                Spacer(Modifier.height(8.dp))
            }
            itemsIndexed(others) { index, rec ->
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay((index * 80 + 100).toLong())
                    visible = true
                }
                AnimatedVisibility(
                    visible,
                    enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 },
                ) {
                    SecondaryCard(rec = rec, onClick = { onSelect(rec) })
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(onClick = onTakeQuiz, modifier = Modifier.weight(1f), style = ButtonStyle.Outlined) {
                    androidx.compose.material3.Text("Retake quiz")
                }
                Button(onClick = onViewHistory, modifier = Modifier.weight(1f), style = ButtonStyle.Outlined) {
                    androidx.compose.material3.Text("History")
                }
            }
        }
    }
}

@Composable
private fun HeroCard(rec: Recommendation, onClick: () -> Unit) {
    val pct = "${"%.0f".format(rec.matchScore * 100)}%"
    val glowColor = PokeColors.PokeRed

    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        PokeCard(Modifier.fillMaxWidth(), onClick = onClick) {
            // Artwork area with radial glow
            Box(
                Modifier.fillMaxWidth().height(240.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(glowColor.copy(alpha = 0.12f), PokeColors.Cream),
                            radius = 400f,
                        )
                    )
            ) {
                // YOUR POKÉMON badge
                Box(
                    Modifier.align(Alignment.TopStart).padding(14.dp)
                        .clip(RoundedCornerShape(6.dp)).background(PokeColors.PokeRed)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    androidx.compose.material3.Text(
                        "YOUR POKÉMON",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 9.sp,
                            letterSpacing = 1.2.sp, color = PokeColors.White)
                    )
                }
                // Match % top-right
                Box(
                    Modifier.align(Alignment.TopEnd).padding(14.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PokeColors.White)
                        .border(1.dp, PokeColors.Border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    androidx.compose.material3.Text(
                        pct,
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PokeColors.PokeRed)
                    )
                }
                // Animated sprite with type effect
                AnimatedPokemonSprite(
                    name = rec.pokemon.name,
                    imageUrl = rec.pokemon.imageUrl,
                    modifier = Modifier.size(200.dp).align(Alignment.Center),
                )
            }
            // Info below artwork
            Column(
                Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                androidx.compose.material3.Text(
                    rec.pokemon.name.replaceFirstChar { it.uppercase() },
                    style = HeroName
                )
                rec.explanation?.let {
                    androidx.compose.material3.Text(
                        it.replace("**", ""),
                        style = BodyStyle,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(4.dp))
                androidx.compose.material3.Text(
                    "Tap to explore →",
                    style = LabelStyle.copy(color = PokeColors.PokeRed, fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}

@Composable
private fun SecondaryCard(rec: Recommendation, onClick: () -> Unit) {
    val pct = "${"%.0f".format(rec.matchScore * 100)}%"
    Row(
        Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(3.dp, RoundedCornerShape(16.dp), ambientColor = PokeColors.Ink.copy(alpha = 0.04f))
            .clip(RoundedCornerShape(16.dp))
            .background(PokeColors.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Sprite thumbnail with type-glow background
        Box(
            Modifier.size(60.dp).clip(RoundedCornerShape(14.dp))
                .background(PokeColors.CreamDeep),
            contentAlignment = Alignment.Center
        ) {
            if (rec.pokemon.imageUrl != null)
                AsyncImage(model = rec.pokemon.imageUrl, contentDescription = rec.pokemon.name,
                    contentScale = ContentScale.Fit, modifier = Modifier.size(48.dp))
            else androidx.compose.material3.Text("?", style = TextStyle(fontSize = 22.sp, color = PokeColors.Muted))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            androidx.compose.material3.Text(rec.pokemon.name.replaceFirstChar { it.uppercase() }, style = CardName)
            rec.explanation?.let {
                androidx.compose.material3.Text(
                    it.replace("**", ""),
                    style = BodyStyle.copy(fontSize = 12.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            androidx.compose.material3.Text(pct, style = MonoStyle.copy(color = PokeColors.Blue))
            androidx.compose.material3.Text("#${rec.rank}", style = LabelStyle)
        }
    }
}

// ── Pokémon Detail ────────────────────────────────────────────────────────────

@Composable
fun PokemonDetailScreen(recommendation: Recommendation, profile: PersonalityProfile?, onBack: () -> Unit) {
    val pokemon = recommendation.pokemon
    val pct = "${"%.0f".format(recommendation.matchScore * 100)}%"
    val accent = if (recommendation.rank == 1) PokeColors.PokeRed else PokeColors.Blue
    val heroBg = if (recommendation.rank == 1) PokeColors.PokeRedLight else PokeColors.BlueLight

    PokemonTheme {
        PokeScreen {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 48.dp)) {
                // Back nav
                item {
                    Row(
                        Modifier.fillMaxWidth()
                            .background(PokeColors.White)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(onClick = onBack, style = ButtonStyle.Ghost, buttonSize = ButtonSize.Small) {
                            androidx.compose.material3.Text("← Back", style = LabelStyle.copy(color = accent))
                        }
                        androidx.compose.material3.Text(
                            pokemon.name.replaceFirstChar { it.uppercase() },
                            style = LabelStyle
                        )
                    }
                    androidx.compose.material3.HorizontalDivider(color = PokeColors.Border, thickness = 1.dp)
                }

                // Hero artwork with radial glow
                item {
                    Box(
                        Modifier.fillMaxWidth().height(280.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(heroBg, PokeColors.Cream),
                                    radius = 500f,
                                )
                            )
                    ) {
                        AnimatedPokemonSprite(
                            name = pokemon.name,
                            imageUrl = pokemon.imageUrl,
                            modifier = Modifier.size(260.dp).align(Alignment.Center),
                        )
                    }
                }

                // Name + score + rank
                item {
                    Column(Modifier.background(PokeColors.White)) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                androidx.compose.material3.Text(
                                    pokemon.name.replaceFirstChar { it.uppercase() },
                                    style = DisplayStyle
                                )
                                androidx.compose.material3.Text("Rank #${recommendation.rank}", style = LabelStyle)
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                androidx.compose.material3.Text(
                                    pct,
                                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 30.sp, color = accent)
                                )
                                androidx.compose.material3.Text("match", style = LabelStyle)
                            }
                        }
                        androidx.compose.material3.HorizontalDivider(
                            Modifier.padding(horizontal = 24.dp), color = PokeColors.Border, thickness = 1.dp
                        )
                    }
                }

                // Why you match
                recommendation.explanation?.let { explanation ->
                    item {
                        Column(
                            Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SectionLabel("Why you match", accent)
                            androidx.compose.material3.Text(
                                explanation.replace("**", ""),
                                style = BodyStyle.copy(fontSize = 15.sp, lineHeight = 24.sp, color = PokeColors.Ink)
                            )
                        }
                        androidx.compose.material3.HorizontalDivider(
                            Modifier.padding(horizontal = 24.dp), color = PokeColors.Border, thickness = 1.dp
                        )
                    }
                }

                // Personality traits
                if (profile != null) {
                    item {
                        Column(
                            Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SectionLabel("Your personality", accent)
                            listOf(
                                "Energy"      to profile.energy,
                                "Curiosity"   to profile.curiosity,
                                "Leadership"  to profile.leadership,
                                "Loyalty"     to profile.loyalty,
                                "Risk-taking" to profile.risk,
                                "Creativity"  to profile.creativity,
                            ).forEach { (label, value) ->
                                StatBar(label = label, value = value, accent = accent)
                            }
                        }
                        androidx.compose.material3.HorizontalDivider(
                            Modifier.padding(horizontal = 24.dp), color = PokeColors.Border, thickness = 1.dp
                        )
                    }
                }

                // About Pokémon
                pokemon.description?.let { desc ->
                    item {
                        Column(
                            Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SectionLabel("About ${pokemon.name.replaceFirstChar { it.uppercase() }}", PokeColors.MutedLight)
                            androidx.compose.material3.Text(
                                desc,
                                style = BodyStyle.copy(fontSize = 14.sp, lineHeight = 22.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBar(label: String, value: Double, accent: Color) {
    val pct = (value * 100).toInt()
    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { triggered = true }
    val animatedWidth by animateFloatAsState(
        targetValue = if (triggered) value.toFloat().coerceIn(0f, 1f) else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
    )
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            androidx.compose.material3.Text(label, style = LabelStyle.copy(color = PokeColors.Ink))
            androidx.compose.material3.Text(
                "${pct}%",
                style = MonoStyle.copy(
                    color = if (value >= 0.65) accent else PokeColors.MutedLight,
                    fontSize = 12.sp
                )
            )
        }
        Box(
            Modifier.fillMaxWidth().height(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(PokeColors.CreamDeep)
        ) {
            Box(
                Modifier.fillMaxWidth(animatedWidth).fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (value >= 0.65) accent else accent.copy(alpha = 0.35f))
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

    PokemonTheme {
        PokeScreen {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth()
                        .background(PokeColors.White)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = onBack, style = ButtonStyle.Ghost, buttonSize = ButtonSize.Small) {
                        androidx.compose.material3.Text("← Back", style = LabelStyle.copy(color = PokeColors.Blue))
                    }
                    androidx.compose.material3.Text("Match history", style = TitleStyle)
                    Button(onClick = onSignOut, style = ButtonStyle.Ghost, buttonSize = ButtonSize.Small) {
                        androidx.compose.material3.Text("Sign out", style = LabelStyle)
                    }
                }
                androidx.compose.material3.HorizontalDivider(color = PokeColors.Border, thickness = 1.dp)

                when (state) {
                    is UiState.Loading, UiState.Idle -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        IndeterminateProgressIndicator(
                            modifier = Modifier.width(120.dp),
                            indicatorColor = PokeColors.PokeRed,
                            trackColor = PokeColors.CreamDeep,
                        )
                    }
                    is UiState.Error -> Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.widthIn(max = 320.dp)
                        ) {
                            InlineError((state as UiState.Error).message)
                            Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth(), style = ButtonStyle.Outlined) {
                                androidx.compose.material3.Text("Sign out")
                            }
                        }
                    }
                    is UiState.Success -> {
                        val recs = (state as UiState.Success).data
                        if (recs.isEmpty()) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    androidx.compose.material3.Text("No history yet", style = TitleStyle)
                                    androidx.compose.material3.Text(
                                        "Complete the quiz to see your matches.",
                                        style = BodyStyle, textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(recs) { _, rec -> HistoryRow(rec) }
                            }
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
        Modifier.fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp), ambientColor = PokeColors.Ink.copy(alpha = 0.04f))
            .clip(RoundedCornerShape(14.dp))
            .background(PokeColors.White)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(PokeColors.CreamDeep),
                contentAlignment = Alignment.Center
            ) {
                if (rec.pokemon.imageUrl != null)
                    AsyncImage(model = rec.pokemon.imageUrl, contentDescription = rec.pokemon.name,
                        contentScale = ContentScale.Fit, modifier = Modifier.size(40.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                androidx.compose.material3.Text(
                    rec.pokemon.name.replaceFirstChar { it.uppercase() },
                    style = CardName.copy(fontSize = 14.sp)
                )
                androidx.compose.material3.Text(rec.generatedAt.take(10), style = LabelStyle)
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            androidx.compose.material3.Text(
                "${"%.0f".format(rec.matchScore * 100)}%",
                style = MonoStyle.copy(color = PokeColors.Blue)
            )
            androidx.compose.material3.Text("#${rec.rank}", style = LabelStyle)
        }
    }
}
