package sgf.models

sealed class Either<L, R> {
    class Left<L>(val value: L): Either<L, Nothing>()
    class Right<R>(val value: R): Either<Nothing, R>()
}