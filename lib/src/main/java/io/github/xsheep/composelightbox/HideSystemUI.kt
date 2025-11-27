package io.github.xsheep.composelightbox

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.DisposableEffectResult
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

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

            controller.hide(WindowInsetsCompat.Type.systemBars())

            onDispose {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }

        } else noop
    }
}