package sgf.models

data class UpperCaseString private constructor(val value: String) {
    companion object {
        operator fun invoke(value: String): UpperCaseString = UpperCaseString(value.toUpperCase())
    }

    override fun toString(): String = value
}