package net.peanuuutz.tomlkt

// -------- NativeLocalDateTime --------

/**
 * A date-time without a time-zone in the ISO-8601 calendar system.
 *
 * For JVM, this is an alias of `java.time.LocalDateTime`. For other platforms,
 * this is `kotlinx.datetime.LocalDateTime`.
 *
 * This type only connects to platform-specific type, so is not serializable.
 * See [TomlLocalDateTime] for the serializable one.
 */
public expect class NativeLocalDateTime

/**
 * Creates a [NativeLocalDateTime] by parsing [text] against ISO-8601 standard.
 */
public expect fun NativeLocalDateTime(text: String): NativeLocalDateTime

// -------- NativeOffsetDateTime --------

/**
 * A date-time with an offset from UTC/Greenwich in the ISO-8601 calendar system.
 *
 * For JVM, this is an alias of `java.time.OffsetDateTime`. For other platforms,
 * this is `kotlinx.datetime.Instant`.
 *
 * This type only connects to platform-specific type, so is not serializable.
 * See [TomlOffsetDateTime] for the serializable one.
 */
public expect class NativeOffsetDateTime

/**
 * Creates a [NativeOffsetDateTime] by parsing [text] against ISO-8601 standard.
 */
public expect fun NativeOffsetDateTime(text: String): NativeOffsetDateTime

// -------- NativeLocalDate --------

/**
 * A date without a time-zone in the ISO-8601 calendar system.
 *
 * For JVM, this is an alias of `java.time.LocalDate`. For other platforms, this
 * is `kotlinx.datetime.LocalDate`.
 *
 * This type only connects to platform-specific type, so is not serializable.
 * See [TomlLocalDate] for the serializable one.
 */
public expect class NativeLocalDate

/**
 * Creates a [NativeLocalDate] by parsing [text] against ISO-8601 standard.
 */
public expect fun NativeLocalDate(text: String): NativeLocalDate

// -------- NativeLocalTime --------

/**
 * A time without a time-zone in the ISO-8601 calendar system.
 *
 * For JVM, this is an alias of `java.time.LocalTime`. For other platforms, this
 * is `kotlinx.datetime.LocalTime`.
 *
 * This type only connects to platform-specific type, so is not serializable.
 * See [TomlLocalTime] for the serializable one.
 */
public expect class NativeLocalTime

/**
 * Creates a [NativeLocalTime] by parsing [text] against ISO-8601 standard.
 */
public expect fun NativeLocalTime(text: String): NativeLocalTime
