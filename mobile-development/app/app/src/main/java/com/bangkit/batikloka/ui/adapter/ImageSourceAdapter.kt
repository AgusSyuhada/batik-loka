package com.bangkit.batikloka.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bangkit.batikloka.R

class ImageSourceAdapter(
    context: Context,
    private val items: Array<String>,
    private val icons: IntArray
) : ArrayAdapter<String>(context, R.layout.dialog_list_choose_image, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.dialog_list_choose_image, parent, false)

        val iconView: ImageView = view.findViewById(R.id.icon)
        val textView: TextView = view.findViewById(R.id.text)

        iconView.setImageResource(icons[position])
        textView.text = items[position]

        return view
    }
}