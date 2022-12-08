package io.github.itsflicker.itsbot.util

import io.github.itsflicker.itsbot.ItsBot
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import kotlinx.coroutines.CoroutineExceptionHandler
import java.util.concurrent.TimeUnit

val reader = VariableReader()

val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
    ItsBot.logger.error(throwable)
}

val httpClient = HttpClient(OkHttp) {
    engine {
        config {
            readTimeout(10, TimeUnit.SECONDS)
        }
    }
}