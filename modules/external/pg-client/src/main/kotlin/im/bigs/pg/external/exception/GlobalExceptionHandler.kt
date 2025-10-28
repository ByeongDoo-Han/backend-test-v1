package im.bigs.pg.external.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(TestPgException::class)
    fun handleTestPgException(ex: TestPgException): ResponseEntity<ApiExceptionResponse> {
        logger.warn("PG 요청 에러 발생 : {}", ex.errorResult)
        val errorResponse = ApiExceptionResponse(
            code = ex.errorResult.code,
            errorCode = ex.errorResult.errorCode,
            message = ex.errorResult.message,
            referenceId = ex.errorResult.referenceId,
            exceptionTime = LocalDateTime.now()
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse)
    }

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(ex: CustomException): ResponseEntity<ApiExceptionResponse> {
        logger.warn("서버 예외 발생 : {}", ex)
        val errorResponse = ApiExceptionResponse(
            code = ex.exceptionCode.httpStatus?.value(),
            errorCode = ex.exceptionCode.code,
            message = ex.exceptionCode.message,
            referenceId = ex.enc,
            exceptionTime = LocalDateTime.now()
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse)
    }
}
