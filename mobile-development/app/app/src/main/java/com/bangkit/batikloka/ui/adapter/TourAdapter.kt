package com.bangkit.batikloka.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.local.entity.TourItem

class TourAdapter(private val tourItems: List<TourItem>) :
    RecyclerView.Adapter<TourAdapter.TourViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TourViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_welcome_tour, parent, false)
        return TourViewHolder(view)
    }

    override fun onBindViewHolder(holder: TourViewHolder, position: Int) {
        holder.bind(tourItems[position])
    }

    override fun getItemCount() = tourItems.size

    class TourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageViewWelcomeTour)
        private val titleTextView: TextView = itemView.findViewById(R.id.textAboveImage)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.textBelowImage)

        fun bind(item: TourItem) {
            imageView.setImageResource(item.imageResId)
            titleTextView.text = item.titleText
            descriptionTextView.text = item.descriptionText
        }
    }
}