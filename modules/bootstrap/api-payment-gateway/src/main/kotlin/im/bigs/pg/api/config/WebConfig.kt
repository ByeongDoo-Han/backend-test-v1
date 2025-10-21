package im.bigs.pg.api.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(cors: CorsRegistry) {
        cors.addMapping("/**")
            .allowedOrigins(
                "http://localhost:3000"
            )
            .allowedMethods("GET", "POST", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}
