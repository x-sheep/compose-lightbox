package io.github.xsheep.composelightbox

import android.net.Uri
import androidx.compose.runtime.annotation.RememberInComposition
import java.net.URI

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
    url: URI,
    contentDescription: String?,
    thumbnail: URI?,
    useThumbnailWhenLoading: Boolean = true
): PhotoItem = PhotoItem(
    url.toString(),
    contentDescription,
    thumbnail?.toString(),
    useThumbnailWhenLoading
)

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
    url: Uri,
    contentDescription: String?,
    thumbnail: Uri?,
    useThumbnailWhenLoading: Boolean = true
): PhotoItem = PhotoItem(
    url.toString(),
    contentDescription,
    thumbnail?.toString(),
    useThumbnailWhenLoading
)
