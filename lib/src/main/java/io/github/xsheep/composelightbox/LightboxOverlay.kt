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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp

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
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        val dir = LocalLayoutDirection.current

        AnimatedVisibility(
            state.hudVisible && state.open,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(Modifier.fillMaxSize()) {
                IconButton(
                    onClick = {
                        state.close()
                    }, Modifier
                        .align(Alignment.TopStart)
                        .padding(padding)
                        .padding(start = 4.dp, top = 4.dp)
                ) {
                    Icon(
                        ImageVector.vectorResource(R.drawable.close),
                        stringResource(R.string.close),
                        Modifier
                            .size(48.dp)
                            .padding(6.dp),
                    )
                }

                if (state.hasPrevious) {
                    IconButton(
                        onClick = {
                            state.goPrevious()
                        }, Modifier
                            .align(Alignment.CenterStart)
                            .padding(
                                start = padding.calculateStartPadding(dir)
                            ).padding(start = 4.dp)
                    ) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.backward),
                            stringResource(R.string.previous),
                            Modifier.size(48.dp)
                        )
                    }
                }

                if (state.hasNext) {
                    IconButton(
                        onClick = {
                            state.goNext()
                        }, Modifier
                            .align(Alignment.CenterEnd)
                            .padding(
                                end = padding.calculateEndPadding(dir)
                            ).padding(end = 4.dp)
                    ) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.forward),
                            stringResource(R.string.next),
                            Modifier.size(48.dp)
                        )
                    }
                }
            }
        }
    }
}

@ExperimentalLightboxApi
val DefaultLightboxOverlay = ::LightboxOverlay