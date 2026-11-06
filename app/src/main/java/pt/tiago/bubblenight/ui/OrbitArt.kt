package pt.tiago.bubblenight.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/** Visual feature of one orbit; each orbit name in the catalog maps to one of these. */
internal enum class OrbitFeature { RING, ASTEROIDS, AURORA, CONCENTRIC, BRIDGE, ECHOES, NEBULA, HORIZON, CROWN, STARFIELD, BIG_MOON }

/**
 * Vector illustration parameters for one orbit. Everything is drawn in Compose Canvas
 * from these values, so the art stays crisp at any size and needs no image assets.
 */
internal data class OrbitArt(
    val planetLight: Color,
    val planetDark: Color,
    val accent: Color,
    val feature: OrbitFeature,
    val tiltDegrees: Float,
    val moons: Int,
    val mountainSeed: Float,
)

private val Violet = Color(0xFFBB9AF7)
private val Orange = Color(0xFFFF9E64)
private val Cyan = Color(0xFF7DCFFF)
private val Green = Color(0xFF9ECE6A)
private val Pink = Color(0xFFF7768E)
private val Blue = Color(0xFF7AA2F7)
private val Gold = Color(0xFFE0AF68)
private val Teal = Color(0xFF73DACA)
private val Grey = Color(0xFFA9B1D6)

/** One entry per orbit, in catalog order (R.array.orbit_names). */
internal fun orbitArt(orbit: Int): OrbitArt = when (orbit.coerceIn(1, 12)) {
    1 -> OrbitArt(Blue, Color(0xFF2A3A66), Blue, OrbitFeature.RING, 12f, 1, 0.3f)                     // First Sky
    2 -> OrbitArt(Grey, Color(0xFF3B4158), Grey, OrbitFeature.BIG_MOON, 0f, 1, 1.1f)                 // Lunar Valley
    3 -> OrbitArt(Green, Color(0xFF2C4A34), Green, OrbitFeature.STARFIELD, 8f, 2, 2.0f)              // Star Garden
    4 -> OrbitArt(Orange, Color(0xFF5A3A2A), Gold, OrbitFeature.ASTEROIDS, 26f, 0, 2.7f)             // Asteroid Belt
    5 -> OrbitArt(Violet, Color(0xFF3A2A5C), Violet, OrbitFeature.AURORA, 4f, 2, 0.8f)               // Violet Aurora
    6 -> OrbitArt(Teal, Color(0xFF1F4A4A), Teal, OrbitFeature.CONCENTRIC, 18f, 3, 1.9f)              // Sea of Orbits
    7 -> OrbitArt(Cyan, Color(0xFF244A60), Cyan, OrbitFeature.BRIDGE, 10f, 1, 0.6f)                  // Celestial Bridge
    8 -> OrbitArt(Grey, Color(0xFF363A55), Violet, OrbitFeature.ECHOES, 16f, 2, 2.4f)                 // Echoes of the Moon
    9 -> OrbitArt(Blue, Color(0xFF1F2F5C), Cyan, OrbitFeature.NEBULA, 30f, 1, 1.5f)                  // Blue Nebula
    10 -> OrbitArt(Pink, Color(0xFF5C2A3C), Pink, OrbitFeature.HORIZON, 0f, 2, 3.1f)                  // Distant Horizon
    11 -> OrbitArt(Gold, Color(0xFF4A3D24), Gold, OrbitFeature.CROWN, 14f, 2, 0.1f)                   // Northern Crown
    else -> OrbitArt(Orange, Color(0xFF4E2C24), Gold, OrbitFeature.RING, 40f, 4, 2.2f)                // Last Constellation
}

/**
 * Draws the orbit's planet, its feature, moons and the layered skyline over the sky.
 *
 * [drift] runs 0..1 from the backdrop animation: the planet and the ridges move by a few pixels,
 * which is enough parallax to sell depth without ever moving anything the player interacts with.
 */
internal fun DrawScope.drawOrbitArt(art: OrbitArt, drift: Float = 0f) {
    val w = size.width
    val h = size.height
    val horizon = art.feature == OrbitFeature.HORIZON
    val center = if (horizon) Offset(w * .5f, h * .84f + drift * h * .006f) else Offset(w * .84f - drift * w * .012f, h * .17f + drift * h * .01f)
    val r = if (horizon) w * .34f else w * .108f

    drawCircle(art.accent.copy(alpha = .09f), r * 2.6f, center)
    drawCircle(
        Brush.radialGradient(
            listOf(art.accent.copy(alpha = .26f), Color.Transparent),
            center = center,
            radius = r * 1.9f,
        ),
        r * 1.9f,
        center,
    )

    drawCircle(
        Brush.radialGradient(
            0f to lerp(art.planetLight, Color.White, .55f),
            .42f to art.planetLight,
            .78f to lerp(art.planetLight, art.planetDark, .8f),
            1f to lerp(art.planetDark, Color(0xFF241A52), .45f),
            center = center - Offset(r * .34f, r * .36f),
            radius = r * 1.55f,
        ),
        r,
        center,
    )
    for (k in 0 until 3) {
        val bandY = center.y - r * .5f + k * r * .5f
        drawOval(
            art.planetDark.copy(alpha = .16f + k * .03f),
            topLeft = Offset(center.x - r * .92f, bandY),
            size = Size(r * 1.84f, r * (.12f + k * .04f)),
        )
    }
    drawCircle(
        Brush.radialGradient(listOf(Color.White.copy(alpha = .38f), Color.Transparent), center = center - Offset(r * .42f, r * .44f), radius = r * .55f),
        r * .55f,
        center - Offset(r * .42f, r * .44f),
    )
    drawArc(
        color = lerp(art.planetLight, Color.White, .7f).copy(alpha = .45f),
        startAngle = 150f, sweepAngle = 120f, useCenter = false,
        topLeft = Offset(center.x - r * .97f, center.y - r * .97f), size = Size(r * 1.94f, r * 1.94f),
        style = Stroke(width = r * .06f),
    )

    when (art.feature) {
        OrbitFeature.RING -> ring(art, center, r, 1.75f, .42f)
        OrbitFeature.ASTEROIDS -> {
            ring(art, center, r, 1.8f, .4f)
            for (i in 0 until 12) {
                val a = i * 0.7f + 0.3f
                val p = Offset(center.x + cos(a) * r * 1.75f, center.y + sin(a) * r * .4f)
                val stone = 1.2f + (i % 3) * .7f
                drawCircle(lerp(art.accent, Palette.rockDeep, .3f).copy(alpha = .9f), stone, p)
                drawCircle(lerp(art.accent, Color.White, .6f).copy(alpha = .6f), stone * .5f, p - Offset(stone * .3f, stone * .3f))
            }
        }
        OrbitFeature.AURORA -> for (k in 0 until 5) {
            val y = h * (.03f + k * .028f) - drift * h * .004f * k
            drawOval(
                Brush.verticalGradient(
                    listOf(art.accent.copy(alpha = .0f), art.accent.copy(alpha = .16f - k * .018f), Color.Transparent),
                    startY = y, endY = y + h * .1f,
                ),
                topLeft = Offset(0f, y), size = Size(w, h * .1f),
            )
        }
        OrbitFeature.CONCENTRIC -> for (k in 1..4) {
            drawCircle(art.accent.copy(alpha = .3f - k * .045f), r * (1f + k * .3f), center, style = Stroke(width = 1.dp.toPx()))
        }
        OrbitFeature.BRIDGE -> {
            val arc = Path().apply {
                moveTo(w * .02f, h * .42f)
                cubicTo(w * .24f, h * .12f, w * .5f, h * .1f, center.x - r * 1.15f, center.y + r * .55f)
            }
            drawPath(arc, art.accent.copy(alpha = .22f), style = Stroke(width = 4.dp.toPx()))
            drawPath(arc, art.accent.copy(alpha = .7f), style = Stroke(width = 1.1.dp.toPx()))
            for (i in 0..5) {
                val t = i / 5f
                val x = w * .02f + (center.x - r * 1.15f - w * .02f) * t
                val y = h * .42f - sin(t * 3.14f) * h * .22f
                drawCircle(art.accent.copy(alpha = .9f), 1.6f, Offset(x, y))
            }
        }
        OrbitFeature.ECHOES -> for (k in 1..4) {
            drawCircle(art.accent.copy(alpha = .2f - k * .035f), r * (1f + k * .2f), center, style = Stroke(width = .9.dp.toPx()))
        }
        OrbitFeature.NEBULA -> {
            val g = Offset(w * .3f, h * .22f)
            rotate(-28f, g) {
                drawOval(
                    Brush.radialGradient(
                        listOf(art.accent.copy(alpha = .45f), lerp(Palette.violetLight, Color.White, .4f).copy(alpha = .25f), Color.Transparent),
                        center = g, radius = w * .2f,
                    ),
                    topLeft = Offset(g.x - w * .19f, g.y - w * .05f),
                    size = Size(w * .38f, w * .1f),
                )
                drawOval(
                    lerp(Palette.violetLight, Color.White, .6f).copy(alpha = .4f),
                    topLeft = Offset(g.x - w * .05f, g.y - w * .018f),
                    size = Size(w * .1f, w * .036f),
                )
            }
        }
        OrbitFeature.HORIZON -> drawCircle(
            Brush.radialGradient(listOf(art.accent.copy(alpha = .32f), Color.Transparent), center = center, radius = r * 1.5f),
            r * 1.5f,
            center,
        )
        OrbitFeature.CROWN -> {
            drawArc(
                color = art.accent.copy(alpha = .9f), startAngle = 200f, sweepAngle = 140f, useCenter = false,
                topLeft = Offset(center.x - r * 1.3f, center.y - r * 1.3f), size = Size(r * 2.6f, r * 2.6f),
                style = Stroke(width = 2.dp.toPx()),
            )
            for (i in 0 until 5) {
                val a = (200f + i * 35f) * kotlin.math.PI.toFloat() / 180f
                val p = Offset(center.x + cos(a) * r * 1.3f, center.y + sin(a) * r * 1.3f)
                drawGlowingStar(p, r * .12f, art.accent, .9f)
            }
        }
        OrbitFeature.STARFIELD -> for (i in 0 until 30) {
            val x = ((i * .618f) % 1f) * w
            val y = ((i * .381f) % 1f) * h * .62f
            drawGlowingStar(Offset(x, y), 1.2f + (i % 3) * .5f, art.accent, .8f)
        }
        OrbitFeature.BIG_MOON -> {
            val moon = Offset(w * .17f, h * .74f + drift * h * .004f)
            val moonR = w * .075f
            drawCircle(
                Brush.radialGradient(
                    listOf(lerp(art.planetLight, Color.White, .45f), art.planetLight, art.planetDark),
                    center = moon - Offset(moonR * .3f, moonR * .3f), radius = moonR * 1.4f,
                ),
                moonR, moon,
            )
            drawCircle(Color(0xFF2A1F5E).copy(alpha = .3f), moonR, moon + Offset(moonR * .12f, moonR * .08f))
            drawCircle(art.planetDark.copy(alpha = .5f), moonR * .24f, moon + Offset(-moonR * .26f, -moonR * .14f))
            drawCircle(art.planetDark.copy(alpha = .4f), moonR * .15f, moon + Offset(moonR * .24f, moonR * .28f))
            drawCircle(art.planetDark.copy(alpha = .3f), moonR * .1f, moon + Offset(moonR * .1f, -moonR * .4f))
        }
    }

    for (i in 0 until art.moons) {
        val a = 0.6f + i * 1.9f
        val p = Offset(center.x + cos(a) * r * 1.75f, center.y + sin(a) * r * .68f)
        val mr = r * .13f
        drawCircle(
            Brush.radialGradient(
                listOf(lerp(art.planetLight, Color.White, .5f), art.planetLight, lerp(art.planetDark, Color(0xFF241A52), .4f)),
                center = p - Offset(mr * .35f, mr * .35f), radius = mr * 1.5f,
            ),
            mr, p,
        )
        drawCircle(art.accent.copy(alpha = .18f), mr * 1.9f, p)
    }

    ridges(w, h, art.mountainSeed, drift)
}

/** Three parallax ridges with their own height, phase and tint. */
private fun DrawScope.ridges(w: Float, h: Float, seed: Float, drift: Float) {
    val layers = listOf(
        Triple(0.885f, Color(0xFF4A3A94).copy(alpha = .45f), 0.6f),
        Triple(0.915f, Color(0xFF3B2C79).copy(alpha = .7f), 1.2f),
        Triple(0.95f, Color(0xFF2B1F5E).copy(alpha = .9f), 2.2f),
    )
    layers.forEachIndexed { index, (baseLine, tint, amp) ->
        val path = Path().apply {
            moveTo(-w * .05f, h)
            for (step in 0..28) {
                val x = w * step / 28f
                val y = h * (baseLine - .03f * amp * sin(3.1f * x / w + seed + index) - .02f * amp * sin(7.3f * x / w + seed * 1.7f + index * 2f))
                if (step == 0) lineTo(-w * .05f, y) else lineTo(x, y)
            }
            lineTo(w * 1.05f, h)
            close()
        }
        translate(0f, drift * (index + 1) * h * .002f) { drawPath(path, tint) }
    }
}

private fun DrawScope.ring(art: OrbitArt, center: Offset, r: Float, rx: Float, ry: Float) {
    rotate(art.tiltDegrees, center) {
        drawOval(
            Brush.linearGradient(
                listOf(Color.Transparent, art.accent.copy(alpha = .22f), art.accent.copy(alpha = .6f), art.accent.copy(alpha = .22f), Color.Transparent),
                start = Offset(center.x - r * rx, center.y), end = Offset(center.x + r * rx, center.y),
            ),
            topLeft = Offset(center.x - r * rx, center.y - r * ry),
            size = Size(r * rx * 2f, r * ry * 2f),
            style = Stroke(width = 3.5.dp.toPx()),
        )
        drawOval(
            art.accent.copy(alpha = .4f),
            topLeft = Offset(center.x - r * rx * 1.06f, center.y - r * ry * 1.06f),
            size = Size(r * rx * 2.12f, r * ry * 2.12f),
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}
