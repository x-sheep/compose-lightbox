package io.github.xsheep.composelightbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffectResult

internal val noop = object : DisposableEffectResult {
    override fun dispose() {}
}

@Composable
@ExperimentalLightboxApi
expect fun HideSystemUI(isHidden: Boolean)
