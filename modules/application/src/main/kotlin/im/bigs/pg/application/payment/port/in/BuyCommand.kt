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
) {
    fun getCardBin(): String {
        return cardNumber.take(4) + cardNumber.substring(5, 7)
    }

    fun getCardLast4(): String {
        return cardNumber.substring(15, cardNumber.length)
    }
}
