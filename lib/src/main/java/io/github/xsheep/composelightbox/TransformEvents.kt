/*
 * Adapted from Transformable.kt from the Jetpack Compose source code.
 *
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.xsheep.composelightbox

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

internal fun Modifier.customTransformable(
    state: TransformableState,
    lightbox: LightboxState,
    enabled: Boolean = true,
) = this then TransformableElement(state, lightbox, enabled)

private data class TransformableElement(
    private val state: TransformableState,
    private val lightbox: LightboxState,
    private val enabled: Boolean,
) : ModifierNodeElement<TransformableNode>() {
    override fun create(): TransformableNode =
        TransformableNode(state, lightbox, enabled)

    override fun update(node: TransformableNode) {
        node.update(state, lightbox, enabled)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "transformable"
        properties["state"] = state
        properties["lightbox"] = lightbox
        properties["enabled"] = enabled
    }
}

private class TransformableNode(
    private var state: TransformableState,
    private var lightbox: LightboxState,
    private var enabled: Boolean,
) : DelegatingNode(), PointerInputModifierNode, CompositionLocalConsumerModifierNode {

    private val channel = Channel<TransformEvent>(capacity = Channel.UNLIMITED)

    private val pointerInputNode =
        delegate(
            SuspendingPointerInputModifierNode {
                if (!enabled) return@SuspendingPointerInputModifierNode
                coroutineScope {
                    launch(start = CoroutineStart.UNDISPATCHED) {
                        while (isActive) {
                            var event = channel.receive()
                            if (event !is TransformEvent.TransformStarted) continue
                            try {
                                state.transform(MutatePriority.UserInput) {
                                    while (event !is TransformEvent.TransformStopped) {
                                        (event as? TransformEvent.TransformDelta)?.let {
                                            transformBy(
                                                it.zoomChange, it.panChange, 0f
                                            )
                                        }
                                        event = channel.receive()
                                    }
                                    (event as? TransformEvent.TransformStopped)?.let {
                                        lightbox.onTransformEnded(it.velocity)
                                    }
                                }
                            } catch (_: CancellationException) {
                                // ignore the cancellation and start over again.
                            }
                        }
                    }

                    awaitEachGesture {
                        try {
                            detectZoom(channel, lightbox.velocityTracker)
                        } catch (exception: CancellationException) {
                            if (!isActive) throw exception
                        } finally {
                            val velocity =
                                lightbox.velocityTracker?.calculateVelocity() ?: Velocity.Zero
                            channel.trySend(TransformEvent.TransformStopped(velocity))
                            lightbox.velocityTracker?.resetTracking()
                        }
                    }
                }
            }
        )

    fun update(
        state: TransformableState,
        lightbox: LightboxState,
        enabled: Boolean,
    ) {
        val needsReset = this.state != state || this.enabled != enabled || this.lightbox != lightbox
        if (needsReset) {
            this.state = state
            this.lightbox = lightbox
            this.enabled = enabled
            pointerInputNode.resetPointerInputHandler()
        }
    }

    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize,
    ) {
        pointerInputNode.onPointerEvent(pointerEvent, pass, bounds)
    }

    override fun onCancelPointerInput() {
        pointerInputNode.onCancelPointerInput()
    }
}

private suspend fun AwaitPointerEventScope.detectZoom(
    channel: Channel<TransformEvent>,
    velocityTracker: VelocityTracker?,
) {
    var zoom = 1f
    var pan = Offset.Zero
    var pastTouchSlop = false
    val touchSlop = viewConfiguration.touchSlop
    awaitFirstDown(requireUnconsumed = false)
    do {
        val event = awaitPointerEvent()
        val canceled = event.changes.fastAny { it.isConsumed }
        if (!canceled) {
            val zoomChange = event.calculateZoom()
            val panChange = event.calculatePan()
            if (zoomChange == 1f && velocityTracker != null) {
                event.changes.fastForEach {
                    velocityTracker.addPointerInputChange(it)
                }
            }

            if (!pastTouchSlop) {
                zoom *= zoomChange
                pan += panChange

                val centroidSize = event.calculateCentroidSize(useCurrent = false)
                val zoomMotion = abs(1 - zoom) * centroidSize
                val panMotion = pan.getDistance()

                if (
                    zoomMotion > touchSlop ||
                    panMotion > touchSlop
                ) {
                    pastTouchSlop = true
                    channel.trySend(TransformEvent.TransformStarted)
                }
            }

            if (pastTouchSlop) {
                if (zoomChange != 1f || panChange != Offset.Zero) {
                    channel.trySend(TransformEvent.TransformDelta(zoomChange, panChange))
                }
                event.changes.fastForEach {
                    if (it.positionChanged()) {
                        it.consume()
                    }
                }
            }
        } else {
            val velocity = velocityTracker?.calculateVelocity() ?: Velocity.Zero
            channel.trySend(TransformEvent.TransformStopped(velocity))
            velocityTracker?.resetTracking()
        }
        val finalEvent = awaitPointerEvent(pass = PointerEventPass.Final)
        // someone consumed while we were waiting for touch slop
        val finallyCanceled = finalEvent.changes.fastAny { it.isConsumed } && !pastTouchSlop
    } while (!canceled && !finallyCanceled && event.changes.fastAny { it.pressed })
}

private sealed class TransformEvent {
    object TransformStarted : TransformEvent()

    class TransformStopped(val velocity: Velocity) : TransformEvent()

    class TransformDelta(val zoomChange: Float, val panChange: Offset) : TransformEvent()
}
