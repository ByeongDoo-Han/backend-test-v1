package im.bigs.pg.infra.persistence

import im.bigs.pg.application.payment.port.out.PaymentQuery
import im.bigs.pg.infra.persistence.config.JpaConfig
import im.bigs.pg.infra.persistence.payment.adapter.PaymentPersistenceAdapter
import im.bigs.pg.infra.persistence.payment.entity.PaymentEntity
import im.bigs.pg.infra.persistence.payment.repository.PaymentJpaRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ContextConfiguration
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DataJpaTest
@ContextConfiguration(classes = [JpaConfig::class])
class PaymentRepositoryPagingTest @Autowired constructor(
    val paymentRepo: PaymentJpaRepository,
) {
    val adapter = PaymentPersistenceAdapter(paymentRepo)

    @Test
    @DisplayName("커서 페이징과 통계가 일관되어야 한다")
    fun `커서 페이징과 통계가 일관되어야 한다`() {
        val baseTs = Instant.parse("2024-01-01T00:00:00Z")
        repeat(35) { i ->
            paymentRepo.save(
                PaymentEntity(
                    partnerId = 1L,
                    amount = BigDecimal("1000"),
                    appliedFeeRate = BigDecimal("0.0300"),
                    feeAmount = BigDecimal("30"),
                    netAmount = BigDecimal("970"),
                    cardBin = null,
                    cardLast4 = "%04d".format(i),
                    approvalCode = "A$i",
                    approvedAt = baseTs.plusSeconds(i.toLong()),
                    status = "APPROVED",
                    createdAt = baseTs.plusSeconds(i.toLong()),
                    updatedAt = baseTs.plusSeconds(i.toLong()),
                ),
            )
        }

        val first = paymentRepo.pageBy(1L, "APPROVED", null, null, null, null, PageRequest.of(0, 21))
        assertEquals(21, first.size)
        val lastOfFirst = first[20]
        val second = paymentRepo.pageBy(
            1L, "APPROVED", null, null,
            lastOfFirst.createdAt, lastOfFirst.id, PageRequest.of(0, 21),
        )
        assertTrue(second.isNotEmpty())

        val sumList = paymentRepo.summary(1L, "APPROVED", null, null)
        val row = sumList.first()
        assertEquals(35L, (row[0] as Number).toLong())
        assertEquals(BigDecimal("35000"), row[1] as BigDecimal)
        assertEquals(BigDecimal("33950"), row[2] as BigDecimal)
    }

    @Test
    @DisplayName("마지막 페이지일 경우 다음 커서 정보가 null이어야 한다")
    fun `마지막 페이지일 경우 다음 커서 정보가 null이어야 한다`() {
        // given
        val baseTs = Instant.parse("2024-01-01T00:00:00Z")
        repeat(15) { i ->
            paymentRepo.save(
                PaymentEntity(
                    partnerId = 1L,
                    amount = BigDecimal("1000"),
                    appliedFeeRate = BigDecimal("0.0300"),
                    feeAmount = BigDecimal("30"),
                    netAmount = BigDecimal("970"),
                    cardBin = null,
                    cardLast4 = "%04d".format(i),
                    approvalCode = "A$i",
                    approvedAt = baseTs.plusSeconds(i.toLong()),
                    status = "APPROVED",
                    createdAt = baseTs.plusSeconds(i.toLong()),
                    updatedAt = baseTs.plusSeconds(i.toLong()),
                )
            )
        }

        // when
        val query = PaymentQuery(partnerId = 1L, limit = 20) // 페이지 크기 20
        val resultPage = adapter.findBy(query)

        // then
        assertEquals(15, resultPage.items.size)
        kotlin.test.assertFalse(resultPage.hasNext, "다음 페이지가 없어야 합니다.")
        kotlin.test.assertNull(resultPage.nextCursorCreatedAt, "다음 생성일시 커서는 null이어야 합니다.")
        kotlin.test.assertNull(resultPage.nextCursorId, "다음 ID 커서는 null이어야 합니다.")
    }

    @Test
    @DisplayName("마지막 페이지가 아닐 경우 다음 커서 정보가 있어야 한다")
    fun `마지막 페이지가 아닐 경우 다음 커서 정보가 있어야 한다`() {
        // given
        val baseTs = Instant.parse("2024-01-01T00:00:00Z")
        repeat(15) { i ->
            paymentRepo.save(
                PaymentEntity(
                    partnerId = 1L,
                    amount = BigDecimal("1000"),
                    appliedFeeRate = BigDecimal("0.0300"),
                    feeAmount = BigDecimal("30"),
                    netAmount = BigDecimal("970"),
                    cardBin = null,
                    cardLast4 = "%04d".format(i),
                    approvalCode = "A$i",
                    approvedAt = baseTs.plusSeconds(i.toLong()),
                    status = "APPROVED",
                    createdAt = baseTs.plusSeconds(i.toLong()),
                    updatedAt = baseTs.plusSeconds(i.toLong()),
                )
            )
        }

        // when
        val query2 = PaymentQuery(partnerId = 1L, limit = 10) // 페이지 크기 10
        val resultPage2 = adapter.findBy(query2)

        // then
        kotlin.test.assertTrue(resultPage2.hasNext, "다음 페이지가 있습니다.")
        kotlin.test.assertNotNull(resultPage2.nextCursorCreatedAt, "다음 생성일시 있습니다.")
        kotlin.test.assertNotNull(resultPage2.nextCursorId, "다음 ID 있습니다.")
    }
}
