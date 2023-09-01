package net.peanuuutz.tomlkt

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate as KotlinLocalDate
import kotlinx.datetime.LocalDateTime as KotlinLocalDateTime
import kotlinx.datetime.LocalTime as KotlinLocalTime

// -------- NativeLocalDateTime --------

public actual typealias NativeLocalDateTime = KotlinLocalDateTime

public actual fun NativeLocalDateTime(text: String): NativeLocalDateTime {
    return KotlinLocalDateTime.parse(text)
}

// -------- NativeOffsetDateTime --------

public actual typealias NativeOffsetDateTime = Instant

public actual fun NativeOffsetDateTime(text: String): NativeOffsetDateTime {
    return Instant.parse(text)
}

// -------- NativeLocalDate --------

public actual typealias NativeLocalDate = KotlinLocalDate

public actual fun NativeLocalDate(text: String): NativeLocalDate {
    return KotlinLocalDate.parse(text)
}

// -------- NativeLocalTime --------

public actual typealias NativeLocalTime = KotlinLocalTime

public actual fun NativeLocalTime(text: String): NativeLocalTime {
    return KotlinLocalTime.parse(text)
}
