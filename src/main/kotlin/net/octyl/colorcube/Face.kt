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

import com.flowpowered.math.imaginary.Quaternionf
import com.flowpowered.math.matrix.Matrix4f
import com.flowpowered.math.vector.Vector3f

enum class Face(
    /**
     * Matrix to make a XY-plane quad face the correct direction.
     */
    val rotationMatrix: Matrix4f,
    /**
     * Axis perpendicular to the face
     */
    val perpendicularAxis: Vector3f
) {
    UP(adaMatrix(-90.0, Vector3f.UNIT_X), Vector3f.UNIT_Y.negate()),
    LEFT(adaMatrix(-90.0, Vector3f.UNIT_Y), Vector3f.UNIT_X),
    FRONT(Matrix4f.IDENTITY, Vector3f.UNIT_Z.negate()),
    RIGHT(adaMatrix(90.0, Vector3f.UNIT_Y), Vector3f.UNIT_X.negate()),
    DOWN(adaMatrix(90.0, Vector3f.UNIT_X), Vector3f.UNIT_Y),
    BACK(adaMatrix(-180.0, Vector3f.UNIT_X), Vector3f.UNIT_Z),
}

private fun adaMatrix(angle: Double, axis: Vector3f): Matrix4f {
    return Matrix4f.createRotation(Quaternionf.fromAngleDegAxis(angle, axis))
}

val FACES = Face.values().size
