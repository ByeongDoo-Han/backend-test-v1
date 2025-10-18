package im.bigs.pg.common.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.Base64
import java.util.UUID
import kotlin.random.Random
import kotlin.test.assertEquals

class EncryptionUtilTest {

    private val objectMapper = jacksonObjectMapper()

    @Test
    @DisplayName("암호화된 데이터는 동일한 키와 IV로 복호화했을 때 원본과 일치해야 한다")
    fun `암복호화 무결성 테스트`() {
        // given
        val originalCardInfo = CardInfo(
            cardNumber = "1111-2222-3333-4444",
            birthDate = "19900101",
            expiry = "1227",
            password = "12",
            amount = BigDecimal.valueOf(10000)
        )
        val apiKey = UUID.randomUUID().toString()
        
        // 12바이트 IV를 랜덤하게 생성하고 Base64URL로 인코딩
        val ivBytes = Random.nextBytes(12)
        val ivBase64Url = Base64.getUrlEncoder().withoutPadding().encodeToString(ivBytes)

        // when
        // 1. 암호화 수행
        val encryptedData = PaymentEncryptor.encrypt(originalCardInfo, apiKey, ivBase64Url)

        // 2. 복호화 수행
        val decryptedJson = PaymentEncryptor.decrypt(encryptedData, apiKey, ivBase64Url)

        // then
        // 3. 복호화된 JSON을 다시 객체로 변환
        val decryptedCardInfo = objectMapper.readValue(decryptedJson, CardInfo::class.java)

        // 4. 원본 객체와 복호화된 객체가 일치하는지 검증
        assertEquals(originalCardInfo, decryptedCardInfo, "복호화된 데이터가 원본과 일치하지 않습니다.")
    }
}
