package net.peanuuutz.tomlkt

private const val DEBUG: Boolean = true

internal fun printIfDebug(something: Any?) {
    if (DEBUG) {
        println(something)
    }
}
