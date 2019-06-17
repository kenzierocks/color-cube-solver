/*
 * This file is part of color-cube-solver, licensed under the MIT License (MIT).
 *
 * Copyright (c) Octavia Togami <https://octyl.net>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.octyl.colorcube

// This is simple math only, we should probably inline it.
@Suppress("NOTHING_TO_INLINE")
inline fun index(face: Face, x: Int, y: Int, size: Int) =
    face.ordinal + (x * FACES) + (y * FACES * size)

private fun Face.indexesHT(size: Int, reverse: Boolean = false): Sequence<Int> {
    return (0 until size).asSequence().map {
        index(this, it, 0, size)
    }.let { if (reverse) it.toList().asReversed().asSequence() else it }
}

private fun Face.indexesHB(size: Int, reverse: Boolean = false): Sequence<Int> {
    return (0 until size).asSequence().map {
        index(this, it, size - 1, size)
    }.let { if (reverse) it.toList().asReversed().asSequence() else it }
}

private fun Face.indexesVL(size: Int, reverse: Boolean = false): Sequence<Int> {
    return (0 until size).asSequence().map {
        index(this, 0, it, size)
    }.let { if (reverse) it.toList().asReversed().asSequence() else it }
}

private fun Face.indexesVR(size: Int, reverse: Boolean = false): Sequence<Int> {
    return (0 until size).asSequence().map {
        index(this, size - 1, it, size)
    }.let { if (reverse) it.toList().asReversed().asSequence() else it }
}

/**
 * Collect the indices for the outer ring of a rotation for this face,
 * in the clockwise direction
 */
fun outerRingIndices(face: Face, size: Int): Sequence<Int> {
    return when (face) {
        Face.FRONT -> Face.UP.indexesHB(size) +
            Face.RIGHT.indexesVL(size) +
            Face.DOWN.indexesHT(size) +
            Face.LEFT.indexesVR(size)
        Face.LEFT -> Face.UP.indexesVL(size) +
            Face.FRONT.indexesVL(size) +
            Face.DOWN.indexesVL(size) +
            Face.BACK.indexesVL(size)
        Face.RIGHT -> Face.UP.indexesVR(size, reverse = true) +
            Face.BACK.indexesVR(size, reverse = true) +
            Face.DOWN.indexesVR(size, reverse = true) +
            Face.FRONT.indexesVR(size, reverse = true)
        Face.BACK -> Face.DOWN.indexesHB(size) +
            Face.RIGHT.indexesVR(size, reverse = true) +
            Face.UP.indexesHT(size, reverse = true) +
            Face.LEFT.indexesVL(size)
        Face.UP -> Face.BACK.indexesHB(size) +
            Face.RIGHT.indexesHT(size, reverse = true) +
            Face.FRONT.indexesHT(size, reverse = true) +
            Face.LEFT.indexesHT(size, reverse = true)
        Face.DOWN -> Face.FRONT.indexesHB(size) +
            Face.RIGHT.indexesHB(size) +
            Face.BACK.indexesHT(size, reverse = true) +
            Face.LEFT.indexesHB(size)
    }
}

fun innerRingIndices(face: Face, size: Int): Sequence<Int> {
    return (face.indexesHT(size) +
        face.indexesVR(size) +
        face.indexesHB(size, reverse = true) +
        face.indexesVL(size, reverse = true))
        .distinct()
}
