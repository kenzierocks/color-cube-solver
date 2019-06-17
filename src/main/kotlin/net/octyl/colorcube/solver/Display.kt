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

package net.octyl.colorcube.solver

import com.google.common.eventbus.Subscribe
import com.techshroom.unplanned.blitter.GraphicsContext
import com.techshroom.unplanned.blitter.matrix.Matrices
import com.techshroom.unplanned.blitter.transform.DefaultTransformer
import com.techshroom.unplanned.core.util.Sync
import com.techshroom.unplanned.core.util.time.Timer
import com.techshroom.unplanned.event.keyboard.KeyState
import com.techshroom.unplanned.event.keyboard.KeyStateEvent
import com.techshroom.unplanned.event.mouse.MouseButtonEvent
import com.techshroom.unplanned.event.window.WindowFramebufferResizeEvent
import com.techshroom.unplanned.input.Key
import com.techshroom.unplanned.window.WindowSettings
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.sendBlocking
import net.octyl.colorcube.SixCube
import net.octyl.colorcube.gl.CenterStaringCamera
import net.octyl.colorcube.gl.SixCubeRenderer
import org.lwjgl.glfw.GLFW.glfwSetWindowRefreshCallback
import java.lang.Math.toRadians
import java.util.concurrent.TimeUnit

class Display(
    initialCube: SixCube,
    private val moves: ReceiveChannel<CubeMove>,
    private val clicksChannel: SendChannel<Unit>
) {
    private val window = WindowSettings.builder()
        .title("Cube Solver")
        .msaa(16)
        .build().createWindow()
    private val ctx: GraphicsContext
        get() = window.graphicsContext
    private var currentCube: SixCube = initialCube
    private var currentlyDrawingMove: CubeMove? = null
    private var currentMoveProgress: Double = 0.0
    private var needsRedraw = true
    private val camera = CenterStaringCamera(distanceAway = 50.0)
    private lateinit var cubeRenderer: SixCubeRenderer

    private fun updateViewMatrix() {
        DefaultTransformer.getInstance().camera().set(
            camera.toViewMatrix()
        )
    }

    fun run() {
        ctx.pushTransformer().use {
            window.eventBus.register(this)
            ctx.makeActiveContext()
            cubeRenderer = SixCubeRenderer(ctx)
            cubeRenderer.initialize()
            val sync = Sync()
            window.isVsyncOn = true
            window.isVisible = true
            val size = window.framebufferSize
            window.eventBus.post(WindowFramebufferResizeEvent.create(window, size.x, size.y))
            glfwSetWindowRefreshCallback(window.windowPointer) { needsRedraw = true }
            val timer = Timer.getInstance()
            var frameDeltaTracker = timer.getValue(TimeUnit.MILLISECONDS)

            while (!window.isCloseRequested) {
                sync.sync(60)

                val frameStartTime = timer.getValue(TimeUnit.MILLISECONDS)
                val delta = (frameStartTime - frameDeltaTracker)
                frameDeltaTracker = frameStartTime

                checkMoveForDraw(delta)

                window.processEvents()
                checkCameraKeys(delta)
                if (camera.dirty) {
                    updateViewMatrix()
                    needsRedraw = true
                }
                if (needsRedraw) {
                    draw()
                }
            }

            cubeRenderer.destroy()
            window.isVisible = false
        }
    }

    private fun checkMoveForDraw(delta: Long) {
        if (currentMoveProgress >= 100) {
            currentCube = currentlyDrawingMove!!.apply(currentCube)
            currentlyDrawingMove = null
            currentMoveProgress = 0.0
            return
        }
        if (currentlyDrawingMove == null) {
            moves.poll()?.let { move ->
                needsRedraw = true
                currentlyDrawingMove = move
            }
        } else {
            currentMoveProgress += delta * 1
            needsRedraw = true
        }
    }

    @Subscribe
    fun onResize(event: WindowFramebufferResizeEvent) {
        val size = event.size.toDouble()
        val aspect = size.x / size.y
        DefaultTransformer.getInstance().projection().set(
            Matrices.perspectiveProjection(toRadians(60.0), aspect, 0.1, 1000.0)
        )
        needsRedraw = true
    }

    @Subscribe
    fun onKey(event: KeyStateEvent) {
        if (event.state == KeyState.PRESSED) {
            when (event.key) {
                Key.ESCAPE -> window.isCloseRequested = true
                else -> {
                }
            }
        }
    }

    private var wasMouseDown = false

    @Subscribe
    fun onMouse(event: MouseButtonEvent) {
        if (event.button == 0) {
            when {
                event.isDown -> wasMouseDown = true
                else -> clicksChannel.sendBlocking(Unit)
            }
        }
    }

    private fun checkCameraKeys(delta: Long) {
        val keyboard = window.keyboard
        val change = 0.001 * delta

        if (keyboard.isKeyDown(Key.W) || keyboard.isKeyDown(Key.UP)) {
            camera.radiansX -= change
        }
        if (keyboard.isKeyDown(Key.S) || keyboard.isKeyDown(Key.DOWN)) {
            camera.radiansX += change
        }

        if (keyboard.isKeyDown(Key.D) || keyboard.isKeyDown(Key.RIGHT)) {
            camera.radiansY += change
        }
        if (keyboard.isKeyDown(Key.A) || keyboard.isKeyDown(Key.LEFT)) {
            camera.radiansY -= change
        }

        if (keyboard.isKeyDown(Key.Q)) {
            camera.distanceAway += change * 10
        }
        if (keyboard.isKeyDown(Key.E)) {
            camera.distanceAway -= change * 10
        }
    }

    private fun draw() {
        ctx.clearGraphicsState()

        cubeRenderer.render(currentCube, currentlyDrawingMove, currentMoveProgress / 100)

        ctx.swapBuffers()
    }
}