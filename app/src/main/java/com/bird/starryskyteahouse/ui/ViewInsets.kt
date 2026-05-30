package com.bird.starryskyteahouse.ui

import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun View.applySystemBarPadding() {
    val baseLeft = paddingLeft
    val baseTop = paddingTop
    val baseRight = paddingRight
    val baseBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val systemBars: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.setPadding(
            baseLeft + systemBars.left,
            baseTop + systemBars.top,
            baseRight + systemBars.right,
            baseBottom + systemBars.bottom
        )
        windowInsets
    }
}
