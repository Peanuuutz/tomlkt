package net.peanuuutz.tomlkt.internal

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor

private val UnsignedIntegerDescriptors: Set<SerialDescriptor> = setOf(
    UByte.serializer().descriptor,
    UShort.serializer().descriptor,
    UInt.serializer().descriptor,
    ULong.serializer().descriptor
)

internal val SerialDescriptor.isUnsignedInteger: Boolean
    get() = this.isInline && this in UnsignedIntegerDescriptors
