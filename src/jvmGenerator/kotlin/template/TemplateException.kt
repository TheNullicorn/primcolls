package template

sealed class TemplateException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : RuntimeException()

class InvalidTemplateNameException(
    override val message: String?,
) : TemplateException() {
    companion object {
        fun malformedParamCount(name: String) =
            InvalidTemplateNameException("Template param count is not an integer: \"$name\"")
    }
}