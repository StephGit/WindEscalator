package ch.stephgit.windescalator.alert

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
import ch.stephgit.windescalator.alert.service.AlarmHandler
import ch.stephgit.windescalator.data.entity.Alert
import com.google.android.material.switchmaterial.SwitchMaterial
import javax.inject.Inject

/*
 view to show alert list, activates alerts in alarmHandler
 */
class AlertRecyclerAdapter  @Inject constructor(
        private val alarmHandler: AlarmHandler) :
        ListAdapter<Alert, AlertRecyclerAdapter.ViewHolder>(AlertDiffCallback()) {


    var onItemClick: ((Alert) -> Unit)? = null
    var onSwitch: ((Alert) -> Unit)? = null
    private lateinit var itemView: View
    private lateinit var parentView: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        parentView = parent
        itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.listitem_alert, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "ALERT_RECYCLER_ADAPTER: add new binding.")
        val currentAlert = getItem(position)
        holder.apply {
            bind(currentAlert)
            itemText.text = currentAlert.name
            switch.isChecked = currentAlert.active
            alarmHandler.addOrUpdate(currentAlert)
        }
    }

    private fun onSwitchChange(alert: Alert) {
        Log.d(TAG, "AlertRecyclerAdapter: switch change $alert")
        if (alert.active) {
            alarmHandler.addOrUpdate(alert)
        } else {
            alarmHandler.removeAlarm(alert)
        }
    }

    fun removeItem(viewHolder: RecyclerView.ViewHolder): Alert {
        val alert = getItem(viewHolder.adapterPosition)
        alarmHandler.removeAlarm(alert)
        return alert;
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemText: TextView = itemView.findViewById(R.id.tv_alertItemText)
        val switch: SwitchMaterial = itemView.findViewById(R.id.sw_alertActive)

        fun bind(alert: Alert) {
            itemView.setOnClickListener {
                onItemClick?.invoke(alert)
            }
            switch.setOnCheckedChangeListener { _, isChecked ->
                alert.active = isChecked
                onSwitchChange(alert)
                onSwitch?.invoke(alert)
            }
        }
    }
}

private class AlertDiffCallback : DiffUtil.ItemCallback<Alert>() {
    override fun areItemsTheSame(oldItem: Alert, newItem: Alert): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Alert, newItem: Alert): Boolean {
        return oldItem.active == newItem.active &&
                oldItem.resource == newItem.resource &&
                oldItem.name == newItem.name &&
                oldItem.id == newItem.id
    }
}