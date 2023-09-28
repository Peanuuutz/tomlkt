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

package net.peanuuutz.tomlkt.internal

import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private const val TotalCapacityPropertyKey: String = "net.peanuuutz.tomlkt.total.capacity"

// At most 32 parsers work in parallel.
private const val DefaultTotalCapacity: Int = 32 * BufferSize

private val TotalCapacity: Int = System.getProperty(TotalCapacityPropertyKey).toIntOrNull() ?: DefaultTotalCapacity

internal actual object BufferPool {
    private val pool: ArrayDeque<CharArray> = ArrayDeque()

    private var currentCapacity: Int = 0

    private val lock: Lock = ReentrantLock()

    actual fun take(): CharArray {
        return lock.withLock {
            val buffer = pool.removeLastOrNull()
            if (buffer != null) {
                currentCapacity -= buffer.size
                buffer
            } else {
                CharArray(BufferSize)
            }
        }
    }

    actual fun release(buffer: CharArray) {
        lock.withLock {
            if (currentCapacity + buffer.size <= TotalCapacity) {
                currentCapacity += buffer.size
                pool.addLast(buffer)
            }
        }
    }
}
