package io.github.xsheep.composelightbox

import androidx.compose.runtime.annotation.RememberInComposition
import platform.Foundation.NSURL

/**
 * Create a new item.
 *
 * @param url The url of the full-size image.
 * @param contentDescription Alternate text for the image. May be null.
 * @param thumbnail The url of the thumbnail. May be null.
 * @param useThumbnailWhenLoading True if the thumbnail is displayed while loading the full image.
 */
@RememberInComposition
fun PhotoItem(
    url: NSURL,
    contentDescription: String?,
    thumbnail: NSURL?,
    useThumbnailWhenLoading: Boolean = true
): PhotoItem = PhotoItem(
    url.absoluteString ?: url.relativeString,
    contentDescription,
    thumbnail?.absoluteString ?: thumbnail?.relativeString,
    useThumbnailWhenLoading
)
