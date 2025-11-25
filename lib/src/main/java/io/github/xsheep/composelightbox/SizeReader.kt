package io.github.xsheep.composelightbox

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

internal class SizeReader(val currentPhoto: PhotoItem) : RequestListener<Drawable> {
    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>?,
        isFirstResource: Boolean
    ): Boolean = false

    override fun onResourceReady(
        resource: Drawable,
        model: Any?,
        target: Target<Drawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        if (resource is BitmapDrawable) {
            currentPhoto.aspectRatio =
                resource.intrinsicWidth.toFloat() / resource.intrinsicHeight.toFloat()
        }
        return false
    }
}
