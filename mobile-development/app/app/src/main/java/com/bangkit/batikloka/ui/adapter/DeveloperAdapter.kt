package com.bangkit.batikloka.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.model.Developer
import com.bumptech.glide.Glide
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
            Glide.with(itemView.context)
                .load(developer.image)
                .placeholder(R.drawable.circle_background)
                .error(R.drawable.circle_background)
                .into(profilePicture)
            developerName.text = developer.name
            learningPath.text = developer.learningPath
            university.text = developer.university

            try {
                val resourceId = itemView.context.resources.getIdentifier(
                    developer.image,
                    "drawable",
                    itemView.context.packageName
                )
                if (resourceId != 0) {
                    profilePicture.setImageResource(resourceId)
                } else {
                    profilePicture.setImageResource(R.drawable.circle_background)
                }
            } catch (e: Exception) {
                profilePicture.setImageResource(R.drawable.circle_background)
            }

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