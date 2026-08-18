# ComposeLightbox

An implementation of a Lightbox for Jetpack Compose. Press an image anywhere in your layout to open a gallery of images.

https://github.com/user-attachments/assets/247b350f-be37-4e02-bbbc-dad781d624bb

## Requirements

* For Android: SDK level 23+
* Compose Multiplatform 1.11 or higher

This library currently supports Android and iOS. For older Android phones, use version [1.x](https://github.com/x-sheep/compose-lightbox/tree/1.x) without Multiplatform support.

## Installation

This library depends on [Coil 3](https://github.com/coil-kt/coil) for image loading and display. Follow the Quick Start instructions to connect it a networking library like Ktor or OkHttp.

Once a regular `AsyncImage()` loads successfully in your app, you can integrate ComposeLightbox.

Add the ComposeLightbox dependency to the `build.gradle` of your app:
```kotlin
implementation("io.github.x-sheep:compose-lightbox:2.0.0")
```

## Usage

1. Add `LightboxHost` above your Scaffold, to make sure the overlay covers the entire app screen.
2. Create a list of `PhotoItem` objects to describe each image.
3. Display the images in your layout with `LightboxImage`. This composable will automatically add a Click handler.

## Example

Simple example:
```kotlin
import io.github.xsheep.composelightbox.*

@Composable
fun Gallery() {
  LightboxHost {
    Scaffold { padding ->
      val photoList = remember {
          listOf<PhotoItem>() // Add your photos here
      }

      LazyColumn(contentPadding = padding) {
        items(photoList) {
          LightboxImage(photoList, it, Modifier.size(300.dp))
        }
      }
    }
  }
}
```

If your layout is changing size when the Lightbox opens and closes, you can try increasing the window insets used in your Scaffold:
```kotlin
Scaffold(contentWindowInsets = WindowInsets.mandatorySystemGestures.union(WindowInsets.displayCutout))
```

# License

Copyright (c) 2025-2026 Lennard Sprong. [MIT License](./LICENSE)
