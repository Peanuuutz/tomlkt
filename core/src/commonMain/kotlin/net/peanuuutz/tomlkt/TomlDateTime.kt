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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import net.peanuuutz.tomlkt.internal.LocalDateSerializer
import net.peanuuutz.tomlkt.internal.LocalDateTimeSerializer
import net.peanuuutz.tomlkt.internal.LocalTimeSerializer
import net.peanuuutz.tomlkt.internal.OffsetDateTimeSerializer

// -------- TomlLocalDateTime --------

/**
 * A date-time without a time-zone in the ISO-8601 calendar system.
 *
 * For JVM, this is an alias of `java.time.LocalDateTime`. For other platforms,
 * this is `kotlinx.datetime.LocalDateTime`.
 *
 * This type has builtin support for
 * [serialization][TomlLocalDateTimeSerializer].
 *
 * @see NativeLocalDateTime
 */
public typealias TomlLocalDateTime = @Serializable(LocalDateTimeSerializer::class) NativeLocalDateTime

/**
 * Creates a [TomlLocalDateTime] by parsing [text] against ISO-8601 standard.
 */
public fun TomlLocalDateTime(text: String): TomlLocalDateTime {
    return NativeLocalDateTime(text)
}

/**
 * Retrieves the default [KSerializer] for [NativeLocalDateTime] in TOML.
 */
@Suppress("FunctionName")
public fun TomlLocalDateTimeSerializer(): KSerializer<NativeLocalDateTime> {
    return LocalDateTimeSerializer
}

// -------- TomlOffsetDateTime --------

/**
 * A date-time with an offset from UTC/Greenwich in the ISO-8601 calendar system.
 *
 * For JVM, this is an alias of `java.time.OffsetDateTime`. For other platforms,
 * this is `kotlinx.datetime.Instant`.
 *
 * This type has builtin support for
 * [serialization][TomlOffsetDateTimeSerializer].
 *
 * @see NativeOffsetDateTime
 */
public typealias TomlOffsetDateTime = @Serializable(OffsetDateTimeSerializer::class) NativeOffsetDateTime

/**
 * Creates a [TomlOffsetDateTime] by parsing [text] against ISO-8601 standard.
 */
public fun TomlOffsetDateTime(text: String): TomlOffsetDateTime {
    return NativeOffsetDateTime(text)
}

/**
 * Retrieves the default [KSerializer] for [NativeOffsetDateTime] in TOML.
 */
@Suppress("FunctionName")
public fun TomlOffsetDateTimeSerializer(): KSerializer<NativeOffsetDateTime> {
    return OffsetDateTimeSerializer
}

// -------- TomlLocalDate --------

/**
 * A date without a time-zone in the ISO-8601 calendar system.
 *
 * For JVM, this is an alias of `java.time.LocalDate`. For other platforms, this
 * is `kotlinx.datetime.LocalDate`.
 *
 * This type has builtin support for [serialization][TomlLocalDateSerializer].
 *
 * @see NativeLocalDate
 */
public typealias TomlLocalDate = @Serializable(LocalDateSerializer::class) NativeLocalDate

/**
 * Creates a [TomlLocalDate] by parsing [text] against ISO-8601 standard.
 */
public fun TomlLocalDate(text: String): TomlLocalDate {
    return NativeLocalDate(text)
}

/**
 * Retrieves the default [KSerializer] for [NativeLocalDate] in TOML.
 */
@Suppress("FunctionName")
public fun TomlLocalDateSerializer(): KSerializer<NativeLocalDate> {
    return LocalDateSerializer
}

// -------- TomlLocalTime --------

/**
 * A time without a time-zone in the ISO-8601 calendar system.
 *
 * For JVM, this is an alias of `java.time.LocalTime`. For other platforms, this
 * is `kotlinx.datetime.LocalTime`.
 *
 * This type has builtin support for [serialization][TomlLocalTimeSerializer].
 *
 * @see NativeLocalTime
 */
public typealias TomlLocalTime = @Serializable(LocalTimeSerializer::class) NativeLocalTime

/**
 * Creates a [TomlLocalTime] by parsing [text] against ISO-8601 standard.
 */
public fun TomlLocalTime(text: String): TomlLocalTime {
    return NativeLocalTime(text)
}

/**
 * Retrieves the default [KSerializer] for [NativeLocalTime] in TOML.
 */
@Suppress("FunctionName")
public fun TomlLocalTimeSerializer(): KSerializer<NativeLocalTime> {
    return LocalTimeSerializer
}
