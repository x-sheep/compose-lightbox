package io.github.xsheep.composelightbox

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.mandatorySystemGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import com.bumptech.glide.Priority
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Create a Host view that contains the Lightbox, as well as all views that are displayed behind it.
 *
 * This view should be placed before any Scaffold, to make sure the Lightbox fully overlaps all of your UI.
 *
 * @sample io.github.xsheep.composelightbox.samples.MainScreen
 *
 * @param state The state of the Lightbox.
 */
@OptIn(ExperimentalGlideComposeApi::class, ExperimentalLightboxApi::class)
@Composable
fun LightboxHost(
    state: LightboxState = rememberLightboxState(),
    content: @Composable () -> Unit
) {
    LightboxHost(state, true, DefaultLightboxOverlay, content)
}

/**
 * Create a Host view that contains the Lightbox, as well as all views that are displayed behind it.
 *
 * This view should be placed before any Scaffold, to make sure the Lightbox fully overlaps all of your UI.
 *
 * @sample io.github.xsheep.composelightbox.samples.MainScreen
 *
 * @param state The state of the Lightbox.
 * @param overlay The view to draw on top of the Lightbox image.
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
@ExperimentalLightboxApi
fun LightboxHost(
    state: LightboxState = rememberLightboxState(),
    hideSystemUI: Boolean = true,
    overlay: @Composable (LightboxState, PaddingValues) -> Unit,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val velocityTracker = remember { VelocityTracker() }

    DisposableEffect(coroutineScope, velocityTracker, state) {
        state.scope = coroutineScope
        state.velocityTracker = velocityTracker
        onDispose {
            if (state.scope === coroutineScope)
                state.scope = null
            if (state.velocityTracker === velocityTracker)
                state.velocityTracker = null
        }
    }

    CompositionLocalProvider(
        LocalLightboxHost provides state
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    state.boxWidthPx = placeable.width
                    state.boxHeightPx = placeable.height
                    if (state.density != density) {
                        state.density = density
                        state.decaySpec = splineBasedDecay(Density(density))
                    }
                    layout(placeable.width, placeable.height) {
                        placeable.placeRelative(0, 0)
                    }
                }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .semantics {
                        if (state.open) {
                            hideFromAccessibility()
                        }
                    }) {
                content()
            }

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

            HideSystemUI(hideSystemUI && state.open && !state.hudVisible)

            val currentPhoto = remember(state.photoList, state.currentIndex) {
                state.photoList?.getOrNull(state.currentIndex)
            }
            val previousPhoto = remember(state.photoList, state.currentIndex) {
                state.photoList?.getOrNull(state.currentIndex - 1)
            }
            val nextPhoto = remember(state.photoList, state.currentIndex) {
                state.photoList?.getOrNull(state.currentIndex + 1)
            }

            AnimatedVisibility(
                state.open,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Spacer(
                    Modifier
                        .graphicsLayer {
                            alpha = 1f - (state.dismissGestureProgress.value * 0.6f)
                        }
                        .background(Color.Black.copy(alpha = 0.9f))
                        .fillMaxSize()
                )
            }

            if (currentPhoto != null) {
                val dir = LocalLayoutDirection.current

                val transformState = rememberTransformableState { zoomChange, rawPanChange, _ ->
                    val panChange = if (dir == LayoutDirection.Rtl)
                        Offset(-rawPanChange.x, rawPanChange.y)
                    else rawPanChange

                    state.dragInProgress(zoomChange, panChange)
                }

                DisposableEffect(transformState) {
                    state.transformState = transformState
                    onDispose {
                        if (state.transformState === transformState)
                            state.transformState = null
                    }
                }

                if (previousPhoto != null) {
                    GlideImage(
                        previousPhoto.url,
                        previousPhoto.contentDescription,
                        Modifier
                            .fillMaxSize()
                            .offset {
                                val pan = if (state.motionState == LightboxState.Motion.CHANGE)
                                    state.pan.value.x.roundToInt()
                                else 0
                                IntOffset(x = pan - state.boxWidthPx, y = 0)
                            },
                        loading = placeholder {
                            Box(contentAlignment = Alignment.Center) {
                                GlideImage(
                                    previousPhoto.thumbnail,
                                    previousPhoto.contentDescription,
                                    Modifier
                                        .fillMaxSize()
                                        .blur(2.dp),
                                    requestBuilderTransform = { it.priority(Priority.LOW) }
                                )
                            }
                        }
                    )
                }

                if (nextPhoto != null) {
                    GlideImage(
                        nextPhoto.url,
                        nextPhoto.contentDescription,
                        Modifier
                            .fillMaxSize()
                            .offset {
                                val pan = if (state.motionState == LightboxState.Motion.CHANGE)
                                    state.pan.value.x.roundToInt()
                                else 0
                                IntOffset(x = pan + state.boxWidthPx, y = 0)
                            },
                        loading = placeholder {
                            Box(contentAlignment = Alignment.Center) {
                                GlideImage(
                                    nextPhoto.thumbnail,
                                    nextPhoto.contentDescription,
                                    Modifier
                                        .fillMaxSize()
                                        .blur(2.dp),
                                    requestBuilderTransform = { it.priority(Priority.NORMAL) }
                                )
                            }
                        }
                    )
                }

                GlideImage(
                    currentPhoto.url,
                    currentPhoto.contentDescription,
                    Modifier
                        .graphicsLayer {
                            val dismissScale = 1f - (state.dismissGestureProgress.value * 0.2f)

                            scaleX = dismissScale * state.scale.value
                            scaleY = scaleX
                            alpha = 1f - state.closingProgress.value
                        }
                        .customTransformable(transformState, state)
                        .offset { state.pan.value.round() }
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    coroutineScope.launch {
                                        state.onDoubleTap(it)
                                    }
                                },
                                onTap = {
                                    state.hudVisible = !state.hudVisible
                                }
                            )
                        },
                    loading = placeholder {
                        Box(contentAlignment = Alignment.Center) {
                            GlideImage(
                                currentPhoto.thumbnail,
                                currentPhoto.contentDescription,
                                Modifier
                                    .fillMaxSize()
                                    .blur(2.dp)
                            )
                            // TODO add progress indicator if this screen is here too long, otherwise don't
                        }
                    },
                    requestBuilderTransform = {
                        it.priority(Priority.HIGH)
                            .addListener(SizeReader(currentPhoto))
                    }
                )
            }

            val insets = WindowInsets.mandatorySystemGestures.union(WindowInsets.displayCutout)
            overlay(state, insets.asPaddingValues())
        }
    }
}

internal val LocalLightboxHost = staticCompositionLocalOf<LightboxState> {
    error("This function should be used within a LightboxHost composable.")
}
