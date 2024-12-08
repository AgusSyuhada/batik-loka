package com.bangkit.batikloka.ui.main.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.remote.api.ApiConfig
import com.bangkit.batikloka.data.remote.response.NewsItem
import com.bangkit.batikloka.data.repository.NewsRepository
import com.bangkit.batikloka.databinding.FragmentNewsBinding
import com.bangkit.batikloka.ui.adapter.NewsAdapter
import com.bangkit.batikloka.ui.main.news.viewmodel.NewsViewModelFactory
import com.bangkit.batikloka.utils.PreferencesManager
import kotlinx.coroutines.launch

class NewsFragment : Fragment() {
    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var newsRepository: NewsRepository
    private lateinit var newsViewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDependencies()
        setupRecyclerView()
        observeViewModel()
        newsViewModel.fetchNews()
    }

    private fun setupDependencies() {
        preferencesManager = PreferencesManager(requireContext())
        val newsApiService = ApiConfig.getNewsApiService(requireContext(), preferencesManager)
        newsRepository = NewsRepository(newsApiService)
        val factory = NewsViewModelFactory(newsRepository)

        newsViewModel = ViewModelProvider(this, factory)[NewsViewModel::class.java]
        newsAdapter = NewsAdapter()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewNews.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = newsAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    newsViewModel.newsState.collect { newsList ->
                        handleNewsState(newsList)
                    }
                }

                launch {
                    newsViewModel.isLoading.collect { isLoading ->
                        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    }
                }

                launch {
                    newsViewModel.error.collect { error ->
                        handleErrorState(error)
                    }
                }
            }
        }
    }

    private fun handleNewsState(newsList: List<NewsItem>) {
        if (newsList.isEmpty()) {
            showEmptyState()
        } else {
            hideErrorState()
            newsAdapter.submitList(newsList)
        }
    }

    private fun handleErrorState(error: String?) {
        if (error != null) {
            showErrorState(error)
        } else {
            hideErrorState()
        }
    }

    private fun showErrorState(errorMessage: String) {
        binding.textNewsError.apply {
            text = errorMessage
            visibility = View.VISIBLE
        }
        binding.recyclerViewNews.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.textNewsError.apply {
            text = getString(R.string.no_news_available)
            visibility = View.VISIBLE
        }
        binding.recyclerViewNews.visibility = View.GONE
    }

    private fun hideErrorState() {
        binding.textNewsError.visibility = View.GONE
        binding.recyclerViewNews.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}