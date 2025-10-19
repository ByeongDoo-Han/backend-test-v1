package im.bigs.pg.external.exception

class CustomException(
    val exceptionCode: ExceptionCode,
    val enc: String?
) : RuntimeException()
