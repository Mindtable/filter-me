package ru.itmo.graphics.viewmodel.domain.scale.utils

import ru.itmo.graphics.viewmodel.domain.PixelData

class PixelPicker(
    private val pixelData: PixelData,
    private val constantIndex: Int,
    private val type: PixelDirection,
) {
    private fun getEmptyPixel(): MutableList<Float> {
        return MutableList(3) { _: Int -> 0f }
    }

    private fun getPixel(index: Int): MutableList<Float> {
        return when (type) {
            PixelDirection.ROW -> {
                if (index < 0 || index >= pixelData.width) {
                    getEmptyPixel()
                } else {
                    pixelData.getPixel(constantIndex, index)
                }
            }
            PixelDirection.COLUMN -> {
                if (index < 0 || index >= pixelData.height) {
                    getEmptyPixel()
                } else {
                    pixelData.getPixel(index, constantIndex)
                }
            }
        }
    }

    fun getPixelCopy(index: Int): MutableList<Float> {
        return getPixel(index).toList().toMutableList()
    }

    fun setPixel(index: Int, bb: List<Float>) {
        val initialPixel = getPixel(index)
        initialPixel[0] = bb[0]
        initialPixel[1] = bb[1]
        initialPixel[2] = bb[2]
    }
}
