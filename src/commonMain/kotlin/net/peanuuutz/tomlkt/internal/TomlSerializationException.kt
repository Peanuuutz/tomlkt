/*
    Copyright 2022 Peanuuutz

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

import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialKind
import net.peanuuutz.tomlkt.internal.parser.Path

internal sealed class TomlEncodingException : SerializationException {
    constructor()
    constructor(message: String) : super(message)
}

internal class NonPrimitiveKeyException : TomlEncodingException()

internal class UnsupportedSerialKindException(kind: SerialKind) : TomlEncodingException("$kind")

internal class NullInArrayOfTableException : TomlEncodingException(
    message = "Due to encoding process, no null is allowed in array of table, " +
            "please mark the corresponding property as @TomlInline"
)

internal sealed class TomlDecodingException(message: String) : SerializationException(message)

internal class UnexpectedTokenException(token: Char, line: Int) : TomlDecodingException(
    message = "'${if (token != '\'') token.escape() else "\\'"}' (L$line)"
)

internal class IncompleteException(line: Int) : TomlDecodingException("(L$line)")

internal class ConflictEntryException(path: Path) : TomlDecodingException(
    message = path.joinToString(".") { it.escape().doubleQuotedIfNeeded() }
)

internal class UnknownKeyException(key: String) : TomlDecodingException(key)