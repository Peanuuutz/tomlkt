package net.peanuuutz.tomlkt

private const val debug: Boolean = true

internal fun printIfDebug(something: Any?) {
    if (debug)
        println(something)
}