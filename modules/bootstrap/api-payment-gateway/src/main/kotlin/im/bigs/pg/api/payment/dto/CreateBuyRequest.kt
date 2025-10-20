package im.bigs.pg.api.payment.dto

import jakarta.validation.constraints.Min
import java.math.BigDecimal

data class CreateBuyRequest(
    val cardNumber: String,
    val birthDate: String,
    val expiry: String,
    val password: String,
    val partnerId: Long,

    @field:Min(1)
    val amount: BigDecimal,
    val productName: String? = null
)
