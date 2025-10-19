package im.bigs.pg.external.dto

data class TestPgExceptionResponse(
    val code: Int?,
    val errorCode: String? = null,
    val message: String,
    val referenceId: String?
)
