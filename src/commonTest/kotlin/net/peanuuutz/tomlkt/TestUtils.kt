package net.peanuuutz.tomlkt

private const val DEBUG: Boolean = false

internal fun printIfDebug(something: Any?) {
    if (DEBUG) {
        println(something)
    }
}
