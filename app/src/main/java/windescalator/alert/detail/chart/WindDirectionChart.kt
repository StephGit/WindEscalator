package windescalator.alert.detail.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import windescalator.R

class WindDirectionChart @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    // Data
    private var data: ChartData? = null

    // Graphics
    private val colorWhite = ResourcesCompat.getColor(resources, R.color.windEscalator_colorWhite, null)
    private val colorSelected = ResourcesCompat.getColor(resources, R.color.windEscalator_colorSelected, null)
    private val colorSlice = ResourcesCompat.getColor(resources, R.color.windEscalator_colorBrandLight, null)
    private val borderPaint = Paint()
    private val labelPaint = Paint()
    private val oval = RectF()

    //calc stuff
    private var centerX = 0
    private var centerY = 0
    private var outerRadius = 0f
    private var outerRadiusSquare = 0f

    init {
        borderPaint.apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            color = colorWhite
        }
        labelPaint.apply {
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            color = colorWhite
        }
    }

    fun setData(data: ChartData) {
        this.data = data
        setSliceDimensions()
        invalidate()
    }

    private fun setSliceDimensions() {
        var lastAngle = -22.5f
        val sweepAngle = 45F
        data?.slices?.forEach {
            it.value.startAngle = lastAngle
            it.value.sweepAngle = sweepAngle
            lastAngle += it.value.sweepAngle

            setLabelLocation(it.key)
        }
    }

    /**
     * Use the angle between the start and sweep angles to help get position of the label
     * formula for x pos: (length of line) * cos(middleAngle) + (distance from left edge of screen)
     * formula for y pos: (length of line) * sin(middleAngle) + (distance from top edge of screen)
     *
     * @param key key of chart slice being altered
     */
    private fun setLabelLocation(key: String) {
        data?.slices?.get(key)?.let {
            val middleAngle = it.sweepAngle / 2 + it.startAngle
            val distanceToCenter = (3 * layoutParams.height / 8)

            it.labelLocation.x = distanceToCenter *
                    Math.cos(Math.toRadians(middleAngle.toDouble())).toFloat() + width / 2
            it.labelLocation.y = distanceToCenter *
                    Math.sin(Math.toRadians(middleAngle.toDouble())).toFloat() +layoutParams.height / 2
        }
    }

    private fun setChartBounds(
            top: Float = 0f, bottom: Float = layoutParams.height.toFloat(),
            left: Float = (width / 2) - (layoutParams.height / 2).toFloat(),
            right: Float = (width / 2) + (layoutParams.height / 2).toFloat()
    ) {
        oval.top = top
        oval.bottom = bottom
        oval.left = left
        oval.right = right
        centerX = width / 2
        centerY = height / 2
        outerRadius = if (centerX > centerY) centerY.toFloat() else centerX.toFloat()
        outerRadiusSquare = outerRadius * outerRadius
    }

    private fun setGraphicSizes() {
        borderPaint.strokeWidth = height / 100f
        labelPaint.textSize = height / 20f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        data?.slices?.let { slices ->
            slices.forEach {
                canvas?.drawArc(oval, it.value.startAngle, it.value.sweepAngle, true, it.value.paint)
                canvas?.drawArc(oval, it.value.startAngle, it.value.sweepAngle, true, borderPaint)
                drawLabels(canvas, it.value)
            }
        }
    }

    private fun drawLabels(canvas: Canvas?, slice: Slice) {
        canvas?.drawText(slice.name, slice.labelLocation.x, slice.labelLocation.y, labelPaint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setChartBounds()
        setGraphicSizes()
        data?.slices?.forEach {
            setLabelLocation(it.key)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val currX = event?.x
        val currY = event?.y
        if (event?.action == MotionEvent.ACTION_DOWN) return true
        if (event?.action == MotionEvent.ACTION_UP) {
            val dx = currX!!.toInt() - centerX
            val dy = currY!!.toInt() - centerY
            val distanceSquare = dx * dx + dy * dy
            //if the distance between touchpoint and centerpoint is smaller than outerRadius and
            // longer than innerRadius, then we're in the clickable area
            if (distanceSquare > 0 && distanceSquare < outerRadiusSquare) {

                //get the angle to detect which slice is currently being click
                var angle = Math.atan2(dy.toDouble(), dx.toDouble())
                var deg = (angle / Math.PI * 180) + (if (angle > 0) 0f else 360f)

                setSliceState(deg)
            }
        }
        return super.onTouchEvent(event)
    }

    private fun setSliceState(deg: Double) {
        data?.slices?.forEach {
            // FIXME map value -22.5 to 22.5 for 337.5° to 22.5°
            if (it.value.startAngle < deg && (it.value.startAngle + it.value.sweepAngle) > deg) {
                println("DIRECTION TOUCHED: " + it.value.name)
                // TODO set state and color (createPaint) of slice
            }
        }

    }
}