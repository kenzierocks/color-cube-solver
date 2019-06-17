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
import com.flowpowered.math.vector.Vector2d
import com.flowpowered.math.vector.Vector2i
import com.techshroom.unplanned.blitter.GraphicsContext
import com.techshroom.unplanned.blitter.binding.BindableDrawable
import com.techshroom.unplanned.blitter.textures.*
import com.techshroom.unplanned.blitter.textures.loader.StandardTextureLoaders
import com.techshroom.unplanned.blitter.transform.MatrixTransformer
import com.techshroom.unplanned.core.util.LifecycleObject
import com.techshroom.unplanned.geometry.Plane
import net.octyl.colorcube.*
import net.octyl.colorcube.solver.CubeMove
import net.octyl.colorcube.solver.Rotate

class SixCubeRenderer(private val ctx: GraphicsContext) : LifecycleObject {

    companion object {
        private const val SCALE = 5
        private const val QUAD_GAP = 0.1 * SCALE
        private const val QUAD_SIZE = 1 * SCALE
    }

    private val colorTextures = SixColor.values().associate { color ->
        val texData = StandardTextureLoaders.RGBA_COLOR_LOADER.load(color.realColor)
        val texture = ctx.textureProvider.load(texData, TextureSettings.builder()
            .upscaling(Upscaling.NEAREST)
            .downscaling(Downscaling.NEAREST)
            .textureWrapping(TextureWrap.REPEAT)
            .build())
        color to texture
    }

    private val quad = ctx.shapes.quad()
        .shape(Plane.XY, Vector2i.ZERO, Vector2i.from(QUAD_SIZE),
            listOf(Vector2d.UNIT_Y, Vector2d.ONE, Vector2d.ZERO, Vector2d.UNIT_X))

    private fun indexToPosition(index: Int): Double {
        return (index * QUAD_SIZE).toDouble() + (index + 1) * QUAD_GAP
    }

    private fun MatrixTransformer.baseTransform(size: Int) {
        // Base is the upper-left of the FRONT face,
        // which is un-rotated, (-size/2, -size/2, size/2)
        val halfSize = (indexToPosition(size) / 2).toFloat()
        translate(
            -halfSize, -halfSize, halfSize
        )
    }

    fun render(sixCube: SixCube, currentMove: CubeMove?, progress: Double) {
        ctx.pushTransformer().use {
            it.model().baseTransform(sixCube.size)
            primaryRenderStep(sixCube, currentMove, progress)
        }
    }

    private fun primaryRenderStep(sixCube: SixCube, currentMove: CubeMove?, progress: Double) {
        quad.asBindable().get().bind().use { bindDrawQuad ->
            for ((i, j) in sixCube.facePoints) {
                ctx.pushTransformer().use {
                    // Translate to cube position
                    it.model().translate(
                        indexToPosition(i).toFloat(),
                        indexToPosition(sixCube.size - j - 1).toFloat(),
                        0f
                    )
                    faceRenderStep(
                        { face -> colorTextures.getValue(sixCube[face, i, j]) },
                        { face -> getMoveTransform(sixCube, face, i, j, currentMove, progress) },
                        bindDrawQuad
                    )
                }
            }
        }
    }

    private inline fun faceRenderStep(texture: (Face) -> Texture,
                                      moveTransform: (Face) -> Matrix4f?,
                                      bindDrawQuad: BindableDrawable) {
        for (face in Face.values()) {
            // Rotate to match the face
            ctx.pushTransformer().use {
                it.model().transform(face.rotationMatrix::mul)
                moveTransform(face)?.let { matrix ->
                    it.model().transform(matrix::mul)
                }
                it.apply(ctx.matrixUploader)
            }
            // Pull out appropriate texture
            texture(face).apply { bind() }.use {
                bindDrawQuad.drawWithoutBinding()
            }
        }
    }

    private fun getMoveTransform(sixCube: SixCube,
                                 face: Face,
                                 x: Int, y: Int,
                                 currentMove: CubeMove?,
                                 progress: Double): Matrix4f? {
        return when (currentMove) {
            null -> null
            is Rotate -> {
                val valid = currentMove.face == face || validRotatingCube(sixCube, face, x, y, currentMove)
                if (valid) {
                    val rotation = when (currentMove.direction) {
                        RotationDirection.CLOCKWISE -> 90
                        RotationDirection.COUNTER_CLOCKWISE -> -90
                    }
                    val quat = Quaternionf.fromAngleDegAxis(rotation * progress, currentMove.face.perpendicularAxis)
                    Matrix4f.createRotation(quat)
                } else null
            }
        }
    }

    private fun validRotatingCube(sixCube: SixCube,
                                  face: Face, x: Int, y: Int,
                                  currentMove: Rotate): Boolean {
        val validIndices = outerRingIndices(currentMove.face, sixCube.size).toSet()
        return index(face, x, y, sixCube.size) in validIndices
    }

    override fun initialize() {
        colorTextures.values.forEach { it.initialize() }
        quad.initialize()
    }

    override fun destroy() {
        quad.destroy()
        colorTextures.values.reversed().forEach { it.destroy() }
    }

}