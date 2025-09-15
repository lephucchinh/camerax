package com.example.camerax.utils

import android.view.View

fun View.setSafeClickListener(interval: Long = 500, onSafeClick: (View) -> Unit) {
    var lastTime = 0L
    setOnClickListener {
        val now = System.currentTimeMillis()
        if (now - lastTime >= interval) {
            lastTime = now
            onSafeClick(it)
        }
    }
}
