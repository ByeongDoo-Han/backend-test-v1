package im.bigs.pg.external.exception

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "pg사 에러 response")
data class ApiExceptionResponse(
    @Schema(description = "code", example = "400")
    val code: Int? = null,
    @Schema(description = "error code", example = "BAD_REQUEST")
    val errorCode: String? = null,
    @Schema(description = "pg사 error message", example = "요청 데이터가 부정확합니다.")
    val message: String? = null,
    @Schema(description = "참초 id", example = "b48c79bd-e1b3-416a-a583-efe90d1ee438")
    val referenceId: String? = null,
    @Schema(description = "에러 발생 시간", example = "2025-10-11T07:07:08")
    val exceptionTime: LocalDateTime = LocalDateTime.now()
)
