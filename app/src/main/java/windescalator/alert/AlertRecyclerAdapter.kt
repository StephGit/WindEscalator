package windescalator.alert

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import windescalator.data.entity.Alert
import javax.inject.Inject
import windescalator.R

class AlertRecyclerAdapter  @Inject constructor(private var alertService: AlertService) :
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

        val currentAlert = getItem(position)
        holder.apply {
            bind(currentAlert)
            itemText.text = currentAlert.name
            switch.isChecked = currentAlert.active
        }
    }

    private fun onSwitchChange(alert: Alert) {
        if (alert.active) {
            alertService.addOrUpdate(alert)
        } else {
            alertService.remove(alert)
        }
    }

    fun removeItem(viewHolder: RecyclerView.ViewHolder): Alert {
        val removedAlert = getItem(viewHolder.adapterPosition)
        alertService.remove(removedAlert)
        return removedAlert
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemText: TextView = itemView.findViewById(R.id.tv_alertItemText)
        val switch: Switch = itemView.findViewById(R.id.sw_alertActive)

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
                oldItem.requestId == newItem.requestId &&
                oldItem.name == newItem.name &&
                oldItem.id == newItem.id
    }
}