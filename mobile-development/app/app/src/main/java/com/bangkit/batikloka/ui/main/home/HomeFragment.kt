package com.bangkit.batikloka.ui.main.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.model.Batik
import com.bangkit.batikloka.data.model.BatikResponse
import com.bangkit.batikloka.ui.adapter.BatikAdapter
import com.bangkit.batikloka.ui.main.MainActivity
import com.bangkit.batikloka.ui.main.catalog.DetailCatalogActivity
import com.google.gson.Gson
import java.io.InputStreamReader

class HomeFragment : Fragment() {
    private lateinit var rvCatalog: RecyclerView
    private lateinit var btnNext: ImageView
    private lateinit var batikAdapter: BatikAdapter
    private var originalBatikList: List<Batik> = listOf()
    private lateinit var btnSeeMore: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCatalog = view.findViewById(R.id.rv_catalog)
        btnNext = view.findViewById(R.id.btn_next)
        btnSeeMore = view.findViewById(R.id.btn_see_more)

        rvCatalog.layoutManager = GridLayoutManager(context, 2)

        loadBatikData()
        setupCatalogNavigation()
    }

    private fun loadBatikData() {
        val jsonFile = "batik.json"
        val inputStream = context?.assets?.open(jsonFile)
        val reader = InputStreamReader(inputStream)
        val batikResponse = Gson().fromJson(reader, BatikResponse::class.java)

        originalBatikList = batikResponse.batik
        setupRandomBatikAdapter()
    }

    private fun setupRandomBatikAdapter() {
        val randomBatikList = originalBatikList.shuffled().take(10)

        batikAdapter = BatikAdapter(randomBatikList) { batik ->
            val intent = Intent(requireContext(), DetailCatalogActivity::class.java).apply {
                putExtra("BATIK_DATA", batik)
            }
            startActivity(intent)
        }

        rvCatalog.adapter = batikAdapter

        btnSeeMore.visibility = if (randomBatikList.size == 10) View.VISIBLE else View.GONE
    }

    private fun setupCatalogNavigation() {
        btnNext.setOnClickListener {
            (activity as? MainActivity)?.navigateToCatalogFragment()
        }

        btnSeeMore.setOnClickListener {
            (activity as? MainActivity)?.navigateToCatalogFragment()
        }
    }
}