package com.pokemonai.client.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.composables.ui.theme.ComposablesTheme

object PokeColors {
    val Cream        = Color(0xFFFDF8F0)
    val CreamDeep    = Color(0xFFF0EAE0)
    val CreamMid     = Color(0xFFF7F2EA)
    val Ink          = Color(0xFF1A1A2E)
    val Muted        = Color(0xFF8A8A9A)
    val MutedLight   = Color(0xFFB8B8C8)
    val Border       = Color(0xFFE8E2D6)
    val BorderLight  = Color(0xFFF2EDE6)

    val PokeRed      = Color(0xFFE63946)
    val PokeRedDark  = Color(0xFFBF2D38)
    val PokeRedLight = Color(0xFFFFF0F1)

    val Blue         = Color(0xFF3D5A99)
    val BlueLight    = Color(0xFFEFF3FB)

    val GrassGreen    = Color(0xFF4CAF50)
    val FireOrange    = Color(0xFFFF7043)
    val WaterBlue     = Color(0xFF2196F3)
    val ElectricYellow = Color(0xFFFFB800)
    val PsychicPink   = Color(0xFFE91E8C)
    val PoisonPurple  = Color(0xFF9C27B0)
    val RockBrown     = Color(0xFF795548)
    val GroundTan     = Color(0xFFA1887F)
    val IceBlue       = Color(0xFF42A5B3)
    val GhostIndigo   = Color(0xFF5C6BC0)
    val NormalGrey    = Color(0xFFAAAAAA)
    val FightingRed   = Color(0xFFD32F2F)
    val FlyingTeal    = Color(0xFF26C6DA)
    val BugOlive      = Color(0xFF7CB342)
    val DragonNavy    = Color(0xFF1565C0)
    val DarkCharcoal  = Color(0xFF424242)
    val SteelGrey     = Color(0xFF90A4AE)

    val White        = Color(0xFFFFFFFF)
    val ErrorRed     = Color(0xFFB00020)
    val ErrorBg      = Color(0xFFFFF0F2)
}

fun typeColor(type: String?): Color = when (type?.lowercase()) {
    "fire"     -> PokeColors.FireOrange
    "water"    -> PokeColors.WaterBlue
    "grass"    -> PokeColors.GrassGreen
    "electric" -> PokeColors.ElectricYellow
    "psychic"  -> PokeColors.PsychicPink
    "poison"   -> PokeColors.PoisonPurple
    "rock"     -> PokeColors.RockBrown
    "ground"   -> PokeColors.GroundTan
    "ice"      -> PokeColors.IceBlue
    "ghost"    -> PokeColors.GhostIndigo
    "fighting" -> PokeColors.FightingRed
    "flying"   -> PokeColors.FlyingTeal
    "bug"      -> PokeColors.BugOlive
    "dragon"   -> PokeColors.DragonNavy
    "dark"     -> PokeColors.DarkCharcoal
    "steel"    -> PokeColors.SteelGrey
    else       -> PokeColors.NormalGrey
}

@Composable
fun PokemonTheme(content: @Composable () -> Unit) {
    ComposablesTheme {
        content()
    }
}
