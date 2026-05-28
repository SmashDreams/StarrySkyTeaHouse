package com.bird.StarrySkyTeaHouse.ui

import android.view.View
import com.bird.StarrySkyTeaHouse.media.TeaMusic

fun View.setTeaClickListener(action: (View) -> Unit) {
    setOnClickListener { view ->
        TeaMusic.getInstance(view.context).playButtonTap()
        action(view)
    }
}
