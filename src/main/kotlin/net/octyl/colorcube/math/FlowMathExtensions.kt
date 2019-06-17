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

package net.octyl.colorcube.math

import com.flowpowered.math.matrix.Matrix4f
import com.flowpowered.math.vector.Vector3d
import com.flowpowered.math.vector.Vector3f

operator fun Vector3f.times(vector3f: Vector3f): Vector3f = mul(vector3f)

operator fun Vector3f.times(double: Double): Vector3f = mul(double)

operator fun Vector3f.minus(vector3f: Vector3f): Vector3f = sub(vector3f)

operator fun Vector3d.times(vector3d: Vector3d): Vector3d = mul(vector3d)

operator fun Matrix4f.times(matrix4f: Matrix4f): Matrix4f = mul(matrix4f)

