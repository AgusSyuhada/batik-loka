package com.bangkit.batikloka.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
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

        animateItemEntry(holder.itemView, position)
    }

    private fun animateItemEntry(itemView: View, position: Int) {
        val springAnimation = SpringAnimation(itemView, SpringAnimation.TRANSLATION_Y)
            .setStartValue(-itemView.height.toFloat())
            .setSpring(
                SpringForce()
                    .setFinalPosition(0f)
                    .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
                    .setStiffness(SpringForce.STIFFNESS_LOW)
            )

        itemView.alpha = 0f
        springAnimation.start()

        itemView.animate()
            .alpha(1f)
            .setStartDelay((position * 100).toLong())
            .setDuration(500)
            .start()
    }

    override fun getItemCount() = tourItems.size

    class TourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageViewWelcomeTour)
        private val titleTextView: TextView = itemView.findViewById(R.id.textAboveImage)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.textBelowImage)

        fun bind(item: TourItem) {
            imageView.setImageResource(item.imageResId)
            titleTextView.text = itemView.context.getString(item.titleText)
            descriptionTextView.text = itemView.context.getString(item.descriptionText)
        }
    }
}