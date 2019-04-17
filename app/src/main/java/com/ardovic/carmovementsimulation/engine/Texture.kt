package com.ardovic.carmovementsimulation.engine

import android.graphics.Rect

class Texture {

    internal var textureId: Int = 0
    private var width: Int = 0
    private var height: Int = 0

    internal var layerData: LayerData
        get() {
            field.setDimensions(width, height)
            return field
        }

    init {
        layerData = LayerData(-0x1)
    }

    internal fun setDimensions(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    internal fun addSprite(src: Rect, dst: Rect, angle: Int) {
        layerData.addSprite(src, dst, angle)
    }
}