package net.peanuuutz.tomlkt

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate as KotlinLocalDate
import kotlinx.datetime.LocalDateTime as KotlinLocalDateTime
import kotlinx.datetime.LocalTime as KotlinLocalTime

// -------- LocalDateTime --------

public actual typealias LocalDateTime = KotlinLocalDateTime

public actual fun LocalDateTime(text: String): LocalDateTime {
    return KotlinLocalDateTime.parse(text)
}

// -------- OffsetDateTime --------

public actual typealias OffsetDateTime = Instant

public actual fun OffsetDateTime(text: String): OffsetDateTime {
    return Instant.parse(text)
}

// -------- LocalDate --------

public actual typealias LocalDate = KotlinLocalDate

public actual fun LocalDate(text: String): LocalDate {
    return KotlinLocalDate.parse(text)
}

// -------- LocalTime --------

public actual typealias LocalTime = KotlinLocalTime

public actual fun LocalTime(text: String): LocalTime {
    return KotlinLocalTime.parse(text)
}
