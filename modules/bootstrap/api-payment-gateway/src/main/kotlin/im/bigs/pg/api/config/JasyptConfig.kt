package im.bigs.pg.api.config

import org.jasypt.encryption.StringEncryptor
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import org.jasypt.iv.NoIvGenerator
import org.jasypt.salt.RandomSaltGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JasyptConfig() {
    @Value("\${jasypt.encryptor.password}")
    private var password: String? = null

    @Bean("jasyptStringEncryptor")
    fun stringEncryptor(): StringEncryptor {
        val config = StandardPBEStringEncryptor()
        config.setPassword(password)
        config.setAlgorithm("PBEWithMD5AndDES")
        config.setKeyObtentionIterations(1000)
        config.setProviderName("SunJCE")
        config.setSaltGenerator(RandomSaltGenerator())
        config.setIvGenerator(NoIvGenerator())
        config.setStringOutputType("base64")
        return config
    }
}
