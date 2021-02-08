package sgf.parser

typealias Parser<I, A> = (Reader<I>) -> Response<I, A>

fun <I, A> fails(): Parser<I, A> = { Response.Denied(it.location()) }
fun <I, A> returns(value: A): Parser<I, A> = { Response.Accept(value, it) }

fun <I> any() : Parser<I, I> = { reader -> reader.read()?.let { Response.Accept(it.first, it.second) } ?: Response.Denied(reader.location()) }

fun <I> not(parser: Parser<I, *>): Parser<I, I> = { reader -> parser(reader).fold(
    { Response.Denied(reader.location()) },
    { any<I>()(reader) })}

fun <I, A> lazy(parser: () -> Parser<I, A>): Parser<I, A> = { parser()(it) }

fun <I, A> `try`(p: Parser<I, A>): Parser<I, A> = { p(it).fold(
    { accepted -> accepted },
    { denied -> Response.Denied(denied.location) }
)}

fun <I, A> join(p: Parser<I, Parser<I, A>>): Parser<I, A> = {
    when (val a = p(it)) {
        is Response.Accept -> {
            when (val b = a.value(a.input)) {
                is Response.Accept -> Response.Accept(b.value, b.input)
                is Response.Denied -> Response.Denied(b.location)
            }
        }
        is Response.Denied -> Response.Denied(a.location)
    }
}

fun <I, A, B> Parser<I, A>.map(f: (A) -> B): Parser<I, B> = {
    this(it).fold(
        { accepted -> Response.Accept(f(accepted.value), accepted.input) },
        { denied -> Response.Denied(denied.location) }
    )
}

fun <I, A, B> Parser<I, A>.flatMap(f: (A) -> Parser<I, B>): Parser<I, B> = join(this.map(f))

fun <I, A> Parser<I, A>.where(predicate: (A) -> Boolean): Parser<I, A> =
    this.flatMap {
        if (predicate(it)) {
            returns(it)
        } else {
            fails()
        }
    }

fun charIn(vararg cs: Char): Parser<Char, Char> = `try`(any<Char>().where { it in cs })
fun charIn(s: String): Parser<Char, Char> = `try`(any<Char>().where { it in s })
fun char(c: Char): Parser<Char, Char> = charIn(c)