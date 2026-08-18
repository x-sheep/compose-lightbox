package io.github.xsheep.composelightbox

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.savedstate.SavedState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure

object LightboxSaver : Saver<LightboxState, SavedState> {
    override fun restore(value: SavedState): LightboxState {
        return decodeFromSavedState(LightboxState.serializer(), value)
    }

    override fun SaverScope.save(value: LightboxState): SavedState {
        return encodeToSavedState(LightboxState.serializer(), value)
    }
}

object LightboxStateSerializer : KSerializer<LightboxState> {
    val itemSerializer = ListSerializer(PhotoItem.serializer())

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("io.github.xsheep.composelightbox.LightboxState") {
            element<Int>("current")
            element<List<PhotoItem>>("photoList", isOptional = true)
        }

    override fun serialize(
        encoder: Encoder,
        value: LightboxState
    ) {
        val current = if (value.open) value.targetIndex else -1
        encoder.encodeInt(current)
        if (current >= 0) {
            encoder.encodeSerializableValue(itemSerializer, value.photoList.orEmpty())
        }
    }

    override fun deserialize(decoder: Decoder): LightboxState =
        decoder.decodeStructure(descriptor) {
            val ret = LightboxState()

            var current = -1
            var photoList: List<PhotoItem>? = null

            while (true) {
                when (decodeElementIndex(descriptor)) {
                    0 -> current = decodeIntElement(descriptor, 0)
                    1 -> photoList = decodeSerializableElement(descriptor, 1, itemSerializer)
                    else -> break
                }
            }

            if (current >= 0 && photoList != null) {
                ret.photoList = photoList
                ret.currentIndex = current
                ret.targetIndex = current
                ret.open = true
            }

            ret
        }
}
