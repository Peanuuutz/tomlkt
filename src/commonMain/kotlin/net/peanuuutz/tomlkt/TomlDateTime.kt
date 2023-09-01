package net.peanuuutz.tomlkt

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
 */
public typealias TomlLocalDateTime = @Serializable(LocalDateTimeSerializer::class) NativeLocalDateTime

/**
 * Creates a [TomlLocalDateTime] by parsing [text] against ISO-8601 standard.
 */
public fun TomlLocalDateTime(text: String): TomlLocalDateTime {
    return NativeLocalDateTime(text)
}

// -------- TomlOffsetDateTime --------

/**
 * A date-time with an offset from UTC/Greenwich in the ISO-8601 calendar system.
 *
 * For JVM, this is an alias of `java.time.OffsetDateTime`. For other platforms,
 * this is `kotlinx.datetime.Instant`.
 */
public typealias TomlOffsetDateTime = @Serializable(OffsetDateTimeSerializer::class) NativeOffsetDateTime

/**
 * Creates a [TomlOffsetDateTime] by parsing [text] against ISO-8601 standard.
 */
public fun TomlOffsetDateTime(text: String): TomlOffsetDateTime {
    return NativeOffsetDateTime(text)
}

// -------- TomlLocalDate --------

/**
 * A date without a time-zone in the ISO-8601 calendar system.
 *
 * For JVM, this is an alias of `java.time.LocalDate`. For other platforms, this
 * is `kotlinx.datetime.LocalDate`.
 */
public typealias TomlLocalDate = @Serializable(LocalDateSerializer::class) NativeLocalDate

/**
 * Creates a [TomlLocalDate] by parsing [text] against ISO-8601 standard.
 */
public fun TomlLocalDate(text: String): TomlLocalDate {
    return NativeLocalDate(text)
}

// -------- TomlLocalTime --------

/**
 * A time without a time-zone in the ISO-8601 calendar system.
 *
 * For JVM, this is an alias of `java.time.LocalTime`. For other platforms, this
 * is `kotlinx.datetime.LocalTime`.
 */
public typealias TomlLocalTime = @Serializable(LocalTimeSerializer::class) NativeLocalTime

/**
 * Creates a [TomlLocalTime] by parsing [text] against ISO-8601 standard.
 */
public fun TomlLocalTime(text: String): TomlLocalTime {
    return NativeLocalTime(text)
}
