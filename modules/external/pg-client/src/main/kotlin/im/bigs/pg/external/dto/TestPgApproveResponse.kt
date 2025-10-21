package im.bigs.pg.external.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class TestPgApproveResponse(
    val approvalCode: String,
    val approvedAt: LocalDateTime,
    val maskedCardLast4: String,
    val amount: BigDecimal,
    val status: String
)
