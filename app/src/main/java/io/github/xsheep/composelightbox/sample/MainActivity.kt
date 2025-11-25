package io.github.xsheep.composelightbox.sample

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.xsheep.composelightbox.LightboxHost
import io.github.xsheep.composelightbox.LightboxImage
import io.github.xsheep.composelightbox.PhotoItem

class MainActivity : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()

            MaterialTheme(colorScheme) {
                LightboxHost {
                    Scaffold(
                        topBar = {
                            TopAppBar(title = {
                                Text("Compose Lightbox sample")
                            })
                        }
                    ) { padding ->
                        val photoList = remember {
                            (10 until 30).map {
                                PhotoItem(
                                    "https://picsum.photos/id/$it/1200/900",
                                    "Photo ${it - 9}",
                                    "https://picsum.photos/id/$it/400/300"
                                )
                            }
                        }

                        LazyVerticalGrid(
                            GridCells.Fixed(2),
                            contentPadding = padding,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(photoList) {
                                LightboxImage(
                                    photoList, it, Modifier
                                        .width(300.dp)
                                        .height(200.dp),
                                    ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}