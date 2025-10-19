package im.bigs.pg.external.exception

import im.bigs.pg.external.dto.TestPgExceptionResponse

class TestPgException(
    val errorResult: TestPgExceptionResponse
) : RuntimeException(errorResult.message)
