/*
    Copyright 2023 Peanuuutz

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

package net.peanuuutz.tomlkt

import net.peanuuutz.tomlkt.internal.NonPrimitiveKeyException
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// -------- TomlArrayBuilder --------

/**
 * Creates a [TomlArray] by populating a [TomlArrayBuilder] with [block].
 *
 * ```kotlin
 * val array = buildTomlArray {
 *     table(TomlComment("Table is inlined if in block array.")) {
 *         literal("boolean", true)
 *     }
 *     literal(0b1101, TomlInteger(TomlInteger.Base.Bin), TomlComment("Integer."))
 *     array(TomlComment("This is an array.")) {
 *         literal("C:\\Windows", TomlLiteralString.Instance)
 *     }
 * }
 * ```
 *
 * will produce:
 *
 * ```toml
 * [
 *     # Table is inlined if in block array.
 *     { boolean = true },
 *     # Integer.
 *     0b1101,
 *     # This is an array.
 *     [ 'C:\Windows' ]
 * ]
 * ```
 */
public inline fun buildTomlArray(
    initialCapacity: Int = 8,
    block: TomlArrayBuilder.() -> Unit
): TomlArray {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

    return TomlArrayBuilder(initialCapacity).apply(block).build()
}

/**
 * The builder for `buildTomlArray { ... }` factory function.
 */
@BuilderDsl
public class TomlArrayBuilder(initialCapacity: Int = 8) {
    /**
     * Adds [element] to this array and annotates it with [elementAnnotations].
     */
    public fun element(
        element: TomlElement,
        elementAnnotations: List<Annotation> = emptyList()
    ) {
        elements.add(element)
        annotations.add(elementAnnotations)
    }

    /**
     * Adds [elements] and their [annotations] to this array.
     *
     * The order of these two lists corresponds. If `annotations` is shorter
     * than `elements`, the rest slots will be filled with empty list; if the
     * former is longer, the extra ones will be discarded.
     */
    public fun elements(
        elements: List<TomlElement>,
        annotations: List<List<Annotation>> = List(elements.size) { emptyList() }
    ) {
        this.elements.addAll(elements)
        this.annotations.addAll(alignElementAnnotations(elements, annotations))
    }

    /**
     * Creates a read-only copy based on current configuration.
     */
    public fun build(): TomlArray {
        return TomlArray(elements.toList(), annotations.toList())
    }

    // ======== Internal ========

    private val elements: MutableList<TomlElement> = ArrayList(initialCapacity)

    private val annotations: MutableList<List<Annotation>> = ArrayList(initialCapacity)
}

// ---- Extensions for TomlArrayBuilder ----

/**
 * Adds [element] to this array and annotates it with [elementAnnotations].
 */
public fun TomlArrayBuilder.element(
    element: TomlElement,
    vararg elementAnnotations: Annotation
) {
    element(element, elementAnnotations.asList())
}

/**
 * Adds [boolean] to this array and annotates it with [elementAnnotations].
 */
public fun TomlArrayBuilder.literal(
    boolean: Boolean,
    elementAnnotations: List<Annotation> = emptyList()
) {
    element(TomlLiteral(boolean), elementAnnotations)
}

/**
 * Adds [boolean] to this array and annotates it with [elementAnnotations].
 */
public fun TomlArrayBuilder.literal(
    boolean: Boolean,
    vararg elementAnnotations: Annotation
) {
    element(TomlLiteral(boolean), elementAnnotations.asList())
}

/**
 * Adds [integer] to this array and annotates it with [elementAnnotations].
 */
public fun TomlArrayBuilder.literal(
    integer: Long,
    elementAnnotations: List<Annotation> = emptyList()
) {
    element(TomlLiteral(integer), elementAnnotations)
}

/**
 * Adds [integer] to this array and annotates it with [elementAnnotations].
 */
public fun TomlArrayBuilder.literal(
    integer: Long,
    vararg elementAnnotations: Annotation
) {
    element(TomlLiteral(integer), elementAnnotations.asList())
}

/**
 * Adds [float] to this array and annotates it with [elementAnnotations].
 */
public fun TomlArrayBuilder.literal(
    float: Double,
    elementAnnotations: List<Annotation> = emptyList()
) {
    element(TomlLiteral(float), elementAnnotations)
}

/**
 * Adds [float] to this array and annotates it with [elementAnnotations].
 */
public fun TomlArrayBuilder.literal(
    float: Double,
    vararg elementAnnotations: Annotation
) {
    element(TomlLiteral(float), elementAnnotations.asList())
}

/**
 * Adds [string] to this array and annotates it with [elementAnnotations].
 */
public fun TomlArrayBuilder.literal(
    string: String,
    elementAnnotations: List<Annotation> = emptyList()
) {
    element(TomlLiteral(string), elementAnnotations)
}

/**
 * Adds [string] to this array and annotates it with [elementAnnotations].
 */
public fun TomlArrayBuilder.literal(
    string: String,
    vararg elementAnnotations: Annotation
) {
    element(TomlLiteral(string), elementAnnotations.asList())
}

/**
 * Adds [localDateTime] to this array and annotates it with [elementAnnotations].
 */
public fun TomlArrayBuilder.literal(
    localDateTime: NativeLocalDateTime,
    elementAnnotations: List<Annotation> = emptyList()
) {
    element(TomlLiteral(localDateTime), elementAnnotations)
}

/**
 * Adds [localDateTime] to this array and annotates it with [elementAnnotations].
 */
public fun TomlArrayBuilder.literal(
    localDateTime: NativeLocalDateTime,
    vararg elementAnnotations: Annotation
) {
    element(TomlLiteral(localDateTime), elementAnnotations.asList())
}

/**
 * Adds [offsetDateTime] to this array and annotates it with
 * [elementAnnotations].
 */
public fun TomlArrayBuilder.literal(
    offsetDateTime: NativeOffsetDateTime,
    elementAnnotations: List<Annotation> = emptyList()
) {
    element(TomlLiteral(offsetDateTime), elementAnnotations)
}

/**
 * Adds [offsetDateTime] to this array and annotates it with
 * [elementAnnotations].
 */
public fun TomlArrayBuilder.literal(
    offsetDateTime: NativeOffsetDateTime,
    vararg elementAnnotations: Annotation
) {
    element(TomlLiteral(offsetDateTime), elementAnnotations.asList())
}

/**
 * Adds [localDate] to this array and annotates it with [elementAnnotations].
 */
public fun TomlArrayBuilder.literal(
    localDate: NativeLocalDate,
    elementAnnotations: List<Annotation> = emptyList()
) {
    element(TomlLiteral(localDate), elementAnnotations)
}

/**
 * Adds [localDate] to this array and annotates it with [elementAnnotations].
 */
public fun TomlArrayBuilder.literal(
    localDate: NativeLocalDate,
    vararg elementAnnotations: Annotation
) {
    element(TomlLiteral(localDate), elementAnnotations.asList())
}

/**
 * Adds [localTime] to this array and annotates it with [elementAnnotations].
 */
public fun TomlArrayBuilder.literal(
    localTime: NativeLocalTime,
    elementAnnotations: List<Annotation> = emptyList()
) {
    element(TomlLiteral(localTime), elementAnnotations)
}

/**
 * Adds [localTime] to this array and annotates it with [elementAnnotations].
 */
public fun TomlArrayBuilder.literal(
    localTime: NativeLocalTime,
    vararg elementAnnotations: Annotation
) {
    element(TomlLiteral(localTime), elementAnnotations.asList())
}

/**
 * Adds a [TomlArray] built with [block] to this array and annotates it with
 * [elementAnnotations].
 */
public fun TomlArrayBuilder.array(
    elementAnnotations: List<Annotation> = emptyList(),
    block: TomlArrayBuilder.() -> Unit
) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

    element(buildTomlArray(block = block), elementAnnotations)
}

/**
 * Adds a [TomlArray] built with [block] to this array and annotates it with
 * [elementAnnotations].
 */
public fun TomlArrayBuilder.array(
    vararg elementAnnotations: Annotation,
    block: TomlArrayBuilder.() -> Unit
) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

    element(buildTomlArray(block = block), elementAnnotations.asList())
}

/**
 * Adds a [TomlTable] built with [block] to this array and annotates it with
 * [elementAnnotations].
 */
public fun TomlArrayBuilder.table(
    elementAnnotations: List<Annotation> = emptyList(),
    block: TomlTableBuilder.() -> Unit
) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

    element(buildTomlTable(block = block), elementAnnotations)
}

/**
 * Adds a [TomlTable] built with [block] to this array and annotates it with
 * [elementAnnotations].
 */
public fun TomlArrayBuilder.table(
    vararg elementAnnotations: Annotation,
    block: TomlTableBuilder.() -> Unit
) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

    element(buildTomlTable(block = block), elementAnnotations.asList())
}

// -------- TomlTableBuilder --------

/**
 * Creates a [TomlTable] by populating a [TomlTableBuilder] with [block].
 *
 * ```kotlin
 * val table = buildTomlTable {
 *     array("array-of-table") {
 *         table(TomlComment("Empty.")) {  }
 *         table(TomlComment("This is a comment for a single table.")) {
 *             literal("multiline", "Multiline\nString", TomlMultilineString.Instance)
 *         }
 *     }
 *     table("\n", TomlComment("Normie table.")) {
 *         literal("color", 0xFF_E4_90, TomlInteger(TomlInteger.Base.Hex, group = 2))
 *     }
 *     element("null", TomlNull, TomlComment("Null,\nas always."))
 *     literal("float", Double.NEGATIVE_INFINITY)
 * }
 * ```
 *
 * will produce:
 *
 * ```toml
 * # Null,
 * # as always.
 * null = null
 * float = -inf
 *
 * # Normie table.
 *
 * ["\n"]
 * color = 0xFF_E4_90
 *
 * # Empty.
 *
 * [[array-of-table]]
 *
 *
 * # This is a comment for a single table.
 *
 * [[array-of-table]]
 * multiline = """
 * Multiline
 * String"""
 * ```
 */
public inline fun buildTomlTable(
    initialCapacity: Int = 8,
    block: TomlTableBuilder.() -> Unit
): TomlTable {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

    return TomlTableBuilder(initialCapacity).apply(block).build()
}

/**
 * The builder for `buildTomlTable { ... }` factory function.
 */
@BuilderDsl
public class TomlTableBuilder(initialCapacity: Int = 8) {
    /**
     * Associates [element] with **primitive** [key] and annotates `element`
     * with [elementAnnotations].
     *
     * @throws NonPrimitiveKeyException if provided non-primitive key.
     */
    public fun element(
        key: Any?,
        element: TomlElement,
        elementAnnotations: List<Annotation> = emptyList()
    ) {
        val tomlKey = key.toTomlKey()
        elements[tomlKey] = element
        annotations[tomlKey] = elementAnnotations
    }

    /**
     * Adds [elements] and their [annotations] to this table.
     *
     * @throws NonPrimitiveKeyException if provided non-primitive key.
     */
    public fun elements(
        elements: Map<*, TomlElement>,
        annotations: Map<*, List<Annotation>> = emptyMap<Any?, _>()
    ) {
        this.elements.putAll(elements.toTomlMap())
        this.annotations.putAll(annotations.toTomlMap())
    }

    /**
     * Creates a read-only copy based on current configuration.
     */
    public fun build(): TomlTable {
        return TomlTable(elements.toMap(), annotations.toMap())
    }

    // ======== Internal ========

    private val elements: MutableMap<String, TomlElement> = LinkedHashMap(initialCapacity)

    private val annotations: MutableMap<String, List<Annotation>> = LinkedHashMap(initialCapacity)
}

// ---- Extensions for TomlTableBuilder ----

/**
 * Associates [element] with **primitive** [key] and annotates `element` with
 * [elementAnnotations].
 *
 * @throws NonPrimitiveKeyException if provided non-primitive key.
 */
public fun TomlTableBuilder.element(
    key: Any?,
    element: TomlElement,
    vararg elementAnnotations: Annotation
) {
    element(key, element, elementAnnotations.asList())
}

/**
 * Associates [boolean] with **primitive** [key] and annotates `boolean` with
 * [elementAnnotations].
 *
 * @throws NonPrimitiveKeyException if provided non-primitive key.
 */
public fun TomlTableBuilder.literal(
    key: Any?,
    boolean: Boolean,
    elementAnnotations: List<Annotation> = emptyList()
) {
    element(key, TomlLiteral(boolean), elementAnnotations)
}

/**
 * Associates [boolean] with **primitive** [key] and annotates `boolean` with
 * [elementAnnotations].
 *
 * @throws NonPrimitiveKeyException if provided non-primitive key.
 */
public fun TomlTableBuilder.literal(
    key: Any?,
    boolean: Boolean,
    vararg elementAnnotations: Annotation
) {
    element(key, TomlLiteral(boolean), elementAnnotations.asList())
}

/**
 * Associates [integer] with **primitive** [key] and annotates `integer` with
 * [elementAnnotations].
 *
 * @throws NonPrimitiveKeyException if provided non-primitive key.
 */
public fun TomlTableBuilder.literal(
    key: Any?,
    integer: Long,
    elementAnnotations: List<Annotation> = emptyList()
) {
    element(key, TomlLiteral(integer), elementAnnotations)
}

/**
 * Associates [integer] with **primitive** [key] and annotates `integer` with
 * [elementAnnotations].
 *
 * @throws NonPrimitiveKeyException if provided non-primitive key.
 */
public fun TomlTableBuilder.literal(
    key: Any?,
    integer: Long,
    vararg elementAnnotations: Annotation
) {
    element(key, TomlLiteral(integer), elementAnnotations.asList())
}

/**
 * Associates [float] with **primitive** [key] and annotates `float` with
 * [elementAnnotations].
 *
 * @throws NonPrimitiveKeyException if provided non-primitive key.
 */
public fun TomlTableBuilder.literal(
    key: Any?,
    float: Double,
    elementAnnotations: List<Annotation> = emptyList()
) {
    element(key, TomlLiteral(float), elementAnnotations)
}

/**
 * Associates [float] with **primitive** [key] and annotates `float` with
 * [elementAnnotations].
 *
 * @throws NonPrimitiveKeyException if provided non-primitive key.
 */
public fun TomlTableBuilder.literal(
    key: Any?,
    float: Double,
    vararg elementAnnotations: Annotation
) {
    element(key, TomlLiteral(float), elementAnnotations.asList())
}

/**
 * Associates [string] with **primitive** [key] and annotates `string` with
 * [elementAnnotations].
 *
 * @throws NonPrimitiveKeyException if provided non-primitive key.
 */
public fun TomlTableBuilder.literal(
    key: Any?,
    string: String,
    elementAnnotations: List<Annotation> = emptyList()
) {
    element(key, TomlLiteral(string), elementAnnotations)
}

/**
 * Associates [string] with **primitive** [key] and annotates `string` with
 * [elementAnnotations].
 *
 * @throws NonPrimitiveKeyException if provided non-primitive key.
 */
public fun TomlTableBuilder.literal(
    key: Any?,
    string: String,
    vararg elementAnnotations: Annotation
) {
    element(key, TomlLiteral(string), elementAnnotations.asList())
}

/**
 * Associates [localDateTime] with **primitive** [key] and annotates
 * `localDateTime` with [elementAnnotations].
 *
 * @throws NonPrimitiveKeyException if provided non-primitive key.
 */
public fun TomlTableBuilder.literal(
    key: Any?,
    localDateTime: NativeLocalDateTime,
    elementAnnotations: List<Annotation> = emptyList()
) {
    element(key, TomlLiteral(localDateTime), elementAnnotations)
}

/**
 * Associates [localDateTime] with **primitive** [key] and annotates
 * `localDateTime` with [elementAnnotations].
 *
 * @throws NonPrimitiveKeyException if provided non-primitive key.
 */
public fun TomlTableBuilder.literal(
    key: Any?,
    localDateTime: NativeLocalDateTime,
    vararg elementAnnotations: Annotation
) {
    element(key, TomlLiteral(localDateTime), elementAnnotations.asList())
}

/**
 * Associates [offsetDateTime] with **primitive** [key] and annotates
 * `offsetDateTime` with [elementAnnotations].
 *
 * @throws NonPrimitiveKeyException if provided non-primitive key.
 */
public fun TomlTableBuilder.literal(
    key: Any?,
    offsetDateTime: NativeOffsetDateTime,
    elementAnnotations: List<Annotation> = emptyList()
) {
    element(key, TomlLiteral(offsetDateTime), elementAnnotations)
}

/**
 * Associates [offsetDateTime] with **primitive** [key] and annotates
 * `offsetDateTime` with [elementAnnotations].
 *
 * @throws NonPrimitiveKeyException if provided non-primitive key.
 */
public fun TomlTableBuilder.literal(
    key: Any?,
    offsetDateTime: NativeOffsetDateTime,
    vararg elementAnnotations: Annotation
) {
    element(key, TomlLiteral(offsetDateTime), elementAnnotations.asList())
}

/**
 * Associates [localDate] with **primitive** [key] and annotates `localDate`
 * with [elementAnnotations].
 *
 * @throws NonPrimitiveKeyException if provided non-primitive key.
 */
public fun TomlTableBuilder.literal(
    key: Any?,
    localDate: NativeLocalDate,
    elementAnnotations: List<Annotation> = emptyList()
) {
    element(key, TomlLiteral(localDate), elementAnnotations)
}

/**
 * Associates [localDate] with **primitive** [key] and annotates `localDate`
 * with [elementAnnotations].
 *
 * @throws NonPrimitiveKeyException if provided non-primitive key.
 */
public fun TomlTableBuilder.literal(
    key: Any?,
    localDate: NativeLocalDate,
    vararg elementAnnotations: Annotation
) {
    element(key, TomlLiteral(localDate), elementAnnotations.asList())
}

/**
 * Associates [localTime] with **primitive** [key] and annotates `localTime`
 * with [elementAnnotations].
 *
 * @throws NonPrimitiveKeyException if provided non-primitive key.
 */
public fun TomlTableBuilder.literal(
    key: Any?,
    localTime: NativeLocalTime,
    elementAnnotations: List<Annotation> = emptyList()
) {
    element(key, TomlLiteral(localTime), elementAnnotations)
}

/**
 * Associates [localTime] with **primitive** [key] and annotates `localTime`
 * with [elementAnnotations].
 *
 * @throws NonPrimitiveKeyException if provided non-primitive key.
 */
public fun TomlTableBuilder.literal(
    key: Any?,
    localTime: NativeLocalTime,
    vararg elementAnnotations: Annotation
) {
    element(key, TomlLiteral(localTime), elementAnnotations.asList())
}

/**
 * Associates a [TomlArray] built with [block] with **primitive** [key] and
 * annotates the array with [elementAnnotations].
 *
 * @throws NonPrimitiveKeyException if provided non-primitive key.
 */
public fun TomlTableBuilder.array(
    key: Any?,
    elementAnnotations: List<Annotation> = emptyList(),
    block: TomlArrayBuilder.() -> Unit
) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

    element(key, buildTomlArray(block = block), elementAnnotations)
}

/**
 * Associates a [TomlArray] built with [block] with **primitive** [key] and
 * annotates the array with [elementAnnotations].
 *
 * @throws NonPrimitiveKeyException if provided non-primitive key.
 */
public fun TomlTableBuilder.array(
    key: Any?,
    vararg elementAnnotations: Annotation,
    block: TomlArrayBuilder.() -> Unit
) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

    element(key, buildTomlArray(block = block), elementAnnotations.asList())
}

/**
 * Associates a [TomlTable] built with [block] with **primitive** [key] and
 * annotates the table with [elementAnnotations].
 *
 * @throws NonPrimitiveKeyException if provided non-primitive key.
 */
public fun TomlTableBuilder.table(
    key: Any?,
    elementAnnotations: List<Annotation> = emptyList(),
    block: TomlTableBuilder.() -> Unit
) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

    element(key, buildTomlTable(block = block), elementAnnotations)
}

/**
 * Associates a [TomlTable] built with [block] with **primitive** [key] and
 * annotates the table with [elementAnnotations].
 *
 * @throws NonPrimitiveKeyException if provided non-primitive key.
 */
public fun TomlTableBuilder.table(
    key: Any?,
    vararg elementAnnotations: Annotation,
    block: TomlTableBuilder.() -> Unit
) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

    element(key, buildTomlTable(block = block), elementAnnotations.asList())
}

// ======== Internal ========

@DslMarker
private annotation class BuilderDsl

// Make sure the annotations are provided to the correct element.
private fun alignElementAnnotations(
    elements: List<TomlElement>,
    annotations: List<List<Annotation>>
): List<List<Annotation>> {
    val elementCount = elements.size
    val annotationsCount = annotations.size
    return when {
        elementCount == annotationsCount -> annotations
        elementCount > annotationsCount -> {
            buildList(elementCount) {
                addAll(annotations)
                repeat(elementCount - annotationsCount) {
                    add(emptyList())
                }
            }
        }
        else -> annotations.subList(0, elementCount)
    }
}

private fun <V> Map<*, V>.toTomlMap(): Map<String, V> {
    return when (val size = size) {
        0 -> emptyMap()
        1 -> {
            val (k, v) = iterator().next()
            mapOf(pair = k.toTomlKey() to v)
        }
        else -> {
            buildMap(size) {
                for ((k, v) in this@toTomlMap) {
                    put(k.toTomlKey(), v)
                }
            }
        }
    }
}
