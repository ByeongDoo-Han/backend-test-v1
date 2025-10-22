package im.bigs.pg.api.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.util.concurrent.TimeUnit

@Configuration
class WebClientConfig {
    companion object {
        private const val TEST_PG_API_URL = "https://api-test-pg.bigs.im"
        private const val CONTENT_TYPE = "Content-Type"
        private const val APPLICATION_JSON = "application/json"
    }

    @Bean
    fun testPgApiWebClient(builder: WebClient.Builder): WebClient {
        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
            .doOnConnected { conn ->
                conn.addHandlerLast(ReadTimeoutHandler(500, TimeUnit.MILLISECONDS))
                conn.addHandlerLast(WriteTimeoutHandler(500, TimeUnit.MILLISECONDS))
            }
        return builder
            .baseUrl(TEST_PG_API_URL)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .defaultHeader(CONTENT_TYPE, APPLICATION_JSON)
            .build()
    }
}
