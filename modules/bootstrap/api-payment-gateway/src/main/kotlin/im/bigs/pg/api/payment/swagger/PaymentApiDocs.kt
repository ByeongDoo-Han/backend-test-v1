package im.bigs.pg.api.payment.swagger

import im.bigs.pg.api.payment.dto.CreateBuyRequest
import im.bigs.pg.api.payment.dto.CreatePaymentRequest
import im.bigs.pg.api.payment.dto.PaymentResponse
import im.bigs.pg.api.payment.dto.QueryResponse
import im.bigs.pg.external.exception.ApiExceptionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDateTime

@Tag(name = "결제 API", description = "결제 생성 및 조회 관련 API")
interface PaymentApiDocs {

    @Operation(summary = "결제 생성", description = "암호화된 카드 정보로 결제를 생성하고 PG사에 승인 요청을 전달합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "결제 생성 및 승인 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(
                            implementation = PaymentResponse::class,
                        ),
                    )
                ]
            ), ApiResponse(
                responseCode = "400", description = "요청 데이터 유효성 오류",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ApiExceptionResponse::class),
                    )
                ]
            ), ApiResponse(
                responseCode = "401", description = "인증 실패",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [
                            ExampleObject(
                                name = "API-KEY 헤더 없음", summary = "API-KEY 헤더 없음"
                            ),
                            ExampleObject(
                                name = "API-KEY 포맷 오류", summary = "API-KEY 포맷 오류"
                            ),
                            ExampleObject(
                                name = "미등록 API-KEY", summary = "미등록 API-KEY"
                            )
                        ]
                    )
                ]
            ), ApiResponse(
                responseCode = "422", description = "PG사 카드 정보 유효성 오류",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ApiExceptionResponse::class),
                        examples = [
                            ExampleObject(
                                name = "STOLEN_OR_LOST", summary = "도난 또는 분실된 카드입니다.",
                                value = """
                                    {
                                        "code":1001,
                                        "errorCode":"STOLEN_OR_LOST",
                                        "message":"도난 또는 분실된 카드입니다.",
                                        "referenceId":"b48c79bd-e1b3-416a-a583-efe90d1ee438",
                                        "exceptionTime": "2025-10-28T07:15:14.707Z"
                                    }
                                """
                            ),
                            ExampleObject(
                                name = "INSUFFICIENT_LIMIT", summary = "한도가 초과되었습니다.",
                                value = """
                                    {
                                        "code":1002,
                                        "errorCode":"STOLEN_OR_LOST",
                                        "message":"한도가 초과되었습니다.",
                                        "referenceId":"b48c79bd-e1b3-416a-a583-efe90d1ee438",
                                        "exceptionTime": "2025-10-28T07:15:14.707Z"
                                    }
                                """
                            ),
                            ExampleObject(
                                name = "EXPIRED_OR_BLOCKED", summary = "정지되었거나 만료된 카드입니다.",
                                value = """
                                    {
                                        "code":1003,
                                        "errorCode":"EXPIRED_OR_BLOCKED",
                                        "message":"정지되었거나 만료된 카드입니다.",
                                        "referenceId":"b48c79bd-e1b3-416a-a583-efe90d1ee438",
                                        "exceptionTime": "2025-10-28T07:15:14.707Z"
                                    }
                                """
                            ),
                            ExampleObject(
                                name = "TAMPERED_CARD", summary = "위조 또는 변조된 카드입니다.",
                                value = """
                                    {
                                        "code":1004,
                                        "errorCode":"TAMPERED_CARD",
                                        "message":"위조 또는 변조된 카드입니다.",
                                        "referenceId":"b48c79bd-e1b3-416a-a583-efe90d1ee438",
                                        "exceptionTime": "2025-10-28T07:15:14.707Z"
                                    }
                                """
                            ),
                            ExampleObject(
                                name = "TAMPERED_CARD2", summary = "위조 또는 변조된 카드입니다. (허용되지 않은 카드)",
                                value = """
                                    {
                                        "code":1005,
                                        "errorCode":"TAMPERED_CARD",
                                        "message":"위조 또는 변조된 카드입니다. (허용되지 않은 카드)",
                                        "referenceId":"b48c79bd-e1b3-416a-a583-efe90d1ee438",
                                        "exceptionTime": "2025-10-28T07:15:14.707Z"
                                    }
                                """
                            )
                        ]
                    )
                ]
            )
        ]
    )
    fun create(@RequestBody @Valid req: CreatePaymentRequest): ResponseEntity<PaymentResponse>

    @Operation(summary = "결제 목록 조회", description = "다양한 조건으로 결제 목록을 조회합니다. (커서 기반 페이지네이션)")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [
                    Content(
                        mediaType = "application/json", schema = Schema(implementation = QueryResponse::class)
                    )
                ]
            )
        ]
    )
    fun query(
        @Parameter(description = "파트너 ID로 필터링") @RequestParam(required = false) partnerId: Long?,
        @Parameter(description = "결제 상태로 필터링 (APPROVED, FAILED 등)") @RequestParam(required = false) status: String?,
        @Parameter(description = "조회 시작 시각 (yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false) from: LocalDateTime?,
        @Parameter(description = "조회 종료 시각 (yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false) to: LocalDateTime?,
        @Parameter(description = "다음 페이지 조회를 위한 커서") @RequestParam(required = false) cursor: String?,
        @Parameter(description = "한 페이지에 보여줄 항목 수") @RequestParam(defaultValue = "20") limit: Int,
    ): ResponseEntity<QueryResponse>

    @Operation(summary = "구매 요청 (테스트)", description = "카드 정보를 입력해 결제 요청 결과를 테스트합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "구매 요청 성공",
                content = [
                    Content(
                        mediaType = "application/json", schema = Schema(implementation = PaymentResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400", description = "구매 요청 실패",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ApiExceptionResponse::class)
                    )
                ]
            )
        ]
    )
    fun buy(
        @RequestBody request: CreateBuyRequest
    ): ResponseEntity<PaymentResponse>
}
