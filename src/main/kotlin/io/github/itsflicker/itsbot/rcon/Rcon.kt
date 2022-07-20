package io.github.itsflicker.itsbot.rcon

import cn.ltcraft.rcon.config.RconServerConfig
import cn.ltcraft.rcon.rcon.RconPacket
import io.github.itsflicker.itsbot.rcon.ex.AuthenticationException
import java.io.IOException
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Created by Angel、 on 2022/4/17 17:08
 */
class Rcon(
    val config: RconServerConfig
) {
    private val sync = Any()
    private val rand = Random()

    private var requestId = 0
    private lateinit var socket: Socket

    private var fail = false;

    /**
     * 创建、连接和验证新的Rcon对象
     */
    init {
        // Connect to host
        try {
            connect(config.serverAddress, config.serverPort, config.password.toByteArray())
        } catch (_: Exception) {
            fail = true
        }
    }
    fun isFail() = fail

    /**
     * Connect to a rcon server
     *
     * @param host Rcon server address
     * @param port Rcon server port
     * @param password Rcon server password
     *
     * @throws IOException
     * @throws AuthenticationException
     */
    @Throws(IOException::class, AuthenticationException::class)
    fun connect(host: String?, port: Int, password: ByteArray) {
        require(!(host == null || host.trim { it <= ' ' }.isEmpty())) { "Host can't be null or empty" }
        require(!(port < 1 || port > 65535)) { "Port is out of range" }

        // Connect to the rcon server
        synchronized(sync) {

            // New random request id
            requestId = rand.nextInt()

            // We can't reuse a socket, so we need a new one
            socket = Socket(host, port)
        }

        // Send the auth packet
        val res = send(RconPacket.SERVERDATA_AUTH, password)

        // Auth failed
        if (res.getRequestId() == -1) {
            throw AuthenticationException("Password rejected by server")
        }
    }

    /**
     * Disconnect from the current server
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun disconnect() {
        synchronized(sync) { socket.close() }
    }

    /**
     * Send a command to the server
     *
     * @param payload The command to send
     * @return The payload of the response
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun command(payload: String?): String {
        require(!(payload == null || payload.trim { it <= ' ' }.isEmpty())) { "Payload can't be null or empty" }
        val response = send(RconPacket.SERVERDATA_EXECCOMMAND, payload.toByteArray())
        val result = String(response.getPayload(), StandardCharsets.UTF_8)
        return if (result.endsWith("\n")) {
            result.substring(0, result.length - 1)
        } else result
    }

    @Throws(IOException::class)
    private fun send(type: Int, payload: ByteArray): RconPacket {
        synchronized(sync) {
            return RconPacket.send(this, type, payload)
        }
    }

}