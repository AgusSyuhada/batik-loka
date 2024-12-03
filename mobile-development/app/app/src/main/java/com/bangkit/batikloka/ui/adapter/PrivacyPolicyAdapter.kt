package com.bangkit.batikloka.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.batikloka.data.model.PrivacyPolicySection
import com.bangkit.batikloka.databinding.ItemPrivacyPolicySectionBinding

class PrivacyPolicyAdapter(private val sections: List<PrivacyPolicySection>) :
    RecyclerView.Adapter<PrivacyPolicyAdapter.PrivacyPolicyViewHolder>() {

    inner class PrivacyPolicyViewHolder(private val binding: ItemPrivacyPolicySectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(section: PrivacyPolicySection) {
            binding.sectionTitle.text = section.title
            binding.sectionContent.text = section.content
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrivacyPolicyViewHolder {
        val binding = ItemPrivacyPolicySectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PrivacyPolicyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PrivacyPolicyViewHolder, position: Int) {
        holder.bind(sections[position])
    }

    override fun getItemCount() = sections.size
}