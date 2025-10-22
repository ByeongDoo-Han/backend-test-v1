package im.bigs.pg.application.pg.port.out

import im.bigs.pg.application.payment.port.`in`.BuyCommand
import java.math.BigDecimal

/** PG 승인 요청 최소 정보. */
data class PgApproveRequest(
    val cardNumber: String,
    val birthDate: String,
    val expiry: String,
    val password: String,
    val amount: BigDecimal
) {
    companion object {
        fun fromBuy(command: BuyCommand): PgApproveRequest {
            return PgApproveRequest(
                cardNumber = command.cardNumber,
                birthDate = command.birthDate,
                expiry = command.expiry,
                password = command.password,
                amount = command.amount
            )
        }
    }
}
