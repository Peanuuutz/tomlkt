package test

import kotlinx.serialization.Serializable

@Serializable
sealed class B1 {
    abstract val i: Int
}

@Serializable
data class M1(
    override val i: Int
) : B1()
