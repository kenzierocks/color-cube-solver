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

import kotlin.random.Random


/**
 * A NxNxN cube. Can be transformed by rotating a "ring" of cubes either direction.
 */
class ColorCube<T>(data: CCArray<T>) {

    private val data = data.duplicate()

    val size: Int
        get() = data.size

    val facePoints: Sequence<Pair<Int, Int>>
        get() = data.facePoints

    operator fun get(face: Face, x: Int, y: Int): T {
        return data[face, x, y]
    }

    /**
     * A rotation is defined by the face to rotate, and the direction to rotate.
     */
    fun rotate(face: Face, direction: RotationDirection): ColorCube<T> {
        return ColorCube(data.duplicate().also { it.rotate(face, direction) })
    }

    fun scramble(rng: Random = Random(System.nanoTime())): ColorCube<T> {
        val rotations = rng.nextInt(500, 1000)
        val cube = data.duplicate()
        for (x in (0 until rotations)) {
            val face = Face.values().random(rng)
            val direction = RotationDirection.values().random(rng)
            cube.rotate(face, direction)
        }
        return ColorCube(cube)
    }

}

inline fun <reified T> CCArray(size: Int, initialValue: T): CCArray<T> {
    return CCArray(size, Array(size * size * FACES) { initialValue })
}

data class CCArray<T>(val size: Int, val data: Array<T>) {

    init {
        require(data.size == size * size * FACES) { "Data is not an ${size}x${size}x$FACES array" }
    }

    fun duplicate() = copy(data = data.copyOf())

    fun rotate(face: Face, direction: RotationDirection) {
        rotateData(outerRingIndices(face, size), direction, 3)
        rotateData(innerRingIndices(face, size), direction, 2)
    }

    private fun rotateData(indexes: Sequence<Int>, direction: RotationDirection, times: Int) {
        val dirIndexes = indexes.indexInDirection(direction)
        // Pull data from array
        val dataIterator = dirIndexes.map { data[it] }.iterator()
        // Rotate: rotated = list[END - times:] + list[times:END - times]
        val rotatePoint = dirIndexes.size - times
        for (i in (rotatePoint until dirIndexes.size)) {
            data[dirIndexes[i]] = dataIterator.next()
        }
        for (i in (0 until rotatePoint)) {
            data[dirIndexes[i]] = dataIterator.next()
        }
    }

    private fun Sequence<Int>.indexInDirection(direction: RotationDirection): IntArray {
        return toList().toIntArray().run {
            when (direction) {
                RotationDirection.CLOCKWISE -> reversedArray()
                else -> this
            }
        }
    }

    operator fun get(face: Face, x: Int, y: Int): T {
        return data[index(face, x, y, size)]
    }

    operator fun set(face: Face, x: Int, y: Int, value: T) {
        data[index(face, x, y, size)] = value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CCArray<*>

        if (size != other.size) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = size
        result = 31 * result + data.contentHashCode()
        return result
    }

}

val <T> CCArray<T>.facePoints: Sequence<Pair<Int, Int>>
    get() {
        return sequence {
            for (i in (0 until size)) {
                for (j in (0 until size)) {
                    yield(i to j)
                }
            }
        }
    }

