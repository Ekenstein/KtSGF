package sgf

import sgf.models.*
import java.lang.StringBuilder
import java.nio.charset.Charset

interface Sgf {
    fun serialize(builder: StringBuilder)
}

class Collection : Sgf {
    private val gameTrees = arrayListOf<GameTree>()

    fun tree(init: GameTree.() -> Unit) {
        val tree = GameTree()
        tree.init()
        gameTrees.add(tree)
    }

    override fun serialize(builder: StringBuilder) {
        for (tree in gameTrees) {
            tree.serialize(builder)
        }
    }
}

class GameTree : Sgf {
    private val sequence = arrayListOf<Node>()
    private val gameTrees = arrayListOf<GameTree>()

    fun root(init: RootNode.() -> Unit) {
        val node = RootNode()
        node.init()
        sequence.add(node)
    }

    fun move(init: MoveNode.() -> Unit) {
        val node = MoveNode()
        node.init()
        sequence.add(node)
    }

    fun tree(init: GameTree.() -> Unit) {
        val tree = GameTree()
        tree.init()
        gameTrees.add(tree)
    }

    override fun serialize(builder: StringBuilder) {
        builder.append("(")
        for (node in sequence) {
            node.serialize(builder)
        }

        for (tree in gameTrees) {
            tree.serialize(builder)
        }

        builder.append(")")
    }
}

fun sgf(init: Collection.() -> Unit): Collection {
    val collection = Collection()
    collection.init()
    return collection
}

abstract class Node : Sgf {
    protected val properties = mutableSetOf<GameProperty>()

    override fun serialize(builder: StringBuilder) {
        builder.append(";")
        for (property in properties) {
            property.serialize(builder)
        }
    }

    protected fun addNode(identifier: String, values: List<SgfValueType>) {
        properties.add(GameProperty(SgfProperty(UpperCaseString(identifier), values)))
    }
}

class MoveNode : Node() {
    fun blackMove(x: UInt, y: UInt) {
        val identifier = UpperCaseString("B")
        val value = listOf(SgfValueType.SgfPoint(x, y))
        properties.add(GameProperty(SgfProperty(identifier, value)))
    }

    fun whiteMove(x: UInt, y: UInt) {
        val identifier = UpperCaseString("W")
        val value = listOf(SgfValueType.SgfPoint(x, y))
        properties.add(GameProperty(SgfProperty(identifier, value)))
    }

    fun moveNumber(number: UInt) {
        val value = listOf(SgfValueType.SgfNumber(number))
        addNode("MN", value)
    }
}

class RootNode : Node() {
    fun application(name: String, version: String) {
        val left = SgfValueType.SgfSimpleText(name, true)
        val right = SgfValueType.SgfSimpleText(version, true)
        val value = listOf(SgfValueType.SgfCompose(left, right))
        val identifier = UpperCaseString("AP")
        properties.add(GameProperty(SgfProperty(identifier, value)))
    }

    fun charset(charset: Charset = Charsets.ISO_8859_1) {
        val value = listOf(SgfValueType.SgfSimpleText(charset.displayName()))
        val identifier = UpperCaseString("CA")
        properties.add(GameProperty(SgfProperty(identifier, value)))
    }

    fun fileFormat(fileFormat: UInt) {
        val value = listOf(SgfValueType.SgfNumber(fileFormat))
        val identifier = UpperCaseString("FF")
        properties.add(GameProperty(SgfProperty(identifier, value)))
    }

    fun size(size: UInt) {
        val value = SgfValueType.SgfNumber(size)
        val identifier = UpperCaseString("SZ")
        properties.add(GameProperty(SgfProperty(identifier, listOf(value))))
    }
}

class GameProperty(private val property: SgfProperty) : Sgf {
    override fun serialize(builder: StringBuilder) {
        builder.append(property.identifier)
        for (value in property.values) {
            val s = valueToString(value)
            builder.append("[$s]")
        }
    }

    private fun signToString(sign: SgfSign) = when(sign) {
        SgfSign.Plus -> "+"
        SgfSign.Minus -> "-"
    }

    private fun escapeIllegalChars(c: Char, isComposed: Boolean): String {
        val illegalChars =  listOf(']', '\\').union(listOf(':').filter { isComposed })
        if (c in illegalChars) {
            return "\\$c"
        }

        return c + ""
    }

    private fun fromLinebreakToSpace(c: Char): Char = if (c == '\n') ' ' else c

    private fun convertIllegalWhitespace(c: Char): Char {
        val legalWhitespaces = listOf(' ', '\n')
        if (c.isWhitespace() && c !in legalWhitespaces) {
            return ' '
        }

        return c
    }

    private fun intToChar(pos: UInt): Char {
        val x = 'a'.toInt()
        return (pos.toInt() + x).toChar()
    }

    private fun valueToString(value: SgfValueType): String = when(value) {
        SgfValueType.SgfNone -> ""
        is SgfValueType.SgfNumber -> value.sign?.let(::signToString).orEmpty() + value.digits.toString()
        is SgfValueType.SgfReal -> valueToString(value.int) + value.fraction?.let { it.toString() }.orEmpty()
        SgfValueType.SgfDouble.Normal -> "1"
        SgfValueType.SgfDouble.Emphasize -> "2"
        SgfValueType.SgfColor.Black -> "B"
        SgfValueType.SgfColor.White -> "W"
        is SgfValueType.SgfSimpleText -> value.text
            .map { fromLinebreakToSpace(it) }
            .map { escapeIllegalChars(it, value.isComposed) }
            .reduce { acc, next -> acc + next }
        is SgfValueType.SgfText -> value.text
            .map { convertIllegalWhitespace(it) }
            .map { escapeIllegalChars(it, value.isComposed) }
            .reduce { acc, next -> acc + next }
        is SgfValueType.SgfPoint -> "" + intToChar(value.x) + intToChar(value.y)
        is SgfValueType.SgfCompose<*, *> -> valueToString(value.left) + ":" + valueToString(value.right)
    }
}