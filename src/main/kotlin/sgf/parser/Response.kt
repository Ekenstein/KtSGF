package sgf.parser

sealed class Response<I, out A> {
    data class Accept<I, out A>(val value: A, val input: Reader<I>) : Response<I, A>()
    data class Denied<I, out A>(val location: Location) : Response<I, A>()

    fun <B> fold(accept: (Accept<I, A>) -> B, denied: (Denied<I, A>) -> B): B =
        when (this) {
            is Accept -> accept(this)
            is Denied -> denied(this)
        }
}