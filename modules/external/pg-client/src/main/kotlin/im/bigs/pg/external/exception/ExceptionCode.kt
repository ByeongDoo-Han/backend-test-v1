package im.bigs.pg.external.exception

import org.springframework.http.HttpStatus

enum class ExceptionCode(
    val httpStatus: HttpStatus? = HttpStatus.valueOf(400),
    val code: String?,
    val message: String?
) {
    // pg
    NO_RESPONSE_FROM_PG(HttpStatus.BAD_REQUEST, "P001", "PG사로부터 응답이 없습니다."),
}
