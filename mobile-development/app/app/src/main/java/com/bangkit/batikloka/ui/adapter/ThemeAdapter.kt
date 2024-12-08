package com.bangkit.batikloka.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.bangkit.batikloka.R

class ThemeAdapter(
    context: Context,
    private val theme: Array<String>
) : ArrayAdapter<String>(context, R.layout.dialog_choose, theme) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.dialog_choose, parent, false)

        val textView: TextView = view.findViewById(R.id.text)

        textView.text = theme[position]

        return view
    }
}