package com.bangkit.batikloka.ui.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.remote.response.NewsItem
import com.bumptech.glide.Glide

class NewsCarouselAdapter(
    private val newsList: List<NewsItem>,
    private val onItemClick: (NewsItem) -> Unit
) : RecyclerView.Adapter<NewsCarouselAdapter.NewsViewHolder>() {

    val originalItemCount: Int = newsList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news_carousel, parent, false)
        return NewsViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val actualPosition = position % newsList.size
        val newsItem = newsList[actualPosition]
        holder.bind(newsItem)
    }

    override fun getItemCount(): Int = if (newsList.size > 1) Int.MAX_VALUE else newsList.size

    class NewsViewHolder(
        itemView: View,
        private val onItemClick: (NewsItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val newsImage: ImageView = itemView.findViewById(R.id.iv_news_image)
        private val newsTitle: TextView = itemView.findViewById(R.id.tv_news_title)
        private val newsDate: TextView = itemView.findViewById(R.id.tv_news_date)

        fun bind(newsItem: NewsItem) {
            val context = itemView.context

            newsTitle.text = newsItem.judul ?: context.getString(R.string.news_title_default)
            newsDate.text = newsItem.waktu ?: context.getString(R.string.news_date_default)

            Glide.with(context)
                .load(newsItem.gambar)
                .placeholder(R.drawable.card)
                .error(R.drawable.card)
                .into(newsImage)

            itemView.setOnClickListener {
                newsItem.link?.let { link ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    context.startActivity(intent)
                }
            }
        }
    }
}