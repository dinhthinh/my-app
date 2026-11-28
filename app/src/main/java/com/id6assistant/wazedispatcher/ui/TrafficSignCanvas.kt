package com.id6assistant.wazedispatcher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

private val SignRed = Color(0xFFEF4444)
private val SignBlue = Color(0xFF2563EB)
private val SignInk = Color(0xFF0F172A)

@Composable
fun SpeedLimitSignCanvas(
    speedLimit: Int,
    size: Dp = 76.dp,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val borderPx = this.size.minDimension * 0.085f
            val radius = (this.size.minDimension - borderPx) / 2f
            drawCircle(color = Color.White, radius = radius)
            drawCircle(
                color = SignRed,
                radius = radius,
                style = Stroke(width = borderPx),
            )
        }
        Text(
            text = if (speedLimit > 0) "$speedLimit" else "--",
            color = SignInk,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
fun MinSpeedSignCanvas(
    speedMin: Int,
    size: Dp = 76.dp,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val borderPx = this.size.minDimension * 0.085f
            val radius = (this.size.minDimension - borderPx) / 2f
            drawCircle(color = SignBlue, radius = radius)
            drawCircle(
                color = Color.White,
                radius = radius,
                style = Stroke(width = borderPx),
            )
        }
        Text(
            text = "$speedMin",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
fun TurnProhibitionSignCanvas(
    alertType: Int,
    size: Dp = 76.dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(size)) {
        val d = this.size.minDimension
        val borderPx = d * 0.085f
        val radius = (d - borderPx) / 2f

        // White background + red outer ring
        drawCircle(color = Color.White, radius = radius)

        // Draw standard traffic sign arrow(s) inside
        val strokePx = d * 0.075f
        val arrowStyle = Stroke(
            width = strokePx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        val cx = d / 2f
        val cy = d / 2f
        val u = d * 0.24f // unit radius inside the sign

        when (alertType) {
            9 -> drawNoPassingCars(cx, cy, u)
            28, 72 -> drawTurnArrow(cx, cy, u, left = true, arrowStyle)
            29, 73 -> drawTurnArrow(cx, cy, u, left = false, arrowStyle)
            30, 74 -> drawUTurnArrow(cx, cy, u, turnLeft = true, arrowStyle)
            31 -> drawStraightArrow(cx, cy, u, arrowStyle)
            65 -> {
                drawStraightArrow(cx, cy, u, arrowStyle)
                drawTurnArrow(cx, cy, u, left = false, arrowStyle)
            }
            67 -> {
                drawStraightArrow(cx, cy, u, arrowStyle)
                drawTurnArrow(cx, cy, u, left = true, arrowStyle)
            }
            66, 69 -> {
                drawTurnArrow(cx, cy, u, left = true, arrowStyle)
                drawUTurnArrow(cx, cy, u * 0.9f, turnLeft = true, arrowStyle)
            }
            70, 71 -> {
                drawTurnArrow(cx, cy, u, left = false, arrowStyle)
                drawUTurnArrow(cx, cy, u * 0.9f, turnLeft = false, arrowStyle)
            }
            68, 39 -> {
                drawTurnArrow(cx, cy, u, left = true, arrowStyle)
                drawTurnArrow(cx, cy, u, left = false, arrowStyle)
            }
            else -> {
                // Fallback generic prohibition bar
                drawLine(
                    color = SignInk,
                    start = Offset(cx - u, cy),
                    end = Offset(cx + u, cy),
                    strokeWidth = strokePx,
                    cap = StrokeCap.Round,
                )
            }
        }

        // Red diagonal prohibition slash with thin white halo for crisp contrast
        val slashOffset = radius * 0.7071f
        val p1 = Offset(cx - slashOffset, cy - slashOffset)
        val p2 = Offset(cx + slashOffset, cy + slashOffset)
        drawLine(
            color = Color.White,
            start = p1,
            end = p2,
            strokeWidth = borderPx * 1.35f,
            cap = StrokeCap.Butt,
        )
        drawLine(
            color = SignRed,
            start = p1,
            end = p2,
            strokeWidth = borderPx * 0.9f,
            cap = StrokeCap.Butt,
        )

        // Red ring on top so slash joins cleanly into the border
        drawCircle(
            color = SignRed,
            radius = radius,
            style = Stroke(width = borderPx),
        )
    }
}

private fun DrawScope.drawNoPassingCars(
    cx: Float,
    cy: Float,
    u: Float,
) {
    drawCarSilhouette(centerX = cx - u * 0.56f, centerY = cy + u * 0.08f, u = u, color = SignRed)
    drawCarSilhouette(centerX = cx + u * 0.56f, centerY = cy + u * 0.08f, u = u, color = SignInk)
}

private fun DrawScope.drawCarSilhouette(
    centerX: Float,
    centerY: Float,
    u: Float,
    color: Color,
) {
    val bodyW = u * 0.86f
    val bodyH = u * 0.44f
    val cabinW = u * 0.56f
    val cabinH = u * 0.34f
    val wheelW = u * 0.18f
    val wheelH = u * 0.16f

    // Cabin (roof)
    drawRoundRect(
        color = color,
        topLeft = Offset(centerX - cabinW / 2f, centerY - bodyH / 2f - cabinH * 0.72f),
        size = Size(cabinW, cabinH),
        cornerRadius = CornerRadius(u * 0.1f, u * 0.1f),
    )
    // Main car body
    drawRoundRect(
        color = color,
        topLeft = Offset(centerX - bodyW / 2f, centerY - bodyH / 2f),
        size = Size(bodyW, bodyH),
        cornerRadius = CornerRadius(u * 0.1f, u * 0.1f),
    )
    // Left & right wheels
    drawRoundRect(
        color = color,
        topLeft = Offset(centerX - bodyW * 0.38f, centerY + bodyH / 2f - wheelH * 0.2f),
        size = Size(wheelW, wheelH),
        cornerRadius = CornerRadius(u * 0.04f, u * 0.04f),
    )
    drawRoundRect(
        color = color,
        topLeft = Offset(centerX + bodyW * 0.38f - wheelW, centerY + bodyH / 2f - wheelH * 0.2f),
        size = Size(wheelW, wheelH),
        cornerRadius = CornerRadius(u * 0.04f, u * 0.04f),
    )
}

private fun DrawScope.drawStraightArrow(
    cx: Float,
    cy: Float,
    u: Float,
    style: Stroke,
) {
    val start = Offset(cx, cy + u * 1.05f)
    val tip = Offset(cx, cy - u * 1.05f)
    drawLine(color = SignInk, start = start, end = tip, strokeWidth = style.width, cap = StrokeCap.Round)
    drawArrowHead(tip = tip, angleRad = -Math.PI / 2.0, headLen = u * 0.48f, strokePx = style.width)
}

private fun DrawScope.drawTurnArrow(
    cx: Float,
    cy: Float,
    u: Float,
    left: Boolean,
    style: Stroke,
) {
    val dir = if (left) -1f else 1f
    val path = Path().apply {
        moveTo(cx, cy + u * 1.05f)
        lineTo(cx, cy - u * 0.1f)
        quadraticTo(cx, cy - u * 0.55f, cx + dir * u * 0.45f, cy - u * 0.55f)
        lineTo(cx + dir * u * 0.95f, cy - u * 0.55f)
    }
    drawPath(path = path, color = SignInk, style = style)
    val tip = Offset(cx + dir * u * 0.98f, cy - u * 0.55f)
    val angle = if (left) Math.PI else 0.0
    drawArrowHead(tip = tip, angleRad = angle, headLen = u * 0.45f, strokePx = style.width)
}

private fun DrawScope.drawUTurnArrow(
    cx: Float,
    cy: Float,
    u: Float,
    turnLeft: Boolean,
    style: Stroke,
) {
    val dir = if (turnLeft) -1f else 1f
    val startX = cx - dir * u * 0.48f
    val targetX = cx + dir * u * 0.48f
    val arcCenterY = cy - u * 0.35f
    val arcR = u * 0.48f
    val rect = Rect(
        left = cx - arcR,
        top = arcCenterY - arcR,
        right = cx + arcR,
        bottom = arcCenterY + arcR,
    )
    val uPath = Path().apply {
        moveTo(startX, cy + u * 1.0f)
        lineTo(startX, arcCenterY)
        if (turnLeft) {
            arcTo(rect = rect, startAngleDegrees = 0f, sweepAngleDegrees = -180f, forceMoveTo = false)
        } else {
            arcTo(rect = rect, startAngleDegrees = 180f, sweepAngleDegrees = 180f, forceMoveTo = false)
        }
        lineTo(targetX, cy + u * 0.45f)
    }
    drawPath(path = uPath, color = SignInk, style = style)
    drawArrowHead(
        tip = Offset(targetX, cy + u * 0.52f),
        angleRad = Math.PI / 2.0,
        headLen = u * 0.42f,
        strokePx = style.width,
    )
}

private fun DrawScope.drawArrowHead(
    tip: Offset,
    angleRad: Double,
    headLen: Float,
    strokePx: Float,
) {
    val spread = Math.toRadians(38.0)
    val leftWing = Offset(
        x = tip.x - (headLen * cos(angleRad - spread)).toFloat(),
        y = tip.y - (headLen * sin(angleRad - spread)).toFloat(),
    )
    val rightWing = Offset(
        x = tip.x - (headLen * cos(angleRad + spread)).toFloat(),
        y = tip.y - (headLen * sin(angleRad + spread)).toFloat(),
    )
    val headPath = Path().apply {
        moveTo(leftWing.x, leftWing.y)
        lineTo(tip.x, tip.y)
        lineTo(rightWing.x, rightWing.y)
    }
    drawPath(
        path = headPath,
        color = SignInk,
        style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
}
