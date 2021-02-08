import sgf.sgf
import java.lang.StringBuilder

fun main(args: Array<String>) {
    val collection = sgf {
        tree {
            root {
                application("test", "1.0")
                size(19u)
                fileFormat(1u)
                charset(Charsets.ISO_8859_1)
            }

            move {
                blackMove(3u, 3u)
                moveNumber(1u)
            }
        }
    }

    val builder = StringBuilder()
    collection.serialize(builder)
    val s = builder.toString()
}