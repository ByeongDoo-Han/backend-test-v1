package im.bigs.pg.api.payment.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "결제 목록 조회 응답 DTO")
data class QueryResponse(
    @Schema(description = "결제 정보 목록")
    val items: List<PaymentResponse>,

    @Schema(description = "조회된 기간에 대한 요약 정보")
    val summary: Summary,

    @Schema(description = "다음 페이지 조회를 위한 커서 정보 (Base64URL)", nullable = true)
    val nextCursor: String?,

    @Schema(description = "다음 페이지 존재 여부")
    val hasNext: Boolean,
)

@Schema(description = "결제 요약 정보")
data class Summary(
    @Schema(description = "조회된 결제 건수", example = "15")
    val count: Long,

    @Schema(description = "조회된 결제 총액", example = "150000")
    val totalAmount: BigDecimal,

    @Schema(description = "조회된 총 정산 금액", example = "145500")
    val totalNetAmount: BigDecimal,
)
