package pt.tiago.bubblenight.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Shared press feedback: a short scale-down that respects the reduced-motion setting. */
@Composable
internal fun rememberPressScale(interactionSource: MutableInteractionSource, reducedMotion: Boolean): Float {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reducedMotion) .965f else 1f,
        animationSpec = tween(90),
        label = "press",
    )
    return scale
}

/**
 * The glass panel every screen is built on: a translucent slab with a lit top edge, a soft inner
 * gradient and a hairline border. [accent] tints the border for highlighted cards.
 */
@Composable
internal fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    accent: Color = Palette.line,
    glow: Boolean = false,
    content: @Composable () -> Unit,
) {
    Box(modifier) {
        if (glow) {
            Canvas(Modifier.matchParentSize()) {
                drawCircle(
                    Brush.radialGradient(
                        listOf(accent.copy(alpha = .26f), Color.Transparent),
                        center = Offset(size.width * .5f, size.height * .1f),
                        radius = size.maxDimension * .8f,
                    ),
                    size.maxDimension * .8f,
                    Offset(size.width * .5f, size.height * .1f),
                )
            }
        }
        Box(
            Modifier
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        0f to Palette.panelSheen.copy(alpha = .95f),
                        .18f to Palette.panelTop.copy(alpha = .96f),
                        1f to Palette.panelBottom.copy(alpha = .97f),
                    ),
                )
                .border(BorderStroke(1.dp, accent.copy(alpha = .6f)), shape),
        ) {
            Canvas(Modifier.matchParentSize()) {
                drawRect(
                    Brush.verticalGradient(
                        0f to Color.White.copy(alpha = .14f),
                        .06f to Color.White.copy(alpha = .06f),
                        .18f to Color.Transparent,
                    ),
                    size = size,
                )
            }
            content()
        }
    }
}

/**
 * Primary and secondary call to action. The fill is a three-stop gradient with a glass sheen,
 * a hairline highlight and a coloured bloom underneath.
 */
@Composable
internal fun GlossyButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = Palette.violet,
    enabled: Boolean = true,
    reducedMotion: Boolean = false,
    trailingGlyph: String? = "›",
    compact: Boolean = false,
) {
    val interactions = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactions, reducedMotion)
    val shape = RoundedCornerShape(if (compact) 14.dp else 18.dp)
    val top = lerp(accent, Color.White, .38f)
    val bottom = lerp(accent, Color(0xFF2A1F5E), .38f)
    Box(modifier.scale(if (enabled) scale else 1f)) {
        Canvas(Modifier.matchParentSize().padding(horizontal = 6.dp, vertical = 4.dp)) {
            drawCircle(
                Brush.radialGradient(
                    listOf(accent.copy(alpha = if (enabled) .38f else .1f), Color.Transparent),
                    center = Offset(size.width * .5f, size.height * .5f),
                    radius = size.width * .55f,
                ),
                size.width * .55f,
                Offset(size.width * .5f, size.height * .5f),
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = if (compact) 48.dp else 56.dp)
                .clip(shape)
                .background(
                    if (enabled) Brush.verticalGradient(0f to top, .45f to accent, 1f to bottom)
                    else Brush.verticalGradient(listOf(Palette.raised, Palette.panelBottom)),
                )
                .border(BorderStroke(1.dp, lerp(accent, Color.White, .6f).copy(alpha = if (enabled) .8f else .25f)), shape)
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                    interactionSource = interactions,
                    indication = null,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.matchParentSize()) {
                drawRect(
                    Brush.verticalGradient(
                        0f to Color.White.copy(alpha = .34f),
                        .42f to Color.White.copy(alpha = .07f),
                        .5f to Color.Transparent,
                    ),
                    size = size,
                )
                drawRoundRect(
                    Color.White.copy(alpha = .22f),
                    topLeft = Offset(1.5f, 1.5f),
                    size = Size(size.width - 3f, size.height * .45f),
                    cornerRadius = CornerRadius(size.height * .35f),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
            Row(
                Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    label,
                    style = TextStyle(
                        fontFamily = BodyFont,
                        fontWeight = FontWeight.SemiBold,
                        color = if (enabled) Color(0xFF1A0F3C) else Palette.muted,
                        fontSize = if (compact) 15.sp else 17.sp,
                        lineHeight = 22.sp,
                    ),
                )
                if (trailingGlyph != null && enabled) {
                    Text(trailingGlyph, color = Color(0xFF1A0F3C).copy(alpha = .8f), fontSize = if (compact) 18.sp else 21.sp)
                }
            }
        }
    }
}

/** Quiet action used inside panels: a glass slab with a glyph. */
@Composable
internal fun GlassButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    reducedMotion: Boolean = false,
    accent: Color = Palette.cyan,
    glyph: String? = null,
) {
    val interactions = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactions, reducedMotion)
    Box(
        modifier
            .scale(if (enabled) scale else 1f)
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(Palette.raised.copy(alpha = .95f), Palette.panelBottom.copy(alpha = .98f))))
            .border(BorderStroke(1.dp, accent.copy(alpha = if (enabled) .55f else .18f)), RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, role = Role.Button, interactionSource = interactions, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (glyph != null) Text(glyph, color = accent.copy(alpha = if (enabled) 1f else .4f), fontSize = 17.sp)
            Text(
                label,
                style = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Medium, color = if (enabled) Palette.strong else Palette.muted, fontSize = 15.sp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Round glass icon button, 48 dp touch target, used for pause, back and orbit arrows. */
@Composable
internal fun GlassIconButton(
    glyph: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = Palette.cyan,
    reducedMotion: Boolean = false,
    diameter: androidx.compose.ui.unit.Dp = 44.dp,
) {
    val interactions = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactions, reducedMotion)
    Box(modifier.size(diameter + 4.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(diameter)
                .scale(scale)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(Palette.raised, Palette.panelBottom)))
                .border(BorderStroke(1.dp, accent.copy(alpha = .55f)), CircleShape)
                .clickable(role = Role.Button, interactionSource = interactions, indication = null, onClick = onClick)
                .semantics { contentDescription = description },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.matchParentSize()) {
                drawRect(
                    Brush.verticalGradient(listOf(Color.White.copy(alpha = .2f), Color.Transparent), startY = 0f, endY = size.height * .55f),
                    size = size,
                )
            }
            Text(glyph, color = Palette.strong, fontSize = (diameter.value * .52f).sp)
        }
    }
}

/** Small uppercase section label with a coloured rule, used to group settings and panels. */
@Composable
internal fun SectionLabel(text: String, modifier: Modifier = Modifier, accent: Color = Palette.violetLight) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(Modifier.size(width = 18.dp, height = 2.dp)) { drawRect(accent, size = size) }
        Text(text, style = Type.eyebrow.copy(color = accent))
    }
}

/** Small tinted capsule, used for difficulty and mode badges. */
@Composable
internal fun GlossyPill(text: String, accent: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(accent.copy(alpha = .4f), accent.copy(alpha = .18f))))
            .border(BorderStroke(1.dp, accent.copy(alpha = .8f)), CircleShape)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(
            text,
            style = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold, color = accent, fontSize = 12.sp, lineHeight = 15.sp),
        )
    }
}
