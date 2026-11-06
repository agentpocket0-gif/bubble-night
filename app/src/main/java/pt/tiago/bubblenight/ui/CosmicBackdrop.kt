package pt.tiago.bubblenight.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.sin

/**
 * One precomputed sky: the background gradient, the light bloom, nebula volumes, the galactic band
 * and three star layers. Brushes are built when the size or orbit changes, never per frame, so the
 * animated backdrop costs a few hundred draw calls and no allocations.
 */
internal class CosmicSky(
    val base: Brush,
    val band: Brush,
    val blobs: List<SkyBlob>,
    val stars: List<SkyStar>,
    val width: Float,
    val height: Float,
)

internal class SkyBlob(val x: Float, val y: Float, val radius: Float, val brush: Brush, val layer: Float)

internal class SkyStar(val x: Float, val y: Float, val radius: Float, val alpha: Float, val layer: Float, val phase: Float, val color: Color)

private fun buildSky(width: Float, height: Float, orbit: Int): CosmicSky {
    val art = orbitArt(orbit)
    val accent = art.accent
    // A lit sky: bright crown, violet body, deeper horizon. Never black.
    val base = Brush.verticalGradient(
        0f to lerp(Palette.nebulaViolet, Color.White, .28f),
        .18f to Palette.nebulaViolet,
        .46f to lerp(Palette.space, Palette.nebulaBlue, .45f),
        .78f to Palette.space,
        1f to Palette.horizon,
    )
    // A tilted galactic band, dim enough to sit behind every panel.
    val band = Brush.linearGradient(
        0f to Color.Transparent,
        .32f to lerp(Palette.nebulaViolet, accent, .25f).copy(alpha = .45f),
        .5f to lerp(Palette.nebulaViolet, Color.White, .25f).copy(alpha = .34f),
        .68f to lerp(Palette.nebulaBlue, accent, .2f).copy(alpha = .4f),
        1f to Color.Transparent,
        start = Offset(width * .05f, 0f),
        end = Offset(width * .95f, height),
    )
    val blobs = listOf(
        // The light source: a bright bloom in the upper third that everything else falls off from.
        SkyBlob(width * .34f, height * .1f, width * 1.1f, radial(Color.White.copy(alpha = .30f), width * 1.1f), .18f),
        SkyBlob(width * .78f, height * .16f, width * .85f, radial(lerp(accent, Color.White, .35f).copy(alpha = .32f), width * .85f), .55f),
        SkyBlob(width * .18f, height * .42f, width * .7f, radial(lerp(Palette.nebulaBlue, Color.White, .2f).copy(alpha = .28f), width * .7f), .35f),
        SkyBlob(width * .58f, height * .8f, width * .95f, radial(lerp(Palette.nebulaDeep, accent, .45f).copy(alpha = .24f), width * .95f), .2f),
    )

    val stars = ArrayList<SkyStar>(190)
    var seed = (orbit * 7919 + 104729).toLong()
    fun next(): Float {
        seed = (seed * 6364136223846793005L + 1442695040888963407L)
        return ((seed ushr 11).toFloat() / (1L shl 53).toFloat())
    }
    repeat(190) { index ->
        val layer = when {
            index < 110 -> .25f
            index < 165 -> .55f
            else -> 1f
        }
        val tint = when {
            index % 17 == 0 -> lerp(accent, Color.White, .5f)
            index % 11 == 0 -> Palette.gold
            index % 5 == 0 -> lerp(Palette.cyan, Color.White, .35f)
            else -> Color.White
        }
        stars.add(
            SkyStar(
                x = next() * width,
                y = next() * height,
                radius = (.7f + next() * 1.9f) * (0.6f + layer * .8f),
                alpha = .25f + next() * .6f,
                layer = layer,
                phase = next() * 6.283f,
                color = tint,
            ),
        )
    }
    return CosmicSky(base, band, blobs, stars, width, height)
}

private fun radial(color: Color, radius: Float): Brush =
    Brush.radialGradient(listOf(color, Color.Transparent), center = Offset.Zero, radius = radius)

/**
 * The shared sky behind every screen: lit gradient, light shafts, nebula volumes, a galactic band
 * and three star layers that drift at different speeds. [drift] runs 0..1 and is pinned to 0 when
 * the player has reduced motion enabled.
 */
internal fun DrawScope.drawCosmicSky(sky: CosmicSky, drift: Float) {
    drawRect(sky.base, Offset.Zero, Size(sky.width, sky.height))

    sky.blobs.forEach { blob ->
        val shift = drift * blob.layer * sky.width * .05f
        translate(blob.x - shift, blob.y + shift * .4f) {
            drawCircle(blob.brush, blob.radius, Offset.Zero)
        }
    }

    rotate(-24f, Offset(sky.width * .5f, sky.height * .42f)) {
        drawRect(sky.band, Offset(-sky.width * .5f, sky.height * .12f), Size(sky.width * 2f, sky.height * .8f))
    }

    // Soft light shafts from the bloom, so the sky reads as illuminated.
    for (index in 0 until 5) {
        val angle = -0.9f + index * 0.28f
        val spread = sky.width * .1f
        val origin = Offset(sky.width * .34f, -sky.height * .05f)
        val tip = Offset(origin.x + sin(angle) * sky.height * 1.5f, origin.y + sky.height * 1.5f)
        drawPath(
            Path().apply {
                moveTo(origin.x, origin.y)
                lineTo(tip.x - spread, tip.y)
                lineTo(tip.x + spread, tip.y)
                close()
            },
            Color.White.copy(alpha = .05f),
        )
    }

    sky.stars.forEach { star ->
        val twinkle = if (drift == 0f) 1f else .72f + .28f * sin(drift * 6.283f * 3f + star.phase)
        val x = star.x + drift * star.layer * sky.width * .035f
        val y = star.y - drift * star.layer * sky.height * .012f
        drawCircle(star.color.copy(alpha = (star.alpha * twinkle).coerceIn(0f, 1f)), star.radius, Offset(x, y))
        if (star.radius > 2.1f) {
            val flare = star.radius * 3.2f * twinkle
            drawLine(star.color.copy(alpha = star.alpha * .28f * twinkle), Offset(x - flare, y), Offset(x + flare, y), .8f)
            drawLine(star.color.copy(alpha = star.alpha * .22f * twinkle), Offset(x, y - flare), Offset(x, y + flare), .8f)
        }
    }
}

/** Bottom vignette that keeps the skyline legible under the launcher and the menu footers. */
internal fun DrawScope.drawVignette(width: Float, height: Float) {
    drawRect(
        Brush.verticalGradient(
            0f to Color.Transparent,
            .7f to Color.Transparent,
            1f to Color(0xFF1A1240).copy(alpha = .55f),
            startY = 0f,
            endY = height,
        ),
        Offset.Zero,
        Size(width, height),
    )
}

/**
 * Full-screen animated backdrop. The orbit picks the planet and the nebula tint, so moving
 * through the campaign changes the sky without loading a single image.
 */
@Composable
internal fun CosmicBackdrop(orbitNumber: Int, reducedMotion: Boolean, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    BoxWithConstraints(modifier.fillMaxSize()) {
        val width = with(density) { maxWidth.toPx() }
        val height = with(density) { maxHeight.toPx() }
        val sky = remember(width, height, orbitNumber) { buildSky(width, height, orbitNumber) }
        val transition = rememberInfiniteTransition(label = "sky")
        val drift by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 52_000, easing = LinearEasing), RepeatMode.Reverse),
            label = "drift",
        )
        val motion = if (reducedMotion) 0f else drift
        Canvas(Modifier.fillMaxSize()) {
            drawCosmicSky(sky, motion)
            drawOrbitArt(orbitArt(orbitNumber), motion)
            drawVignette(size.width, size.height)
        }
    }
}
