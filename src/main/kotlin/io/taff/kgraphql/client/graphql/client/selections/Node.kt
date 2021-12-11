package io.taff.kgraphql.client.graphql.client.selections

import io.taff.kgraphql.client.graphql.client.Compilable

/**
 * Abstraction around selections.
 */
data class Node(val value: String) : Compilable<String> {
    override fun compile() = value
}
