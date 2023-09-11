package ru.itmo.graphics.tools

class HalfFloatUtils {
    companion object {

        // Code from https://itecnote.com/tecnote/java-half-precision-floating-point-in-java/
        fun toHalfFloat(value: Float): Int {
            val bits = java.lang.Float.floatToIntBits(value)
            val sign = bits ushr 16 and 0x8000
            var rounded = (bits and 0x7fffffff) + 0x1000

            if (rounded >= 0x47800000)
            {
                return if (bits and 0x7fffffff >= 0x47800000) {
                    if (rounded < 0x7f800000) {
                        sign or 0x7c00
                    } else {
                        sign or 0x7c00 or (bits and 0x007fffff ushr 13)
                    }
                } else sign or 0x7bff
            }

            if (rounded >= 0x38800000)
                return sign or (rounded - 0x38000000 ushr 13)

            if (rounded < 0x33000000)
                return sign

            rounded = bits and 0x7fffffff ushr 23

            return sign or ((bits and 0x7fffff or 0x800000) + (0x800000 ushr rounded - 102) ushr 126 - rounded)
        }

        fun fromHalfFloat(bits: Int): Float {
            var mant = bits and 0x03ff
            var exp = bits and 0x7c00

            if (exp == 0x7c00) {
                exp = 0x3fc00
            } else if (exp != 0) {
                exp += 0x1c000

                if (mant == 0 && exp > 0x1c400) {
                    return Float.fromBits(bits and 0x8000 shl 16 or (exp shl 13) or 0x3ff)
                }
            } else if (mant != 0) {
                exp = 0x1c400

                do {
                    mant = mant shl 1
                    exp -= 0x400
                } while (mant and 0x400 == 0)

                mant = mant and 0x3ff
            }

            return java.lang.Float.intBitsToFloat(bits and 0x8000 shl 16 or (exp or mant) shl 13)
        }
    }

}