package im.bigs.pg.external.exception

import im.bigs.pg.application.pg.port.out.TestPgErrorResult

class TestPgException(
    val errorResult: TestPgErrorResult
) : RuntimeException(errorResult.message)
