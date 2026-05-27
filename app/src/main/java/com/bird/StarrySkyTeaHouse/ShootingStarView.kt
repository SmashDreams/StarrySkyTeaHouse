package com.bird.StarrySkyTeaHouse

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.max

class ShootingStarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val trailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 3f }
    private var progress = 0f
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 5200L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener { animation ->
            progress = animation.animatedValue as Float
            invalidate()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode && !animator.isStarted) animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return
        drawStar(canvas, progress, 0.18f, 0.16f, 130f, 0.85f)
        drawStar(canvas, (progress + 0.38f) % 1f, 0.72f, 0.08f, 96f, 0.55f)
        drawStar(canvas, (progress + 0.68f) % 1f, 0.48f, 0.33f, 112f, 0.65f)
    }

    private fun drawStar(
        canvas: Canvas,
        phase: Float,
        startXRatio: Float,
        startYRatio: Float,
        length: Float,
        alphaScale: Float
    ) {
        val travel = max(width, height).toFloat() * 0.72f
        val x = width * startXRatio + travel * phase
        val y = height * startYRatio + travel * 0.34f * phase
        val tailX = x - length
        val tailY = y - length * 0.34f
        val fade = if (phase < 0.12f) phase / 0.12f else if (phase > 0.88f) (1f - phase) / 0.12f else 1f
        val alpha = (210 * fade * alphaScale).toInt().coerceIn(0, 210)
        trailPaint.shader = LinearGradient(
            tailX, tailY, x, y,
            Color.argb(0, 244, 200, 106),
            Color.argb(alpha, 244, 200, 106),
            Shader.TileMode.CLAMP
        )
        canvas.drawLine(tailX, tailY, x, y, trailPaint)
        trailPaint.shader = null
        starPaint.color = Color.argb(alpha, 255, 246, 190)
        canvas.drawCircle(x, y, 4.2f, starPaint)
        starPaint.color = Color.argb((alpha * 0.55f).toInt(), 255, 246, 190)
        canvas.drawCircle(x, y, 9.5f, starPaint)
    }
}
