package com.bangkit.batikloka.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.model.Batik
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

class BatikAdapter(
    private val batikList: List<Batik>,
    private val onItemClickListener: (Batik) -> Unit,
) : RecyclerView.Adapter<BatikAdapter.BatikViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BatikViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_catalog, parent, false)
        return BatikViewHolder(view)
    }

    override fun onBindViewHolder(holder: BatikViewHolder, position: Int) {
        val batik = batikList[position]
        holder.bind(batik)

        holder.itemView.setOnClickListener {
            onItemClickListener(batik)
        }
    }

    override fun getItemCount(): Int {
        return batikList.size
    }

    inner class BatikViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvItemTitle: TextView = itemView.findViewById(R.id.tv_item_title)
        private val ivItemImage: ImageView = itemView.findViewById(R.id.iv_item_image)

        fun bind(batik: Batik) {
            Glide.with(itemView.context)
                .load(batik.image)
                .transform(
                    CenterCrop(),
                    RoundedCornersTransformation(
                        16, 0,
                        RoundedCornersTransformation.CornerType.ALL
                    )
                )
                .into(ivItemImage)
            tvItemTitle.text = batik.name
        }
    }
}