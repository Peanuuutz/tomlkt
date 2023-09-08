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
