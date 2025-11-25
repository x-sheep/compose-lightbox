package io.github.xsheep.composelightbox

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.annotation.RememberInComposition
import java.net.URI

/**
 * Describes a single photo to be displayed in a Lightbox.
 *
 * Instances of this class must not be recreated during composition, as it will be compared by reference.
 */
class PhotoItem : Parcelable {
    internal var aspectRatio = Float.NaN

    /** The url of the full-size image. */
    val url: String
    /** Alternate text for the image. */
    val contentDescription: String?
    /** The url of the thumbnail, if it exists. */
    val thumbnail: String?

    /** @see Parcelable */
    override fun describeContents() = 0

    /** @see Parcelable */
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(url)
        dest.writeString(contentDescription)
        dest.writeString(thumbnail)
    }

    companion object {
        @[JvmField Suppress("unused")]
        val CREATOR = object : Parcelable.Creator<PhotoItem> {
            override fun createFromParcel(source: Parcel) = PhotoItem(
                source.readString()!!,
                source.readString(),
                source.readString(),
            )

            override fun newArray(size: Int): Array<out PhotoItem?> = arrayOfNulls(size)
        }
    }

    /**
     * Create a new item.
     *
     * @param url The url of the full-size image.
     * @param contentDescription Alternate text for the image. May be null.
     * @param thumbnail The url of the thumbnail. May be null.
     */
    @RememberInComposition
    constructor(url: String, contentDescription: String?, thumbnail: String?) {
        this.url = url
        this.contentDescription = contentDescription
        this.thumbnail = thumbnail
    }

    /**
     * Create a new item.
     *
     * @param url The url of the full-size image.
     * @param contentDescription Alternate text for the image. May be null.
     * @param thumbnail The url of the thumbnail. May be null.
     */
    @RememberInComposition
    constructor(url: Uri, contentDescription: String?, thumbnail: Uri?) : this(
        url.toString(),
        contentDescription,
        thumbnail?.toString()
    )

    /**
     * Create a new item.
     *
     * @param url The url of the full-size image.
     * @param contentDescription Alternate text for the image. May be null.
     * @param thumbnail The url of the thumbnail. May be null.
     */
    @RememberInComposition
    constructor(url: URI, contentDescription: String?, thumbnail: URI?) : this(
        url.toString(),
        contentDescription,
        thumbnail?.toString()
    )
}
