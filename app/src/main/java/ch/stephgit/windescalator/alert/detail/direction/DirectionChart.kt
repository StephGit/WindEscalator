package ch.stephgit.windescalator.alert.detail.direction

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import ch.stephgit.windescalator.R

class DirectionChart @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    // Data
    private lateinit var chartData: DirectionChartData

    // Graphics
    private val colorWhite = ResourcesCompat.getColor(resources, R.color.windEscalator_colorWhite, null)
    @SuppressLint("ResourceType")
    private val colorSelected = resources.getString(R.color.windEscalator_colorSelectedLight)
    @SuppressLint("ResourceType")
    private val colorSlice = resources.getString(R.color.windEscalator_colorBrandLight)
    private val borderPaint = Paint()
    private val labelPaint = Paint()
    private val oval = RectF()

    //calc stuff
    private var initialAngle = -22.5f
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

    fun getSelectedData(): List<String> {
        var selection = ArrayList<String>()
        this.chartData.slices.entries.forEach{
            if (it.value.state == SliceState.SELECTED) selection.add(it.key)
        }
        return selection
    }

    /**
     * Updates chartData based on saved alert.directions
     * Slices not in alert.directions are set to unselected state
     */

    fun setData(directions: List<String>) {
        this.chartData.slices.forEach {
            if (!directions.contains(it.key)) {
                updateColorState(it.value, colorSlice, SliceState.UNSELECTED)
            }
        }
    }

    fun setInitialData(dataDirection: DirectionChartData) {
        this.chartData = dataDirection
        setSliceDimensions()
        invalidate()
    }

    private fun setSliceDimensions() {
        var lastAngle = initialAngle // initial angle
        val sweepAngle = 45f
        chartData.slices.forEach {
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
        chartData.slices.get(key)?.let {
            val middleAngle = it.sweepAngle / 2 + it.startAngle
            val distanceToCenter = (3 * layoutParams.height / chartData!!.totalValue)

            it.labelLocation.x = distanceToCenter *
                    Math.cos(Math.toRadians(middleAngle.toDouble())).toFloat() + width / 2
            it.labelLocation.y = distanceToCenter *
                    Math.sin(Math.toRadians(middleAngle.toDouble())).toFloat() +layoutParams.height / 2
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        chartData.slices.let { slices ->
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
        chartData.slices.forEach {
            setLabelLocation(it.key)
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
        borderPaint.strokeWidth = height / 200f
        labelPaint.textSize = height / 20f
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

                //get the theta-angle of polar coordinates
                var angle = Math.atan2(dy.toDouble(), dx.toDouble())
                // calculate degrees to detect slice
                var deg = (angle / Math.PI * 180) + (if (angle > 0) 0f else 360f)

                handleTouchedSlice(deg)
            }
        }
        return super.onTouchEvent(event)
    }

    // TODO add test
    private fun handleTouchedSlice(deg: Double) {
        chartData.slices.forEach {
            // evaluate touched degree with angle of slice statement
            // or for value -22.5 to 0 and 337.5° to 360°
            if ((it.value.startAngle <= deg && (it.value.startAngle + it.value.sweepAngle) > deg) ||
                    (it.value.startAngle == initialAngle && deg <= 360 && deg >= (360 + initialAngle ))) {
                when (it.value.state) {
                    SliceState.UNSELECTED -> updateColorState(it.value, colorSelected, SliceState.SELECTED)
                    SliceState.SELECTED -> updateColorState(it.value, colorSlice, SliceState.UNSELECTED)
                    else -> {}
                }
                return@forEach
            }
        }
    }

    private fun updateColorState(slice: Slice, newColor: String, newState: SliceState) {
        val newPaint = Paint()
        newPaint.color = Color.parseColor(newColor)
        slice.state = newState
        slice.paint = newPaint
        invalidate()
        requestLayout()
    }
}