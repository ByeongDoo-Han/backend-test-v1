package im.bigs.pg.application.payment.port.`in`

import java.math.BigDecimal

data class BuyCommand(
    val cardNumber: String,
    val birthDate: String,
    val expiry: String,
    val password: String,
    val amount: BigDecimal,
    val productName: String?,
    val partnerId: Long
)
