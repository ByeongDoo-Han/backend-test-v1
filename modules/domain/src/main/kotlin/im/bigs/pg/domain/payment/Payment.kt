package im.bigs.pg.domain.payment

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.math.BigDecimal
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 결제 이력의 스냅샷.
 * - 저장 시점의 수수료율/수수료/정산금 등 계산 결과를 그대로 보존합니다.
 * - 카드 정보는 최소한의 식별 정보만 저장(마스킹/부분 저장)하도록 설계되었습니다.
 *
 * @property partnerId 제휴사 식별자
 * @property amount 결제 금액(정수 금액 권장)
 * @property appliedFeeRate 적용된 수수료율(저장 시점의 값)
 * @property feeAmount 수수료 금액
 * @property netAmount 공제 후 금액(정산금)
 * @property cardBin 선택 저장되는 카드 BIN(없을 수 있음)
 * @property cardLast4 마스킹용 마지막 4자리(없을 수 있음)
 * @property approvalCode 승인 식별 코드
 * @property approvedAt 승인 시각(UTC)
 * @property status 상태(승인/취소 등)
 * @property createdAt 생성 시각(정렬/커서 키)
 * @property updatedAt 갱신 시각
 */
data class Payment(
    val id: Long? = null,
    val partnerId: Long,
    val amount: BigDecimal,
    val appliedFeeRate: BigDecimal,
    val feeAmount: BigDecimal,
    val netAmount: BigDecimal,
    val cardBin: String? = null,
    val cardLast4: String? = null,
    val approvalCode: String,
    @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val approvedAt: LocalDateTime,
    val status: PaymentStatus,
    @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

/** 결제 상태. 취소 시에도 원본 행을 유지하고 상태만 변경하는 방식 등을 고려합니다. */
/** 결제 상태.
 * - 승인(Approved), 취소(Canceled) 등 단순 상태를 표현합니다.
 */
enum class PaymentStatus(val value: String) {
    APPROVED("APPROVED"),
    CANCELED("CANCELED");

    companion object {
        fun from(value: String?): PaymentStatus? {
            return PaymentStatus.entries.firstOrNull { it.value == value }
        }
    }
}

/** 조회 API의 통계 응답에 사용되는 값 모음. */
/** 조회 API의 통계 응답에 사용되는 값 모음. */
data class PaymentSummary(
    val count: Long,
    val totalAmount: BigDecimal,
    val totalNetAmount: BigDecimal,
)

data class EncryptingInfo(
    val cardNumber: String,
    val birthDate: String,
    val expiry: String,
    val password: String,
    val amount: BigDecimal
)

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
    fun encrypt(info: EncryptingInfo, apiKey: String, ivBase64Url: String): String {
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
}