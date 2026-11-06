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
import kotlin.math.sqrt

/**
 * The board shell: a lit glass arena floating over the sky. Everything here is presentation — the
 * playfield geometry still comes from the 18x32 logical grid used by the engine.
 */
internal fun DrawScope.drawBoardShell(
    unitX: Float,
    unitY: Float,
    accent: Color,
    dangerRow: Int,
    danger: Boolean,
    reducedMotion: Boolean,
    pulse: Float,
) {
    val w = size.width
    val h = size.height
    val radius = CornerRadius(w * .085f)

    // Bloom above the panel, so the arena reads as lit from the sky.
    drawCircle(
        Brush.radialGradient(listOf(Color.White.copy(alpha = .28f), accent.copy(alpha = .14f), Color.Transparent), center = Offset(w * .5f, h * .04f), radius = w * 1.0f),
        w * 1.0f,
        Offset(w * .5f, h * .04f),
    )

    // A lit glass arena: pale violet at the crown, deeper toward the launch bay, never black.
    drawRoundRect(
        Brush.verticalGradient(
            0f to Color(0xFF9B87E8).copy(alpha = .55f),
            .22f to Color(0xFF6E58C4).copy(alpha = .62f),
            .62f to Color(0xFF4E3C9C).copy(alpha = .72f),
            1f to Color(0xFF3A2B7A).copy(alpha = .82f),
        ),
        topLeft = Offset(0f, 0f),
        size = Size(w, h),
        cornerRadius = radius,
    )

    // Interior dust and a faint scatter of stars.
    val dust = listOf(
        .12f to .18f, .86f to .26f, .24f to .44f, .78f to .55f, .16f to .66f,
        .88f to .72f, .34f to .82f, .66f to .14f, .46f to .34f, .58f to .64f,
    )
    dust.forEachIndexed { index, (fx, fy) ->
        val a = if (index % 3 == 0) .55f else .3f
        drawCircle(Color.White.copy(alpha = a), if (index % 4 == 0) 1.5f else .9f, Offset(w * fx, h * fy))
    }

    // Lit top edge and hairline frame.
    drawRoundRect(
        Brush.verticalGradient(listOf(Color.White.copy(alpha = .22f), Color.Transparent), startY = 0f, endY = h * .12f),
        topLeft = Offset(1f, 1f),
        size = Size(w - 2f, h * .12f),
        cornerRadius = radius,
    )
    drawRoundRect(
        Color.White.copy(alpha = .35f),
        topLeft = Offset(1f, 1f),
        size = Size(w - 2f, h - 2f),
        cornerRadius = radius,
        style = Stroke(width = 1.2.dp.toPx()),
    )

    // Launch bay: a darker recess with a rim of light around the launcher.
    val bayCenter = Offset(w * .5f, h * (29f / 32f))
    drawCircle(
        Brush.radialGradient(listOf(Color(0xFF2A1F5E).copy(alpha = .55f), Color.Transparent), center = bayCenter, radius = unitX * 4.2f),
        unitX * 4.2f,
        bayCenter,
    )
    drawArc(
        color = Color.White.copy(alpha = .22f),
        startAngle = 190f,
        sweepAngle = 160f,
        useCenter = false,
        topLeft = Offset(bayCenter.x - unitX * 3.6f, bayCenter.y - unitX * 2.2f),
        size = Size(unitX * 7.2f, unitX * 4.4f),
        style = Stroke(width = 1.dp.toPx()),
    )

    // Skyline behind the launcher: two ridges, the near one darker.
    val ridge = Path().apply {
        moveTo(0f, h * .9f)
        cubicTo(w * .14f, h * .84f, w * .2f, h * .89f, w * .3f, h * .915f)
        cubicTo(w * .45f, h * .95f, w * .55f, h * .885f, w * .7f, h * .915f)
        cubicTo(w * .84f, h * .945f, w * .9f, h * .84f, w, h * .9f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(ridge, Color(0xFF3B2C79).copy(alpha = .8f))
    val nearRidge = Path().apply {
        moveTo(0f, h * .95f)
        cubicTo(w * .22f, h * .9f, w * .34f, h * .97f, w * .52f, h * .945f)
        cubicTo(w * .72f, h * .92f, w * .84f, h * .975f, w, h * .94f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(nearRidge, Color(0xFF2B1F5E))

    // Danger line: dashed, glowing when the stack is close to it.
    val dangerY = dangerRow.coerceIn(1, 31) * unitY
    val glow = if (danger) (if (reducedMotion) 1f else .55f + .45f * pulse) else .35f
    val dashWidth = unitX * .55f
    var x = 0f
    while (x < w) {
        drawLine(
            if (danger) Palette.rose.copy(alpha = .95f * glow) else Color.White.copy(alpha = .22f),
            Offset(x, dangerY),
            Offset(x + dashWidth, dangerY),
            if (danger) 2.dp.toPx() else 1.dp.toPx(),
            cap = StrokeCap.Round,
        )
        x += dashWidth * 1.7f
    }
    if (danger) {
        drawRect(
            Brush.verticalGradient(
                listOf(Palette.rose.copy(alpha = .16f * glow), Color.Transparent),
                startY = dangerY,
                endY = dangerY + unitY * 2.2f,
            ),
            topLeft = Offset(0f, dangerY),
            size = Size(w, unitY * 2.2f),
        )
    }
}

/**
 * The launcher: a metallic turret with a lit charge ring, a barrel that follows the aim and a
 * soft bloom so it reads as the focal point of the board.
 */
internal fun DrawScope.drawLauncher(
    center: Offset,
    unitX: Float,
    aimRadians: Float,
    accent: Color,
    charge: Float,
) {
    val r = unitX * 1.32f

    drawCircle(
        Brush.radialGradient(listOf(accent.copy(alpha = .35f), Color.Transparent), center = center, radius = r * 2.2f),
        r * 2.2f,
        center,
    )

    rotate(aimRadians * 180f / PI.toFloat(), center) {
        val barrel = Path().apply {
            moveTo(center.x - unitX * .34f, center.y - r * .5f)
            lineTo(center.x - unitX * .16f, center.y - r * 1.62f)
            lineTo(center.x + unitX * .16f, center.y - r * 1.62f)
            lineTo(center.x + unitX * .34f, center.y - r * .5f)
            close()
        }
        drawPath(
            barrel,
            Brush.verticalGradient(
                listOf(lerp(accent, Color.White, .55f), accent, lerp(accent, Color(0xFF2A1F5E), .45f)),
                startY = center.y - r * 1.62f,
                endY = center.y - r * .4f,
            ),
        )
        drawPath(barrel, Color.White.copy(alpha = .4f), style = Stroke(width = 1.dp.toPx()))
    }

    drawCircle(
        Brush.radialGradient(
            listOf(lerp(Palette.raised, Color.White, .45f), Palette.raised, Color(0xFF2A1F5E)),
            center = center - Offset(r * .3f, r * .35f),
            radius = r * 1.5f,
        ),
        r,
        center,
    )
    drawCircle(Palette.panelBottom.copy(alpha = .85f), r * .78f, center)
    drawCircle(
        Brush.radialGradient(listOf(Color(0xFF241A52), Palette.panelTop), center = center, radius = r * .8f),
        r * .74f,
        center,
    )
    drawCircle(accent.copy(alpha = .7f), r, center, style = Stroke(width = 1.4.dp.toPx()))
    drawCircle(lerp(accent, Color.White, .7f).copy(alpha = .45f), r * .78f, center, style = Stroke(width = .8.dp.toPx()))

    drawArc(
        color = accent.copy(alpha = .95f),
        startAngle = -90f,
        sweepAngle = 360f * charge.coerceIn(0f, 1f),
        useCenter = false,
        topLeft = Offset(center.x - r * .93f, center.y - r * .93f),
        size = Size(r * 1.86f, r * 1.86f),
        style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round),
    )
}

/**
 * Aim guide: a glowing ribbon along the traced path, chevrons that travel toward the landing spot
 * and a target ring that breathes when the shot is legal.
 */
internal fun DrawScope.drawAimGuide(
    points: List<Offset>,
    valid: Boolean,
    accent: Color,
    phase: Float,
    destination: Offset?,
    unitX: Float,
) {
    if (points.size < 2) {
        destination?.let { drawTargetRing(it, unitX, valid, accent, phase) }
        return
    }
    val tint = if (valid) accent else Palette.muted
    // Dotted guide, like the arcade originals: round beads that travel toward the landing spot.
    var total = 0f
    for (i in 0 until points.lastIndex) {
        val dx = points[i + 1].x - points[i].x
        val dy = points[i + 1].y - points[i].y
        total += sqrt(dx * dx + dy * dy)
    }
    val spacing = (unitX * .55f).coerceAtLeast(6f)
    val beadRadius = (unitX * .13f).coerceAtLeast(1.6f)
    var travelled = (if (valid) (phase * spacing) else 0f)
    var segment = 0
    var segmentStart = 0f
    while (travelled < total && segment < points.lastIndex) {
        val dx = points[segment + 1].x - points[segment].x
        val dy = points[segment + 1].y - points[segment].y
        val length = sqrt(dx * dx + dy * dy)
        if (length <= 0f) { segment++; continue }
        if (travelled <= segmentStart + length) {
            val t = ((travelled - segmentStart) / length).coerceIn(0f, 1f)
            val x = points[segment].x + dx * t
            val y = points[segment].y + dy * t
            val fade = (1f - (travelled / total) * .55f) * (if (valid) 1f else .5f)
            drawCircle(tint.copy(alpha = .16f * fade), beadRadius * 1.9f, Offset(x, y))
            drawCircle(tint.copy(alpha = .9f * fade), beadRadius, Offset(x, y))
            travelled += spacing
        } else {
            segmentStart += length
            segment++
        }
    }
    if (valid) {
        val count = 5
        for (k in 0 until count) {
            val t = ((phase + k.toFloat() / count) % 1f)
            val p = pointAlong(points, t) ?: continue
            val next = pointAlong(points, (t + .012f).coerceAtMost(1f)) ?: continue
            val dx = next.x - p.x
            val dy = next.y - p.y
            val len = sqrt(dx * dx + dy * dy).coerceAtLeast(.001f)
            val ux = dx / len
            val uy = dy / len
            val size = unitX * .26f
            drawLine(tint.copy(alpha = .7f), Offset(p.x - ux * size, p.y - uy * size), p, 1.8.dp.toPx(), cap = StrokeCap.Round)
            drawLine(tint.copy(alpha = .7f), Offset(p.x - uy * size * .7f, p.y + ux * size * .7f), p, 1.8.dp.toPx(), cap = StrokeCap.Round)
        }
    }
    destination?.let { drawTargetRing(it, unitX, valid, accent, phase) }
}

private fun DrawScope.drawTargetRing(center: Offset, unitX: Float, valid: Boolean, accent: Color, phase: Float) {
    val tint = if (valid) accent else Palette.muted
    val breathe = .9f + .1f * sin(phase * 6.283f)
    if (valid) {
        drawCircle(
            Brush.radialGradient(listOf(tint.copy(alpha = .35f), Color.Transparent), center = center, radius = unitX * 1.5f),
            unitX * 1.5f,
            center,
        )
    }
    drawCircle(tint.copy(alpha = .85f), unitX * 1.02f * breathe, center, style = Stroke(width = 1.6.dp.toPx()))
    drawCircle(tint.copy(alpha = .4f), unitX * 1.28f * breathe, center, style = Stroke(width = 1.dp.toPx()))
    repeat(4) { index ->
        val a = index * PI.toFloat() / 2f + phase * 1.2f
        val inner = unitX * .78f
        val outer = unitX * .95f
        drawLine(
            tint.copy(alpha = .9f),
            Offset(center.x + cos(a) * inner, center.y + sin(a) * inner),
            Offset(center.x + cos(a) * outer, center.y + sin(a) * outer),
            1.6.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
    if (!valid) {
        drawLine(Palette.muted.copy(alpha = .7f), Offset(center.x - unitX * .3f, center.y - unitX * .3f), Offset(center.x + unitX * .3f, center.y + unitX * .3f), 1.2.dp.toPx())
        drawLine(Palette.muted.copy(alpha = .7f), Offset(center.x + unitX * .3f, center.y - unitX * .3f), Offset(center.x - unitX * .3f, center.y + unitX * .3f), 1.2.dp.toPx())
    }
}

/** Point at [fraction] of the polyline length; used by the chevrons and the flying bubble. */
internal fun pointAlong(points: List<Offset>, fraction: Float): Offset? {
    if (points.isEmpty()) return null
    if (points.size == 1) return points.first()
    var total = 0f
    for (i in 0 until points.lastIndex) total += distance(points[i], points[i + 1])
    if (total <= 0f) return points.first()
    var remaining = total * fraction.coerceIn(0f, 1f)
    for (i in 0 until points.lastIndex) {
        val segment = distance(points[i], points[i + 1])
        if (remaining <= segment || i == points.lastIndex - 1) {
            val t = (remaining / segment.coerceAtLeast(.0001f)).coerceIn(0f, 1f)
            return Offset(points[i].x + (points[i + 1].x - points[i].x) * t, points[i].y + (points[i + 1].y - points[i].y) * t)
        }
        remaining -= segment
    }
    return points.last()
}

private fun distance(a: Offset, b: Offset): Float {
    val dx = b.x - a.x
    val dy = b.y - a.y
    return sqrt(dx * dx + dy * dy)
}

/** One particle of a pop burst: direction, speed, size and warmth, in board units. */
internal class BurstParticle(val dx: Float, val dy: Float, val speed: Float, val size: Float, val warm: Boolean)

/**
 * Deterministic particle set for a burst. Generated once per event (not per frame) so the pop
 * animation allocates nothing while it runs.
 */
internal fun burstParticles(seed: Long, count: Int = 14): List<BurstParticle> {
    var state = seed * 6364136223846793005L + 1442695040888963407L
    fun next(): Float {
        state = state * 6364136223846793005L + 1442695040888963407L
        return ((state ushr 11).toFloat() / (1L shl 53).toFloat())
    }
    return List(count) { index ->
        val angle = next() * 6.283f
        BurstParticle(
            dx = cos(angle),
            dy = sin(angle),
            speed = .8f + next() * 2.4f,
            size = .06f + next() * .12f,
            warm = index % 3 == 0,
        )
    }
}

/** Expanding shockwave plus sparks, drawn over the popped bubbles. */
internal fun DrawScope.drawPopBurst(
    particles: List<BurstParticle>,
    origin: Offset,
    progress: Float,
    color: Color,
    unitX: Float,
) {
    if (progress <= 0f || progress >= 1f) return
    val fade = 1f - progress
    drawCircle(
        Brush.radialGradient(listOf(Color.White.copy(alpha = .45f * fade), color.copy(alpha = .22f * fade), Color.Transparent), center = origin, radius = unitX * (1.1f + progress * 2.4f)),
        unitX * (1.1f + progress * 2.4f),
        origin,
    )
    drawCircle(
        color.copy(alpha = .65f * fade),
        unitX * (1.05f + progress * 1.9f),
        origin,
        style = Stroke(width = 1.6.dp.toPx() * fade),
    )
    particles.forEach { particle ->
        val travelled = particle.speed * progress * unitX * 1.6f
        val p = Offset(origin.x + particle.dx * travelled, origin.y + particle.dy * travelled + progress * progress * unitX * .55f)
        val tint = if (particle.warm) Palette.gold else color
        drawCircle(tint.copy(alpha = fade), unitX * particle.size * (1f - progress * .5f), p)
        drawLine(tint.copy(alpha = fade * .45f), p, Offset(p.x - particle.dx * unitX * .3f, p.y - particle.dy * unitX * .3f), 1.2.dp.toPx(), cap = StrokeCap.Round)
    }
}

/** Seated bubble in the launcher gets a subtle ring so the loaded piece is unmistakable. */
internal fun DrawScope.drawSeatedRing(center: Offset, unitX: Float, color: Color, charge: Float) {
    drawCircle(color.copy(alpha = .6f), unitX * .99f, center, style = Stroke(width = 1.2.dp.toPx()))
    drawArc(
        color = lerp(color, Color.White, .6f).copy(alpha = .9f),
        startAngle = -90f,
        sweepAngle = 360f * charge.coerceIn(0f, 1f),
        useCenter = false,
        topLeft = Offset(center.x - unitX * 1.06f, center.y - unitX * 1.06f),
        size = Size(unitX * 2.12f, unitX * 2.12f),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
    )
}

/** Small glass capsule that holds the "next" bubble under the launcher. */
internal fun DrawScope.drawNextCapsule(center: Offset, unitX: Float, accent: Color) {
    translate(center.x, center.y) {
        drawCircle(
            Brush.verticalGradient(listOf(Palette.raised.copy(alpha = .9f), Palette.panelBottom.copy(alpha = .95f))),
            unitX * .82f,
            Offset.Zero,
        )
        drawCircle(accent.copy(alpha = .55f), unitX * .82f, Offset.Zero, style = Stroke(width = 1.dp.toPx()))
    }
}
