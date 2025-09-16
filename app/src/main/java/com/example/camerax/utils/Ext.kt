package com.example.camerax.utils

import android.graphics.Bitmap
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

fun Bitmap.cropToRatio(x: Int, y: Int): Bitmap {
    val targetRatio = x.toFloat() / y.toFloat()
    val currentRatio = width.toFloat() / height.toFloat()

    var newWidth = width
    var newHeight = height
    var offsetX = 0
    var offsetY = 0

    if (currentRatio > targetRatio) {
        // ảnh quá ngang -> cắt bớt 2 bên
        newWidth = (height * targetRatio).toInt()
        offsetX = (width - newWidth) / 2
    } else {
        // ảnh quá dọc -> cắt bớt trên/dưới
        newHeight = (width / targetRatio).toInt()
        offsetY = (height - newHeight) / 2
    }

    return Bitmap.createBitmap(this, offsetX, offsetY, newWidth, newHeight)
}
