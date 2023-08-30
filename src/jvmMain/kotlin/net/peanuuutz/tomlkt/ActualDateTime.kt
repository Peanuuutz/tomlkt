package net.peanuuutz.tomlkt

import java.time.format.DateTimeParseException
import java.time.LocalDate as JvmLocalDate
import java.time.LocalDateTime as JvmLocalDateTime
import java.time.LocalTime as JvmLocalTime
import java.time.OffsetDateTime as JvmOffsetDateTime

// -------- LocalDateTime --------

public actual typealias LocalDateTime = JvmLocalDateTime

public actual fun LocalDateTime(text: String): LocalDateTime {
    return try {
        JvmLocalDateTime.parse(text)
    } catch (e: DateTimeParseException) {
        throw IllegalArgumentException(e)
    }
}

// -------- OffsetDateTime --------

public actual typealias OffsetDateTime = JvmOffsetDateTime

public actual fun OffsetDateTime(text: String): OffsetDateTime {
    return try {
        JvmOffsetDateTime.parse(text)
    } catch (e: DateTimeParseException) {
        throw IllegalArgumentException(e)
    }
}

// -------- LocalDate --------

public actual typealias LocalDate = JvmLocalDate

public actual fun LocalDate(text: String): LocalDate {
    return try {
        JvmLocalDate.parse(text)
    } catch (e: DateTimeParseException) {
        throw IllegalArgumentException(e)
    }
}

// -------- LocalTime --------

public actual typealias LocalTime = JvmLocalTime

public actual fun LocalTime(text: String): LocalTime {
    return try {
        JvmLocalTime.parse(text)
    } catch (e: DateTimeParseException) {
        throw IllegalArgumentException(e)
    }
}
