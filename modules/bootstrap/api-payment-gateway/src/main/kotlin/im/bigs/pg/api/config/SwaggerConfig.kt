package im.bigs.pg.api.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val info = Info()
            .title("결제 시스템 API 서버")
            .version("v1.0.0")
            .description("결제 시스템 API 서버입니다.")

        return OpenAPI()
            .info(info)
    }
}
