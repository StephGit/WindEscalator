package windescalator.alert.detail.chart

import android.graphics.Paint
import android.graphics.PointF

data class Slice (
        val name: String,
        var startAngle: Float,
        var sweepAngle: Float,
        var labelLocation: PointF,
        val paint: Paint,
        var state: SliceState
        )
