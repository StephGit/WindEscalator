package ch.stephgit.windescalator.wind

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.alert.detail.WindResource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class WindDataAdapter : ListAdapter<WindResource, WindDataAdapter.ViewHolder>(WindResourceDiffCallback()) {

    var onItemClick: ((WindResource) -> Unit)? = null
    private val fmt: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wind_data, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resource = getItem(position)
        holder.bind(resource)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val resourceIcon: ImageView = itemView.findViewById(R.id.iv_wind_resource_icon)
        private val resourceName: TextView = itemView.findViewById(R.id.tv_wind_resource_name)
        private val statusIndicator: ImageView = itemView.findViewById(R.id.iv_wind_status)
        private val windDataLayout: LinearLayout = itemView.findViewById(R.id.ll_wind_data)
        private val windForce: TextView = itemView.findViewById(R.id.tv_wind_force)
        private val windDirection: TextView = itemView.findViewById(R.id.tv_wind_direction)
        private val windTime: TextView = itemView.findViewById(R.id.tv_wind_time)
        private val lastChecked: TextView = itemView.findViewById(R.id.tv_wind_last_checked)
        private val offlineMessage: TextView = itemView.findViewById(R.id.tv_wind_offline)

        fun bind(resource: WindResource) {
            itemView.setOnClickListener { onItemClick?.invoke(resource) }
            resourceName.text = resource.displayName
            resourceIcon.setBackgroundResource(resource.icon)

            statusIndicator.setImageResource(
                if (resource.online) R.drawable.bullet_online else R.drawable.bullet_offline
            )

            if (resource.online && resource.latestForce > 0) {
                windDataLayout.visibility = View.VISIBLE
                offlineMessage.visibility = View.GONE
                windForce.text = itemView.context.getString(R.string.progress_kts, resource.latestForce.toString())
                windDirection.text = resource.latestDirection
                windTime.text = resource.latestTime
            } else if (resource.online) {
                windDataLayout.visibility = View.GONE
                offlineMessage.visibility = View.GONE
            } else {
                windDataLayout.visibility = View.GONE
                offlineMessage.visibility = View.VISIBLE
            }

            if (resource.lastChecked > 0) {
                val formattedTime = Instant.ofEpochMilli(resource.lastChecked)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .format(fmt)
                lastChecked.text = itemView.context.getString(R.string.wind_last_checked, formattedTime)
                lastChecked.visibility = View.VISIBLE
            } else {
                lastChecked.visibility = View.GONE
            }
        }
    }
}

private class WindResourceDiffCallback : DiffUtil.ItemCallback<WindResource>() {
    override fun areItemsTheSame(oldItem: WindResource, newItem: WindResource): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: WindResource, newItem: WindResource): Boolean {
        return oldItem == newItem
    }
}
