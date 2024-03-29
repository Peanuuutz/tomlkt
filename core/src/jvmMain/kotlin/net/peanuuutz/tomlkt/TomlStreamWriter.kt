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

import java.io.OutputStream

@Deprecated(
    message = "This writer is expensive. Please use TomlNativeWriter instead.",
    replaceWith = ReplaceWith(
        expression = "TomlNativeWriter(outputStream.writer())",
        imports = [
            "net.peanuuutz.tomlkt.TomlNativeWriter",
            "kotlin.io.path.writer"
        ]
    )
)
public class TomlStreamWriter(
    private val outputStream: OutputStream
) : AbstractTomlWriter() {
    override fun writeString(string: String) {
        outputStream.write(string.toByteArray())
    }
}
