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

import com.techshroom.unplanned.blitter.textures.TextureData
import com.techshroom.unplanned.blitter.textures.TextureFormat
import com.techshroom.unplanned.blitter.textures.loader.TextureLoader
import org.lwjgl.BufferUtils
import org.lwjgl.stb.STBIIOCallbacks
import org.lwjgl.stb.STBImage.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.NULL
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel

object ByteChannelTextureLoader : TextureLoader<ReadableByteChannel, IOException> {
    override fun load(source: ReadableByteChannel): TextureData {
        MemoryStack.stackPush().use { stack ->
            val xBuf = stack.mallocInt(1)
            val yBuf = stack.mallocInt(1)
            val channels = stack.mallocInt(1)
            val callbacks = source.asCallbacks()
            val data = stbi_load_from_callbacks(callbacks, NULL, xBuf, yBuf, channels, 4)

            if (data == null) {
                val why = stbi_failure_reason()
                throw IllegalArgumentException("Image $source could not be loaded: $why")
            }

            val format = TextureFormat.RGBA
            val copy = BufferUtils.createByteBuffer(xBuf[0] * yBuf[0] * format.channels)
            copy.put(data).flip()
            data.rewind()
            stbi_image_free(data)

            return TextureData.wrap(xBuf[0], yBuf[0], copy, format)
        }
    }
}

private fun ReadableByteChannel.asCallbacks(): STBIIOCallbacks {
    val ctx = object {
        var atEof = false

        fun readFully(target: ByteBuffer): Int {
            var result: Int = -1
            while (result == -1) {
                result = when (val readCount = this@asCallbacks.read(target)) {
                    -1 -> {
                        atEof = true
                        0
                    }
                    0 -> -1 // Continue in this case, since a zero means EOF to stb
                    else -> readCount
                }
            }

            return result
        }
    }
    return STBIIOCallbacks.mallocStack()
        .read { _, data, size ->
            val target = MemoryUtil.memByteBuffer(data, size)
            ctx.readFully(target)
        }
        .skip { _, n -> ctx.readFully(BufferUtils.createByteBuffer(n)) }
        .eof { if (ctx.atEof) 1 else 0 }
}
