package im.bigs.pg.api.payment.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import java.math.BigDecimal

@Schema(description = "결제 생성을 위한 요청 DTO")
data class CreatePaymentRequest(
    @Schema(description = "파트너 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    val partnerId: Long,

    @field:Min(1)
    @Schema(description = "결제 금액", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000")
    val amount: BigDecimal,

    @Schema(description = "카드 BIN (앞 6~8자리)", example = "424211")
    val cardBin: String? = null,

    @Schema(description = "카드 마지막 4자리", example = "1111")
    val cardLast4: String? = null,

    @Schema(description = "상품명", example = "테스트 상품")
    val productName: String? = null
)
