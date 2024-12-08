package com.bangkit.batikloka.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.local.entity.ScanHistoryEntity
import com.bangkit.batikloka.databinding.ItemHistoryBinding
import com.bangkit.batikloka.utils.HistoryDiffCallback
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryScanAdapter(
    private val context: Context,
    private val onDeleteClick: (ScanHistoryEntity) -> Unit,
    private val onItemClick: (ScanHistoryEntity) -> Unit
) : ListAdapter<ScanHistoryEntity, HistoryScanAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ScanHistoryEntity) {
            with(binding) {
                itemDate.text = SimpleDateFormat(
                    "dd MMM yyyy HH:mm",
                    Locale.getDefault()
                ).format(item.scanDate)

                itemLabel.text = item.label
                itemProbability.text = String.format(
                    context.getString(R.string.probability),
                    item.probability * 100
                )

                Glide.with(context)
                    .load(File(item.imagePath))
                    .transform(
                        CenterCrop(),
                        RoundedCornersTransformation(
                            16, 0,
                            RoundedCornersTransformation.CornerType.ALL
                        )
                    )
                    .into(itemImage)

                buttonDelete.setOnClickListener {
                    onDeleteClick(item)
                }

                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }
}