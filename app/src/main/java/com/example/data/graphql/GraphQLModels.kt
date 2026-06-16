package com.example.data.graphql

/**
 * Standard GraphQL request wrapper.
 */
data class GraphQLRequest(
    val query: String,
    val variables: Map<String, Any> = emptyMap(),
    val operationName: String? = null
)

/**
 * Standard GraphQL response envelope.
 */
data class GraphQLResponse<out T>(
    val data: T? = null,
    val errors: List<GraphQLError>? = null
)

/**
 * GraphQL sub-node for errors mimicking real standards.
 */
data class GraphQLError(
    val message: String,
    val locations: List<ErrorLocation>? = null,
    val path: List<Any>? = null,
    val extensions: Map<String, Any>? = null
)

data class ErrorLocation(
    val line: Int,
    val column: Int
)

/**
 * Helpers returning GraphQLResponse<Any> to enforce static type inference.
 */
fun successResponse(payload: Any): GraphQLResponse<Any> {
    return GraphQLResponse(data = payload)
}

fun errorResponse(message: String): GraphQLResponse<Any> {
    return GraphQLResponse(errors = listOf(GraphQLError(message = message)))
}
