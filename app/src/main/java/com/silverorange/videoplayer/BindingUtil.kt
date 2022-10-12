package com.silverorange.videoplayer

import android.widget.TextView
import androidx.databinding.BindingAdapter
import io.noties.markwon.Markwon

@BindingAdapter("bind:markwon", "bind:text")
fun bindMarkdown(textView: TextView, markwon: Markwon, text: String) {
    markwon.setMarkdown(textView, text)
}
