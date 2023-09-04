package net.peanuuutz.tomlkt

import java.time.format.DateTimeParseException
import java.time.LocalDate as JvmLocalDate
import java.time.LocalDateTime as JvmLocalDateTime
import java.time.LocalTime as JvmLocalTime
import java.time.OffsetDateTime as JvmOffsetDateTime

// -------- NativeLocalDateTime --------

public actual typealias NativeLocalDateTime = JvmLocalDateTime

public actual fun NativeLocalDateTime(text: String): NativeLocalDateTime {
    return try {
        JvmLocalDateTime.parse(text)
    } catch (e: DateTimeParseException) {
        throw IllegalArgumentException(e)
    }
}

// -------- NativeOffsetDateTime --------

public actual typealias NativeOffsetDateTime = JvmOffsetDateTime

public actual fun NativeOffsetDateTime(text: String): NativeOffsetDateTime {
    return try {
        JvmOffsetDateTime.parse(text)
    } catch (e: DateTimeParseException) {
        throw IllegalArgumentException(e)
    }
}

// -------- NativeLocalDate --------

public actual typealias NativeLocalDate = JvmLocalDate

public actual fun NativeLocalDate(text: String): NativeLocalDate {
    return try {
        JvmLocalDate.parse(text)
    } catch (e: DateTimeParseException) {
        throw IllegalArgumentException(e)
    }
}

// -------- NativeLocalTime --------

public actual typealias NativeLocalTime = JvmLocalTime

public actual fun NativeLocalTime(text: String): NativeLocalTime {
    return try {
        JvmLocalTime.parse(text)
    } catch (e: DateTimeParseException) {
        throw IllegalArgumentException(e)
    }
}
