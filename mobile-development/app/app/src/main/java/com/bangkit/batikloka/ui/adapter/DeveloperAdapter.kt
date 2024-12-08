package com.bangkit.batikloka.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.model.Developer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import de.hdodenhof.circleimageview.CircleImageView

class DeveloperAdapter(
    private val developers: List<Developer>,
    private val onItemClick: (Developer) -> Unit,
) : RecyclerView.Adapter<DeveloperAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profilePicture: CircleImageView = itemView.findViewById(R.id.profilePicture)
        private val developerName: TextView = itemView.findViewById(R.id.developerName)
        private val learningPath: TextView = itemView.findViewById(R.id.learningPath)
        private val university: TextView = itemView.findViewById(R.id.university)

        fun bind(developer: Developer) {
            val glideOptions = RequestOptions()
                .placeholder(R.drawable.circle_background)
                .error(R.drawable.circle_background)
                .diskCacheStrategy(DiskCacheStrategy.ALL)

            val imageToLoad = when {
                developer.image.isNotEmpty() -> developer.image
                else -> {
                    val resourceId = itemView.context.resources.getIdentifier(
                        developer.image,
                        "drawable",
                        itemView.context.packageName
                    )
                    if (resourceId != 0) resourceId else R.drawable.circle_background
                }
            }

            Glide.with(itemView.context)
                .load(imageToLoad)
                .apply(glideOptions)
                .into(profilePicture)

            developerName.text = developer.name
            learningPath.text = developer.learningPath
            university.text = developer.university

            itemView.setOnClickListener { onItemClick(developer) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_developers_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(developers[position])
    }

    override fun getItemCount() = developers.size
}