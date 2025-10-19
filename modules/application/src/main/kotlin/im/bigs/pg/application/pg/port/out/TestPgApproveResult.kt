package im.bigs.pg.application.pg.port.out

import java.math.BigDecimal
import java.time.LocalDateTime

data class TestPgApproveResult(
    val approvalCode: String,
    val approvedAt: LocalDateTime,
    val maskedCardLast4: String,
    val amount: BigDecimal,
    val status: String
)
