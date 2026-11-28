package com.unioffice.oppopad

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class StylusCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var isDrawingEnabled = false

    private val paths = mutableListOf<Pair<Path, Paint>>()
    private var currentPath = Path()
    private var currentColor = Color.BLUE
    private var currentStrokeWidth = 6f
    private var isHighlighter = false
    private var isEraser = false

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    private fun createPaint(): Paint {
        return Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            if (isEraser) {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                strokeWidth = 36f
            } else if (isHighlighter) {
                color = Color.argb(95, Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor))
                strokeWidth = 28f
            } else {
                color = currentColor
                strokeWidth = currentStrokeWidth
            }
        }
    }

    fun setPenMode() {
        isHighlighter = false
        isEraser = false
    }

    fun setHighlighterMode() {
        isHighlighter = true
        isEraser = false
    }

    fun setEraserMode() {
        isHighlighter = false
        isEraser = true
    }

    fun setPenColor(color: Int) {
        currentColor = color
    }

    fun setStrokeWidth(width: Float) {
        currentStrokeWidth = width
    }

    fun undo(): Boolean {
        if (paths.isNotEmpty()) {
            paths.removeAt(paths.size - 1)
            invalidate()
            return true
        }
        return false
    }

    fun clearCanvas() {
        paths.clear()
        currentPath.reset()
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isDrawingEnabled) {
            return false
        }

        val toolType = event.getToolType(0)
        if (toolType == 5 || (toolType == MotionEvent.TOOL_TYPE_FINGER && event.size > 0.25f)) {
            return true
        }

        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath = Path()
                currentPath.moveTo(x, y)
                paths.add(Pair(currentPath, createPaint()))
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                currentPath.lineTo(x, y)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                currentPath.lineTo(x, y)
                invalidate()
                return true
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for ((path, paint) in paths) {
            canvas.drawPath(path, paint)
        }
    }
}
