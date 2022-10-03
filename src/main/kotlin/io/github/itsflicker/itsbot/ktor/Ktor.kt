package io.github.itsflicker.itsbot.ktor

import io.github.itsflicker.itsbot.ItsBot
import io.github.itsflicker.itsbot.config.IBConfig
import io.github.itsflicker.itsbot.listener.GroupMessageListener
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.util.*
import kotlinx.coroutines.CoroutineExceptionHandler
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.info
import org.slf4j.helpers.NOPLogger
import java.util.concurrent.TimeUnit

object Ktor {

    val module: Application.() -> Unit = {
        routing {
            post("/captcha") {
                val captcha = call.receiveParameters()
                val code = captcha.getOrFail("code")
                val uuid = captcha.getOrFail("uuid")
                val name = captcha.getOrFail("name")
                ItsBot.logger.info { "Received captcha: $code ($name to $uuid)" }
                GroupMessageListener.captchaCache.put(code, name to uuid)
                call.respond(HttpStatusCode.OK, mapOf("time" to System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)))
            }
        }
    }

    fun createServer(vararg module: Application.() -> Unit): ApplicationEngine {
        return embeddedServer(CIO, applicationEngineEnvironment {

            val coroutineLogger = MiraiLogger.Factory.create(this::class, "[ib]")
            parentCoroutineContext = CoroutineExceptionHandler { _, throwable ->
                coroutineLogger.error(throwable)
            }

            log = NOPLogger.NOP_LOGGER

            connector {
                host = IBConfig.options.host
                port = IBConfig.options.port
            }

            modules += module
        })
    }

}