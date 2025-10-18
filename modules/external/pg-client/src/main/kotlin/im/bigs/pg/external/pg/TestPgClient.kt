package im.bigs.pg.external.pg

import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.application.pg.port.out.TestPgApproveResult
import im.bigs.pg.application.pg.port.out.TestPgErrorResult
import im.bigs.pg.application.pg.port.out.TestPgRequestDto
import im.bigs.pg.common.util.CardInfo
import im.bigs.pg.common.util.PaymentEncryptor
import im.bigs.pg.domain.payment.PaymentStatus
import im.bigs.pg.external.exception.TestPgException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class TestPgClient(
    private val testPgApiWebClient: WebClient
) : PgClientOutPort {
    private val apiKey = "11111111-1111-4111-8111-111111111111"
    private val iv = "AAAAAAAAAAAAAAAA"
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun supports(partnerId: Long): Boolean = partnerId == 2L

    override fun approve(request: PgApproveRequest): PgApproveResult {
        val info = CardInfo(
            cardNumber = request.cardNumber,
            birthDate = request.birthDate,
            expiry = request.expiry,
            password = request.password,
            amount = request.amount
        )
        val enc = PaymentEncryptor.encrypt(info, apiKey, iv)
        val encRequestDto = TestPgRequestDto(
            enc = enc
        )
        val approveResult = testPgApiWebClient.post()
            .uri("/api/v1/pay/credit-card")
            .header("API-KEY", apiKey)
            .bodyValue(encRequestDto)
            .retrieve()
            .onStatus({ status -> status.isError }) { clientResponse ->
                clientResponse.bodyToMono(TestPgErrorResult::class.java)
                    .flatMap { errorBody ->
                        logger.warn("errorbody : {}", errorBody)
                        Mono.error(TestPgException(errorBody))
                    }
            }
            .bodyToMono(TestPgApproveResult::class.java)
            .block(Duration.ofSeconds(5))

        if (approveResult == null) {
            throw IllegalArgumentException("PG사로부터 응답이 없습니다.")
        }

        return PgApproveResult(
            approvalCode = approveResult.approvalCode,
            approvedAt = approveResult.approvedAt,
            status = if (approveResult.status == "APPROVED") PaymentStatus.APPROVED else PaymentStatus.CANCELED
        )
    }
}
