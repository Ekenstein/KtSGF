package sgf.models

sealed class SgfValueType {
    object SgfNone : SgfValueType()

    data class SgfNumber(val digits: UInt, val sign: SgfSign? = null) : SgfValueType()

    data class SgfReal(val int: SgfNumber, val fraction: UInt? = null) : SgfValueType()

    sealed class SgfDouble : SgfValueType() {
        object Normal : SgfDouble()
        object Emphasize : SgfDouble()
    }

    sealed class SgfColor : SgfValueType() {
        object Black : SgfColor()
        object White : SgfColor()
    }

    data class SgfSimpleText(val text: String, val isComposed: Boolean = false) : SgfValueType()
    data class SgfText(val text: String, val isComposed: Boolean = false) : SgfValueType()
    data class SgfPoint(val x: UInt, val y: UInt) : SgfValueType()

    data class SgfCompose<L : SgfValueType, R : SgfValueType>(val left: L, val right: R) : SgfValueType()
}

enum class SgfSign {
    Plus,
    Minus
}