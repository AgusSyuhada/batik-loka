package com.bangkit.batikloka.ui.main.news.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.remote.response.NewsItem
import com.bangkit.batikloka.databinding.ActivityDetailNewsBinding
import com.bumptech.glide.Glide

class DetailNewsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailNewsBinding

    companion object {
        const val EXTRA_NEWS_ITEM = "extra_news_item"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupNewsDetails()
        setupToolbarScrollEffect()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }

        binding.toolbarTitle.visibility = View.VISIBLE
    }

    private fun setupNewsDetails() {
        val newsItem = intent.getParcelableExtra<NewsItem>(EXTRA_NEWS_ITEM)
        newsItem?.let {
            displayNewsContent(it)
            setupNewsLink(it)
        } ?: showErrorState()
    }

    private fun displayNewsContent(newsItem: NewsItem) {
        val context = this

        val title = newsItem.judul ?: context.getString(R.string.news_title_unavailable)
        binding.tvNewsTitle.text = title
        binding.toolbarTitle.text = title

        binding.tvNewsDate.text = newsItem.waktu
            ?: context.getString(R.string.news_date_unavailable)

        binding.tvNewsContent.text = newsItem.body
            ?: context.getString(R.string.news_content_unavailable)

        Glide.with(this)
            .load(newsItem.gambar)
            .placeholder(R.drawable.card)
            .error(R.drawable.card)
            .into(binding.ivNewsImage)
    }

    private fun setupNewsLink(newsItem: NewsItem) {
        if (!newsItem.link.isNullOrEmpty()) {
            binding.tvNewsLink.apply {
                visibility = View.VISIBLE
                text = getString(R.string.news_source_format, newsItem.link)
                setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(newsItem.link))
                    startActivity(intent)
                }
            }
        } else {
            binding.tvNewsLink.visibility = View.GONE
        }
    }

    private fun showErrorState() {
        binding.tvNewsTitle.text = getString(R.string.news_error_load)
        binding.tvNewsContent.text = getString(R.string.news_error_description)
    }

    private fun setupToolbarScrollEffect() {
        binding.scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = binding.scrollView.scrollY
            binding.toolbarTitle.visibility =
                if (scrollY > binding.tvNewsTitle.height) View.VISIBLE else View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}