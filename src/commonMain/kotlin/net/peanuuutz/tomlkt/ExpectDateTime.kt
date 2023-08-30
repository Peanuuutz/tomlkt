package net.peanuuutz.tomlkt

// -------- LocalDateTime --------

/**
 * A date-time without a time-zone in the ISO-8601 calendar system.
 *
 * For JVM, this is an alias of `java.time.LocalDateTime`. For other platforms,
 * this is `kotlinx.datetime.LocalDateTime`.
 */
public expect class LocalDateTime

/**
 * Creates a [LocalDateTime] by parsing [text] against ISO-8601 standard.
 */
public expect fun LocalDateTime(text: String): LocalDateTime

// -------- OffsetDateTime --------

/**
 * A date-time with an offset from UTC/Greenwich in the ISO-8601 calendar system.
 *
 * For JVM, this is an alias of `java.time.OffsetDateTime`. For other platforms,
 * this is `kotlinx.datetime.Instant`.
 */
public expect class OffsetDateTime

/**
 * Creates a [OffsetDateTime] by parsing [text] against ISO-8601 standard.
 */
public expect fun OffsetDateTime(text: String): OffsetDateTime

// -------- LocalDate --------

/**
 * A date without a time-zone in the ISO-8601 calendar system.
 *
 * For JVM, this is an alias of `java.time.LocalDate`. For other platforms, this
 * is `kotlinx.datetime.LocalDate`.
 */
public expect class LocalDate

/**
 * Creates a [LocalDate] by parsing [text] against ISO-8601 standard.
 */
public expect fun LocalDate(text: String): LocalDate

// -------- LocalTime --------

/**
 * A time without a time-zone in the ISO-8601 calendar system.
 *
 * For JVM, this is an alias of `java.time.LocalTime`. For other platforms, this
 * is `kotlinx.datetime.LocalTime`.
 */
public expect class LocalTime

/**
 * Creates a [LocalTime] by parsing [text] against ISO-8601 standard.
 */
public expect fun LocalTime(text: String): LocalTime
