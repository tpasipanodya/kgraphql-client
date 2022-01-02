# kgraphql-client
![Build](https://github.com/tpasipanodya/kgraphql-client/actions/workflows/.github/workflows/release.yaml/badge.svg)

A light-weight, declarative GraphQL client for Kotlin.

### What is it?

A graphql query builder & runner that trades compile-time type-safety for a simple, flexible & dynamic
query API. Example use cases include testing and verifying APIs. 

### What is it not? 

A full-fledged graphql client with builtin type checking, validations, caching, and all the other bells
and whistles. There are several other great libraries like Expedia's
[graphql-kotlin](https://github.com/ExpediaGroup/graphql-kotlin) that already serve those purposes.

## Using The Graphql Client
To Download:

```kotlin
implementation("io.taff:kgraphql-client:0.5.0")
```

Given a graphql service hosted at `http://fancyservice.com/graphql` with the schema:

```graphql
type Author {
    name: String!
}

type Query {
    author(name: String!) : Author
}
```

You can query that service as shown below:
```kotlin
/** Create a client*/
val client = Client.new()
    .url("http://fancyservice.com/graphql")
    .build()

/** Call that service returning a deserialized Kotlin type */
val query = client.query("author") {
    input(name = "id", value = 1)
    select("name")
}

/** Deserializing the response into Kotlin types */
data class Author(val name: String)

val author = query.resultAs<Author>()

/** Or as JSON (maps & lists) */
val authorJson = query.resultAs<Map<String, Any>>()

configure {
    
    /** You can also customize how the response will be deserialized via Jackson object mappers */
    objectMapper = myCustomObjectMapper

    /** Or capture the underlying fuel request & response, e.g for logging purposes */
    onRequest = { request: Request -> println(request) }
    onResponse = { response: response, result: result -> 
        println("response: $response, result: $result") 
    }
}

```

## License

`kgraphql-client` is Open Source software released under the [MIT license](https://opensource.org/licenses/MIT).
