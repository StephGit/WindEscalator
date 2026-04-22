package ch.stephgit.windescalator.alert

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.TAG
import ch.stephgit.windescalator.alert.detail.direction.Direction
import ch.stephgit.windescalator.alert.detail.direction.DirectionChart
import ch.stephgit.windescalator.alert.detail.direction.DirectionChartData
import com.google.android.material.switchmaterial.SwitchMaterial
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/*
 view to show alert list, activates alerts in alarmHandler
 */
class AlertRecyclerAdapter @Inject constructor() :
    ListAdapter<ch.stephgit.windescalator.data.Alert, AlertRecyclerAdapter.ViewHolder>(AlertDiffCallback()) {


    var onItemClick: ((ch.stephgit.windescalator.data.Alert) -> Unit)? = null
    var onSwitch: ((ch.stephgit.windescalator.data.Alert) -> Unit)? = null
    private var resourceAvailability: Map<Int, Boolean> = emptyMap()
    private lateinit var itemView: View
    private lateinit var parentView: ViewGroup
    private val fmt: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")

    fun updateResourceAvailability(availability: Map<Int, Boolean>) {
        this.resourceAvailability = availability
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        parentView = parent
        itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.listitem_alert, parent, false)
        return ViewHolder(itemView)
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "ALERT_RECYCLER_ADAPTER: add new binding.")
        val alert = getItem(position)

        holder.apply {
            bind(alert)
            initChartData(itemView.context.getString(R.color.windEscalator_colorSelectedLight))
            itemText.text = alert.name
            val now = System.currentTimeMillis()
            if (alert.active && alert.nextRun > now) {
                itemNext.text = "Next: " + Instant.ofEpochMilli(alert.nextRun).atZone(ZoneId.systemDefault()).toLocalDateTime().format(fmt)
            } else if (alert.active) {
                itemNext.text = "Next: active"
            } else {
                itemNext.text = ""
            }
            itemTime.text = alert.startTime + "\n" + alert.endTime
            itemForce.text = alert.windForceKts.toString()
            alert.directions?.let { itemDirs.setData(it) }
            val dataAvailable = resourceAvailability[alert.resource] ?: false
            statusIndicator.setImageResource(
                if (dataAvailable) R.drawable.bullet_online else R.drawable.bullet_offline
            )
            switch.isChecked = alert.active
        }
    }


    fun removeItem(viewHolder: RecyclerView.ViewHolder): ch.stephgit.windescalator.data.Alert {
        return getItem(viewHolder.absoluteAdapterPosition)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemText: TextView = itemView.findViewById(R.id.tv_alertItemText)
        val itemNext: TextView = itemView.findViewById(R.id.tv_alertNextRun)
        val itemTime: TextView = itemView.findViewById(R.id.tv_alertTimeWindow)
        val itemForce: TextView = itemView.findViewById(R.id.tv_alertForce)
        val itemDirs: DirectionChart = itemView.findViewById(R.id.alert_wind_direction)
        val switch: SwitchMaterial = itemView.findViewById(R.id.sw_alertActive)
        val statusIndicator: ImageView = itemView.findViewById(R.id.iv_alertDataStatus)
        private val windDirectionData = DirectionChartData()

        fun bind(alert: ch.stephgit.windescalator.data.Alert) {
            itemView.setOnClickListener {
                onItemClick?.invoke(alert)
            }
            switch.setOnCheckedChangeListener { _, isChecked ->
                alert.active = isChecked
                onSwitch?.invoke(alert)
            }
        }

        fun initChartData(color: String) {
            val directions: Array<Direction> = Direction.values()
            directions.forEach {
                windDirectionData.add(it.name, color)
            }
            itemDirs.setInitialData(windDirectionData, false)
        }

    }
}

private class AlertDiffCallback : DiffUtil.ItemCallback<ch.stephgit.windescalator.data.Alert>() {
    override fun areItemsTheSame(oldItem: ch.stephgit.windescalator.data.Alert, newItem: ch.stephgit.windescalator.data.Alert): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ch.stephgit.windescalator.data.Alert, newItem: ch.stephgit.windescalator.data.Alert): Boolean {
        return oldItem.active == newItem.active &&
                oldItem.resource == newItem.resource &&
                oldItem.nextRun == newItem.nextRun &&
                oldItem.name == newItem.name &&
                oldItem.id == newItem.id &&
                oldItem.startTime == newItem.startTime &&
                oldItem.endTime == newItem.endTime &&
                oldItem.windForceKts == newItem.windForceKts &&
                oldItem.directions == newItem.directions
    }
}
