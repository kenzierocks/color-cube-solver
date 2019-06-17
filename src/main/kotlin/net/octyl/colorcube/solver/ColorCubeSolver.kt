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

import com.google.common.util.concurrent.ThreadFactoryBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.sendBlocking
import net.octyl.colorcube.Face
import net.octyl.colorcube.SixCube
import java.util.concurrent.Executors

class ColorCubeSolver(
    initialCube: SixCube,
    private val messageChannel: ReceiveChannel<SolverMessage>,
    private val movesChannel: SendChannel<CubeMove>
) {
    private var currentCube = initialCube

    private suspend fun updateCube(move: CubeMove) {
        currentCube = move.apply(currentCube)
        movesChannel.send(move)
    }

    private val executor = Executors.newFixedThreadPool(8, ThreadFactoryBuilder()
        .setNameFormat("solver-thread-%d")
        .setDaemon(true)
        .build())
    private val coroutineScope = CoroutineScope(executor.asCoroutineDispatcher() + CoroutineName("Solver"))

    fun start() {
        coroutineScope.launch { run() }
    }

    private suspend fun run() {
    }

}