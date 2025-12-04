# ComposeLightbox

An implementation of a Lightbox for Jetpack Compose. Press an image anywhere in your layout to open a gallery of images.

## Requirements

* Android SDK level 16+
* Jetpack Compose version 2025.11.00 or higher

Platforms other than Android are currently not supported.

## Installation

Make sure Jitpack is added to the list of package repositories in `settings.gradle`.
```groovy
dependencyResolutionManagement {
  repositories {
    ... // other repositories
    maven { url 'https://jitpack.io' }
  }
}
```

Then add the dependency to the `build.gradle` of your app:
```groovy
dependencies {
  ... // other dependencies
  implementation 'com.github.x-sheep:compose-lightbox:0.1.5'
}
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

Copyright (c) 2025 Lennard Sprong. [MIT License](./LICENSE)
