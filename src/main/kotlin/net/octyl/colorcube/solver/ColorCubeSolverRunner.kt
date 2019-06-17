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

import com.techshroom.unplanned.core.util.time.Timer
import joptsimple.OptionException
import joptsimple.OptionParser
import joptsimple.OptionSet
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.launch
import net.octyl.colorcube.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.system.exitProcess

object ColorCubeSolverRunner {

    private val PARSER = OptionParser()

    private val HELP = PARSER.acceptsAll(listOf("h", "help"), "Show this help")
        .forHelp()

    private val WAIT_FOR_CLICK = PARSER.acceptsAll(listOf("c", "wait-for-click"), "Wait for a screen click between stages")

    private val SIZE = PARSER.acceptsAll(listOf("s", "size"), "Size of the cube, must be odd")
        .withRequiredArg()
        .ofType(Int::class.java)
        .defaultsTo(3)

    private val SCRAMBLE_TIMES = PARSER.acceptsAll(listOf("scramble-times"), "Number of times to rotate when scrambling")
        .withRequiredArg()
        .ofType(Int::class.java)
        .defaultsTo(500)

    private val MOVE_SPEED = PARSER.acceptsAll(listOf("m", "move-speed"), "How many frames it should take to perform one move")
        .withRequiredArg()
        .ofType(Int::class.java)
        .defaultsTo(2)

    private val appScope = CoroutineScope(Dispatchers.Default + CoroutineName("Application"))

    @JvmStatic
    fun main(args: Array<String>) {
        val opts: OptionSet
        try {
            opts = PARSER.parse(*args)
        } catch (e: OptionException) {
            System.err.println(e.message)
            PARSER.printHelpOn(System.err)
            exitProcess(1)
        }

        if (opts.has(HELP)) {
            PARSER.printHelpOn(System.err)
            return
        }

        val waitForClick = opts.has(WAIT_FOR_CLICK)
        val cube = ColorCube(CCArray(opts.size(), SixColor.COLOR_1).apply {
            for (face in Face.values()) {
                val color = SixColor.values()[face.ordinal]
                for ((i, j) in facePoints) {
                    this[face, i, j] = color
                }
            }
        })
        val msgChannel = Channel<SolverMessage>(5)
        val movesChannel = Channel<CubeMove>(100)
        val clicksChannel = Channel<Unit>(UNLIMITED)

        appScope.launch {
            if (waitForClick) {
                clicksChannel.receive()
            }
            val startingCube = scramble(cube, opts.scrambleTimes(), movesChannel)
            if (waitForClick) {
                clicksChannel.receive()
                launchClickDiscarder(clicksChannel)
            }
            val solver = ColorCubeSolver(startingCube, msgChannel, movesChannel)

            solver.start()
        }
        if (!waitForClick) {
            launchClickDiscarder(clicksChannel)
        }

        val display = Display(cube, opts.moveSpeed(), movesChannel, clicksChannel)
        display.run()
    }

    private inline fun checkUser(condition: Boolean, message: () -> String) {
        if (!condition) {
            System.err.println(message())
            exitProcess(1)
        }
    }

    private fun OptionSet.size(): Int {
        val size = valueOf(SIZE)
        checkUser(size >= 3) { "Size must be greater than or equal to 3." }
        checkUser(size % 2 == 1) { "Size must be odd." }
        return size
    }

    private fun OptionSet.scrambleTimes(): Int {
        val scrambleTimes = valueOf(SCRAMBLE_TIMES)
        checkUser(scrambleTimes >= 0) { "Scramble times must be greater than or equal to 0." }
        return scrambleTimes
    }

    private fun OptionSet.moveSpeed(): Int {
        val moveSpeed = valueOf(MOVE_SPEED)
        checkUser(moveSpeed > 0) { "Move speed must be greater than 0." }
        return moveSpeed
    }

    private fun launchClickDiscarder(clicksChannel: Channel<Unit>) {
        appScope.launch { while (true) clicksChannel.receive() }
    }

    private suspend fun scramble(
        initialCube: SixCube,
        rotations: Int,
        movesChannel: Channel<CubeMove>,
        rng: Random = Random(Timer.getInstance().getValue(TimeUnit.MILLISECONDS))
    ): SixCube {
        var cube = initialCube
        var lastFace: Face? = null
        var lastDirection: RotationDirection? = null
        for (x in (0 until rotations)) {
            var face: Face
            var direction: RotationDirection
            do {
                face = Face.values().random(rng)
                direction = RotationDirection.values().random(rng)
            } while (face == lastFace && direction == lastDirection?.opposite)
            lastFace = face
            lastDirection = direction

            movesChannel.send(Rotate(face, direction))
            cube = cube.rotate(face, direction)
        }
        return cube
    }

}
