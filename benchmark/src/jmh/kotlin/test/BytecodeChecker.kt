package test

import kotlinx.serialization.Serializable

@Serializable
data class M(
    val vs: List<V>
) {
    @Serializable
    @JvmInline
    value class V(val ns: String?)
}
