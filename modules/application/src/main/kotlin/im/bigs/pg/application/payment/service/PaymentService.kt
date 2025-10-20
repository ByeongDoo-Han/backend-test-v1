package im.bigs.pg.application.payment.service

import im.bigs.pg.application.partner.port.out.FeePolicyOutPort
import im.bigs.pg.application.partner.port.out.PartnerOutPort
import im.bigs.pg.application.payment.port.`in`.BuyCommand
import im.bigs.pg.application.payment.port.`in`.PaymentCommand
import im.bigs.pg.application.payment.port.`in`.PaymentUseCase
import im.bigs.pg.application.payment.port.out.PaymentOutPort
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.domain.calculation.FeeCalculator
import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentStatus
import org.springframework.stereotype.Service
import java.math.BigDecimal

/**
 * 결제 생성 유스케이스 구현체.
 * - 입력(REST 등) → 도메인/외부PG/영속성 포트를 순차적으로 호출하는 흐름을 담당합니다.
 * - 수수료 정책 조회 및 적용(계산)은 도메인 유틸리티를 통해 수행합니다.
 */
@Service
class PaymentService(
    private val partnerRepository: PartnerOutPort,
    private val feePolicyRepository: FeePolicyOutPort,
    private val paymentRepository: PaymentOutPort,
    private val pgClients: List<PgClientOutPort>,
) : PaymentUseCase {
    /**
     * 결제 승인/수수료 계산/저장을 순차적으로 수행합니다.
     * - 현재 예시 구현은 하드코드된 수수료(3% + 100)로 계산합니다.
     * - 과제: 제휴사별 수수료 정책을 적용하도록 개선해 보세요.
     */
    /**
     * @param command 제휴사 식별자, 결제 금액(정수 금액 권장), 카드 BIN(없을 수 있음), 카드 마지막 4자리(없을 수 있음), 상품명
     * @throws IllegalArgumentException 존재 하지 않는 파트너 id`
     * @throws IllegalArgumentException 비활성 파트너 id
     * @throws IllegalArgumentException 요청한 파트너 id에 해당하는 pg id 없음
     * @return pgClient 결제 승인 및 수수료 계산 후 생성된 결제 객체를 반환
     */
    override fun pay(command: PaymentCommand): Payment {
        val partner = partnerRepository.findById(command.partnerId)
            ?: throw IllegalArgumentException("Partner not found: ${command.partnerId}")
        require(partner.active) { "Partner is inactive: ${partner.id}" }

        val pgClient = pgClients.firstOrNull { it.supports(partner.id) }
            ?: throw IllegalStateException("No PG client for partner ${partner.id}")

        // pg api 테스트용 카드 정보
        val approveCommand = PgApproveRequest(
            cardNumber = "1111-1111-1111-1111",
            birthDate = "19900101",
            expiry = "1227",
            password = "12",
            amount = command.amount
        )

        // 결제 승인
        val approve = pgClient.approve(approveCommand)

        // 수수료 계산
        val feePolicy = feePolicyRepository.findEffectivePolicy(partner.id)
        val feePolicyRate = feePolicy?.percentage ?: BigDecimal.ZERO
        val feePolicyFixed = feePolicy?.fixedFee
        val (fee, net) = FeeCalculator.calculateFee(command.amount, feePolicyRate, feePolicyFixed)
        val payment = Payment(
            partnerId = partner.id,
            amount = command.amount,
            appliedFeeRate = feePolicyRate,
            feeAmount = fee,
            netAmount = net,
            cardBin = command.cardBin,
            cardLast4 = command.cardLast4,
            approvalCode = approve.approvalCode,
            approvedAt = approve.approvedAt,
            status = PaymentStatus.APPROVED,
        )

        // 저장
        return paymentRepository.save(payment)
    }

    override fun buy(command: BuyCommand): Payment {
        val partner = partnerRepository.findById(command.partnerId)
            ?: throw IllegalArgumentException("Partner not found: ${command.partnerId}")
        require(partner.active) { "Partner is inactive: ${partner.id}" }

        val pgClient = pgClients.firstOrNull { it.supports(partner.id) }
            ?: throw IllegalStateException("No PG client for partner ${partner.id}")

        val approveCommand = PgApproveRequest(
            cardNumber = command.cardNumber,
            birthDate = command.birthDate,
            expiry = command.expiry,
            password = command.password,
            amount = command.amount
        )
        val approve = pgClient.approve(approveCommand)

        val feePolicy = feePolicyRepository.findEffectivePolicy(partner.id)
        val feePolicyRate = feePolicy?.percentage ?: BigDecimal.ZERO
        val feePolicyFixed = feePolicy?.fixedFee
        val (fee, net) = FeeCalculator.calculateFee(command.amount, feePolicyRate, feePolicyFixed)
        val payment = Payment(
            partnerId = partner.id,
            amount = command.amount,
            appliedFeeRate = feePolicyRate,
            feeAmount = fee,
            netAmount = net,
            cardBin = command.cardNumber.take(4) + command.cardNumber.substring(5, 9),
            cardLast4 = command.cardNumber.substring(15, command.cardNumber.length),
            approvalCode = approve.approvalCode,
            approvedAt = approve.approvedAt,
            status = PaymentStatus.APPROVED,
        )
        return paymentRepository.save(payment)
    }
}
