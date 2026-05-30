package com.bird.starryskyteahouse.ui

import android.view.View
import com.bird.starryskyteahouse.media.TeaMusic

fun View.setTeaClickListener(action: (View) -> Unit) {
    setOnClickListener { view ->
        TeaMusic.getInstance(view.context).playButtonTap()
        action(view)
    }
}
