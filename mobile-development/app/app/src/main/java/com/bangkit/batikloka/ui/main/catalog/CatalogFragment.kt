package com.bangkit.batikloka.ui.main.catalog

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.model.Batik
import com.bangkit.batikloka.data.model.BatikResponse
import com.bangkit.batikloka.ui.adapter.BatikAdapter
import com.google.gson.Gson
import java.io.InputStreamReader

class CatalogFragment : Fragment() {

    private lateinit var rvCatalog: RecyclerView
    private lateinit var batikAdapter: BatikAdapter
    private var originalBatikList: List<Batik> = listOf()
    private lateinit var tabContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_catalog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCatalog = view.findViewById(R.id.rv_catalog)
        tabContainer = view.findViewById(R.id.tabs_container)
        rvCatalog.layoutManager = GridLayoutManager(context, 2)

        loadBatikData()
        setupCategoryTabs()
    }

    private fun loadBatikData() {
        val jsonFile = "batik.json"
        val inputStream = context?.assets?.open(jsonFile)
        val reader = InputStreamReader(inputStream)
        val batikResponse = Gson().fromJson(reader, BatikResponse::class.java)

        originalBatikList = batikResponse.batik
        batikAdapter = BatikAdapter(originalBatikList)
        rvCatalog.adapter = batikAdapter
    }

    private fun setupCategoryTabs() {
        val categories = listOf("Semua Wilayah") +
                originalBatikList.map { it.category }.distinct().sorted()

        tabContainer.removeAllViews()

        categories.forEachIndexed { index, category ->
            val tabTextView = TextView(requireContext()).apply {
                text = category
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    40.dpToPx()
                ).apply {
                    setMargins(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
                    setPadding(20.dpToPx(), 0.dpToPx(), 20.dpToPx(), 0.dpToPx())
                }
                background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.rounded_tab_background)
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (index == 0) R.color.caramel_gold else R.color.black
                    )
                )

                setOnClickListener {
                    updateTabStyles(index)
                    filterBatikByCategory(category)
                }
            }
            tabContainer.addView(tabTextView)
        }
    }

    private fun updateTabStyles(selectedIndex: Int) {
        for (i in 0 until tabContainer.childCount) {
            val tab = tabContainer.getChildAt(i) as TextView
            if (i == selectedIndex) {
                tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.caramel_gold))
            } else {
                tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }
    }

    private fun filterBatikByCategory(category: String) {
        val filteredList = if (category == "Semua Wilayah") {
            originalBatikList
        } else {
            originalBatikList.filter { it.category == category }
        }

        batikAdapter = BatikAdapter(filteredList)
        rvCatalog.adapter = batikAdapter
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}