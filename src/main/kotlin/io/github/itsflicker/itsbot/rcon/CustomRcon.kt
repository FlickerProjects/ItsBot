package io.github.itsflicker.itsbot.rcon

import net.kronos.rkon.core.Rcon
import java.io.IOException

class CustomRcon(
    private val host: String,
    private val port: Int,
    private val password: ByteArray
) : Rcon(host, port, password) {

    constructor(host: String, port: Int, password: String) : this(host, port, password.toByteArray())

    @Throws(IOException::class)
    override fun command(payload: String): String? {
        if (!empty(payload)) {
            try {
                val response = super.command(payload)
                if (empty(response)) {
                    return "Succeed with no response."
                }
                return response.removeColor()
            } catch (_: Exception) {
                disconnect()
                connect(host, port, password)
                val response = super.command(payload)
                if (empty(response)) {
                    return "Succeed with no response."
                }
                return response.removeColor()
            }
        }
        return null
    }

    override fun toString(): String {
        val s = socket
        return s.inetAddress.toString() + ":" + s.port
    }

    companion object {

        //Short-circuit evaluation
        private fun empty(s: String?): Boolean =
            (s == null) || s.trim { it <= ' ' }.isEmpty()

        fun String.removeColor(): String =
            replace("§[\\da-z]".toRegex(), "")

    }
}