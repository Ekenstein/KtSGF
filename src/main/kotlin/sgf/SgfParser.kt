package sgf

import sgf.models.SgfGameTree
import sgf.models.SgfProperty
import sgf.parser.Parser
import sgf.parser.Response

class SgfParser {
    private val propertyParser: Parser<Char, SgfProperty> = { }

    fun parse(s: String): Response<Char, List<SgfGameTree>> {

    }
}