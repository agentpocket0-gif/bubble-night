package pt.tiago.bubblenight.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import pt.tiago.bubblenight.R

/**
 * The lit cosmic palette: a violet night sky bright enough to read as a lit scene, with glass
 * panels that carry light instead of swallowing it. Every screen, board and illustration takes its
 * colours from here, so the whole game reads as one product. No image assets are involved.
 */
internal object Palette {
    // Sky, from the lit crown of the sky down to the deeper horizon.
    val void = Color(0xFF221A55)
    val space = Color(0xFF31256F)
    val nebulaDeep = Color(0xFF42318F)
    val nebulaViolet = Color(0xFF7A5AD8)
    val nebulaBlue = Color(0xFF4468D8)
    val horizon = Color(0xFF3A2C7A)

    // Glass surfaces: translucent panels lit from the top edge.
    val panelTop = Color(0xFF4B3C8C)
    val panelBottom = Color(0xFF332769)
    val panelSheen = Color(0xFF7D66C9)
    val raised = Color(0xFF52418F)
    val line = Color(0xFF9A83E0)
    val lineSoft = Color(0xFF6A57A8)

    // Type.
    val strong = Color(0xFFFFFFFF)
    val text = Color(0xFFEDE7FF)
    val muted = Color(0xFFBCB0E4)

    // Accents: violet is the identity colour, cyan the highlight, gold the reward colour.
    val violet = Color(0xFF9B6BFF)
    val violetLight = Color(0xFFCDAEFF)
    val violetDeep = Color(0xFF6A3FD8)
    val cyan = Color(0xFF6FE3FF)
    val cyanDeep = Color(0xFF2AA6D8)
    val gold = Color(0xFFFFD27A)
    val goldDeep = Color(0xFFF0A63A)
    val mint = Color(0xFF5BF0B0)
    val rose = Color(0xFFFF7A9E)
    val roseDeep = Color(0xFFD8416A)

    val onAccent = Color(0xFF1A0F3C)

    // The six bubble colours: bright, saturated centres that still read on a lit sky.
    val bubbleBlue = Color(0xFF3D8BFF)
    val bubblePurple = Color(0xFFB06BFF)
    val bubbleGreen = Color(0xFF3FE08A)
    val bubbleOrange = Color(0xFFFFA23D)
    val bubblePink = Color(0xFFFF6B95)
    val bubbleCyan = Color(0xFF3FE2FF)
    val rockLight = Color(0xFFD8DCF0)
    val rockMid = Color(0xFF9BA2C4)
    val rockDeep = Color(0xFF565C7E)
    val starShell = Color(0xFF4A3A80)

    fun bubble(color: UiBubbleColor?): Color = when (color) {
        UiBubbleColor.BLUE -> bubbleBlue
        UiBubbleColor.PURPLE -> bubblePurple
        UiBubbleColor.GREEN -> bubbleGreen
        UiBubbleColor.ORANGE -> bubbleOrange
        UiBubbleColor.PINK -> bubblePink
        UiBubbleColor.CYAN -> bubbleCyan
        null -> bubbleBlue
    }

    fun difficulty(difficulty: UiDifficulty): Color = when (difficulty) {
        UiDifficulty.EASY -> mint
        UiDifficulty.MEDIUM -> cyan
        UiDifficulty.HARD -> rose
    }
}

internal val DisplayFont = FontFamily(Font(R.font.bricolage_grotesque))
internal val BodyFont = FontFamily(Font(R.font.figtree))

/** Shared type ramp; screens compose these instead of declaring their own. */
internal object Type {
    val display = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Bold, color = Palette.strong, fontSize = 36.sp, lineHeight = 40.sp)
    val title = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold, color = Palette.strong, fontSize = 25.sp, lineHeight = 31.sp)
    val cardTitle = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold, color = Palette.strong, fontSize = 20.sp, lineHeight = 26.sp)
    val body = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Normal, color = Palette.text, fontSize = 16.sp, lineHeight = 22.sp)
    val label = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Medium, color = Palette.muted, fontSize = 13.sp, lineHeight = 18.sp)
    val eyebrow = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold, color = Palette.violetLight, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 2.sp)
    val numeric = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Bold, color = Palette.strong, fontSize = 24.sp, lineHeight = 28.sp)
}
