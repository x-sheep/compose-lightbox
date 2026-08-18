package io.github.xsheep.composelightbox

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
@ExperimentalLightboxApi
fun LightboxOverlayButton(
    imageVector: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .clip(CircleShape)
            .clickable(
                role = Role.Button,
                onClick = onClick
            ).size(LocalViewConfiguration.current.minimumTouchTargetSize),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .padding(4.dp)
                .background(Color(0, 0, 0, 0x20), CircleShape)
                .matchParentSize()
        )

        Image(
            imageVector,
            contentDescription,
            Modifier.size(40.dp)
        )
    }
}