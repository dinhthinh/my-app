package com.id6assistant.wazedispatcher.car

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import com.id6assistant.wazedispatcher.data.WazeSourceStatus
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Draws the Android Auto WazeMod HUD onto the host-provided map Surface within [visibleArea].
 */
class CarHudRenderer {
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_BG }
    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_CARD }
    private val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.argb(28, 255, 255, 255)
    }
    private val warnBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
        color = COLOR_AMBER
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    fun draw(canvas: Canvas, visibleArea: Rect?, model: CarHudUiModel) {
        val area = if (visibleArea != null && !visibleArea.isEmpty) {
            Rect(
                visibleArea.left.coerceIn(0, canvas.width),
                visibleArea.top.coerceIn(0, canvas.height),
                visibleArea.right.coerceIn(0, canvas.width),
                visibleArea.bottom.coerceIn(0, canvas.height),
            )
        } else {
            Rect(0, 0, canvas.width, canvas.height)
        }
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), bgPaint)

        if (area.width() < 60 || area.height() < 60) return

        val pad = min(area.width(), area.height()) * 0.045f
        val left = area.left + pad
        val top = area.top + pad
        val contentW = (area.width() - pad * 2f).coerceAtLeast(80f)
        val contentH = (area.height() - pad * 2f).coerceAtLeast(80f)

        drawHeader(canvas, left, top, contentW, model)

        val bodyTop = top + 56f
        val bodyH = (contentH - 56f).coerceAtLeast(48f)

        when (model.mode) {
            CarHudMode.PERMISSION_REQUIRED -> drawBanner(
                canvas = canvas,
                left = left,
                top = bodyTop,
                width = contentW,
                height = bodyH,
                title = "Cần cấp quyền truy cập WazeMod V14+",
                subtitle = "Vui lòng mở RoadCast ID6 trên điện thoại để xác nhận quyền.",
                titleColor = COLOR_AMBER,
                borderPaint = warnBorderPaint,
            )
            CarHudMode.IDLE_DISCONNECTED -> drawBanner(
                canvas = canvas,
                left = left,
                top = bodyTop,
                width = contentW,
                height = bodyH,
                title = "Chưa kết nối WazeMod",
                subtitle = "Mở toàn màn hình và bấm \"Connect WazeMod\" ở góc trên phải (hoặc bấm Play trên điện thoại).",
                titleColor = COLOR_TEXT_PRIMARY,
                borderPaint = cardBorderPaint,
            )
            CarHudMode.ACTIVE_HUD -> drawActiveHud(canvas, left, bodyTop, contentW, bodyH, model)
        }
    }

    private fun drawHeader(canvas: Canvas, left: Float, top: Float, maxW: Float, model: CarHudUiModel) {
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 24f
        textPaint.color = COLOR_TEXT_PRIMARY
        canvas.drawText("RoadCast ID6", left, top + 22f, textPaint)

        val statusPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 15f
            color = when (model.sourceStatus) {
                WazeSourceStatus.CONNECTED -> COLOR_GREEN
                WazeSourceStatus.PERMISSION_REQUIRED, WazeSourceStatus.REVOKED -> COLOR_AMBER
                else -> COLOR_TEXT_MUTED
            }
        }
        val ellipsized = TextUtils.ellipsize(model.targetLabel, statusPaint, maxW, TextUtils.TruncateAt.END)
        canvas.drawText(ellipsized, 0, ellipsized.length, left, top + 44f, statusPaint)
    }

    private fun drawBanner(
        canvas: Canvas,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        title: String,
        subtitle: String,
        titleColor: Int,
        borderPaint: Paint,
    ) {
        val rect = RectF(left, top, left + width, top + height)
        canvas.drawRoundRect(rect, 20f, 20f, cardPaint)
        canvas.drawRoundRect(rect, 20f, 20f, borderPaint)

        val innerW = (width - 40f).toInt().coerceAtLeast(80)
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 22f
            color = titleColor
        }
        val subtitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.DEFAULT
            textSize = 16f
            color = COLOR_TEXT_SECONDARY
        }

        val titleLayout = StaticLayout.Builder
            .obtain(title, 0, title.length, titlePaint, innerW)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setMaxLines(2)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()
        val subtitleLayout = StaticLayout.Builder
            .obtain(subtitle, 0, subtitle.length, subtitlePaint, innerW)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setMaxLines(3)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()

        val gap = 12f
        val totalH = titleLayout.height + gap + subtitleLayout.height
        val startY = rect.centerY() - totalH / 2f
        val textLeft = rect.left + (width - innerW) / 2f

        canvas.save()
        canvas.translate(textLeft, startY)
        titleLayout.draw(canvas)
        canvas.translate(0f, titleLayout.height + gap)
        subtitleLayout.draw(canvas)
        canvas.restore()
    }

    private fun drawActiveHud(
        canvas: Canvas,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        model: CarHudUiModel,
    ) {
        if (model.avgZone && height >= 190f) {
            val gap = 14f
            val avgH = 74f
            val signsRect = RectF(left, top, left + width, top + height - avgH - gap)
            drawCard(canvas, signsRect)
            drawSignsGrid(canvas, signsRect, model)

            val avgRect = RectF(left, signsRect.bottom + gap, left + width, top + height)
            drawCard(canvas, avgRect)
            drawAvgZoneCard(canvas, avgRect, model)
        } else {
            val signsRect = RectF(left, top, left + width, top + height)
            drawCard(canvas, signsRect)
            drawSignsGrid(canvas, signsRect, model)
        }
    }

    private fun drawCard(canvas: Canvas, rect: RectF) {
        canvas.drawRoundRect(rect, 20f, 20f, cardPaint)
        canvas.drawRoundRect(rect, 20f, 20f, cardBorderPaint)
    }

    private fun drawSignsGrid(canvas: Canvas, rect: RectF, model: CarHudUiModel) {
        val signItems = ArrayList<Pair<Int, Int>>() // kind: 0=max, 1=min, 2=prohibition; value=speed/type
        signItems.add(0 to model.speedLimit)
        if (model.speedMin > 0) {
            signItems.add(1 to model.speedMin)
        }
        model.prohibitionTypes.forEach { type ->
            signItems.add(2 to type)
        }

        val padX = 20f
        val padY = 16f
        val availW = (rect.width() - padX * 2f).coerceAtLeast(80f)
        val availH = (rect.height() - padY * 2f).coerceAtLeast(60f)

        val count = signItems.size
        // Try 1 row first; if too cramped horizontally on split-screen, use 2 rows
        val maxSingleRowR = min(availH * 0.42f, availW / (count * 2.25f)).coerceAtMost(64f)
        val useTwoRows = count > 2 && maxSingleRowR < 34f && availH >= 130f

        val rows: List<List<Pair<Int, Int>>> = if (useTwoRows) {
            val split = (count + 1) / 2
            listOf(signItems.subList(0, split), signItems.subList(split, count))
        } else {
            listOf(signItems)
        }

        val maxCols = rows.maxOf { it.size }
        val rowCount = rows.size
        val signRadius = min(
            (availH / rowCount) * 0.40f,
            (availW / maxCols) * 0.42f,
        ).coerceIn(22f, 68f)

        val stepX = signRadius * 2.32f
        val stepY = signRadius * 2.24f
        val totalGridH = (rowCount - 1) * stepY + signRadius * 2f
        val startCy = rect.centerY() - totalGridH / 2f + signRadius

        rows.forEachIndexed { rowIdx, rowItems ->
            val cy = startCy + rowIdx * stepY
            val rowW = (rowItems.size - 1) * stepX + signRadius * 2f
            var cx = rect.centerX() - rowW / 2f + signRadius
            rowItems.forEach { (kind, value) ->
                when (kind) {
                    0 -> drawSpeedLimitSign(canvas, cx, cy, signRadius, value)
                    1 -> drawMinSpeedSign(canvas, cx, cy, signRadius, value)
                    else -> drawProhibitionSign(canvas, cx, cy, signRadius, value)
                }
                cx += stepX
            }
        }
    }

    private fun drawAvgZoneCard(canvas: Canvas, rect: RectF, model: CarHudUiModel) {
        val innerPad = 18f
        val barW = (rect.width() - innerPad * 2f).coerceAtLeast(80f)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 15f
        textPaint.color = COLOR_AMBER
        canvas.drawText("Vùng đo TB · ${model.avgPercent}%", rect.left + innerPad, rect.top + 26f, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.typeface = Typeface.DEFAULT
        textPaint.textSize = 14f
        textPaint.color = COLOR_TEXT_SECONDARY
        canvas.drawText(
            "Còn ${model.avgRemainingM} m · KN ${model.avgRecommendedSpeed} km/h",
            rect.right - innerPad,
            rect.top + 26f,
            textPaint,
        )

        val barTop = rect.top + 40f
        fillPaint.color = COLOR_TRACK
        canvas.drawRoundRect(RectF(rect.left + innerPad, barTop, rect.left + innerPad + barW, barTop + 10f), 5f, 5f, fillPaint)

        val progW = (barW * model.avgPercent / 100f).coerceAtLeast(10f)
        fillPaint.color = COLOR_AMBER_DARK
        canvas.drawRoundRect(RectF(rect.left + innerPad, barTop, rect.left + innerPad + progW, barTop + 10f), 5f, 5f, fillPaint)
    }

    private fun drawSpeedLimitSign(canvas: Canvas, cx: Float, cy: Float, r: Float, speedLimit: Int) {
        val border = r * 0.17f
        fillPaint.color = Color.WHITE
        canvas.drawCircle(cx, cy, r, fillPaint)

        strokePaint.color = COLOR_SIGN_RED
        strokePaint.strokeWidth = border
        canvas.drawCircle(cx, cy, r - border / 2f, strokePaint)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = r * 0.78f
        textPaint.color = COLOR_SIGN_INK
        val label = if (speedLimit > 0) speedLimit.toString() else "--"
        canvas.drawText(label, cx, cy + textPaint.textSize * 0.34f, textPaint)
    }

    private fun drawMinSpeedSign(canvas: Canvas, cx: Float, cy: Float, r: Float, speedMin: Int) {
        val border = r * 0.15f
        fillPaint.color = COLOR_SIGN_BLUE
        canvas.drawCircle(cx, cy, r, fillPaint)

        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = border
        canvas.drawCircle(cx, cy, r - border / 2f, strokePaint)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = r * 0.78f
        textPaint.color = Color.WHITE
        canvas.drawText(speedMin.toString(), cx, cy + textPaint.textSize * 0.34f, textPaint)
    }

    private fun drawProhibitionSign(canvas: Canvas, cx: Float, cy: Float, r: Float, alertType: Int) {
        val border = r * 0.17f
        val innerR = r - border / 2f
        val u = r * 0.48f
        val arrowW = r * 0.15f

        fillPaint.color = Color.WHITE
        canvas.drawCircle(cx, cy, r, fillPaint)

        strokePaint.color = COLOR_SIGN_INK
        strokePaint.strokeWidth = arrowW
        when (alertType) {
            9 -> {
                drawCar(canvas, cx - u * 0.56f, cy + u * 0.08f, u, COLOR_SIGN_RED)
                drawCar(canvas, cx + u * 0.56f, cy + u * 0.08f, u, COLOR_SIGN_INK)
            }
            28, 72 -> drawTurnArrow(canvas, cx, cy, u, left = true)
            29, 73 -> drawTurnArrow(canvas, cx, cy, u, left = false)
            30, 74 -> drawUTurnArrow(canvas, cx, cy, u, turnLeft = true)
            31 -> drawStraightArrow(canvas, cx, cy, u)
            65 -> {
                drawStraightArrow(canvas, cx, cy, u)
                drawTurnArrow(canvas, cx, cy, u, left = false)
            }
            67 -> {
                drawStraightArrow(canvas, cx, cy, u)
                drawTurnArrow(canvas, cx, cy, u, left = true)
            }
            66, 69 -> {
                drawTurnArrow(canvas, cx, cy, u, left = true)
                drawUTurnArrow(canvas, cx, cy, u * 0.9f, turnLeft = true)
            }
            70, 71 -> {
                drawTurnArrow(canvas, cx, cy, u, left = false)
                drawUTurnArrow(canvas, cx, cy, u * 0.9f, turnLeft = false)
            }
            68, 39 -> {
                drawTurnArrow(canvas, cx, cy, u, left = true)
                drawTurnArrow(canvas, cx, cy, u, left = false)
            }
            else -> canvas.drawLine(cx - u, cy, cx + u, cy, strokePaint)
        }

        val slash = innerR * 0.7071f
        strokePaint.strokeCap = Paint.Cap.BUTT
        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = border * 1.35f
        canvas.drawLine(cx - slash, cy - slash, cx + slash, cy + slash, strokePaint)

        strokePaint.color = COLOR_SIGN_RED
        strokePaint.strokeWidth = border * 0.9f
        canvas.drawLine(cx - slash, cy - slash, cx + slash, cy + slash, strokePaint)

        strokePaint.strokeCap = Paint.Cap.ROUND
        strokePaint.color = COLOR_SIGN_RED
        strokePaint.strokeWidth = border
        canvas.drawCircle(cx, cy, innerR, strokePaint)
    }

    private fun drawCar(canvas: Canvas, centerX: Float, centerY: Float, u: Float, color: Int) {
        fillPaint.color = color
        val bodyW = u * 0.86f
        val bodyH = u * 0.44f
        val cabinW = u * 0.56f
        val cabinH = u * 0.34f
        val wheelW = u * 0.18f
        val wheelH = u * 0.16f
        val cr = u * 0.1f

        canvas.drawRoundRect(
            RectF(centerX - cabinW / 2f, centerY - bodyH / 2f - cabinH * 0.72f, centerX + cabinW / 2f, centerY - bodyH / 2f + cabinH * 0.28f),
            cr, cr, fillPaint,
        )
        canvas.drawRoundRect(
            RectF(centerX - bodyW / 2f, centerY - bodyH / 2f, centerX + bodyW / 2f, centerY + bodyH / 2f),
            cr, cr, fillPaint,
        )
        canvas.drawRoundRect(
            RectF(centerX - bodyW * 0.38f, centerY + bodyH / 2f - wheelH * 0.2f, centerX - bodyW * 0.38f + wheelW, centerY + bodyH / 2f + wheelH * 0.8f),
            u * 0.04f, u * 0.04f, fillPaint,
        )
        canvas.drawRoundRect(
            RectF(centerX + bodyW * 0.38f - wheelW, centerY + bodyH / 2f - wheelH * 0.2f, centerX + bodyW * 0.38f, centerY + bodyH / 2f + wheelH * 0.8f),
            u * 0.04f, u * 0.04f, fillPaint,
        )
    }

    private fun drawStraightArrow(canvas: Canvas, cx: Float, cy: Float, u: Float) {
        val tipY = cy - u * 1.05f
        canvas.drawLine(cx, cy + u * 1.05f, cx, tipY, strokePaint)
        drawArrowHead(canvas, cx, tipY, -Math.PI / 2.0, u * 0.48f)
    }

    private fun drawTurnArrow(canvas: Canvas, cx: Float, cy: Float, u: Float, left: Boolean) {
        val dir = if (left) -1f else 1f
        val path = Path().apply {
            moveTo(cx, cy + u * 1.05f)
            lineTo(cx, cy - u * 0.1f)
            quadTo(cx, cy - u * 0.55f, cx + dir * u * 0.45f, cy - u * 0.55f)
            lineTo(cx + dir * u * 0.95f, cy - u * 0.55f)
        }
        canvas.drawPath(path, strokePaint)
        drawArrowHead(canvas, cx + dir * u * 0.98f, cy - u * 0.55f, if (left) Math.PI else 0.0, u * 0.45f)
    }

    private fun drawUTurnArrow(canvas: Canvas, cx: Float, cy: Float, u: Float, turnLeft: Boolean) {
        val dir = if (turnLeft) -1f else 1f
        val startX = cx - dir * u * 0.48f
        val targetX = cx + dir * u * 0.48f
        val arcCenterY = cy - u * 0.35f
        val arcR = u * 0.48f
        val rect = RectF(cx - arcR, arcCenterY - arcR, cx + arcR, arcCenterY + arcR)
        val path = Path().apply {
            moveTo(startX, cy + u * 1.0f)
            lineTo(startX, arcCenterY)
            if (turnLeft) {
                arcTo(rect, 0f, -180f, false)
            } else {
                arcTo(rect, 180f, 180f, false)
            }
            lineTo(targetX, cy + u * 0.45f)
        }
        canvas.drawPath(path, strokePaint)
        drawArrowHead(canvas, targetX, cy + u * 0.52f, Math.PI / 2.0, u * 0.42f)
    }

    private fun drawArrowHead(canvas: Canvas, tipX: Float, tipY: Float, angleRad: Double, headLen: Float) {
        val spread = Math.toRadians(38.0)
        val lx = tipX - (headLen * cos(angleRad - spread)).toFloat()
        val ly = tipY - (headLen * sin(angleRad - spread)).toFloat()
        val rx = tipX - (headLen * cos(angleRad + spread)).toFloat()
        val ry = tipY - (headLen * sin(angleRad + spread)).toFloat()
        val path = Path().apply {
            moveTo(lx, ly)
            lineTo(tipX, tipY)
            lineTo(rx, ry)
        }
        canvas.drawPath(path, strokePaint)
    }

    private companion object {
        val COLOR_BG = Color.parseColor("#0F172A")
        val COLOR_CARD = Color.parseColor("#1E293B")
        val COLOR_TRACK = Color.parseColor("#334155")
        val COLOR_TEXT_PRIMARY = Color.parseColor("#F8FAFC")
        val COLOR_TEXT_SECONDARY = Color.parseColor("#CBD5E1")
        val COLOR_TEXT_MUTED = Color.parseColor("#94A3B8")
        val COLOR_GREEN = Color.parseColor("#4ADE80")
        val COLOR_AMBER = Color.parseColor("#FBBF24")
        val COLOR_AMBER_DARK = Color.parseColor("#F59E0B")
        val COLOR_SIGN_RED = Color.parseColor("#EF4444")
        val COLOR_SIGN_BLUE = Color.parseColor("#2563EB")
        val COLOR_SIGN_INK = Color.parseColor("#0F172A")
    }
}
