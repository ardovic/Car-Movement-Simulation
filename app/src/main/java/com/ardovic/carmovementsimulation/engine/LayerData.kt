package com.ardovic.carmovementsimulation.engine

import android.graphics.Rect
import java.util.*

class LayerData internal constructor(internal val argb: Int) {

    private val vertices: ArrayList<Float> = ArrayList()
    private val indices: ArrayList<Short> = ArrayList()
    private val textureCoordinates: ArrayList<Float> = ArrayList()

    private var textureWidth: Int = 0
    private var textureHeight: Int = 0

    internal fun setDimensions(width: Int, height: Int) {
        textureWidth = width
        textureHeight = height
    }

    internal fun addSprite(src: Rect, dst: Rect, angle: Int) {
        val cos = Math.cos(angle.toDouble() / 180 * Math.PI)
        val sin = Math.sin(angle.toDouble() / 180 * Math.PI)
        val halfWidth = (dst.right - dst.left) / 2f
        val halfHeight = (dst.top - dst.bottom) / 2f
        val hotX = floatArrayOf(-halfWidth, -halfWidth, halfWidth, halfWidth)
        val hotY = floatArrayOf(halfHeight, -halfHeight, -halfHeight, halfHeight)
        for (i in 0..3) {
            var transformedX = (cos * hotX[i] - sin * hotY[i]).toFloat()
            var transformedY = (sin * hotX[i] + cos * hotY[i]).toFloat()
            transformedX += dst.left + halfWidth
            transformedY += dst.bottom + halfHeight
            vertices.add(transformedX)
            vertices.add(transformedY)
            vertices.add(0f)
        }
        val lastValue: Short = if (!indices.isEmpty()) indices[indices.size - 1] else -1
        indices.add((lastValue + 1).toShort())
        indices.add((lastValue + 2).toShort())
        indices.add((lastValue + 3).toShort())
        indices.add((lastValue + 1).toShort())
        indices.add((lastValue + 3).toShort())
        indices.add((lastValue + 4).toShort())
        val srcX = floatArrayOf(src.left.toFloat(), src.left.toFloat(), src.right.toFloat(), src.right.toFloat())
        val srcY = floatArrayOf(src.top.toFloat(), src.bottom.toFloat(), src.bottom.toFloat(), src.top.toFloat())
        for (i in 0..3) {
            textureCoordinates.add(srcX[i] / textureWidth)
            textureCoordinates.add(srcY[i] / textureHeight)
        }
    }

    internal fun clear() {
        vertices.clear()
        indices.clear()
        textureCoordinates.clear()
    }

    internal fun getVertices(): FloatArray? {
        return convertToPrimitive(vertices.toTypedArray())
    }

    internal fun getIndices(): ShortArray? {
        return convertToPrimitive(indices.toTypedArray())
    }

    internal fun getTextureCoordinates(): FloatArray? {
        return convertToPrimitive(textureCoordinates.toTypedArray())
    }

    private fun convertToPrimitive(objectArray: Array<Float>?): FloatArray? {
        if (objectArray == null || objectArray.isEmpty()) return null
        val primitiveArray = FloatArray(objectArray.size)
        for (i in objectArray.indices)
            primitiveArray[i] = objectArray[i]
        return primitiveArray
    }

    private fun convertToPrimitive(objectArray: Array<Short>?): ShortArray? {
        if (objectArray == null || objectArray.isEmpty()) return null
        val primitiveArray = ShortArray(objectArray.size)
        for (i in objectArray.indices)
            primitiveArray[i] = objectArray[i]
        return primitiveArray
    }
}
