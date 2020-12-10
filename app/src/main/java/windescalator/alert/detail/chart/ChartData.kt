package windescalator.alert.detail.chart

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.Log
import windescalator.TAG
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

class ChartData {
    val totalValue = 8 // fixed amount of slices
    val slices = LinkedHashMap<String, Slice>()

    fun add(name: String, color: String? = null) {
        if (slices.containsKey(name)) {
            Log.i(TAG,"Slice is allready in Chart")
        } else {
            color?.let {
                slices[name] = Slice(name, 0f, 0f,
                        PointF(), createPaint(it), SliceState.SELECTED)
            } ?: run {
                slices[name] = Slice(name, 0f, 0f,
                        PointF(), createPaint(null), SliceState.SELECTED)
            }
        }
    }

    private fun createPaint(color: String?): Paint {
        val newPaint = Paint()
        color?.let {
            newPaint.color = Color.parseColor(color)
        } ?: run {
            val randomValue = Random()
            newPaint.color = Color.argb(255, randomValue.nextInt(255),
                    randomValue.nextInt(255), randomValue.nextInt(255))
        }
        newPaint.isAntiAlias = true
        return newPaint
    }
}