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

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate as KotlinLocalDate
import kotlinx.datetime.LocalDateTime as KotlinLocalDateTime
import kotlinx.datetime.LocalTime as KotlinLocalTime

// -------- NativeLocalDateTime --------

public actual typealias NativeLocalDateTime = KotlinLocalDateTime

public actual fun NativeLocalDateTime(text: String): NativeLocalDateTime {
    return KotlinLocalDateTime.parse(text)
}

// -------- NativeOffsetDateTime --------

@ExperimentalTime
public actual typealias NativeOffsetDateTime = Instant

@ExperimentalTime
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
