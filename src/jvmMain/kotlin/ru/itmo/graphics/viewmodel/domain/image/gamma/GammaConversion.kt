package ru.itmo.graphics.viewmodel.domain.image.gamma

import kotlin.math.pow

object GammaConversion {
    fun applyGamma(bb: MutableList<Float>, gamma: Float) {
        if (gamma == 0f) {
            performSRGB(bb)
            return
        }

        bb[0] = bb[0].pow(1 / gamma)
        bb[1] = bb[1].pow(1 / gamma)
        bb[2] = bb[2].pow(1 / gamma)
    }

    fun applyReverseGamma(bb: MutableList<Float>, gamma: Float) {
        if (gamma == 0f) {
            applyGamma(bb, 1 / 2.4f)
        } else {
            applyGamma(bb, 1 / gamma)
        }
    }

    private fun performSRGB(bb: MutableList<Float>) {
        val cie = Array(3) { 0f }

        for (i in 0..2) {
            if (bb[i] <= 0.04045f) {
                bb[i] /= 12.92f
            } else {
                bb[i] = ((bb[i] + 0.055f) / 1.055f).pow(2.4f)
            }
        }

        cie[0] = 0.4124f * bb[0] + 0.3576f * bb[1] + 0.1805f * bb[2]
        cie[1] = 0.2126f * bb[0] + 0.7152f * bb[1] + 0.0722f * bb[2]
        cie[2] = 0.0193f * bb[0] + 0.1192f * bb[1] + 0.9505f * bb[2]

        bb[0] = 3.2406f * cie[0] - 1.5372f * cie[1] - 0.4986f * cie[2]
        bb[1] = -0.9689f * cie[0] + 1.8758f * cie[1] + 0.0415f * cie[2]
        bb[2] = 0.0557f * cie[0] - 0.2040f * cie[1] + 1.0570f * cie[2]

        for (i in 0..2) {
            if (bb[i] <= 0.0031308f) {
                bb[i] *= 12.92f
            } else {
                bb[i] = (1.055f * bb[i]).pow(1 / 2.4f) - 0.055f
            }

            bb[i] = bb[i].coerceIn(0f, 1f)
        }
    }
}
