package windescalator.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import windescalator.R

class RoundSelectorButton @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle) {
    //the number of slice
    private val mSlices = 8

    //the angle of each slice
    private val degreeStep = 360 / mSlices
    private val quarterDegreeMinus = -22.5 // -45
    private var mOuterRadius = 0f
    private var mInnerRadius = 0f

    //using radius square to prevent square root calculation
    private var outerRadiusSquare = 0f
    private var innerRadiusSquare = 0f
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mSliceOval = RectF()
    private val innerRadiusRatio = 0
    private val quarterCircle = Math.PI / 2


    //color for your slice
    private var DIRECTIONS: Array<String> = arrayOf<String>("E", "SE", "S", "SW", "W", "NW", "N", "NE") // Array of strings, just for the sample
    private val colorText = ResourcesCompat.getColor(resources, R.color.windEscalator_colorWhite, null)
    private val colorSlice = ResourcesCompat.getColor(resources, R.color.windEscalator_colorBrandLight, null)
    private var mCenterX = 0
    private var mCenterY = 0
    private var mOnSliceClickListener: OnSliceClickListener? = null
    private val mTouchSlop: Int
    private var mPressed = false
    private var mLatestDownX = 0f
    private var mLatestDownY = 0f

    interface OnSliceClickListener {
        fun onSlickClick(slicePosition: Int)
    }

    fun setOnSliceClickListener(onSliceClickListener: OnSliceClickListener) {
        mOnSliceClickListener = onSliceClickListener
    }

    public override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mCenterX = w / 2
        mCenterY = h / 2
        mOuterRadius = if (mCenterX > mCenterY) mCenterY.toFloat() else mCenterX.toFloat()
        mInnerRadius = mOuterRadius * innerRadiusRatio
        outerRadiusSquare = mOuterRadius * mOuterRadius
        innerRadiusSquare = mInnerRadius * mInnerRadius
        mSliceOval.left = mCenterX - mOuterRadius
        mSliceOval.right = mCenterX + mOuterRadius
        mSliceOval.top = mCenterY - mOuterRadius
        mSliceOval.bottom = mCenterY + mOuterRadius
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val currX = event.x
        val currY = event.y
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mLatestDownX = currX
                mLatestDownY = currY
                mPressed = true
            }
            MotionEvent.ACTION_MOVE -> if (Math.abs(currX - mLatestDownX) > mTouchSlop || Math.abs(currY - mLatestDownY) > mTouchSlop) mPressed = false
            MotionEvent.ACTION_UP -> if (mPressed) {
                val dx = currX.toInt() - mCenterX
                val dy = currY.toInt() - mCenterY
                val distanceSquare = dx * dx + dy * dy

                //if the distance between touchpoint and centerpoint is smaller than outerRadius and longer than innerRadius, then we're in the clickable area
                if (distanceSquare > innerRadiusSquare && distanceSquare < outerRadiusSquare) {
// TODO angle detection is wrong, only works if slices start at 0 degrees
                    //get the angle to detect which slice is currently being click
                    var angle = Math.atan2(dy.toDouble(), dx.toDouble())
                    if (angle >= -quarterCircle && angle < 0) {
                        angle += quarterCircle
                    } else if (angle >= -Math.PI && angle < -quarterCircle) {
                        angle += Math.PI + Math.PI + quarterCircle
                    } else if (angle >= 0 && angle < Math.PI) {
                        angle += quarterCircle
                    }
                    val rawSliceIndex = angle / (Math.PI * 2) * mSlices
                    if (mOnSliceClickListener != null) {
                        mOnSliceClickListener!!.onSlickClick(rawSliceIndex.toInt())
                    }
                }
            }
        }
        return true
    }

    public override fun onDraw(canvas: Canvas) {
        var startAngle = quarterDegreeMinus

        //draw slice
        for (i in 0 until mSlices) {
            mPaint.style = Paint.Style.FILL
            mPaint.color = colorSlice
            canvas.drawArc(mSliceOval, startAngle.toFloat(), degreeStep.toFloat(), true, mPaint)
            mPaint.style = Paint.Style.STROKE
            mPaint.color = colorText
            canvas.drawArc(mSliceOval, startAngle.toFloat(), degreeStep.toFloat(), true, mPaint)
            mPaint.color = colorText
            val medianAngle: Double = (startAngle.toFloat() + (degreeStep.toFloat() / 2F)) * (Math.PI / 180F)
            mPaint.textSize = 30F
            mPaint.textScaleX = 1.0F
            mPaint.letterSpacing = 0.05F

            mPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(DIRECTIONS[i],
                    mCenterX.toFloat() + ((mOuterRadius - 80) * Math.cos(medianAngle).toFloat()),
                    mCenterY.toFloat() + ((mOuterRadius - 80) * Math.sin(medianAngle).toFloat()),
                    mPaint)
            startAngle += degreeStep
        }
    }

    companion object {
        private const val quarterCircle = Math.PI / 2
    }

    init {
        val viewConfiguration = ViewConfiguration.get(context)
        mTouchSlop = viewConfiguration.scaledTouchSlop
        mPaint.strokeWidth = 6f
    }
}