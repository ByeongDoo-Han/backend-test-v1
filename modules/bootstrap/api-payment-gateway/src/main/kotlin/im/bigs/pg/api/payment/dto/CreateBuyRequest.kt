package im.bigs.pg.api.payment.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import java.math.BigDecimal

data class CreateBuyRequest(
    @Schema(description = "카드번호", example = "1111-1111-1111-1111")
    val cardNumber: String,
    @Schema(description = "생년월일", example = "19900101")
    val birthDate: String,
    @Schema(description = "만료일", example = "1227")
    val expiry: String,
    @Schema(description = "비밀번호 앞 두자리", example = "12")
    val password: String,
    @Schema(description = "파트너 id", example = "2")
    val partnerId: Long,

    @field:Min(1)
    @Schema(description = "결제 금액", example = "10000")
    val amount: BigDecimal,
    @Schema(description = "제품 이름", example = "소고기")
    val productName: String? = null
)
