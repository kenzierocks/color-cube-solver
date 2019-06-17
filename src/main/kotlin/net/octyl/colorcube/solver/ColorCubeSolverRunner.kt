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

    private val appScope = CoroutineScope(Dispatchers.Unconfined + CoroutineName("Application"))

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
        val size = opts.valueOf(SIZE)
        if (size < 3) {
            System.err.println("Size must be greater than or equal to 3.")
            exitProcess(1)
        }
        if (size % 2 == 0) {
            System.err.println("Size must be odd.")
            exitProcess(1)
        }
        val cube = ColorCube(CCArray(size, SixColor.COLOR_1).apply {
            for (face in Face.values()) {
                val color = SixColor.values()[face.ordinal]
                for ((i, j) in facePoints) {
                    this[face, i, j] = color
                }
            }
        })
        val msgChannel = Channel<SolverMessage>(5)
        val movesBufferChannel = Channel<CubeMove>(100)
        val movesChannel = Channel<CubeMove>(100)
        val clicksChannel = Channel<Unit>(UNLIMITED)

        appScope.launch {
            if (waitForClick) {
                clicksChannel.receive()
            }
            val startingCube = scramble(cube, movesChannel)
            val solver = ColorCubeSolver(startingCube, msgChannel, movesBufferChannel)

            solver.start()
        }
        appScope.launch {
            while (true) {
                val next = movesBufferChannel.receive()
                if (waitForClick) {
                    clicksChannel.receive()
                }
                movesChannel.send(next)
            }
        }
        if (!waitForClick) {
            // discard clicks
            appScope.launch { while (true) clicksChannel.receive() }
        }

        val display = Display(cube, movesChannel, clicksChannel)
        display.run()
    }

    private suspend fun scramble(
        initialCube: SixCube,
        movesChannel: Channel<CubeMove>,
        rng: Random = Random(Timer.getInstance().getValue(TimeUnit.MILLISECONDS))
    ): SixCube {
        val rotations = rng.nextInt(500, 1000)
        var cube = initialCube
        for (x in (0 until rotations)) {
            val face = Face.values().random(rng)
            val direction = RotationDirection.values().random(rng)
            movesChannel.send(Rotate(face, direction))
            cube = cube.rotate(face, direction)
        }
        return cube
    }

}
