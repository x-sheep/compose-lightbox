package io.github.xsheep.composelightbox

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.tween
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.isFinite
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Create and remember a LightboxState.
 * @param key When this input changes, the LightboxState will be recreated.
 */
@Composable
fun rememberLightboxState(key: Any? = null) = rememberSaveable(key) { LightboxState() }

/**
 * The state used to control a Lightbox.
 */
class LightboxState internal constructor() : Parcelable {
    internal var velocityTracker: VelocityTracker? = null
    internal var scope: CoroutineScope? = null

    internal val scale = Animatable(
        initialValue = 1f,
        typeConverter = Float.VectorConverter,
        visibilityThreshold = 0.01f,
        label = "scale"
    )
    internal val pan = Animatable(
        initialValue = Offset.Zero,
        typeConverter = Offset.VectorConverter,
        label = "pan"
    )

    internal val dismissGestureProgress = Animatable(
        initialValue = 0f,
        typeConverter = Float.VectorConverter,
        visibilityThreshold = 0.01f,
        label = "dismissGestureProgress"
    )

    internal val closingProgress = Animatable(
        initialValue = 0f,
        typeConverter = Float.VectorConverter,
        visibilityThreshold = 0.01f,
        label = "closingProgress"
    )

    private fun dismissForPan(y: Float) = min(1f, 4 * abs(y) / boxHeightPx.toFloat())

    /** The list of photos being presented. This will be `null` if the lightbox is closed and no animations are running. */
    var photoList by mutableStateOf(null as List<PhotoItem>?); internal set

    /** The index of the photo currently being presented. */
    var currentIndex by mutableIntStateOf(0); internal set

    /**
     * When a transition is in progress, this value is between the current index and the new index
     * being navigated to. Otherwise this value is identical to `currentIndex`.
     */
    val currentIndexFraction: Float
        get() {
            val frac = pan.value.x / boxWidthPx.toFloat()
            return if (motionState == Motion.CHANGE && frac.isFinite())
                currentIndex.toFloat() - frac
            else currentIndex.toFloat()
        }

    internal val isOverscrolling: Boolean
        get() {
            if (motionState != Motion.CHANGE) return false
            return (!hasPrevious && pan.value.x > 0) || (!hasNext && pan.value.x < 0)
        }

    /** True if the lightbox is currently open. */
    var open by mutableStateOf(false); internal set

    /** True if the HUD on top of the lightbox is currently visible. */
    var hudVisible by mutableStateOf(true)

    /** True if the lightbox has more photos when going backwards. */
    val hasPrevious get() = currentIndex > 0 && photoList != null

    /** True if the lightbox has more photos when going forwards. */
    val hasNext get() = currentIndex + 1 < (photoList?.size ?: 0)

    internal var isMinimalZoom = true
    internal var boxWidthPx = 0
    internal var boxHeightPx = 0
    internal var density = 1f
    internal var decaySpec = splineBasedDecay<Offset>(Density(1f))

    internal val boxCenter get() = Offset(boxWidthPx.toFloat() / 2, boxHeightPx.toFloat() / 2)

    /**
     * Open the Lightbox with a given photo.
     * @param photo The photo to focus on.
     * @param list The list of photos to present. This list must contain `photo`, or be empty.
     * @param sourceBounds The bounds of the originating image view, in absolute coordinates. May be null.
     */
    fun open(photo: PhotoItem, list: List<PhotoItem>, sourceBounds: Rect? = null) {
        scope?.launch {
            openInternal(photo, list, sourceBounds)
        }
    }

    private suspend fun openInternal(
        photo: PhotoItem,
        list: List<PhotoItem>,
        sourceBounds: Rect?,
        velocity: Velocity = Velocity.Zero
    ) {
        val index = list.indexOf(photo)
        require(list.isEmpty() || index >= 0) { "photo must be contained inside list." }

        dismissGestureProgress.snapTo(0f)
        closingProgress.snapTo(0f)
        if (open && photoList === list && abs(currentIndex - index) == 1) {
            motionState = Motion.CHANGE
            coroutineScope {
                // TODO the speed at which this scale/pan combo happens is pretty nauseating
                launch { scale.animateTo(1f) }
                launch {
                    pan.animateTo(
                        Offset((currentIndex - index) * boxWidthPx.toFloat(), 0f),
                        initialVelocity = Offset(velocity.x, 0f)
                    )
                }
            }
            currentIndex = index
            isMinimalZoom = true
            motionState = Motion.NONE
            pan.snapTo(Offset.Zero)
        } else {

            if (sourceBounds != null) {
                scale.snapTo(
                    max(
                        sourceBounds.width / boxWidthPx.toFloat(),
                        sourceBounds.height / boxHeightPx.toFloat()
                    )
                )
                pan.snapTo(
                    (sourceBounds.center - boxCenter) * 2f
                )
            } else {
                scale.snapTo(1f)
                pan.snapTo(Offset.Zero)
            }
            photoList = list.ifEmpty { listOf(photo) }
            currentIndex = if (list.isEmpty()) 0 else index
            isMinimalZoom = true
            motionState = Motion.NONE
            open = true

            if (sourceBounds != null) {
                coroutineScope {
                    launch { scale.animateTo(1f, tween(500)) }
                    launch { pan.animateTo(Offset.Zero, tween(500)) }
                }
            }
        }
    }

    /** Close the Lightbox. */
    fun close() {
        val scope = scope
        if (scope != null) {
            scope.launch { closeInternal() }
        } else {
            open = false
            photoList = null
        }
    }

    suspend fun closeInternal() {
        if (!open) return
        open = false

        try {
            coroutineScope {
                launch { closingProgress.animateTo(1f) }
                launch { scale.animateTo(scale.targetValue * 0.85f) }
            }
        } finally {
            photoList = null
        }
    }

    /**
     * Go to the previous photo in the list.
     *
     * If `hasPrevious` is false, this function does nothing.
     */
    fun goPrevious() {
        val list = photoList ?: return
        if (!hasPrevious) return

        open(list[currentIndex - 1], list)
    }

    /**
     * Go to the next photo in the list.
     *
     * If `hasNext` is false, this function does nothing.
     */
    fun goNext() {
        val list = photoList ?: return
        if (!hasNext) return

        open(list[currentIndex + 1], list)
    }

    internal var transformState: TransformableState? = null

    internal enum class Motion {
        NONE, DISMISS, CHANGE, PAN
    }

    internal var motionState by mutableStateOf(Motion.NONE)

    internal suspend fun onDoubleTap(offset: Offset) {
        coroutineScope {
            if (scale.value > 1.1f) {
                launch { scale.animateTo(1f) }
                launch { pan.animateTo(Offset.Zero) }
                isMinimalZoom = true
                hudVisible = true

            } else {
                launch { scale.animateTo(scale.targetValue * 4, tween(250)) }
                launch { pan.animateTo(boxCenter - offset, tween(250)) }
                isMinimalZoom = false
                hudVisible = false
            }
        }
    }

    internal fun dragInProgress(zoomChange: Float, panChange: Offset) {
        val scope = scope ?: return
        scope.launch {
            if (zoomChange != 1f && motionState == Motion.NONE)
                motionState = Motion.PAN

            if (motionState == Motion.PAN)
                scale.snapTo(scale.value * zoomChange)

            if (isMinimalZoom && motionState == Motion.NONE) {
                val delta = 8 * density
                if (abs(panChange.y) > delta) {
                    motionState = Motion.DISMISS
                } else if (abs(panChange.x) > delta) {
                    motionState = Motion.CHANGE
                }
                pan.snapTo(Offset.Zero)
            } else if (!isMinimalZoom) {
                motionState = Motion.PAN
            }

            when (motionState) {
                Motion.DISMISS -> {
                    pan.snapTo(Offset(0f, pan.value.y + panChange.y))
                    dismissGestureProgress.snapTo(dismissForPan(pan.value.y))
                }

                Motion.CHANGE -> {
                    pan.snapTo(Offset(pan.value.x + panChange.x, 0f))
                }

                Motion.PAN -> {
                    pan.snapTo(pan.value + panChange)
                }

                else -> {}
            }
        }
    }

    internal fun onTransformEnded(velocity: Velocity) {
        scope?.launch { waitForTransformEnd(velocity) }
    }

    private suspend fun waitForTransformEnd(velocity: Velocity) {
        val velocityOffset = Offset(velocity.x, velocity.y) / max(1f, scale.targetValue)
        val targetPan = decaySpec.calculateTargetValue(
            Offset.VectorConverter,
            pan.targetValue,
            velocityOffset
        )

        if (motionState == Motion.DISMISS && dismissForPan(targetPan.y) == 1f) {
            open = false
            val direction = if (targetPan.y < 0) -1.5f else +1.5f
            pan.animateTo(
                Offset(0f, boxHeightPx.toFloat() * direction),
                tween(250, easing = LinearEasing),
                Offset(0f, velocity.y)
            )
            photoList = null

            return
        }

        isMinimalZoom = scale.targetValue <= 1f
        val photoList = photoList ?: return
        val aspectRatio = photoList[currentIndex].aspectRatio
        val toNext = motionState == Motion.CHANGE && targetPan.x < -boxWidthPx / 2 && hasNext
        val toPrevious = motionState == Motion.CHANGE && targetPan.x > boxWidthPx / 2 && hasPrevious
        if (toNext) {
            openInternal(photoList[currentIndex + 1], photoList, null, velocity)
        } else if (toPrevious) {
            openInternal(photoList[currentIndex - 1], photoList, null, velocity)
        } else if (isMinimalZoom) {
            coroutineScope {
                launch { scale.animateTo(1f) }
                launch { pan.animateTo(Offset.Zero) }
                launch { dismissGestureProgress.animateTo(0f) }
                hudVisible = true
            }
        } else {
            val imageWidthPx: Float
            val imageHeightPx: Float

            if (aspectRatio >= boxWidthPx.toFloat() / boxHeightPx.toFloat()) {
                imageWidthPx = boxWidthPx.toFloat()
                imageHeightPx = imageWidthPx / aspectRatio
            } else {
                imageHeightPx = boxHeightPx.toFloat()
                imageWidthPx = imageHeightPx * aspectRatio
            }

            dismissGestureProgress.snapTo(0f)
            val limitX = imageWidthPx * 0.5f
            val limitY = imageHeightPx * 0.5f

            pan.updateBounds(
                Offset(-limitX, -limitY),
                Offset(limitX, limitY),
            )
            try {
                val overshot = pan.animateDecay(velocityOffset, decaySpec)
                if (overshot.endReason == AnimationEndReason.BoundReached && overshot.endState.velocity.isFinite) {
                    if (abs(overshot.endState.value.x) == limitX) {
                        val vertical = Offset(0f, overshot.endState.velocity.y)
                        pan.animateDecay(vertical, decaySpec)
                    } else if (abs(overshot.endState.value.y) == limitY) {
                        val horizontal = Offset(overshot.endState.velocity.x, 0f)
                        pan.animateDecay(horizontal, decaySpec)
                    }
                }
            } finally {
                pan.updateBounds(null, null)
            }
        }
        motionState = Motion.NONE
    }

    /** @see Parcelable */
    override fun describeContents() = 0

    /** @see Parcelable */
    override fun writeToParcel(dest: Parcel, flags: Int) {
        val current = if (open) currentIndex else -1
        dest.writeInt(current)
        if (current >= 0) {
            dest.writeList(photoList.orEmpty())
        }
    }

    companion object {
        /** @see Parcelable */
        @[JvmField Suppress("unused")]
        val CREATOR = object : Parcelable.Creator<LightboxState> {
            override fun createFromParcel(source: Parcel): LightboxState {
                val current = source.readInt()
                val ret = LightboxState()
                if (current < 0) return ret

                ret.photoList = source.createTypedArrayList(PhotoItem.CREATOR)
                ret.currentIndex = current
                ret.open = true
                return ret
            }

            override fun newArray(size: Int): Array<out LightboxState?> = arrayOfNulls(size)
        }
    }
}
