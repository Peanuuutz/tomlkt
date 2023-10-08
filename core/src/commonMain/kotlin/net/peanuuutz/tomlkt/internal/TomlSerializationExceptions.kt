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

import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialKind
import kotlin.reflect.KClass

// -------- Encoding --------

internal sealed class TomlEncodingException(message: String) : SerializationException(message)

internal fun throwSubclassNotRegistered(subclass: KClass<*>, baseClass: KClass<*>): Nothing {
    val message = "Class ${subclass.simpleName} is not registered for polymorphic serialization " +
            "in the scope of ${baseClass.simpleName}. Mark ${baseClass.simpleName} as sealed or " +
            "register ${subclass.simpleName} in a serializers module (and switch out the default " +
            "one in the Toml {  } factory function)"
    error(message)
}

// ---- NonPrimitiveKeyException ----

internal class NonPrimitiveKeyException(message: String) : TomlEncodingException(message)

internal fun throwNonPrimitiveKey(key: Any?): Nothing {
    throw NonPrimitiveKeyException(key.toString())
}

// ---- UnsupportedSerialKindException ----

internal class UnsupportedSerialKindException(message: String) : TomlEncodingException(message)

internal fun throwUnsupportedSerialKind(kind: SerialKind): Nothing {
    throw UnsupportedSerialKindException(kind.toString())
}

internal fun throwUnsupportedSerialKind(message: String): Nothing {
    throw UnsupportedSerialKindException(message)
}

// ---- PolymorphicCollectionException ----

private const val PolymorphicCollection: String = "Collection-like type cannot be polymorphic"

internal class PolymorphicCollectionException : TomlEncodingException(PolymorphicCollection)

internal fun throwPolymorphicCollection(): Nothing {
    throw PolymorphicCollectionException()
}

// ---- NullInArrayOfTableException ----

internal class NullInArrayOfTableException(message: String) : TomlEncodingException(message)

internal fun throwNullInArrayOfTable(path: List<String>): Nothing {
    val pathString = path.joinToString(separator = ".")
    val message = "Null is not allowed in array of table. Please annotate the corresponding property " +
            "(at $pathString) with @TomlBlockArray or @TomlInline"
    throw NullInArrayOfTableException(message)
}

// ---- EmptyArrayOfTableInMapException ----

private const val EmptyArrayOfTableInMap: String = "At most one empty array of table is allowed in a map"

internal class EmptyArrayOfTableInMapException : TomlEncodingException(EmptyArrayOfTableInMap)

internal fun throwEmptyArrayOfTableInMap(): Nothing {
    throw EmptyArrayOfTableInMapException()
}

// -------- Decoding --------

internal sealed class TomlDecodingException(message: String) : SerializationException(message)

// ---- UnexpectedTokenException ----

internal class UnexpectedTokenException(message: String) : TomlDecodingException(message)

internal fun throwUnexpectedToken(token: Char, line: Int): Nothing {
    val tokenRepresentation = if (token != '\'') token.escape() else "\\'"
    val message = "'$tokenRepresentation' (L$line)"
    throw UnexpectedTokenException(message)
}

// ---- IncompleteException ----

internal class IncompleteException(message: String) : TomlDecodingException(message)

internal fun throwIncomplete(line: Int): Nothing {
    throw IncompleteException("(L$line)")
}

// ---- ConflictEntryException ----

internal class ConflictEntryException(message: String) : TomlDecodingException(message)

internal fun throwConflictEntry(path: Path): Nothing {
    val message = path.joinToString(".") { key ->
        key.escape().doubleQuotedIfNotPure()
    }
    throw ConflictEntryException(message)
}

// ---- UnknownKeyException ----

internal class UnknownKeyException(message: String) : TomlDecodingException(message)

internal fun throwUnknownKey(key: String): Nothing {
    throw UnknownKeyException(key)
}
