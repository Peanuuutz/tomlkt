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

package net.peanuuutz.tomlkt.internal.parser

import net.peanuuutz.tomlkt.TomlElement
import net.peanuuutz.tomlkt.internal.Path

internal sealed class TreeNode(val key: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as TreeNode
        return key == other.key
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}

internal class KeyNode(
    key: String,
    val isLast: Boolean
) : TreeNode(key) {
    val children: MutableMap<String, TreeNode> = mutableMapOf()

    fun add(node: TreeNode) {
        children[node.key] = node
    }

    operator fun get(key: String): TreeNode? {
        return children[key]
    }
}

internal class ArrayNode(key: String) : TreeNode(key) {
    val children: MutableList<KeyNode> = mutableListOf()

    fun add(node: KeyNode) {
        children.add(node)
    }

    operator fun get(index: Int): KeyNode {
        return children[index]
    }
}

internal class ValueNode(
    key: String,
    val element: TomlElement
) : TreeNode(key)

// -------- Extensions --------

internal fun KeyNode.addByPath(
    path: Path,
    node: TreeNode,
    arrayOfTableIndices: Map<Path, Int>?
): Boolean {
    return addByPathRecursively(path, node, arrayOfTableIndices, 0)
}

private tailrec fun KeyNode.addByPathRecursively(
    path: Path,
    node: TreeNode,
    arrayOfTableIndices: Map<Path, Int>?,
    index: Int
): Boolean {
    val child = get(path[index])
    if (index == path.lastIndex) {
        return when {
            child == null -> {
                add(node)
                true
            }
            child !is KeyNode -> {
                false
            }
            node !is KeyNode -> {
                false
            }
            else -> {
                // If false, this table is a super-table after a sub-table.
                child.isLast.not()
            }
        }
    }
    return when (child) {
        null -> {
            val intermediate = KeyNode(path[index], isLast = node is ValueNode)
            add(intermediate)
            intermediate.addByPathRecursively(path, node, arrayOfTableIndices, index + 1)
        }
        is KeyNode -> {
            child.addByPathRecursively(path, node, arrayOfTableIndices, index + 1)
        }
        is ArrayNode -> {
            check(arrayOfTableIndices != null)
            val currentPath = path.subList(0, index + 1)
            val childIndex = arrayOfTableIndices[currentPath]!!
            val grandChild = child[childIndex]
            grandChild.addByPathRecursively(path, node, arrayOfTableIndices, index + 1)
        }
        is ValueNode -> {
            false
        }
    }
}

internal fun <N : TreeNode> KeyNode.getByPath(
    path: Path,
    arrayOfTableIndices: Map<Path, Int>?
): N {
    return getByPathRecursively(path, arrayOfTableIndices, 0)
}

private tailrec fun <N : TreeNode> KeyNode.getByPathRecursively(
    path: Path,
    arrayOfTableIndices: Map<Path, Int>?,
    index: Int
): N {
    val child = get(path[index])
    if (index == path.lastIndex) {
        @Suppress("UNCHECKED_CAST")
        return child as? N ?: error("Node on $path not found")
    }
    return when (child) {
        null, is ValueNode -> {
            error("Node on $path not found")
        }
        is KeyNode -> {
            child.getByPathRecursively(path, arrayOfTableIndices, index + 1)
        }
        is ArrayNode -> {
            check(arrayOfTableIndices != null)
            val currentPath = path.subList(0, index + 1)
            val childIndex = arrayOfTableIndices[currentPath]!!
            val grandChild = child[childIndex]
            grandChild.getByPathRecursively(path, arrayOfTableIndices, index + 1)
        }
    }
}
