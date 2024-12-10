package com.bangkit.batikloka.ui.main.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.local.database.NewsDatabase
import com.bangkit.batikloka.data.model.Batik
import com.bangkit.batikloka.data.model.BatikResponse
import com.bangkit.batikloka.data.remote.api.ApiConfig
import com.bangkit.batikloka.data.remote.response.NewsItem
import com.bangkit.batikloka.data.repository.NewsRepository
import com.bangkit.batikloka.databinding.FragmentHomeBinding
import com.bangkit.batikloka.ui.adapter.BatikAdapter
import com.bangkit.batikloka.ui.adapter.NewsCarouselAdapter
import com.bangkit.batikloka.ui.main.MainActivity
import com.bangkit.batikloka.ui.main.catalog.DetailCatalogActivity
import com.bangkit.batikloka.ui.main.news.viewmodel.NewsViewModelFactory
import com.bangkit.batikloka.utils.PreferencesManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var newsRepository: NewsRepository
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var newsAdapter: NewsCarouselAdapter
    private lateinit var batikAdapter: BatikAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var originalBatikList: List<Batik> = listOf()
    private val autoScrollHandler = Handler(Looper.getMainLooper())
    private lateinit var pagerSnapHelper: PagerSnapHelper
    private var currentNewsPosition = 0

    companion object {
        private const val AUTO_SCROLL_INTERVAL = 5000L
        private const val BATIK_JSON_FILE = "batik.json"
        private const val RANDOM_BATIK_COUNT = 10
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout = binding.swipeRefreshLayout

        initializeComponents()
        setupSwipeRefresh()
        setupUiComponents()
        observeData()
    }

    private fun initializeComponents() {
        preferencesManager = PreferencesManager(requireContext())
        val newsApiService = ApiConfig.getNewsApiService(requireContext(), preferencesManager)
        val newsDao = NewsDatabase.getDatabase(requireContext()).newsDao()
        newsRepository = NewsRepository(newsApiService, newsDao)
        val factory = NewsViewModelFactory(newsRepository)
        homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
    }

    private fun setupUiComponents() {
        setupBatikCatalog()
        setupNewsCarousel()
        setupCatalogNavigation()
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
    }

    private fun setupBatikCatalog() {
        binding.rvCatalog.layoutManager = GridLayoutManager(context, 2)
        loadBatikData()
    }

    private fun setupNewsCarousel() {
        binding.imageCarousel.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        setupNewsCarouselSnap()
    }

    private suspend fun fetchNewsData() {
        withContext(Dispatchers.IO) {
            homeViewModel.fetchNews()
        }
    }

    private fun refreshData() {
        stopAutoScroll()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    homeViewModel.fetchNews()
                }
                withContext(Dispatchers.Main) {
                    loadBatikData()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    handleNewsError(e)
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun observeData() {
        binding.progressBarUpcoming.visibility = View.VISIBLE
        binding.imageCarousel.visibility = View.INVISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeNewsState() }
                launch { observeErrorState() }
                launch { observeRefreshState() }
                launch {
                    kotlinx.coroutines.delay(500)
                    fetchNewsData()
                }
            }
        }
    }

    private suspend fun observeRefreshState() {
        homeViewModel.isRefreshing.collect { isRefreshing ->
            withContext(Dispatchers.Main) {
                swipeRefreshLayout.isRefreshing = isRefreshing
            }
        }
    }

    private suspend fun observeNewsState() {
        homeViewModel.newsState.collect { newsList ->
            withContext(Dispatchers.Main) {
                if (newsList.isNotEmpty()) {
                    val latestNewsList = newsList.take(5)
                    setupNewsAdapter(latestNewsList)
                    binding.textNewsError.visibility = View.GONE
                    binding.progressBarUpcoming.visibility = View.GONE
                    binding.imageCarousel.visibility = View.VISIBLE
                    binding.newsIndicator.visibility = View.VISIBLE
                } else {
                    showEmptyNewsState()
                }
            }
        }
    }

    private suspend fun observeErrorState() {
        homeViewModel.error.collect { errorMessage ->
            withContext(Dispatchers.Main) {
                errorMessage?.let {
                    handleNewsError(Exception(it))
                    binding.progressBarUpcoming.visibility = View.GONE
                }
            }
        }
    }

    private fun setupNewsCarouselSnap() {
        if (binding.imageCarousel.onFlingListener == null) {
            pagerSnapHelper = PagerSnapHelper()
            pagerSnapHelper.attachToRecyclerView(binding.imageCarousel)
        }

        binding.imageCarousel.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val snapView = pagerSnapHelper.findSnapView(layoutManager)

                    snapView?.let {
                        val position = layoutManager.getPosition(it)

                        if (::newsAdapter.isInitialized && newsAdapter.itemCount > 0) {
                            currentNewsPosition = position % newsAdapter.originalItemCount

                            updateNewsIndicator(currentNewsPosition)
                        }
                    }
                }
            }
        })
    }

    private fun handleNewsError(exception: Throwable) {
        binding.imageCarousel.visibility = View.GONE
        binding.newsIndicator.visibility = View.GONE
        binding.textNewsError.visibility = View.VISIBLE
        binding.textNewsError.text = getString(R.string.failed_to_load_news, exception.message)
    }

    private fun showEmptyNewsState() {
        binding.imageCarousel.visibility = View.GONE
        binding.newsIndicator.visibility = View.GONE
        binding.textNewsError.visibility = View.VISIBLE
        binding.textNewsError.text = getString(R.string.no_news_available)
    }

    private fun setupNewsAdapter(newsList: List<NewsItem>) {
        newsAdapter = NewsCarouselAdapter(newsList) {
            stopAutoScroll()
        }
        binding.imageCarousel.adapter = newsAdapter
        binding.newsIndicator.visibility = View.VISIBLE
        setupNewsIndicator(newsList.size)

        if (newsList.size > 1) startAutoScroll()
    }

    private fun loadBatikData() {
        val inputStream = context?.assets?.open(BATIK_JSON_FILE)
        val reader = InputStreamReader(inputStream)
        val batikResponse = Gson().fromJson(reader, BatikResponse::class.java)

        originalBatikList = batikResponse.batik
        setupRandomBatikAdapter()
    }

    private fun setupRandomBatikAdapter() {
        val randomBatikList = originalBatikList.shuffled().take(RANDOM_BATIK_COUNT)
        batikAdapter = BatikAdapter(randomBatikList) { batik ->
            val intent = Intent(requireContext(), DetailCatalogActivity::class.java).apply {
                putExtra("BATIK_DATA", batik)
            }
            startActivity(intent)
        }

        binding.rvCatalog.adapter = batikAdapter
        binding.btnSeeMore.visibility = View.GONE
    }

    private fun setupCatalogNavigation() {
        binding.btnNext.setOnClickListener {
            (activity as? MainActivity)?.navigateToCatalogFragment()
        }

        binding.btnSeeMore.setOnClickListener {
            (activity as? MainActivity)?.navigateToCatalogFragment()
        }
    }

    private fun setupNewsIndicator(totalItems: Int) {
        binding.newsIndicator.removeAllViews()
        repeat(totalItems) { position ->
            val indicator = ImageView(requireContext()).apply {
                setImageResource(
                    if (position == 0) R.drawable.indicator_active
                    else R.drawable.indicator_inactive
                )
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 0, 8, 0)
                }
            }
            binding.newsIndicator.addView(indicator)
        }
    }

    private fun updateNewsIndicator(position: Int) {
        for (i in 0 until binding.newsIndicator.childCount) {
            val indicator = binding.newsIndicator.getChildAt(i) as ImageView
            indicator.setImageResource(
                if (i == position) R.drawable.indicator_active
                else R.drawable.indicator_inactive
            )
        }
    }

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            if (::newsAdapter.isInitialized && newsAdapter.itemCount > 1) {
                val layoutManager = binding.imageCarousel.layoutManager as LinearLayoutManager
                val currentPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                val nextPosition = if (currentPosition < newsAdapter.itemCount - 1) {
                    currentPosition + 1
                } else {
                    0
                }
                val smoothScroller = object : LinearSmoothScroller(context) {
                    override fun getVerticalSnapPreference(): Int = SNAP_TO_START
                    override fun getHorizontalSnapPreference(): Int = SNAP_TO_START
                }
                smoothScroller.targetPosition = nextPosition
                layoutManager.startSmoothScroll(smoothScroller)
                autoScrollHandler.postDelayed(this, AUTO_SCROLL_INTERVAL)
            }
        }
    }

    private fun startAutoScroll() {
        stopAutoScroll()
        autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_INTERVAL)
    }

    private fun stopAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
    }

    override fun onPause() {
        super.onPause()
        stopAutoScroll()
    }

    override fun onResume() {
        super.onResume()
        if (::newsAdapter.isInitialized && newsAdapter.itemCount > 1) {
            startAutoScroll()
        }
    }

    override fun onDestroyView() {
        stopAutoScroll()
        super.onDestroyView()
        _binding = null
    }
}