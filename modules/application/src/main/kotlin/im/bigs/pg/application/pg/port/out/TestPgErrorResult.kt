package im.bigs.pg.application.pg.port.out

data class TestPgErrorResult(
    val code: Int?,
    val errorCode: String? = null,
    val message: String,
    val referenceId: String?
)