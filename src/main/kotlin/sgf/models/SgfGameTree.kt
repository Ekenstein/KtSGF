package sgf.models

data class SgfGameTree(val sequence: List<SgfNode>, val trees: List<SgfGameTree>)