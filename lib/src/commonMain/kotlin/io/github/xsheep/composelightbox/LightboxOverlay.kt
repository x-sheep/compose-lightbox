package io.github.xsheep.composelightbox

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import io.github.xsheep.composelightbox.generated.resources.Res
import io.github.xsheep.composelightbox.generated.resources.backward
import io.github.xsheep.composelightbox.generated.resources.close
import io.github.xsheep.composelightbox.generated.resources.forward
import io.github.xsheep.composelightbox.generated.resources.next
import io.github.xsheep.composelightbox.generated.resources.previous
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

/**
 * A default user interface that can be displayed over the Lightbox.
 *
 * This contains a Close button, a counter, and a Next/Previous button. You can copy and modify this
 * function to make a custom overlay.
 *
 * @param state The current state of the Lightbox.
 * @param padding The current window insets. These values must be applied manually using `Modifier.padding()`.
 */
@Composable
@ExperimentalLightboxApi
fun LightboxOverlay(state: LightboxState, padding: PaddingValues) {
    val dir = LocalLayoutDirection.current

    AnimatedVisibility(
        state.hudVisible && state.open,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(Modifier.fillMaxSize()) {
            LightboxOverlayButton(
                vectorResource(Res.drawable.close),
                stringResource(Res.string.close),
                Modifier
                    .align(Alignment.TopStart)
                    .padding(padding)
                    .padding(start = 4.dp, top = 4.dp)
            ) {
                state.close()
            }

            if (state.hasPrevious) {
                LightboxOverlayButton(
                    vectorResource(Res.drawable.backward),
                    stringResource(Res.string.previous),
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(
                            start = padding.calculateStartPadding(dir)
                        )
                        .padding(start = 4.dp)
                ) {
                    state.goPrevious()
                }
            }

            if (state.hasNext) {
                LightboxOverlayButton(
                    vectorResource(Res.drawable.forward),
                    stringResource(Res.string.next),
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(
                            end = padding.calculateEndPadding(dir)
                        )
                        .padding(end = 4.dp)
                ) {
                    state.goNext()
                }
            }
        }
    }
}

@ExperimentalLightboxApi
val DefaultLightboxOverlay = ::LightboxOverlay