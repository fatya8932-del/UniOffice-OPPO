package com.unioffice.oppopad

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class StylusCanvasView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private data class Stroke(val path: Path, val paint: Paint)

    private val strokes = mutableListOf<Stroke>()
    private var currentPath: Path? = null
    private var currentColor = Color.BLUE
    private var currentStrokeWidth = 5f
    var isDrawingEnabled = false

    private fun createPaint(color: Int, width: Float): Paint {
        return Paint().apply {
            this.color = color
            isAntiAlias = true
            strokeWidth = width
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
    }

    fun setPenColor(color: Int) {
        currentColor = color
    }

    fun clearCanvas() {
        strokes.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (stroke in strokes) {
            canvas.drawPath(stroke.path, stroke.paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isDrawingEnabled) return false

        // ระบบกันมือโดนจอ (Palm Rejection): รับเฉพาะสัญญาณจากปากกาสไตลัส หรือนิ้วเดียว
        val toolType = event.getToolType(0)
       if (toolType == 5 || (toolType == MotionEvent.TOOL_TYPE_FINGER && event.size > 0.15f)) {

            return false
        }

        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val newPath = Path().apply { moveTo(x, y) }
                val pressureWidth = if (event.pressure > 0) currentStrokeWidth * (0.5f + event.pressure) else currentStrokeWidth
                strokes.add(Stroke(newPath, createPaint(currentColor, pressureWidth)))
                currentPath = newPath
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                currentPath?.lineTo(x, y)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                currentPath = null
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
