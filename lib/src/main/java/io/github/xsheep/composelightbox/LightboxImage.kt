package io.github.xsheep.composelightbox

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.Placeholder

/**
 * An image that opens the Lightbox when pressed.
 *
 * These images can be used anywhere in your UI.
 *
 * This composable must have LightboxHost as a parent.
 *
 * @sample io.github.xsheep.composelightbox.samples.Gallery
 *
 * @param photoList The list of photos to present when pressed. May be empty.
 * @param photo The photo being displayed by this image. This must be a member of `photoList` if the list is not empty.
 * @param enabled Trus if the Lightbox can be opened by pressing this view.
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LightboxImage(
    photoList: List<PhotoItem>,
    photo: PhotoItem,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    enabled: Boolean = true
) {
    LightboxImage(
        photoList = photoList,
        photo = photo,
        modifier = modifier,
        loading = null,
        failure = null,
        contentScale = contentScale,
        enabled = enabled
    )
}

/**
 * An image that opens the Lightbox when pressed.
 *
 * These images can be used anywhere in your UI.
 *
 * This composable must have LightboxHost as a parent.
 *
 * @sample io.github.xsheep.composelightbox.samples.Gallery
 *
 * @param photoList The list of photos to present when pressed. May be empty.
 * @param photo The photo being displayed by this image. This must be a member of `photoList` if the list is not empty.
 * @param enabled Trus if the Lightbox can be opened by pressing this view.
 * @param loading The placeholder to display while loading the iamge. Will only be displayed inline, and not in the Lightbox.
 * @param failure The placeholder to display when the request fails. Will only be displayed inline, and not in the Lightbox.
 */
@ExperimentalGlideComposeApi
@Composable
fun LightboxImage(
    photoList: List<PhotoItem>,
    photo: PhotoItem,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    enabled: Boolean = true,
    loading: Placeholder?,
    failure: Placeholder?,
) {
    val host = LocalLightboxHost.current
    val dir = LocalLayoutDirection.current

    var bounds by remember { mutableStateOf(Rect(Offset.Unspecified, Float.NaN)) }

    GlideImage(
        photo.thumbnail ?: photo.url,
        photo.contentDescription,
        modifier
            .clickable(enabled) {
                host.open(photo, photoList, bounds.takeIf { it.isFinite })
            }
            .onGloballyPositioned {
                val root = it.findRootCoordinates()
                val box = root.localBoundingBoxOf(it)
                bounds = if (dir == LayoutDirection.Rtl) {
                    val rootWidth = root.size.width
                    box.copy(left = rootWidth - box.right, right = rootWidth - box.left)
                } else box
            },
        loading = loading,
        failure = failure,
        contentScale = contentScale,
    )
}
