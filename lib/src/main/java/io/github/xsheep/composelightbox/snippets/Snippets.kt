@file:Suppress("UNUSED_PARAMETER", "unused", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
@file:SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")

package io.github.xsheep.composelightbox.snippets

import android.annotation.SuppressLint
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.xsheep.composelightbox.LightboxHost
import io.github.xsheep.composelightbox.LightboxImage
import io.github.xsheep.composelightbox.PhotoItem

@Composable
fun MainScreen() {
    LightboxHost {
        Scaffold { padding ->
            // your UI here
        }
    }
}

@Composable
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
