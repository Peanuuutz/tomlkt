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

package net.peanuuutz.tomlkt.internal.parser

import net.peanuuutz.tomlkt.TomlElement
import net.peanuuutz.tomlkt.internal.ConflictEntryException

internal typealias Path = List<String>

internal sealed class TreeNode(val key: String) {
    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        return key == (other as TreeNode).key
    }

    final override fun hashCode(): Int = key.hashCode()
}

internal class KeyNode(key: String) : TreeNode(key) {
    private val _children: MutableSet<TreeNode> = mutableSetOf()
    val children: Set<TreeNode> get() = _children

    fun add(node: TreeNode): Boolean = _children.add(node)

    operator fun get(key: String): TreeNode? = _children.find { it.key == key }
}

internal class ArrayNode(key: String) : TreeNode(key) {
    private val _array: MutableList<KeyNode> = mutableListOf()
    val array: List<KeyNode> get() = _array

    fun add(node: KeyNode): Boolean = _array.add(node)

    operator fun get(index: Int): KeyNode = _array[index]
}

internal class ValueNode(key: String, val value: TomlElement) : TreeNode(key)

// Extensions

internal fun KeyNode.addByPath(
    path: Path,
    node: TreeNode,
    arrayOfTableIndices: Map<Path, Int>?
) = addByPathRecursively(path, node, arrayOfTableIndices, 0)

private tailrec fun KeyNode.addByPathRecursively(
    path: Path,
    node: TreeNode,
    arrayOfTableIndices: Map<Path, Int>?,
    index: Int
) {
    val child = get(path[index])
    if (index == path.lastIndex) {
        if (child != null)
            throw ConflictEntryException(path)
        add(node)
    } else when (child) {
        null -> KeyNode(path[index]).also(this::add).addByPathRecursively(path, node, arrayOfTableIndices, index + 1)
        is KeyNode -> child.addByPathRecursively(path, node, arrayOfTableIndices, index + 1)
        is ArrayNode -> {
            require(arrayOfTableIndices != null)
            child[arrayOfTableIndices[path.subList(0, index + 1)]!!].addByPathRecursively(path, node, arrayOfTableIndices, index + 1)
        }
        is ValueNode -> throw ConflictEntryException(path)
    }
}

internal fun <N : TreeNode> KeyNode.getByPath(
    path: Path,
    arrayOfTableIndices: Map<Path, Int>?
): N = getByPathRecursively(path, arrayOfTableIndices, 0)

@Suppress("UNCHECKED_CAST")
private tailrec fun <N : TreeNode> KeyNode.getByPathRecursively(
    path: Path,
    arrayOfTableIndices: Map<Path, Int>?,
    index: Int
): N {
    val child = get(path[index])
    return if (index == path.lastIndex)
        child as? N ?: error("Node on $path not found")
    else when (child) {
        null, is ValueNode -> error("Node on $path not found")
        is KeyNode -> child.getByPathRecursively<N>(path, arrayOfTableIndices, index + 1)
        is ArrayNode -> {
            require(arrayOfTableIndices != null)
            child[arrayOfTableIndices[path.subList(0, index + 1)]!!].getByPathRecursively(path, arrayOfTableIndices, index + 1)
        }
    }
}