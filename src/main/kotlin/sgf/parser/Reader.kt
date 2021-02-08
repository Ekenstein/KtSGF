package sgf.parser

interface Reader<out T> {
    fun read(): Pair<T, Reader<T>>?
    fun location(): Location

    private class FromList(private val source: List<Char>, private val position: Int) : Reader<Char> {
        override fun read(): Pair<Char, Reader<Char>>? = source.getOrNull(position)?.let { it to FromList(source, position + 1) }
        override fun location(): Location = Location(position)
    }

    companion object {
        fun string(s: String): Reader<Char> = FromList(s.toList(), 0)
    }
}