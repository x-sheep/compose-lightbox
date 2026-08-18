@file:Suppress("UNUSED_PARAMETER", "unused", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")

package io.github.xsheep.composelightbox.snippets

import androidx.annotation.RestrictTo
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.xsheep.composelightbox.LightboxHost
import io.github.xsheep.composelightbox.LightboxImage
import io.github.xsheep.composelightbox.PhotoItem

@Composable
@RestrictTo(RestrictTo.Scope.LIBRARY)
fun MainScreen() {
    LightboxHost {
        Scaffold { padding ->
            // your UI here
        }
    }
}

@Composable
@RestrictTo(RestrictTo.Scope.LIBRARY)
fun Gallery() {
    LightboxHost {
        val photoList = remember {
            listOf<PhotoItem>() // Add your photos here
        }

        LazyColumn {
            items(photoList) {
                LightboxImage(photoList, it)
            }
        }
    }
}

@Composable
@RestrictTo(RestrictTo.Scope.LIBRARY)
fun Scaffold(content: @Composable (PaddingValues) -> Unit) {}
