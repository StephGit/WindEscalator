package ch.stephgit.windescalator.alert

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.TAG
import ch.stephgit.windescalator.alert.detail.direction.Direction
import ch.stephgit.windescalator.alert.detail.direction.DirectionChart
import ch.stephgit.windescalator.alert.detail.direction.DirectionChartData
import ch.stephgit.windescalator.alert.service.AlarmHandler
import com.google.android.material.switchmaterial.SwitchMaterial
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/*
 view to show alert list, activates alerts in alarmHandler
 */
class AlertRecyclerAdapter @Inject constructor(

    private val alarmHandler: AlarmHandler
) :
    ListAdapter<ch.stephgit.windescalator.data.FbAlert, AlertRecyclerAdapter.ViewHolder>(AlertDiffCallback()) {


    var onItemClick: ((ch.stephgit.windescalator.data.FbAlert) -> Unit)? = null
    var onSwitch: ((ch.stephgit.windescalator.data.FbAlert) -> Unit)? = null
    private lateinit var itemView: View
    private lateinit var parentView: ViewGroup
    private val fmt: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")

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
            alert.nextRun?.let {
                itemNext.text = "Next: " + Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime().format(fmt) };
            itemTime.text = alert.startTime + "\n" + alert.endTime
            itemForce.text = alert.windForceKts.toString()
            alert.directions?.let { itemDirs.setData(it) }
            switch.isChecked = alert.active
//            if (alert.active) alarmHandler.addOrUpdate(alert) //TODO due to db-updates on nextRun RecyclerView always creates a new view binding this leads to a never ending cycle
        }
    }

    private fun onSwitchChange(alert: ch.stephgit.windescalator.data.FbAlert) {
        Log.d(TAG, "AlertRecyclerAdapter: switch change $alert")

        if (alert.active) {
            //TODO Firebase Alert
//            alarmHandler.addOrUpdate(alert)
        } else {
            //TODO Firebase Alert
//            alarmHandler.removeAlarm(alert.id!!, false)
        }
    }

    fun removeItem(viewHolder: RecyclerView.ViewHolder): ch.stephgit.windescalator.data.FbAlert {
        val alert = getItem(viewHolder.absoluteAdapterPosition)
        //TODO Firebase Alert
//        alarmHandler.removeAlarm(alert.id!!, false)
        return alert;
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemText: TextView = itemView.findViewById(R.id.tv_alertItemText)
        val itemNext: TextView = itemView.findViewById(R.id.tv_alertNextRun)
        val itemTime: TextView = itemView.findViewById(R.id.tv_alertTimeWindow)
        val itemForce: TextView = itemView.findViewById(R.id.tv_alertForce)
        val itemDirs: DirectionChart = itemView.findViewById(R.id.alert_wind_direction)
        val switch: SwitchMaterial = itemView.findViewById(R.id.sw_alertActive)
        private val windDirectionData = DirectionChartData()

        fun bind(alert: ch.stephgit.windescalator.data.FbAlert) {
            itemView.setOnClickListener {
                onItemClick?.invoke(alert)
            }
            switch.setOnCheckedChangeListener { _, isChecked ->
                alert.active = isChecked
                onSwitchChange(alert)
                onSwitch?.invoke(alert)
            }

            switch.setOnClickListener { click ->
                Log.d(TAG, "AlertRecyclerAdapter: switch clicked")
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

private class AlertDiffCallback : DiffUtil.ItemCallback<ch.stephgit.windescalator.data.FbAlert>() {
    override fun areItemsTheSame(oldItem: ch.stephgit.windescalator.data.FbAlert, newItem: ch.stephgit.windescalator.data.FbAlert): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ch.stephgit.windescalator.data.FbAlert, newItem: ch.stephgit.windescalator.data.FbAlert): Boolean {
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