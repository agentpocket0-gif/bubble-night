package pt.tiago.bubblenight.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Cached brushes for the 3D bubble look. A board can show well over a hundred bubbles, so the
 * gradients are built once per (colour, radius) pair and reused: drawing a frame allocates nothing
 * beyond the few paths the symbols need.
 */
private object BubbleBrushes {
    private const val CAPACITY = 128
    private val cache = LinkedHashMap<Long, Brush>(CAPACITY, .75f, true)

    private fun cached(color: Color, radius: Float, kind: Int, build: () -> Brush): Brush {
        val bucket = (radius * 4f).toInt().coerceIn(0, 4000)
        val key = (color.value.toLong() shl 20) xor (bucket.toLong() shl 4) xor kind.toLong()
        cache[key]?.let { return it }
        val brush = build()
        if (cache.size >= CAPACITY) cache.remove(cache.keys.first())
        cache[key] = brush
        return brush
    }

    /** Lit sphere: highlight toward the upper left, soft terminator at the lower right. */
    fun sphere(base: Color, radius: Float): Brush = cached(base, radius, 1) {
        Brush.radialGradient(
            0f to lerp(base, Color.White, .78f),
            .32f to lerp(base, Color.White, .30f),
            .62f to base,
            .86f to lerp(base, Color(0xFF2A1E58), .26f),
            1f to lerp(base, Color(0xFF3A2A6E), .40f),
            center = Offset(-radius * .34f, -radius * .38f),
            radius = radius * 1.45f,
        )
    }

    /** Soft occlusion that rounds the silhouette without darkening it to black. */
    fun terminator(radius: Float): Brush = cached(Color.Black, radius, 2) {
        Brush.radialGradient(
            0f to Color.Transparent,
            .58f to Color.Transparent,
            1f to Color(0xFF2A1E52).copy(alpha = .5f),
            center = Offset(-radius * .16f, -radius * .2f),
            radius = radius * 1.3f,
        )
    }

    /** Specular blob: white core fading to nothing, so it reads as a reflection, not a dot. */
    fun specular(radius: Float): Brush = cached(Color.White, radius, 3) {
        Brush.radialGradient(
            0f to Color.White.copy(alpha = .95f),
            .45f to Color.White.copy(alpha = .55f),
            1f to Color.White.copy(alpha = 0f),
            center = Offset.Zero,
            radius = radius,
        )
    }

    fun ambient(radius: Float): Brush = cached(Color.Black, radius, 4) {
        Brush.radialGradient(
            0f to Color(0xFF2A1A55).copy(alpha = .45f),
            .6f to Color(0xFF2A1A55).copy(alpha = .2f),
            1f to Color.Transparent,
            center = Offset.Zero,
            radius = radius,
        )
    }

    fun rim(base: Color, radius: Float): Brush = cached(base, radius, 5) {
        Brush.radialGradient(
            0f to Color.Transparent,
            .72f to Color.Transparent,
            1f to lerp(base, Color.White, .85f).copy(alpha = .6f),
            center = Offset.Zero,
            radius = radius * 1.08f,
        )
    }

    fun rockLight(radius: Float): Brush = cached(Palette.rockLight, radius, 6) {
        Brush.linearGradient(
            0f to Palette.rockLight,
            .55f to Palette.rockMid,
            1f to Palette.rockDeep,
            start = Offset(-radius, -radius),
            end = Offset(radius, radius),
        )
    }
}

/**
 * Draws one bubble as a glossy sphere: ambient shadow, lit body, terminator, rim light, specular
 * highlight, a bounce light underneath and (optionally) the accessibility symbol.
 */
internal fun DrawScope.drawGlossyBubble(
    center: Offset,
    radius: Float,
    bubble: UiBubble,
    symbols: Boolean,
    alpha: Float = 1f,
    scale: Float = 1f,
    shadow: Boolean = true,
) {
    if (alpha <= 0f || scale <= 0f) return
    val r = radius * scale
    if (bubble.kind == UiBubbleKind.ROCK) {
        drawGlossyRock(center, r, alpha, shadow)
        return
    }
    val base = if (bubble.kind == UiBubbleKind.STAR) Palette.starShell else Palette.bubble(bubble.color)

    if (shadow) {
        translate(center.x, center.y + r * .3f) {
            drawOval(
                BubbleBrushes.ambient(r * 1.25f),
                topLeft = Offset(-r * 1.02f, -r * .5f),
                size = Size(r * 2.04f, r * 1.0f),
            )
        }
    }

    translate(center.x, center.y) {
        drawCircle(BubbleBrushes.sphere(base, r), r, Offset.Zero, alpha = alpha)
        drawCircle(BubbleBrushes.terminator(r), r, Offset.Zero, alpha = alpha)
        drawCircle(BubbleBrushes.rim(base, r), r, Offset.Zero, alpha = alpha)
        drawCircle(lerp(base, Color.White, .35f).copy(alpha = alpha * .4f), r, Offset.Zero, style = Stroke(width = (r * .045f).coerceAtLeast(.6f)))

        rotate(-24f, Offset(-r * .34f, -r * .44f)) {
            drawOval(
                BubbleBrushes.specular(r * .52f),
                topLeft = Offset(-r * .62f, -r * .56f),
                size = Size(r * .58f, r * .34f),
                alpha = alpha,
            )
        }
        drawCircle(Color.White.copy(alpha = .95f * alpha), r * .085f, Offset(-r * .12f, -r * .62f))
        drawCircle(Color.White.copy(alpha = .55f * alpha), r * .04f, Offset(r * .04f, -r * .72f))
        drawOval(
            Brush.radialGradient(
                listOf(lerp(base, Color.White, .6f).copy(alpha = .38f * alpha), Color.Transparent),
                center = Offset(-r * .1f, r * .5f),
                radius = r * .55f,
            ),
            topLeft = Offset(-r * .46f, r * .34f),
            size = Size(r * .92f, r * .4f),
        )

        if (bubble.kind == UiBubbleKind.STAR) {
            drawCircle(Palette.gold.copy(alpha = alpha * .9f), r * .93f, Offset.Zero, style = Stroke(width = (r * .07f).coerceAtLeast(1f)))
            drawCircle(Palette.text.copy(alpha = alpha * .45f), r * .66f, Offset.Zero, style = Stroke(width = (r * .035f).coerceAtLeast(.6f)))
            drawStar(Offset.Zero, r * .42f, Palette.gold.copy(alpha = alpha), 5)
            drawCircle(Color.White.copy(alpha = alpha * .8f), r * .1f, Offset(-r * .06f, -r * .06f))
        } else if (symbols) {
            drawBubbleSymbol(Offset.Zero, r * .38f, bubble.color ?: UiBubbleColor.BLUE, alpha)
        }
    }
}

/** Faceted stone with three lit planes; still clearly a rock at a glance. */
private fun DrawScope.drawGlossyRock(center: Offset, radius: Float, alpha: Float, shadow: Boolean) {
    val r = radius
    if (shadow) {
        translate(center.x, center.y + r * .3f) {
            drawOval(
                BubbleBrushes.ambient(r * 1.2f),
                topLeft = Offset(-r * .95f, -r * .48f),
                size = Size(r * 1.9f, r * .96f),
            )
        }
    }
    translate(center.x, center.y) {
        drawCircle(Palette.rockDeep.copy(alpha = alpha), r, Offset.Zero)
        val shell = Path().apply {
            moveTo(-r * .68f, -r * .56f); lineTo(-r * .12f, -r * .96f)
            lineTo(r * .63f, -r * .62f); lineTo(r * .91f, r * .12f)
            lineTo(r * .42f, r * .85f); lineTo(-r * .52f, r * .72f)
            lineTo(-r * .91f, r * .08f); close()
        }
        drawPath(shell, BubbleBrushes.rockLight(r), alpha = alpha)
        val top = Path().apply {
            moveTo(-r * .68f, -r * .56f); lineTo(-r * .12f, -r * .96f)
            lineTo(r * .63f, -r * .62f); lineTo(r * .05f, -r * .1f); lineTo(-r * .7f, -r * .18f); close()
        }
        drawPath(top, lerp(Palette.rockLight, Color.White, .4f).copy(alpha = alpha * .85f))
        val right = Path().apply {
            moveTo(r * .63f, -r * .62f); lineTo(r * .91f, r * .12f); lineTo(r * .42f, r * .85f)
            lineTo(r * .05f, -r * .1f); close()
        }
        drawPath(right, Palette.rockDeep.copy(alpha = alpha * .5f))
        drawPath(shell, lerp(Palette.rockLight, Color.White, .6f).copy(alpha = alpha * .55f), style = Stroke(width = (r * .05f).coerceAtLeast(.6f)))
        drawLine(Palette.rockDeep.copy(alpha = alpha * .7f), Offset(-r * .12f, -r * .96f), Offset(r * .05f, -r * .1f), (r * .05f).coerceAtLeast(.6f))
        drawLine(Palette.rockDeep.copy(alpha = alpha * .5f), Offset(-r * .91f, r * .08f), Offset(r * .05f, -r * .1f), (r * .04f).coerceAtLeast(.5f))
        drawOval(
            BubbleBrushes.specular(r * .3f),
            topLeft = Offset(-r * .42f, -r * .74f),
            size = Size(r * .38f, r * .2f),
            alpha = alpha * .75f,
        )
    }
}

/**
 * Accessibility symbols: one distinct shape per colour, unchanged from the original game so
 * players who rely on them keep the same mapping.
 */
private fun DrawScope.drawBubbleSymbol(center: Offset, r: Float, color: UiBubbleColor, alpha: Float) {
    val ink = Color(0xFF1A1030).copy(alpha = alpha * .8f)
    val sheen = Color.White.copy(alpha = alpha * .35f)
    translate(center.x, center.y) {
        when (color) {
            UiBubbleColor.BLUE -> {
                drawCircle(ink, r * .72f, Offset.Zero)
                drawCircle(sheen, r * .72f, Offset.Zero, style = Stroke(width = (r * .12f).coerceAtLeast(.5f)))
            }
            UiBubbleColor.PURPLE -> drawStar(Offset.Zero, r, ink, 5)
            UiBubbleColor.GREEN -> {
                val path = Path().apply { moveTo(0f, -r); lineTo(r * .88f, r * .72f); lineTo(-r * .88f, r * .72f); close() }
                drawPath(path, ink)
            }
            UiBubbleColor.ORANGE -> drawRoundRect(
                ink,
                topLeft = Offset(-r * .68f, -r * .68f),
                size = Size(r * 1.36f, r * 1.36f),
                cornerRadius = CornerRadius(r * .18f),
            )
            UiBubbleColor.PINK -> {
                val path = Path().apply { moveTo(0f, -r); lineTo(r, 0f); lineTo(0f, r); lineTo(-r, 0f); close() }
                drawPath(path, ink)
            }
            UiBubbleColor.CYAN -> {
                drawRoundRect(ink, Offset(-r * .28f, -r), Size(r * .56f, r * 2f), CornerRadius(r * .2f))
                drawRoundRect(ink, Offset(-r, -r * .28f), Size(r * 2f, r * .56f), CornerRadius(r * .2f))
            }
        }
    }
}

/** Five-point star, filled. Shared by the star meter, achievements and captive stars. */
internal fun DrawScope.drawStar(center: Offset, radius: Float, color: Color, points: Int) {
    val path = Path()
    val inner = radius * .46f
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) radius else inner
        val angle = -PI / 2 + i * PI / points
        val x = center.x + cos(angle).toFloat() * r
        val y = center.y + sin(angle).toFloat() * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}

/** Star outline used for empty slots in the star meter. */
internal fun DrawScope.drawStarOutline(center: Offset, radius: Float, color: Color) {
    val path = Path()
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) radius else radius * .46f
        val angle = -PI / 2 + i * PI / 5
        val x = center.x + cos(angle).toFloat() * r
        val y = center.y + sin(angle).toFloat() * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color, style = Stroke(width = 1.dp.toPx()))
}

/** Small gold star with a glow, used on results and achievements. */
internal fun DrawScope.drawGlowingStar(center: Offset, radius: Float, color: Color, alpha: Float = 1f) {
    drawCircle(
        Brush.radialGradient(
            listOf(color.copy(alpha = .45f * alpha), Color.Transparent),
            center = center,
            radius = radius * 2.4f,
        ),
        radius * 2.4f,
        center,
    )
    drawStar(center, radius, color.copy(alpha = alpha), 5)
    drawStar(center, radius * .58f, lerp(color, Color.White, .55f).copy(alpha = alpha), 5)
}
