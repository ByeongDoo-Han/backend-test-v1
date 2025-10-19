package im.bigs.pg.external.exception

import java.time.LocalDateTime

data class ApiExceptionResponse(
    val code: Int? = null,
    val errorCode: String? = null,
    val message: String? = null,
    val referenceId: String? = null,
    val exceptionTime: LocalDateTime = LocalDateTime.now()
)
