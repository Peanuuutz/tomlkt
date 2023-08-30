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

// -------- Encoding --------

internal sealed class TomlEncodingException : SerializationException {
    constructor()
    constructor(message: String) : super(message)
}

internal class NonPrimitiveKeyException : TomlEncodingException()

internal class UnsupportedSerialKindException(kind: SerialKind) : TomlEncodingException(
    message = kind.toString()
)

internal class NullInArrayOfTableException : TomlEncodingException(
    message = "Null is not allowed in array of table, " +
            "please mark the corresponding property as @TomlBlockArray or @TomlInline"
)

internal class EmptyArrayOfTableInMapException : TomlEncodingException(
    message = "Empty array of table can only be the first in map"
)

// -------- Decoding --------

internal sealed class TomlDecodingException : SerializationException {
    constructor()
    constructor(message: String) : super(message)
}

internal class UnexpectedTokenException(token: Char, line: Int) : TomlDecodingException(
    message = run {
        val tokenRepresentation = if (token != '\'') token.escape() else "\\'"
        "'$tokenRepresentation' (L$line)"
    }
)

internal class IncompleteException(line: Int) : TomlDecodingException(
    message = "(L$line)"
)

internal class ConflictEntryException(path: Path) : TomlDecodingException(
    message = path.joinToString(".") { it.escape().doubleQuotedIfNotPure() }
)

internal class UnknownKeyException(key: String) : TomlDecodingException(
    message = key
)
