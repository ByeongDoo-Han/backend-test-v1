package im.bigs.pg.application.payment.service

import im.bigs.pg.application.partner.port.out.FeePolicyOutPort
import im.bigs.pg.application.partner.port.out.PartnerOutPort
import im.bigs.pg.application.payment.port.`in`.PaymentCommand
import im.bigs.pg.application.payment.port.out.PaymentOutPort
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.domain.partner.FeePolicy
import im.bigs.pg.domain.partner.Partner
import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.DisplayName
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals

class PaymentServiceTest {
    private val partnerRepo = mockk<PartnerOutPort>()
    private val feeRepo = mockk<FeePolicyOutPort>()
    private val paymentRepo = mockk<PaymentOutPort>()
    private val pgClient = object : PgClientOutPort {
        override fun supports(partnerId: Long) = true
        override fun approve(request: PgApproveRequest) =
            PgApproveResult("APPROVAL-123", LocalDateTime.of(2024, 1, 1, 0, 0), PaymentStatus.APPROVED)
    }

    @Test
    @DisplayName("결제 시 수수료 정책을 적용하고 저장해야 한다")
    fun `결제 시 수수료 정책을 적용하고 저장해야 한다`() {
        val service = PaymentService(partnerRepo, feeRepo, paymentRepo, listOf(pgClient))
        every { partnerRepo.findById(1L) } returns Partner(1L, "TEST", "Test", true)
        every { feeRepo.findEffectivePolicy(1L, any()) } returns FeePolicy(
            id = 10L, partnerId = 1L, effectiveFrom = LocalDateTime.ofInstant(Instant.parse("2020-01-01T00:00:00Z"), ZoneOffset.UTC),
            percentage = BigDecimal("0.0300"), fixedFee = BigDecimal("100")
        )
        val savedSlot = slot<Payment>()
        every { paymentRepo.save(capture(savedSlot)) } answers { savedSlot.captured.copy(id = 99L) }

        val cmd = PaymentCommand(partnerId = 1L, amount = BigDecimal("10000"), cardLast4 = "4242")
        val res = service.pay(cmd)

        assertEquals(99L, res.id)
        assertEquals(BigDecimal("400"), res.feeAmount)
        assertEquals(BigDecimal("9600"), res.netAmount)
        assertEquals(PaymentStatus.APPROVED, res.status)
    }

    @Test
    @DisplayName("다른 파트너 ID로 결제 시 각기 다른 수수료 정책이 올바르게 적용되어야 한다")
    fun `다른 파트너 ID로 결제 시 각기 다른 수수료 정책이 올바르게 적용되어야 한다`() {
        // given
        val service = PaymentService(partnerRepo, feeRepo, paymentRepo, listOf(pgClient))
        val savedSlot = slot<Payment>()
        every { paymentRepo.save(capture(savedSlot)) } answers { savedSlot.captured.copy(id = 100L) }

        // Partner 1 (3% 수수료)
        val partner1 = Partner(1L, "PARTNER-A", "Partner A", true)
        val feePolicy1 = FeePolicy(10L, 1L, LocalDateTime.now(), BigDecimal("0.0300"), BigDecimal.ZERO)
        every { partnerRepo.findById(1L) } returns partner1
        every { feeRepo.findEffectivePolicy(1L, any()) } returns feePolicy1

        // Partner 2 (5% 수수료)
        val partner2 = Partner(2L, "PARTNER-B", "Partner B", true)
        val feePolicy2 = FeePolicy(11L, 2L, LocalDateTime.now(), BigDecimal("0.0500"), BigDecimal.ZERO)
        every { partnerRepo.findById(2L) } returns partner2
        every { feeRepo.findEffectivePolicy(2L, any()) } returns feePolicy2

        // Partner 3 (5% 수수료)
        val partner3 = Partner(3L, "PARTNER-C", "Partner C", true)
        val feePolicy3 = FeePolicy(12L, 3L, LocalDateTime.now(), BigDecimal("0.0235"), BigDecimal.ZERO)
        every { partnerRepo.findById(3L) } returns partner3
        every { feeRepo.findEffectivePolicy(3L, any()) } returns feePolicy3

        // when (Partner 1 결제)
        val cmd1 = PaymentCommand(partnerId = 1L, amount = BigDecimal("10000"), cardLast4 = "1111")
        service.pay(cmd1)

        // then (Partner 1 수수료 검증)
        val capturedPayment1 = savedSlot.captured
        assertEquals(BigDecimal("300"), capturedPayment1.feeAmount) // 10000 * 3%
        assertEquals(BigDecimal("9700"), capturedPayment1.netAmount)

        // when (Partner 2 결제)
        val cmd2 = PaymentCommand(partnerId = 2L, amount = BigDecimal("20000"), cardLast4 = "2222")
        service.pay(cmd2)

        // then (Partner 2 수수료 검증)
        val capturedPayment2 = savedSlot.captured
        assertEquals(BigDecimal("1000"), capturedPayment2.feeAmount) // 20000 * 5%
        assertEquals(BigDecimal("19000"), capturedPayment2.netAmount)

        // when (Partner 3 결제)
        val cmd3 = PaymentCommand(partnerId = 3L, amount = BigDecimal("1111"), cardLast4 = "1111")
        service.pay(cmd3)

        // then (Partner 3 수수료 검증)
        val capturedPayment3 = savedSlot.captured
        assertEquals(BigDecimal("26"), capturedPayment3.feeAmount) // 20000 * 5%
        assertEquals(BigDecimal("1085"), capturedPayment3.netAmount)
    }
}
