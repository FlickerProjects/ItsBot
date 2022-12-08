package io.github.itsflicker.itsbot.image

interface ImageCreator {
    suspend fun generate(args: Map<String, Any>): ByteArray
}