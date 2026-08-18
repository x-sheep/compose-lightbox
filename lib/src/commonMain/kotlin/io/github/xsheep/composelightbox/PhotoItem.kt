package io.github.xsheep.composelightbox

import androidx.compose.runtime.annotation.RememberInComposition
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Describes a single photo to be displayed in a Lightbox.
 *
 * Instances of this class must not be recreated during composition, as it will be compared by reference.
 */
@Serializable
class PhotoItem {
    @Transient
    internal var aspectRatio = Float.NaN

    /** The url of the full-size image. */
    val url: String

    /** Alternate text for the image. */
    val contentDescription: String?

    /**
     * The url of the thumbnail, if it exists.
     *
     * This is used by LightboxImage, and also as a loading placeholder when `useThumbnailWhenLoading` is true.
     * */
    val thumbnail: String?

    /** True if the thumbnail is displayed while loading the full image. */
    val useThumbnailWhenLoading: Boolean

    /**
     * Create a new item.
     *
     * @param url The url of the full-size image.
     * @param contentDescription Alternate text for the image. May be null.
     * @param thumbnail The url of the thumbnail. May be null.
     * @param useThumbnailWhenLoading True if the thumbnail is displayed while loading the full image.
     */
    @RememberInComposition
    constructor(
        url: String,
        contentDescription: String?,
        thumbnail: String?,
        useThumbnailWhenLoading: Boolean = true
    ) {
        this.url = url
        this.contentDescription = contentDescription
        this.thumbnail = thumbnail
        this.useThumbnailWhenLoading = useThumbnailWhenLoading
    }

    /**
     * Create a new item.
     *
     * @param url The url of the full-size image.
     * @param contentDescription Alternate text for the image. May be null.
     * @param thumbnail The url of the thumbnail. May be null.
     * @param useThumbnailWhenLoading True if the thumbnail is displayed while loading the full image.
     */
    @RememberInComposition
    constructor(
        url: coil3.Uri,
        contentDescription: String?,
        thumbnail: coil3.Uri?,
        useThumbnailWhenLoading: Boolean = true
    ) {
        this.url = url.toString()
        this.contentDescription = contentDescription
        this.thumbnail = thumbnail?.toString()
        this.useThumbnailWhenLoading = useThumbnailWhenLoading
    }
}