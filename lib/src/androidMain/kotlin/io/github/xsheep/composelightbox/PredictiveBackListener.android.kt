package io.github.xsheep.composelightbox

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CancellationException
import kotlin.math.min
import kotlin.math.sqrt

@Composable
internal actual fun PredictiveBackListener(
    state: LightboxState
) {
    PredictiveBackHandler(state.open) { flow ->
        try {
            flow.collect {
                state.dismissGestureProgress.snapTo(sqrt(min(1f, it.progress * 2)))
            }
            state.close()
        } catch (_: CancellationException) {
            state.dismissGestureProgress.snapTo(0f)
        }
    }
}
