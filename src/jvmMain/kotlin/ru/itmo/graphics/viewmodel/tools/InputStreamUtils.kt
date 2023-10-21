package ru.itmo.graphics.viewmodel.tools

import java.io.InputStream

class InputStreamUtils {
    companion object {
        fun isWhitespace(c: Int): Boolean {
            return c == 8 || c == 10 || c == 13 || c == 32
        }

        fun readWhitespace(fileStream: InputStream): Int {
            val c = fileStream.read()

            if (!isWhitespace(c)) {
                throw Exception("Expected whitespace character, encountered: $c")
            }

            return c
        }

        fun readNumber(fileStream: InputStream): Int {
            var number = 0
            var c = fileStream.read()
            while (c.toChar().minus('0') in 0..9) {
                number = number * 10 + c.toChar().minus('0')
                c = fileStream.read()
            }

            if (!isWhitespace(c)) {
                throw Exception("Expected digit character, encountered: ${c.toChar()} ($c)")
            }

            return number
        }

        fun readPositiveNumber(fileStream: InputStream): Int {
            val number = readNumber(fileStream)

            if (number <= 0) {
                throw Exception("Number must be positive. Got $number")
            }

            return number
        }
    }
}
