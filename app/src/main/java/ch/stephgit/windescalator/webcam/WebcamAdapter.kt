package ch.stephgit.windescalator.webcam

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.alert.detail.WindResource
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class WebcamAdapter : ListAdapter<WindResource, WebcamAdapter.ViewHolder>(WebcamDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_webcam, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resource = getItem(position)
        holder.bind(resource)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val resourceIcon: ImageView = itemView.findViewById(R.id.iv_webcam_resource_icon)
        private val resourceName: TextView = itemView.findViewById(R.id.tv_webcam_resource_name)
        private val openBrowser: ImageView = itemView.findViewById(R.id.iv_webcam_open_browser)
        private val webcamImage: ImageView = itemView.findViewById(R.id.iv_webcam_image)
        private val noUrlMessage: TextView = itemView.findViewById(R.id.tv_webcam_no_url)

        fun bind(resource: WindResource) {
            resourceName.text = resource.displayName
            resourceIcon.setBackgroundResource(resource.icon)

            if (resource.webcamUrl.isNotBlank()) {
                webcamImage.visibility = View.VISIBLE
                noUrlMessage.visibility = View.GONE

                Glide.with(itemView.context)
                    .load(resource.webcamUrl)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.ic_baseline_videocam_24)
                    .error(R.drawable.ic_baseline_videocam_24)
                    .into(webcamImage)

                openBrowser.visibility = View.VISIBLE
                openBrowser.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resource.webcamUrl))
                    itemView.context.startActivity(intent)
                }

                webcamImage.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resource.webcamUrl))
                    itemView.context.startActivity(intent)
                }
            } else {
                webcamImage.visibility = View.GONE
                noUrlMessage.visibility = View.VISIBLE
                openBrowser.visibility = View.GONE
            }
        }
    }
}

private class WebcamDiffCallback : DiffUtil.ItemCallback<WindResource>() {
    override fun areItemsTheSame(oldItem: WindResource, newItem: WindResource): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: WindResource, newItem: WindResource): Boolean {
        return oldItem == newItem
    }
}
