package com.bangkit.batikloka.ui.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.remote.response.NewsItem
import com.bangkit.batikloka.databinding.NewsCardBinding
import com.bumptech.glide.Glide

class NewsAdapter : ListAdapter<NewsItem, NewsAdapter.NewsViewHolder>(NewsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = NewsCardBinding.inflate(inflater, parent, false)
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val newsItem = getItem(position)
        holder.bind(newsItem)
    }

    inner class NewsViewHolder(private val binding: NewsCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(newsItem: NewsItem) {
            val context = binding.root.context

            Glide.with(binding.root.context)
                .load(newsItem.gambar)
                .placeholder(R.drawable.card)
                .error(R.drawable.card)
                .into(binding.ivNewsImage)

            binding.tvNewsTitle.text =
                newsItem.judul ?: context.getString(R.string.news_title_default)
            binding.tvNewsDate.text =
                newsItem.waktu ?: context.getString(R.string.news_date_default)

            binding.root.setOnClickListener {
                newsItem.link?.let { link ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    it.context.startActivity(intent)
                }
            }
        }
    }

    class NewsDiffCallback : DiffUtil.ItemCallback<NewsItem>() {
        override fun areItemsTheSame(oldItem: NewsItem, newItem: NewsItem): Boolean {
            return oldItem.link == newItem.link
        }

        override fun areContentsTheSame(oldItem: NewsItem, newItem: NewsItem): Boolean {
            return oldItem == newItem
        }
    }
}