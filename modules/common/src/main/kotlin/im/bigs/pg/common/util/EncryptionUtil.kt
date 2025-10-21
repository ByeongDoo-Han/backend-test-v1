package im.bigs.pg.common.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.math.BigDecimal
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 암호화 요청을 위한 평문 데이터 클래스.
 */
data class CardInfo(
    val cardNumber: String,
    val birthDate: String,
    val expiry: String,
    val password: String,
    val amount: BigDecimal
)

/**
 * 결제 정보 암호화를 위한 유틸리티 객체.
 */
object PaymentEncryptor {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_ALGORITHM = "AES"
    private const val HASH_ALGORITHM = "SHA-256"
    private const val TAG_BIT_LENGTH = 128
    private val objectMapper = jacksonObjectMapper()

    /**
     * 카드 정보를 입력해서 암호화된 enc 문자열을 생성하는 메서드
     *
     * @param info 암호화할 평문 카드 정보
     * @param apiKey API-KEY, SHA-256 해시를 통해 암호화 키로 변환
     * @param ivBase64Url Base64URL로 인코딩된 12바이트 IV
     * @return Base64URL로 인코딩된 최종 암호 (ciphertext||tag)
     */
    fun encrypt(info: CardInfo, apiKey: String, ivBase64Url: String): String {
        // 1. 평문을 JSON 문자열로 직렬화
        val plainTextJson = objectMapper.writeValueAsString(info)

        // 2. API-KEY로부터 암호화 키 생성 (SHA-256)
        val keyBytes = MessageDigest.getInstance(HASH_ALGORITHM)
            .digest(apiKey.toByteArray(Charsets.UTF_8))
        val secretKey = SecretKeySpec(keyBytes, KEY_ALGORITHM)

        // 3. iv 디코딩
        val ivBytes = Base64.getUrlDecoder().decode(ivBase64Url)
        require(ivBytes.size == 12) { "IV는 12바이트여야 합니다." }

        // 4. AES-256-GCM 암호화
        val cipher = Cipher.getInstance(ALGORITHM)
        val parameterSpec = GCMParameterSpec(TAG_BIT_LENGTH, ivBytes)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)

        val encryptedBytes = cipher.doFinal(plainTextJson.toByteArray(Charsets.UTF_8))

        // 5. 결과를 Base64URL로 인코딩
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedBytes)
    }

    /**
     * 암호화된 데이터를 복호화하여 원본 문자열(JSON)을 반환합니다.
     * (테스트 및 검증용)
     */
    fun decrypt(encryptedData: String, apiKey: String, ivBase64Url: String): String {
        val keyBytes = MessageDigest.getInstance(HASH_ALGORITHM)
            .digest(apiKey.toByteArray(Charsets.UTF_8))
        val secretKey = SecretKeySpec(keyBytes, KEY_ALGORITHM)

        val ivBytes = Base64.getUrlDecoder().decode(ivBase64Url)
        require(ivBytes.size == 12) { "IV는 반드시 12바이트여야 합니다." }

        val decodedData = Base64.getUrlDecoder().decode(encryptedData)

        val cipher = Cipher.getInstance(ALGORITHM)
        val parameterSpec = GCMParameterSpec(TAG_BIT_LENGTH, ivBytes)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)

        val decryptedBytes = cipher.doFinal(decodedData)

        return String(decryptedBytes, Charsets.UTF_8)
    }
}
