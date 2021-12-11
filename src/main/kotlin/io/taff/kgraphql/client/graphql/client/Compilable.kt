package io.taff.kgraphql.client.graphql.client

/**
 * Compiler for segemnts of a graphql operation including the operation itself.
 *
 * @param T The expected compilation return type.
 */
interface Compilable<T> {

    /**
     * Compile this to a T.
     */
    fun compile(): T
}
