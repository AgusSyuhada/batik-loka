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
    private val batikList: MutableList<Batik>,
    private val originalBatikList: List<Batik>? = null,
    private val onItemClick: (Batik) -> Unit,
    private val initialLoadCount: Int = RANDOM_BATIK_COUNT
) : RecyclerView.Adapter<BatikAdapter.BatikViewHolder>() {

    companion object {
        private const val RANDOM_BATIK_COUNT = 10
    }

    val currentList: List<Batik>
        get() = batikList

    private var loadedBatikSet = mutableSetOf<Batik>()

    constructor(
        batikList: List<Batik>,
        onItemClick: (Batik) -> Unit
    ) : this(batikList.toMutableList(), null, onItemClick)

    init {
        loadedBatikSet.addAll(batikList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BatikViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_catalog, parent, false)
        return BatikViewHolder(view)
    }

    override fun onBindViewHolder(holder: BatikViewHolder, position: Int) {
        val batik = batikList[position]
        holder.bind(batik)

        holder.itemView.setOnClickListener {
            onItemClick(batik)
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
                .placeholder(R.drawable.card)
                .error(R.drawable.card)
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

    fun addMoreBatik(newBatikList: List<Batik>) {
        val filteredNewBatikList = newBatikList.filter { !loadedBatikSet.contains(it) }

        if (filteredNewBatikList.isNotEmpty()) {
            val currentSize = batikList.size
            batikList.addAll(filteredNewBatikList)
            loadedBatikSet.addAll(filteredNewBatikList)
            notifyItemRangeInserted(currentSize, filteredNewBatikList.size)
        }
    }

    fun resetToInitialState() {
        val initialList = originalBatikList
            ?.shuffled()
            ?.take(initialLoadCount)
            ?: emptyList()

        batikList.clear()
        loadedBatikSet.clear()

        batikList.addAll(initialList)
        loadedBatikSet.addAll(initialList)

        notifyDataSetChanged()
    }
}