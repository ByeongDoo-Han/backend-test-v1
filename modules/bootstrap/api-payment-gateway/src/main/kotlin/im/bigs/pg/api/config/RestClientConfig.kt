package im.bigs.pg.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {
    companion object {
        private const val TEST_PG_API_URL = "https://api-test-pg.bigs.im"
        private const val CONTENT_TYPE = "Content-Type"
        private const val APPLICATION_JSON = "application/json"
    }

    @Bean
    fun testPgApiRestClient(): RestClient {
        return RestClient.builder()
            .baseUrl(TEST_PG_API_URL)
            .defaultHeader(CONTENT_TYPE, APPLICATION_JSON)
            .build()
    }
}
