package io.taff.kgraphql.client.graphql.client

/**
 * The supported types of graphql operations.
 */
enum class OperationType {

    QUERY,

    MUTATION;

    fun compile() = name.toLowerCase()
}
