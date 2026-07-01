package com.pokemonai.client.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Animatable
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
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.atan2
import kotlin.math.PI
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import com.pokemonai.client.shared.resources.Res
import com.pokemonai.client.shared.resources.pokeball_open
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

private val DisplayStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 30.sp, color = Color.Unspecified, lineHeight = 36.sp)
private val HeroName     = TextStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Color.Unspecified)
private val TitleStyle   = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Color.Unspecified)
private val CardName     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Unspecified)
private val BodyStyle    = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, color = PokeColors.Muted, lineHeight = 22.sp)
private val LabelStyle   = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, color = PokeColors.Muted)
private val CapStyle     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.4.sp, color = PokeColors.MutedLight)
private val MonoStyle    = TextStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.Monospace, color = Color.Unspecified)

// ── Primary button (composables Button uses black by default) ─────────────────

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val colors = LocalAppColors.current
    val bg = if (!enabled) colors.primary.copy(alpha = 0.4f)
             else if (pressed) lerp(colors.primary, Color.Black, 0.12f)
             else colors.primary
    Box(
        modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Text(
            text,
            style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = colors.onPrimary)
        )
    }
}

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
                val angle = (angleDeg + p * 120).toDouble() * (PI / 180.0)
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
            val alpha = (sin(mist * PI * 2).toFloat() * 0.5f + 0.5f) * 0.2f
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

@Composable
private fun ThemeToggle(modifier: Modifier = Modifier) {
    val dark = LocalDarkTheme.current
    val toggle = LocalToggleTheme.current
    val colors = LocalAppColors.current
    Box(
        modifier.size(40.dp).clip(CircleShape).background(colors.surface).clickable { toggle() },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(20.dp)) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val r = size.minDimension
            if (dark) {
                // Crescent moon: a disc with an offset disc carved out in the surface colour.
                drawCircle(colors.accent, r * 0.34f, c)
                drawCircle(colors.surface, r * 0.30f, Offset(c.x + r * 0.16f, c.y - r * 0.12f))
            } else {
                // Sun: core disc + radiating rays.
                drawCircle(colors.accent, r * 0.22f, c)
                val rays = 8
                for (i in 0 until rays) {
                    val a = (PI * 2 * i / rays).toFloat()
                    drawLine(
                        colors.accent,
                        Offset(c.x + cos(a) * r * 0.32f, c.y + sin(a) * r * 0.32f),
                        Offset(c.x + cos(a) * r * 0.46f, c.y + sin(a) * r * 0.46f),
                        strokeWidth = r * 0.06f,
                    )
                }
            }
        }
    }
}

// ── Shared primitives ─────────────────────────────────────────────────────────

@Composable
private fun PokeScreen(applyInsets: Boolean = true, content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(LocalAppColors.current.background)) {
        val inner = if (applyInsets) {
            Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars)
        } else {
            Modifier.fillMaxSize()
        }
        Box(inner, content = content)
    }
}

@Composable
private fun PokeCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalAppColors.current
    val base = modifier
        .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp),
            ambientColor = Color.Black.copy(alpha = 0.05f),
            spotColor = Color.Black.copy(alpha = 0.08f))
        .clip(RoundedCornerShape(20.dp))
        .background(colors.surface)
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
private fun SectionLabel(text: String, accent: Color? = null) {
    val color = accent ?: LocalAppColors.current.primary
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.width(3.dp).height(14.dp).clip(RoundedCornerShape(2.dp)).background(color))
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
            val colors = LocalAppColors.current
            Column(
                Modifier.fillMaxSize().padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Pokéball logo
                Box(
                    Modifier.size(72.dp).clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.15f))
                        .border(1.5.dp, colors.border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.size(32.dp).clip(CircleShape).background(colors.primary))
                    Box(Modifier.fillMaxWidth().height(1.5.dp).background(colors.primary.copy(alpha = 0.4f)))
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
                        PrimaryButton(
                            text = if (loading) "Signing in…" else "Sign in",
                            onClick = { vm.login(emailState.text.toString(), passwordState.text.toString()) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading && emailState.text.isNotBlank() && passwordState.text.isNotBlank(),
                        )
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

    LaunchedEffect(Unit) { vm.loadQuestions() }
    LaunchedEffect(submitState) { if (submitState is UiState.Success) onSubmitted() }

    val colors = LocalAppColors.current
    Box(
        Modifier.fillMaxSize().background(colors.background).windowInsetsPadding(WindowInsets.systemBars),
    ) {
        when (val q = questions) {
            is UiState.Loading, UiState.Idle -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                IndeterminateProgressIndicator(
                    modifier = Modifier.width(120.dp),
                    indicatorColor = colors.primary,
                    trackColor = colors.border,
                )
            }
            is UiState.Error -> Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
                androidx.compose.material3.Text(
                    q.message,
                    style = MaterialTheme.typography.bodyMedium.copy(color = colors.onBackground),
                    textAlign = TextAlign.Center,
                )
            }
            is UiState.Success -> QuestionFlow(
                questions = q.data,
                submitting = submitState is UiState.Loading,
                error = (submitState as? UiState.Error)?.message,
                onSubmit = { vm.submitAnswers(it) },
            )
        }
    }
}

@Composable
private fun QuestionFlow(
    questions: List<com.pokemonai.client.core.Question>,
    submitting: Boolean,
    error: String?,
    onSubmit: (List<AnswerInput>) -> Unit,
) {
    val colors = LocalAppColors.current
    var index by remember { mutableStateOf(0) }
    val answers = remember { mutableStateMapOf<String, Int>() }
    var submitted by remember { mutableStateOf(false) }
    val total = questions.size
    if (total == 0) return
    val q = questions[index]

    // Let the user retry if the submission failed.
    LaunchedEffect(error) { if (error != null) submitted = false }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
        // Top bar: back · title · theme toggle
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(colors.surface)
                    .clickable(enabled = index > 0) { if (index > 0) index-- },
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Text(
                    "←",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (index > 0) colors.primary else colors.muted),
                )
            }
            androidx.compose.material3.Text(
                "Personality",
                style = MaterialTheme.typography.titleMedium.copy(color = colors.onBackground, fontWeight = FontWeight.Bold),
            )
            ThemeToggle()
        }

        Spacer(Modifier.height(24.dp))

        androidx.compose.material3.Text(
            "Question ${index + 1} of $total",
            style = MaterialTheme.typography.labelMedium.copy(color = colors.muted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
        )
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(colors.border)) {
            Box(Modifier.fillMaxWidth((index + 1f) / total).fillMaxHeight().clip(CircleShape).background(colors.primary))
        }

        Spacer(Modifier.height(36.dp))

        androidx.compose.material3.Text(
            q.text,
            style = MaterialTheme.typography.headlineSmall.copy(color = colors.onBackground, fontWeight = FontWeight.Bold, lineHeight = 34.sp),
        )

        Spacer(Modifier.height(28.dp))

        if (error != null) {
            androidx.compose.material3.Text(
                error,
                style = MaterialTheme.typography.bodySmall.copy(color = colors.secondary),
            )
            Spacer(Modifier.height(8.dp))
        }

        // Five-point scale — tapping records the answer and auto-advances (or submits).
        val options = listOf(
            5 to "Strongly agree",
            4 to "Agree",
            3 to "Neutral",
            2 to "Disagree",
            1 to "Strongly disagree",
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEach { (value, label) ->
                val selected = answers[q.id] == value
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(if (selected) colors.primary else colors.surface)
                        .clickable(enabled = !submitting && !submitted) {
                            if (submitted) return@clickable
                            answers[q.id] = value
                            if (index < total - 1) {
                                index++
                            } else {
                                submitted = true
                                onSubmit(questions.map { AnswerInput(it.id, answers[it.id] ?: 3) })
                            }
                        }
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        Modifier.size(18.dp).clip(CircleShape)
                            .background(if (selected) colors.onPrimary else colors.background)
                            .border(2.dp, if (selected) colors.onPrimary else colors.muted, CircleShape)
                    )
                    androidx.compose.material3.Text(
                        label,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = if (selected) colors.onPrimary else colors.onSurface,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        ),
                    )
                }
            }
        }

        if (submitting) {
            Spacer(Modifier.height(12.dp))
            androidx.compose.material3.Text(
                "Finding your match…",
                style = MaterialTheme.typography.bodyMedium.copy(color = colors.muted),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
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
                        PrimaryButton("Try again", { vm.generate() }, Modifier.fillMaxWidth())
                    }
                    else -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(28.dp),
                        modifier = Modifier.padding(32.dp),
                    ) {
                        val colors = LocalAppColors.current
                        // Spinning Pokéball
                        Box(
                            Modifier.size(80.dp).clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(colors.primary.copy(alpha = 0.18f), colors.surface)
                                    )
                                )
                                .border(1.5.dp, colors.border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                Modifier.size(40.dp).clip(CircleShape)
                                    .background(colors.primary.copy(alpha = 0.15f))
                                    .border(2.dp, colors.primary.copy(alpha = 0.5f), CircleShape)
                                    .rotate(rotation)
                            )
                            Box(Modifier.size(12.dp).clip(CircleShape).background(colors.primary))
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
                            indicatorColor = colors.primary,
                            trackColor = colors.border,
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
    onSettings: () -> Unit,
    onSignOut: () -> Unit,
) {
    val vm = koinInject<RecommendationViewModel>()
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { if (state is UiState.Idle) vm.load() }

    PokemonTheme {
        PokeScreen(applyInsets = false) {
            when (state) {
                is UiState.Loading, UiState.Idle -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    IndeterminateProgressIndicator(
                        modifier = Modifier.width(120.dp),
                        indicatorColor = LocalAppColors.current.primary,
                        trackColor = LocalAppColors.current.border,
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
                    else ResultList(recs, onSelectRecommendation, onTakeQuiz, onViewHistory, onSettings)
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
            PrimaryButton("Take the quiz", onTakeQuiz, Modifier.fillMaxWidth())
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
    onSettings: () -> Unit,
) {
    val top = recs.first()
    val others = recs.drop(1).take(9)
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val colors = LocalAppColors.current

    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 104.dp + navBottom)
        ) {
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
        }
    }
        PokeNavBar(
            selected = NavTab.MATCHES,
            onSelect = { tab ->
                when (tab) {
                    NavTab.MATCHES -> {}
                    NavTab.HISTORY -> onViewHistory()
                    NavTab.RETAKE -> onTakeQuiz()
                    NavTab.SETTINGS -> onSettings()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp + navBottom),
        )
    }
}

@Composable
private fun HeroCard(rec: Recommendation, onClick: () -> Unit) {
    val pct = "${(rec.matchScore * 100).toInt()}%"
    val colors = LocalAppColors.current
    val types = rec.pokemon.types.ifEmpty { listOf(pokemonType(rec.pokemon.name)) }
    val accent = typeColor(types.first())
    val artworkGradient = Brush.verticalGradient(
        listOf(accent.lighten(0.22f), accent, accent.darken(0.12f))
    )

    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        PokeCard(Modifier.fillMaxWidth(), onClick = onClick) {
            // Artwork area — type-coloured gradient with sparkle texture and a glow
            Box(
                Modifier.fillMaxWidth().height(260.dp).background(artworkGradient)
            ) {
                SparklePattern(Modifier.fillMaxSize())
                // Radial glow so the sprite reads against the colour
                Box(
                    Modifier.align(Alignment.Center).fillMaxWidth(0.9f).aspectRatio(1f)
                        .background(
                            Brush.radialGradient(listOf(Color.White.copy(alpha = 0.45f), Color.Transparent)),
                            CircleShape,
                        )
                )
                // YOUR POKÉMON badge — dark chip works on any type colour
                Box(
                    Modifier.align(Alignment.TopStart).padding(14.dp)
                        .clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha = 0.28f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    androidx.compose.material3.Text(
                        "YOUR POKÉMON",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 9.sp,
                            letterSpacing = 1.2.sp, color = Color.White)
                    )
                }
                // Match % top-right — white chip with accent text
                Box(
                    Modifier.align(Alignment.TopEnd).padding(14.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    androidx.compose.material3.Text(
                        "$pct MATCH",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp, color = accent.darken(0.25f))
                    )
                }
                // Animated sprite with type effect
                AnimatedPokemonSprite(
                    name = rec.pokemon.name,
                    imageUrl = rec.pokemon.imageUrl,
                    modifier = Modifier.size(210.dp).align(Alignment.Center),
                )
            }
            // Info below artwork
            Column(
                Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.Text(
                    rec.pokemon.name.replaceFirstChar { it.uppercase() },
                    style = HeroName
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    types.forEach { DetailTypeBadge(it) }
                }
                rec.explanation?.let {
                    androidx.compose.material3.Text(
                        it.replace("**", ""),
                        style = BodyStyle,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(2.dp))
                androidx.compose.material3.Text(
                    "Tap to explore →",
                    style = LabelStyle.copy(color = colors.primary, fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}

@Composable
private fun SecondaryCard(rec: Recommendation, onClick: () -> Unit) {
    val pct = "${(rec.matchScore * 100).toInt()}%"
    val colors = LocalAppColors.current
    val type = rec.pokemon.types.firstOrNull() ?: pokemonType(rec.pokemon.name)
    val accent = typeColor(type)
    Row(
        Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(3.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = 0.04f))
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Type-coloured accent stripe down the left edge
        Box(Modifier.width(5.dp).height(64.dp).background(accent))
        // Sprite thumbnail with type-tinted background
        Box(
            Modifier.size(56.dp).clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.radialGradient(listOf(accent.copy(alpha = 0.28f), accent.copy(alpha = 0.08f)))
                ),
            contentAlignment = Alignment.Center
        ) {
            if (rec.pokemon.imageUrl != null)
                AsyncImage(model = rec.pokemon.imageUrl, contentDescription = rec.pokemon.name,
                    contentScale = ContentScale.Fit, modifier = Modifier.size(46.dp))
            else androidx.compose.material3.Text("?", style = TextStyle(fontSize = 22.sp, color = colors.muted))
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
            androidx.compose.material3.Text(pct, style = MonoStyle.copy(color = accent.darken(0.2f)))
            androidx.compose.material3.Text("#${rec.rank}", style = LabelStyle)
        }
    }
}

// ── Pokémon Detail ────────────────────────────────────────────────────────────

private fun Color.darken(f: Float): Color = lerp(this, Color.Black, f)
private fun Color.lighten(f: Float): Color = lerp(this, Color.White, f)

// Canonical defending weaknesses derived from the (single) primary type.
private fun typeWeaknesses(type: String): List<String> = when (type.lowercase()) {
    "fire"     -> listOf("water", "ground", "rock")
    "water"    -> listOf("electric", "grass")
    "grass"    -> listOf("fire", "ice", "poison", "flying", "bug")
    "electric" -> listOf("ground")
    "ice"      -> listOf("fire", "fighting", "rock", "steel")
    "fighting" -> listOf("flying", "psychic")
    "poison"   -> listOf("ground", "psychic")
    "ground"   -> listOf("water", "grass", "ice")
    "flying"   -> listOf("electric", "ice", "rock")
    "psychic"  -> listOf("bug", "ghost", "dark")
    "bug"      -> listOf("fire", "flying", "rock")
    "rock"     -> listOf("water", "grass", "fighting", "ground", "steel")
    "ghost"    -> listOf("ghost", "dark")
    "dragon"   -> listOf("ice", "dragon")
    "dark"     -> listOf("fighting", "bug")
    "steel"    -> listOf("fire", "fighting", "ground")
    else       -> listOf("fighting")
}

@Composable
fun PokemonDetailScreen(recommendation: Recommendation, profile: PersonalityProfile?, onBack: () -> Unit) {
    val pokemon = recommendation.pokemon
    // Real types from the API; fall back to name-derived type until a sync populates them.
    val types = pokemon.types.ifEmpty { listOf(pokemonType(pokemon.name)) }
    val accent = typeColor(types.first())
    val secondary = types.getOrNull(1)?.let { typeColor(it) } ?: accent
    val numberLabel = pokemon.externalId
        ?.let { "N°" + it.toString().padStart(3, '0') }
        ?: "RANK #${recommendation.rank}"
    val heroGradient = Brush.verticalGradient(listOf(accent.lighten(0.08f), accent, accent.darken(0.28f)))

    PokemonTheme {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val wide = maxWidth >= 720.dp
            if (wide) {
                // Desktop / large tablet: the same card, centered on an ambient gradient.
                Box(
                    Modifier.fillMaxSize()
                        .background(Brush.linearGradient(listOf(accent.lighten(0.7f), secondary.lighten(0.5f))))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Card(
                        modifier = Modifier.width(460.dp).fillMaxHeight(0.94f),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 18.dp),
                    ) {
                        DetailScroll(
                            recommendation, profile, types, accent, numberLabel,
                            heroGradient = heroGradient, heroHeight = 300.dp,
                            modifier = Modifier.fillMaxSize(), applyInsets = false,
                        )
                    }
                }
            } else {
                // Phone: full-bleed; gradient runs under the status bar, content insets.
                DetailScroll(
                    recommendation, profile, types, accent, numberLabel,
                    heroGradient = heroGradient, heroHeight = 320.dp,
                    modifier = Modifier.fillMaxSize(), applyInsets = true,
                )
            }
            // Floating back button — stays put while the content scrolls.
            FloatingBackButton(
                accent = accent,
                onBack = onBack,
                modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(16.dp),
            )
        }
    }
}

@Composable
private fun FloatingBackButton(accent: Color, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.shadow(6.dp, CircleShape).size(40.dp).clip(CircleShape)
            .background(Color.White).clickable(onClick = onBack),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Text(
            "←",
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, color = accent.darken(0.2f)),
        )
    }
}

@Composable
private fun DetailScroll(
    recommendation: Recommendation,
    profile: PersonalityProfile?,
    types: List<String>,
    accent: Color,
    numberLabel: String,
    heroGradient: Brush,
    heroHeight: Dp,
    modifier: Modifier,
    applyInsets: Boolean,
) {
    val pokemon = recommendation.pokemon
    Box(modifier.background(heroGradient)) {
        val scrollMod = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        Column(if (applyInsets) scrollMod.windowInsetsPadding(WindowInsets.systemBars) else scrollMod) {
            PokedexHero(
                name = pokemon.name,
                imageUrl = pokemon.imageUrl,
                modifier = Modifier.fillMaxWidth().height(heroHeight),
            )
            PokedexPanel(
                recommendation = recommendation,
                profile = profile,
                types = types,
                accent = accent,
                numberLabel = numberLabel,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PokedexHero(
    name: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    // Release-from-pokeball entrance: the Pokémon erupts from the centre of the
    // open ball — starting as a point at the opening, then scaling outward.
    val reveal = remember(name) { Animatable(0f) }
    LaunchedEffect(name) {
        reveal.snapTo(0f)
        reveal.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
    }
    val p = reveal.value
    val rise = (p / 0.85f).coerceIn(0f, 1f)        // sprite reaches its seat at 85%
    val spriteScale = 0.12f + 0.88f * backEaseOut(rise)
    val spriteAlpha = (p / 0.18f).coerceIn(0f, 1f)
    val spriteLift = (1f - rise) * 24f             // starts ~24dp lower, at the opening
    val burst = (p / 0.55f).coerceIn(0f, 1f)       // light burst from the opening
    val burstScale = 0.2f + 1.5f * burst
    val burstAlpha = (1f - burst) * 0.9f
    val openingOffset = 28.dp                      // ball opening relative to hero centre

    Box(modifier) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(listOf(Color.White.copy(alpha = 0.14f), Color.Transparent), radius = 600f)
            )
        )
        SparklePattern(Modifier.fillMaxSize())
        Box(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth(0.68f).aspectRatio(1f).offset(y = 22.dp)
                .background(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.34f), Color.Transparent)), CircleShape)
        )
        // Smaller ball so the Pokémon takes center stage; white wash hides the dark interior.
        Box(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth(0.6f).aspectRatio(1f).offset(y = 16.dp)
        ) {
            OpenPokeball(Modifier.fillMaxSize())
            Box(
                Modifier.align(Alignment.Center).offset(y = (-4).dp)
                    .fillMaxWidth(0.58f).aspectRatio(1f)
                    .background(
                        Brush.radialGradient(
                            0f to Color.White, 0.46f to Color.White, 1f to Color.White.copy(alpha = 0f)
                        ),
                        CircleShape,
                    )
            )
        }
        // Flash of light bursting from the ball opening
        Box(
            Modifier.align(Alignment.Center).offset(y = openingOffset)
                .fillMaxWidth(0.62f).aspectRatio(1f)
                .graphicsLayer {
                    scaleX = burstScale; scaleY = burstScale; alpha = burstAlpha
                }
                .background(
                    Brush.radialGradient(listOf(Color.White, Color.White.copy(alpha = 0f))),
                    CircleShape,
                )
        )
        // Radiating light rays shooting out of the opening as it releases
        BurstRays(
            progress = burst,
            modifier = Modifier.align(Alignment.Center).offset(y = openingOffset)
                .fillMaxWidth(0.95f).aspectRatio(1f),
        )
        // Soft glow halo that follows the Pokémon out, brightest at release
        Box(
            Modifier.align(Alignment.Center).offset(y = (spriteLift + 6f).dp)
                .fillMaxWidth(0.72f).aspectRatio(1f)
                .graphicsLayer {
                    val s = 0.55f + 0.45f * rise
                    scaleX = s; scaleY = s
                    alpha = 0.2f + 0.45f * (1f - rise)
                }
                .background(
                    Brush.radialGradient(listOf(Color.White.copy(alpha = 0.7f), Color.Transparent)),
                    CircleShape,
                )
        )
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.align(Alignment.Center)
                    .fillMaxWidth(0.8f).aspectRatio(1f)
                    .offset(y = (spriteLift + 6f).dp)
                    .graphicsLayer {
                        scaleX = spriteScale
                        scaleY = spriteScale
                        alpha = spriteAlpha
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                    },
            )
        }
    }
}

// Back-ease-out: overshoots slightly past 1.0 then settles, for a lively "pop".
private fun backEaseOut(t: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1f
    val x = t - 1f
    return 1f + c3 * x * x * x + c1 * x * x
}

// Light rays bursting out of the pokéball opening during the release animation.
@Composable
private fun BurstRays(progress: Float, modifier: Modifier) {
    if (progress <= 0f || progress >= 1f) return
    Canvas(modifier) {
        val n = 12
        val cx = size.width / 2f
        val cy = size.height / 2f
        val inner = size.minDimension * 0.12f
        val outer = size.minDimension * (0.22f + 0.30f * progress)
        val alpha = (1f - progress).coerceIn(0f, 1f)
        val ray = Color(0xFFFFF4C2) // warm gold so it pops on the ball and the page
        rotate(progress * 22f, pivot = Offset(cx, cy)) {
            for (i in 0 until n) {
                val a = (i * (360f / n)) * (PI.toFloat() / 180f)
                val longer = if (i % 2 == 0) 1f else 0.7f
                drawLine(
                    ray.copy(alpha = alpha * 0.85f),
                    Offset(cx + cos(a) * inner, cy + sin(a) * inner),
                    Offset(cx + cos(a) * outer * longer, cy + sin(a) * outer * longer),
                    strokeWidth = size.minDimension * 0.013f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                )
            }
        }
    }
}

// ── Bottom navigation bar ───────────────────────────────────────────────────
// Custom themed bar (pure Compose Multiplatform — identical on Android, desktop, iOS).
// Selected tab expands into a filled pill with a label, like the reference design.

enum class NavTab { MATCHES, HISTORY, RETAKE, SETTINGS }

@Composable
fun PokeNavBar(selected: NavTab, onSelect: (NavTab) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Row(
        modifier
            .shadow(8.dp, RoundedCornerShape(50), ambientColor = Color.Black.copy(alpha = 0.08f))
            .clip(RoundedCornerShape(50))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(50))
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        NavBarItem(NavTab.MATCHES, "Matches", selected, onSelect)
        NavBarItem(NavTab.HISTORY, "History", selected, onSelect)
        NavBarItem(NavTab.RETAKE, "Retake", selected, onSelect)
        NavBarItem(NavTab.SETTINGS, "Settings", selected, onSelect)
    }
}

@Composable
private fun NavBarItem(tab: NavTab, label: String, selected: NavTab, onSelect: (NavTab) -> Unit) {
    val colors = LocalAppColors.current
    val isSelected = tab == selected
    val bg by animateColorAsState(if (isSelected) colors.primary else Color.Transparent)
    val fg = if (isSelected) colors.onPrimary else colors.muted
    Row(
        Modifier.clip(RoundedCornerShape(50))
            .background(bg)
            .clickable { onSelect(tab) }
            .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        NavTabIcon(tab, fg, Modifier.size(20.dp))
        AnimatedVisibility(isSelected) {
            androidx.compose.material3.Text(
                label,
                style = LabelStyle.copy(color = fg, fontWeight = FontWeight.SemiBold),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun NavTabIcon(tab: NavTab, color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val s = size.minDimension
        val stroke = Stroke(width = s * 0.09f, cap = androidx.compose.ui.graphics.StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round)
        when (tab) {
            NavTab.MATCHES -> {
                // House: roof + body
                drawPath(Path().apply {
                    moveTo(0.12f * s, 0.52f * s); lineTo(0.5f * s, 0.16f * s); lineTo(0.88f * s, 0.52f * s)
                }, color, style = stroke)
                drawPath(Path().apply {
                    moveTo(0.24f * s, 0.46f * s); lineTo(0.24f * s, 0.86f * s)
                    lineTo(0.76f * s, 0.86f * s); lineTo(0.76f * s, 0.46f * s)
                }, color, style = stroke)
            }
            NavTab.HISTORY -> {
                // Clock face + hands
                val r = s * 0.38f
                drawCircle(color, radius = r, center = center, style = stroke)
                drawLine(color, center, Offset(center.x, center.y - r * 0.62f), stroke.width)
                drawLine(color, center, Offset(center.x + r * 0.5f, center.y), stroke.width)
            }
            NavTab.RETAKE -> {
                // Circular refresh arrow
                val r = s * 0.34f
                drawArc(
                    color, startAngle = 300f, sweepAngle = 250f, useCenter = false,
                    topLeft = Offset(center.x - r, center.y - r),
                    size = Size(r * 2f, r * 2f), style = stroke,
                )
                val end = (300f + 250f) * (PI.toFloat() / 180f)
                val tip = Offset(center.x + r * cos(end), center.y + r * sin(end))
                val fwd = Offset(-sin(end), cos(end)) // tangent in direction of sweep
                val ang = atan2(fwd.y, fwd.x)
                val len = s * 0.2f
                drawLine(color, tip, Offset(tip.x + len * cos(ang + 2.6f), tip.y + len * sin(ang + 2.6f)), stroke.width)
                drawLine(color, tip, Offset(tip.x + len * cos(ang - 2.6f), tip.y + len * sin(ang - 2.6f)), stroke.width)
            }
            NavTab.SETTINGS -> {
                // Gear: a ring with radial teeth
                val teeth = 8
                val toothInner = s * 0.30f
                val toothOuter = s * 0.44f
                for (i in 0 until teeth) {
                    val a = (i * (360f / teeth)) * (PI.toFloat() / 180f)
                    drawLine(
                        color,
                        Offset(center.x + cos(a) * toothInner, center.y + sin(a) * toothInner),
                        Offset(center.x + cos(a) * toothOuter, center.y + sin(a) * toothOuter),
                        strokeWidth = s * 0.11f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    )
                }
                drawCircle(color, radius = s * 0.24f, center = center, style = stroke)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PokedexPanel(
    recommendation: Recommendation,
    profile: PersonalityProfile?,
    types: List<String>,
    accent: Color,
    numberLabel: String,
    modifier: Modifier = Modifier,
) {
    val pokemon = recommendation.pokemon
    val matchPct = (recommendation.matchScore * 100).toInt()
    val weaknesses = types.flatMap { typeWeaknesses(it) }.distinct()
    val baseStats = listOfNotNull(
        pokemon.hp?.let { "HP" to it },
        pokemon.attack?.let { "Attack" to it },
        pokemon.defense?.let { "Defense" to it },
        pokemon.specialAttack?.let { "Sp. Atk" to it },
        pokemon.specialDefense?.let { "Sp. Def" to it },
        pokemon.speed?.let { "Speed" to it },
    )

    Column(
        modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            androidx.compose.material3.Text(
                pokemon.name.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(Color.Black.copy(alpha = 0.4f), Offset(0f, 2f), 8f),
                ),
            )
            androidx.compose.material3.Text(
                numberLabel,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White, fontWeight = FontWeight.Bold,
                    shadow = Shadow(Color.Black.copy(alpha = 0.4f), Offset(0f, 1f), 6f),
                ),
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Pill("$matchPct% MATCH")
            Pill("RANK #${recommendation.rank}")
        }

        recommendation.explanation?.let { why ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CardSectionLabel("Why you matched")
                androidx.compose.material3.Text(
                    why.replace("**", ""),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White, lineHeight = 21.sp,
                        shadow = Shadow(Color.Black.copy(alpha = 0.35f), Offset(0f, 1f), 5f),
                    ),
                )
            }
        }

        pokemon.description?.let { desc ->
            androidx.compose.material3.Text(
                desc,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.95f), lineHeight = 20.sp,
                    shadow = Shadow(Color.Black.copy(alpha = 0.35f), Offset(0f, 1f), 5f),
                ),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CardSectionLabel("Type")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                types.forEach { DetailTypeBadge(it) }
            }
        }

        if (profile != null) {
            StatAccordion("Personality") {
                listOf(
                    Triple("Energy", profile.energy, Color(0xFFFF6D00)),
                    Triple("Curiosity", profile.curiosity, Color(0xFF29B6F6)),
                    Triple("Leadership", profile.leadership, Color(0xFFAB47BC)),
                    Triple("Loyalty", profile.loyalty, Color(0xFF66BB6A)),
                    Triple("Risk", profile.risk, Color(0xFFEF5350)),
                    Triple("Creativity", profile.creativity, Color(0xFFEC407A)),
                ).forEach { (label, value, color) ->
                    CardStatRow(label, value.toFloat().coerceIn(0f, 1f), null, color)
                }
            }
        }

        StatsAccordion(baseStats)

        if (pokemon.category != null || pokemon.weight != null || pokemon.height != null) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                pokemon.category?.let { InfoLine("Category", it) }
                pokemon.weight?.let { InfoLine("Weight", "${it / 10}.${it % 10} kg") }
                pokemon.height?.let { InfoLine("Height", "${it / 10}.${it % 10} m") }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CardSectionLabel("Weaknesses")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                weaknesses.forEach { DetailTypeBadge(it) }
            }
        }
    }
}

// MMO/ARPG-flavoured stat colours: vitality red, physical orange, armour blue,
// arcane purple, resist teal, agility lime.
private fun statColor(label: String): Color = when (label) {
    "HP" -> Color(0xFFFF5350)
    "Attack" -> Color(0xFFFF8A3D)
    "Defense" -> Color(0xFF4FA3F0)
    "Sp. Atk" -> Color(0xFFC264E0)
    "Sp. Def" -> Color(0xFF2BD4C0)
    "Speed" -> Color(0xFFAEE83A)
    else -> Color.White
}

// Reusable collapsible section (default closed) used for Personality and Base Stats.
@Composable
private fun StatAccordion(title: String, content: @Composable ColumnScope.() -> Unit) {
    var open by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.Black.copy(alpha = 0.22f)),
    ) {
        Row(
            Modifier.fillMaxWidth().clickable { open = !open }.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CardSectionLabel(title)
            androidx.compose.material3.Text(
                if (open) "▲" else "▼",
                style = MaterialTheme.typography.labelMedium.copy(color = Color.White.copy(alpha = 0.9f)),
            )
        }
        AnimatedVisibility(open) {
            Column(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun StatsAccordion(stats: List<Pair<String, Int>>) {
    StatAccordion("Base Stats") {
        if (stats.isEmpty()) {
            androidx.compose.material3.Text(
                "Base stats aren't available yet — sync the Pokédex to populate them.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f)),
            )
        } else {
            stats.forEach { (label, value) ->
                CardStatRow(label, (value / 200f).coerceIn(0f, 1f), value.toString(), statColor(label))
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        CardSectionLabel(label)
        androidx.compose.material3.Text(
            value,
            style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
private fun SparklePattern(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val step = 46f
        val s = 3.2f
        val color = Color.White.copy(alpha = 0.07f)
        var y = 0f
        var row = 0
        while (y < size.height + step) {
            var x = if (row % 2 == 0) 0f else step / 2f
            while (x < size.width + step) {
                drawLine(color, Offset(x, y - s), Offset(x, y + s), strokeWidth = 1.5f)
                drawLine(color, Offset(x - s, y), Offset(x + s, y), strokeWidth = 1.5f)
                x += step
            }
            y += step
            row++
        }
    }
}

@Composable
private fun OpenPokeball(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.pokeball_open),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
}

@Composable
private fun CardSectionLabel(text: String) {
    androidx.compose.material3.Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            color = Color.White.copy(alpha = 0.92f), fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp,
            shadow = Shadow(Color.Black.copy(alpha = 0.3f), Offset(0f, 1f), 4f),
        ),
    )
}

@Composable
private fun CardStatRow(label: String, fraction: Float, valueLabel: String?, color: Color = Color.White) {
    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { triggered = true }
    val w by animateFloatAsState(
        targetValue = if (triggered) fraction.coerceIn(0f, 1f) else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
    )
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        // Colored dot carries the trait's identity; the label stays white for readability.
        Box(Modifier.size(9.dp).clip(CircleShape).background(color))
        androidx.compose.material3.Text(
            label,
            modifier = Modifier.width(80.dp),
            style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Medium),
        )
        Box(Modifier.weight(1f).height(10.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.28f))) {
            Box(Modifier.fillMaxWidth(w).fillMaxHeight().clip(CircleShape).background(color))
        }
        if (valueLabel != null) {
            androidx.compose.material3.Text(
                valueLabel,
                modifier = Modifier.width(30.dp),
                style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun Pill(text: String) {
    Box(
        Modifier.clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = 0.22f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        androidx.compose.material3.Text(
            text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp
            ),
        )
    }
}

@Composable
private fun DetailTypeBadge(type: String) {
    Row(
        Modifier.clip(RoundedCornerShape(50)).background(typeColor(type))
            .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        TypeIcon(type, 22.dp)
        androidx.compose.material3.Text(
            type.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun TypeIcon(type: String, diameter: Dp) {
    Canvas(Modifier.size(diameter)) {
        val rr = size.minDimension / 2f
        // White disc so the type colour reads as the glyph against it.
        drawCircle(Color.White, rr, center)
        drawTypeGlyph(type, center, rr * 0.62f, typeColor(type), Color.White)
    }
}

private fun DrawScope.drawTypeGlyph(type: String, c: Offset, r: Float, fg: Color, bg: Color) {
    when (type.lowercase()) {
        "fire" -> drawPath(Path().apply {
            moveTo(c.x, c.y - r)
            cubicTo(c.x + r, c.y - r * 0.1f, c.x + r * 0.55f, c.y + r, c.x, c.y + r)
            cubicTo(c.x - r * 0.55f, c.y + r, c.x - r, c.y - r * 0.1f, c.x, c.y - r)
            close()
        }, fg)
        "water" -> drawPath(Path().apply {
            moveTo(c.x, c.y - r)
            cubicTo(c.x + r * 0.95f, c.y + r * 0.1f, c.x + r * 0.6f, c.y + r, c.x, c.y + r)
            cubicTo(c.x - r * 0.6f, c.y + r, c.x - r * 0.95f, c.y + r * 0.1f, c.x, c.y - r)
            close()
        }, fg)
        "grass" -> {
            drawPath(Path().apply {
                moveTo(c.x - r * 0.8f, c.y + r * 0.7f)
                cubicTo(c.x - r * 0.4f, c.y - r, c.x + r * 0.4f, c.y - r, c.x + r * 0.8f, c.y - r * 0.7f)
                cubicTo(c.x + r * 0.4f, c.y + r, c.x - r * 0.4f, c.y + r, c.x - r * 0.8f, c.y + r * 0.7f)
                close()
            }, fg)
            drawLine(bg, Offset(c.x - r * 0.45f, c.y + r * 0.4f), Offset(c.x + r * 0.45f, c.y - r * 0.4f), strokeWidth = r * 0.16f)
        }
        "bug" -> {
            val head = Offset(c.x, c.y - r * 0.55f)
            val thorax = Offset(c.x, c.y - r * 0.02f)
            val abdomen = Offset(c.x, c.y + r * 0.52f)
            // antennae
            drawLine(fg, head, Offset(c.x - r * 0.4f, c.y - r), strokeWidth = r * 0.1f)
            drawLine(fg, head, Offset(c.x + r * 0.4f, c.y - r), strokeWidth = r * 0.1f)
            // legs
            listOf(-0.25f, 0f, 0.25f).forEach { dy ->
                val y = thorax.y + dy * r
                drawLine(fg, Offset(c.x - r * 0.18f, y), Offset(c.x - r * 0.7f, y - r * 0.12f), strokeWidth = r * 0.08f)
                drawLine(fg, Offset(c.x + r * 0.18f, y), Offset(c.x + r * 0.7f, y - r * 0.12f), strokeWidth = r * 0.08f)
            }
            // body segments
            drawCircle(fg, r * 0.4f, abdomen)
            drawCircle(fg, r * 0.27f, thorax)
            drawCircle(fg, r * 0.24f, head)
        }
        "electric" -> drawPath(Path().apply {
            moveTo(c.x + r * 0.35f, c.y - r)
            lineTo(c.x - r * 0.55f, c.y + r * 0.15f)
            lineTo(c.x - r * 0.02f, c.y + r * 0.15f)
            lineTo(c.x - r * 0.35f, c.y + r)
            lineTo(c.x + r * 0.55f, c.y - r * 0.15f)
            lineTo(c.x + r * 0.02f, c.y - r * 0.15f)
            close()
        }, fg)
        "ice" -> listOf(90.0, 30.0, 150.0).forEach { deg ->
            val rad = (deg * PI / 180).toFloat()
            val dx = cos(rad) * r
            val dy = sin(rad) * r
            drawLine(fg, Offset(c.x - dx, c.y - dy), Offset(c.x + dx, c.y + dy), strokeWidth = r * 0.22f)
        }
        "psychic", "fairy" -> {
            drawCircle(fg, r, c, style = Stroke(width = r * 0.28f))
            drawCircle(fg, r * 0.34f, c)
        }
        "ghost" -> {
            drawPath(Path().apply {
                moveTo(c.x - r, c.y + r)
                lineTo(c.x - r, c.y)
                cubicTo(c.x - r, c.y - r * 1.3f, c.x + r, c.y - r * 1.3f, c.x + r, c.y)
                lineTo(c.x + r, c.y + r)
                lineTo(c.x + r * 0.5f, c.y + r * 0.6f)
                lineTo(c.x, c.y + r)
                lineTo(c.x - r * 0.5f, c.y + r * 0.6f)
                close()
            }, fg)
            drawCircle(bg, r * 0.16f, Offset(c.x - r * 0.32f, c.y - r * 0.1f))
            drawCircle(bg, r * 0.16f, Offset(c.x + r * 0.32f, c.y - r * 0.1f))
        }
        "poison" -> {
            drawCircle(fg, r * 0.55f, Offset(c.x - r * 0.35f, c.y + r * 0.3f))
            drawCircle(fg, r * 0.42f, Offset(c.x + r * 0.45f, c.y + r * 0.1f))
            drawCircle(fg, r * 0.5f, Offset(c.x, c.y - r * 0.4f))
        }
        "rock", "ground" -> {
            drawRect(fg, topLeft = Offset(c.x - r, c.y), size = Size(2 * r, r * 0.55f))
            drawRect(fg, topLeft = Offset(c.x - r * 0.6f, c.y - r * 0.7f), size = Size(1.2f * r, r * 0.55f))
        }
        "fighting" -> {
            // a clenched fist: palm + four knuckles + thumb
            drawCircle(fg, r * 0.6f, Offset(c.x, c.y + r * 0.18f))
            listOf(-0.42f, -0.14f, 0.14f, 0.42f).forEach { dx ->
                drawCircle(fg, r * 0.15f, Offset(c.x + dx * r, c.y - r * 0.34f))
            }
            drawCircle(fg, r * 0.2f, Offset(c.x - r * 0.62f, c.y + r * 0.05f))
        }
        "flying" -> {
            drawLine(fg, Offset(c.x - r, c.y + r * 0.3f), Offset(c.x, c.y - r * 0.5f), strokeWidth = r * 0.3f)
            drawLine(fg, Offset(c.x, c.y - r * 0.5f), Offset(c.x + r, c.y + r * 0.3f), strokeWidth = r * 0.3f)
        }
        "dragon" -> drawPath(Path().apply {
            moveTo(c.x, c.y - r); lineTo(c.x + r, c.y); lineTo(c.x, c.y + r); lineTo(c.x - r, c.y); close()
        }, fg)
        "dark" -> {
            drawCircle(fg, r, c)
            drawCircle(bg, r * 0.92f, Offset(c.x + r * 0.5f, c.y - r * 0.32f))
        }
        "steel" -> drawPath(Path().apply {
            for (i in 0 until 6) {
                val a = (PI / 3 * i - PI / 2).toFloat()
                val x = c.x + cos(a) * r
                val y = c.y + sin(a) * r
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }, fg)
        else -> drawCircle(fg, r * 0.7f, c) // normal & any unmapped type
    }
}

// ── History ───────────────────────────────────────────────────────────────────

@Composable
fun HistoryScreen(onMatches: () -> Unit, onRetake: () -> Unit, onSettings: () -> Unit) {
    val vm = koinInject<RecommendationViewModel>()
    val state by vm.state.collectAsState()
    val colors = LocalAppColors.current

    LaunchedEffect(Unit) { vm.loadHistory() }

    PokemonTheme {
        PokeScreen {
            Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth()
                        .background(colors.surface)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Text("Your past tests", style = TitleStyle)
                }
                androidx.compose.material3.HorizontalDivider(color = colors.border, thickness = 1.dp)

                Box(Modifier.weight(1f).fillMaxWidth()) {
                when (state) {
                    is UiState.Loading, UiState.Idle -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        IndeterminateProgressIndicator(
                            modifier = Modifier.width(120.dp),
                            indicatorColor = colors.primary,
                            trackColor = colors.border,
                        )
                    }
                    is UiState.Error -> Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
                        InlineError((state as UiState.Error).message)
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
                            val tests = recs.groupBy { it.generatedAt }.values.toList()
                            LazyColumn(
                                Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(top = 16.dp, bottom = 92.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                itemsIndexed(tests) { _, testRecs -> TestHistoryCard(testRecs) }
                            }
                        }
                    }
                }
                }
            }
            PokeNavBar(
                selected = NavTab.HISTORY,
                onSelect = { tab ->
                    when (tab) {
                        NavTab.MATCHES -> onMatches()
                        NavTab.HISTORY -> {}
                        NavTab.RETAKE -> onRetake()
                        NavTab.SETTINGS -> onSettings()
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
            )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    onMatches: () -> Unit,
    onHistory: () -> Unit,
    onRetake: () -> Unit,
    onSignOut: () -> Unit,
) {
    val colors = LocalAppColors.current
    val dark = LocalDarkTheme.current

    PokemonTheme {
        PokeScreen {
            Box(Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxSize()) {
                    Row(
                        Modifier.fillMaxWidth().background(colors.surface)
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        androidx.compose.material3.Text("Settings", style = TitleStyle)
                    }
                    androidx.compose.material3.HorizontalDivider(color = colors.border, thickness = 1.dp)

                    Column(
                        Modifier.weight(1f).fillMaxWidth().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        SectionLabel("Appearance")
                        Row(
                            Modifier.fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(14.dp), ambientColor = Color.Black.copy(alpha = 0.04f))
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.surface)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                androidx.compose.material3.Text("Theme", style = CardName.copy(fontSize = 15.sp))
                                androidx.compose.material3.Text(
                                    if (dark) "Dark mode" else "Light mode",
                                    style = LabelStyle,
                                )
                            }
                            ThemeToggle()
                        }

                        SectionLabel("Account")
                        Button(
                            onClick = onSignOut,
                            modifier = Modifier.fillMaxWidth(),
                            style = ButtonStyle.Outlined,
                        ) {
                            androidx.compose.material3.Text("Sign out")
                        }
                    }
                }
                PokeNavBar(
                    selected = NavTab.SETTINGS,
                    onSelect = { tab ->
                        when (tab) {
                            NavTab.MATCHES -> onMatches()
                            NavTab.HISTORY -> onHistory()
                            NavTab.RETAKE -> onRetake()
                            NavTab.SETTINGS -> {}
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun TestHistoryCard(recs: List<Recommendation>) {
    val colors = LocalAppColors.current
    val winner = recs.first()
    var expanded by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp), ambientColor = Color.Black.copy(alpha = 0.04f))
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .clickable { expanded = !expanded }
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(colors.background),
                    contentAlignment = Alignment.Center
                ) {
                    if (winner.pokemon.imageUrl != null)
                        AsyncImage(model = winner.pokemon.imageUrl, contentDescription = winner.pokemon.name,
                            contentScale = ContentScale.Fit, modifier = Modifier.size(40.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    androidx.compose.material3.Text(
                        winner.pokemon.name.replaceFirstChar { it.uppercase() },
                        style = CardName.copy(fontSize = 14.sp)
                    )
                    androidx.compose.material3.Text(winner.generatedAt.take(10), style = LabelStyle)
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                androidx.compose.material3.Text(
                    "${(winner.matchScore * 100).toInt()}%",
                    style = MonoStyle.copy(color = colors.secondary)
                )
                androidx.compose.material3.Text(
                    if (recs.size > 1) "${recs.size} matches ${if (expanded) "▲" else "▼"}" else "Top match",
                    style = LabelStyle
                )
            }
        }
        AnimatedVisibility(expanded && recs.size > 1) {
            Column(
                Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.HorizontalDivider(color = colors.border, thickness = 1.dp)
                recs.forEach { TestMatchRow(it) }
            }
        }
    }
}

@Composable
private fun TestMatchRow(rec: Recommendation) {
    val colors = LocalAppColors.current
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            androidx.compose.material3.Text(
                "#${rec.rank}",
                style = LabelStyle.copy(color = colors.muted),
                modifier = Modifier.width(26.dp)
            )
            Box(
                Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(colors.background),
                contentAlignment = Alignment.Center
            ) {
                if (rec.pokemon.imageUrl != null)
                    AsyncImage(model = rec.pokemon.imageUrl, contentDescription = rec.pokemon.name,
                        contentScale = ContentScale.Fit, modifier = Modifier.size(26.dp))
            }
            androidx.compose.material3.Text(
                rec.pokemon.name.replaceFirstChar { it.uppercase() },
                style = BodyStyle.copy(fontSize = 14.sp, color = colors.onSurface)
            )
        }
        androidx.compose.material3.Text(
            "${(rec.matchScore * 100).toInt()}%",
            style = MonoStyle.copy(color = colors.secondary)
        )
    }
}
