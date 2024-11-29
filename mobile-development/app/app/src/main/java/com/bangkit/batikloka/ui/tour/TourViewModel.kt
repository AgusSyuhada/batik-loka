package com.bangkit.batikloka.ui.tour

import androidx.lifecycle.ViewModel
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.local.entity.TourItem

class TourViewModel : ViewModel() {
    private val _tourItems = listOf(
        TourItem(
            imageResId = R.drawable.tour_slide_1,
            titleText = R.string.tour_slide_1_title,
            descriptionText = R.string.tour_slide_1_description
        ),
        TourItem(
            imageResId = R.drawable.tour_slide_2,
            titleText = R.string.tour_slide_2_title,
            descriptionText = R.string.tour_slide_2_description
        ),
        TourItem(
            imageResId = R.drawable.tour_slide_3,
            titleText = R.string.tour_slide_3_title,
            descriptionText = R.string.tour_slide_3_description
        )
    )

    val tourItems: List<TourItem>
        get() = _tourItems

    fun getTourItemCount(): Int {
        return _tourItems.size
    }

    fun getTourItemAt(index: Int): TourItem? {
        return if (index in _tourItems.indices) {
            _tourItems[index]
        } else {
            null
        }
    }
}