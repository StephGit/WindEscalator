package ch.stephgit.windescalator.alert.detail.direction

import android.graphics.Paint
import android.graphics.PointF

data class Slice (
        val name: String,
        var startAngle: Float,
        var sweepAngle: Float,
        var labelLocation: PointF,
        var paint: Paint,
        var state: SliceState
        )
