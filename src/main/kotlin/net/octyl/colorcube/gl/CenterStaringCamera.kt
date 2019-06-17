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

package net.octyl.colorcube.gl

import com.flowpowered.math.imaginary.Quaternionf
import com.flowpowered.math.matrix.Matrix4f
import com.flowpowered.math.vector.Vector3f
import com.techshroom.unplanned.blitter.matrix.Matrices
import net.octyl.colorcube.math.times

/**
 * Camera that always looks at the center
 */
class CenterStaringCamera(
    /**
     * Distance away from the center.
     */
    distanceAway: Double = 0.0,
    /**
     * Rotation horizontally around the center, in radians.
     */
    radiansX: Double = 0.0,
    /**
     * Rotation vertically around the center, in radians.
     */
    radiansY: Double = 0.0
) {
    var dirty = true

    var distanceAway: Double = distanceAway
        set(value) {
            field = value
            dirty = true
        }

    var radiansX: Double = radiansX
        set(value) {
            field = value
            dirty = true
        }

    var radiansY: Double = radiansY
        set(value) {
            field = value
            dirty = true
        }

    /**
     * Convert the tracked positions into an actual view matrix.
     */
    fun toViewMatrix(): Matrix4f {
        dirty = false
        // Compute actual camera position & rotation
        val rotation = Quaternionf.fromAxesAnglesRad(radiansX, radiansY, 0.0)
        val camPos = rotation.direction * distanceAway
        // Rotate & translate ZA WARUDO
        return Matrix4f.createRotation(rotation)
            .translate(camPos)
    }

}