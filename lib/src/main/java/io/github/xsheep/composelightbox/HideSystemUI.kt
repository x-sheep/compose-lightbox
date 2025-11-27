package io.github.xsheep.composelightbox

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.DisposableEffectResult
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

private val noop = object : DisposableEffectResult {
    override fun dispose() {}
}

@Composable
@ExperimentalLightboxApi
fun HideSystemUI(isHidden: Boolean) {
    val window = LocalActivity.current?.window

    DisposableEffect(isHidden, window) {
        if (isHidden && window != null) {
            val controller = window.let {
                WindowInsetsControllerCompat(it, it.decorView)
            }

            controller.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())

            onDispose {
                controller.show(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = BEHAVIOR_DEFAULT
            }

        } else noop
    }
}