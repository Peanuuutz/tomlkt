package net.peanuuutz.tomlkt

private const val debug: Boolean = false

internal fun printIfDebug(something: Any?) {
    if (debug)
        println(something)
}