package im.bigs.pg.external.pg

import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.application.pg.port.out.TestPgApproveRequest
import im.bigs.pg.common.util.CardInfo
import im.bigs.pg.common.util.PaymentEncryptor
import im.bigs.pg.domain.payment.PaymentStatus
import im.bigs.pg.external.dto.TestPgApproveResponse
import im.bigs.pg.external.dto.TestPgExceptionResponse
import im.bigs.pg.external.exception.CustomException
import im.bigs.pg.external.exception.ExceptionCode
import im.bigs.pg.external.exception.TestPgException
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class TestPgClient(
    private val testPgApiWebClient: WebClient
) : PgClientOutPort {
    companion object {
        private const val API_KEY = "11111111-1111-4111-8111-111111111111"
        private const val API_KEY_HEADER = "API-KEY"
        private const val IV = "AAAAAAAAAAAAAAAA"
        private const val TEST_PG_API_URI = "/api/v1/pay/credit-card"
    }

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
        val enc = PaymentEncryptor.encrypt(info, API_KEY, IV)
        val encRequestDto = TestPgApproveRequest(
            enc = enc
        )
        val approveResult = testPgApiWebClient.post()
            .uri(TEST_PG_API_URI)
            .header(API_KEY_HEADER, API_KEY)
            .bodyValue(encRequestDto)
            .retrieve()
            .onStatus({ status -> status.isError }) { clientResponse ->
                clientResponse.bodyToMono(TestPgExceptionResponse::class.java)
                    .flatMap { errorBody ->
                        logger.warn("errorbody : {}", errorBody)
                        Mono.error(TestPgException(errorBody))
                    }
            }
            .bodyToMono(TestPgApproveResponse::class.java)
            .block(Duration.ofSeconds(5))
        logger.info("success : {}", approveResult)
        if (approveResult == null) {
            throw CustomException(ExceptionCode.NO_RESPONSE_FROM_PG, enc)
        }

        return PgApproveResult(
            approvalCode = approveResult.approvalCode,
            approvedAt = approveResult.approvedAt,
            status = if (approveResult.status == PaymentStatus.APPROVED.value) PaymentStatus.APPROVED else PaymentStatus.CANCELED
        )
    }
}
