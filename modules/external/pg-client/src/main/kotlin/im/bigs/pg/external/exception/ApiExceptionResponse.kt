package im.bigs.pg.external.exception

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class ApiExceptionResponse(
    @Schema(description = "pg사 code", example = "1005")
    val code: Int? = null,
    @Schema(description = "pg사 error code", example = "TAMPERED_CARD")
    val errorCode: String? = null,
    @Schema(description = "pg사 error message", example = "위조 또는 변조된 카드입니다. (허용되지 않은 카드)")
    val message: String? = null,
    @Schema(description = "참초 id", example = "b48c79bd-e1b3-416a-a583-efe90d1ee438")
    val referenceId: String? = null,
    @Schema(description = "에러 발생 시간", example = "2025-10-11T07:07:08")
    val exceptionTime: LocalDateTime = LocalDateTime.now()
)
