package io.taff.kgraphql.client.graphql.client

import io.taff.spek.expekt.any.satisfy
import io.taff.spek.expekt.iterable.containInOrder
import io.taff.spek.expekt.map.beAMapOf
import io.taff.spek.expekt.should
import io.taff.kgraphql.client.configure
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


object QuerySpek : Spek({

    configure {
        onRequest  = { println("Running the following: \n\t$it") }
        onResponse =  { response, result -> println("Got the following result: $result \n\tand response: $response") }
    }

    val remoteService1 = DummyRemoteService( 5577)
    val remoteService2 = DummyRemoteService(7755)
    val remoteServices = listOf(remoteService1, remoteService2)

    beforeEachTest { remoteServices.forEach { it.start() } }

    afterEachTest { remoteServices.forEach { it.stop() } }

    describe("query") {
        val writer by memoized {  remoteService2.writers[0] }
        val otherWriter by memoized {  remoteService2.writers[1] }

        context("with a non-iterable parameter") {
            context("a query with a non-iterable argument") {
                val result by memoized {
                    remoteService1.client.query("writer") {
                        input("name", value = remoteService1.writers.first().name)
                        select("name")
                        onAssociation("publications") {
                            onFragment("Book") { select("title", "text") }
                            onFragment("Song") { select("title", "lyrics") }
                        }
                    }.resultAsMap()!!
                }

                it("successfully loads") {
                    result should satisfy { size == 2 }

                    result should beAMapOf(
                        "name" to writer.name,
                        "publications" to writer.publications.map { publication ->
                            mapOf(
                                "title" to publication.title,
                                when (publication) {
                                    is Publication.Book -> "text" to publication.text
                                    is Publication.Song -> "lyrics" to publication.lyrics
                                }
                            )
                        }
                    )
                }

                context("with multiple services") {
                    val otherResult by memoized {
                        remoteService2.client.query("writer") {
                            input("name", value = remoteService2.writers[1].name)
                            select("name")
                            onAssociation("publications") {
                                onFragment("Book") { select("title", "text") }
                                onFragment("Song") { select("title", "lyrics") }
                            }
                        }.resultAsMap()!!
                    }

                    it("successfully loads") {
                        result should beAMapOf(
                            "name" to writer.name,
                            "publications" to writer.publications.map { publication ->
                                mapOf(
                                    "title" to publication.title,
                                    when (publication) {
                                        is Publication.Book -> "text" to publication.text
                                        is Publication.Song -> "lyrics" to publication.lyrics
                                    }
                                )
                            }
                        )

                        otherResult should beAMapOf(
                            "name" to otherWriter.name,
                            "publications" to otherWriter.publications.map { publication ->
                                mapOf(
                                    "title" to publication.title,
                                    when (publication) {
                                        is Publication.Book -> "text" to publication.text
                                        is Publication.Song -> "lyrics" to publication.lyrics
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }

        context("a query with an iterable parameter") {
            val result by memoized {
                remoteService1.client.query("writers") {
                    input("names", value = remoteService1.writers.map { it.name })
                    select("name")
                    onAssociation("publications") {
                        onFragment("Book") { select("title", "text") }
                        onFragment("Song") { select("title", "lyrics") }
                    }
                }.resultAsListOfMaps()!!
            }

            it("successfully loads") {
                result should satisfy { size == 2 }

                result should containInOrder(
                    *remoteService1.writers.map { writer ->
                        mapOf(
                            "name" to writer.name,
                            "publications" to writer.publications.map { publication ->
                                mapOf(
                                    "title" to publication.title,
                                    when (publication) {
                                        is Publication.Book -> "text" to publication.text
                                        is Publication.Song -> "lyrics" to publication.lyrics
                                    }
                                )
                            }
                        )
                    }.toTypedArray()
                )
            }

            context("with multiple services") {
                val otherRsult by memoized {
                    remoteService2.client.query("writers") {
                        input("names", value = listOf(otherWriter.name))
                        select("name")
                        onAssociation("publications") {
                            onFragment("Book") { select("title", "text") }
                            onFragment("Song") { select("title", "lyrics") }
                        }
                    }.resultAsListOfMaps()!!
                }


                it("successfully loads") {
                    result should satisfy { size == 2 }
                    result should containInOrder(
                        mapOf(
                            "name" to writer.name,
                            "publications" to writer.publications.map { publication ->
                                mapOf(
                                    "title" to publication.title,
                                    when (publication) {
                                        is Publication.Book -> "text" to publication.text
                                        is Publication.Song -> "lyrics" to publication.lyrics
                                    }
                                )
                            }
                        )
                    )

                    otherRsult should satisfy { size == 1 }
                    otherRsult should containInOrder(
                        mapOf(
                            "name" to otherWriter.name,
                            "publications" to otherWriter.publications.map { publication ->
                                mapOf(
                                    "title" to publication.title,
                                    when (publication) {
                                        is Publication.Book -> "text" to publication.text
                                        is Publication.Song -> "lyrics" to publication.lyrics
                                    }
                                )
                            }
                        )
                    )
                }
            }
        }
    }

    describe("asType") {
        val result by memoized {
                remoteService1.client
                    .query("songs") { select("title", "lyrics") }
                    .resultAs<List<Publication.Song>>()!!
        }

        it("correctly parses the response") {
            result should satisfy { size == 1 }
            result should containInOrder(*remoteService1.writers[1].publications.toTypedArray())
        }
    }
})
