import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jfrog.gradle.plugin.artifactory.dsl.ResolverConfig
import groovy.lang.GroovyObject

plugins {
	kotlin("jvm") version "1.7.10"
	id("com.jfrog.artifactory") version "4.28.4"
	id("org.jetbrains.dokka") version "1.7.10"
	id("maven-publish")
	idea
}

group = "io.taff"
version = "0.5.7${ if (isReleaseBuild()) "" else "-SNAPSHOT" }"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
	maven("https://jitpack.io")
	maven {
		name = "JFrog"
		url = uri("https://tmpasipanodya.jfrog.io/artifactory/releases")
		credentials {
			username = System.getenv("ARTIFACTORY_USERNAME")
			password = System.getenv("ARTIFACTORY_PASSWORD")
		}
	}
}

dependencies {
	runtimeOnly("org.jetbrains.kotlin:kotlin-reflect")
	runtimeOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	api("com.github.kittinunf.fuel:fuel:2.3.1")
	api("com.github.kittinunf.fuel:fuel-coroutines:2.3.1")
	api("org.slf4j:slf4j-simple:1.7.36")
	api("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
	api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.3")
	testImplementation("io.taff:spek-expekt:0.7.5")
	testImplementation(enforcedPlatform("org.junit:junit-bom:5.9.0"))
	testImplementation("com.apurebase:kgraphql:0.17.15")
	testImplementation("io.javalin:javalin:4.6.4")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks {
	register<Jar>("dokkaJar") {
		from(dokkaHtml)
		dependsOn(dokkaHtml)
		archiveClassifier.set("javadoc")
	}

	register<Jar>("sourcesJar") {
		from(sourceSets.main.get().allSource)
		archiveClassifier.set("sources")
	}
}

tasks.withType<Test> { useJUnitPlatform() }

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			this.groupId = project.group.toString()
			this.artifactId = project.name
			this.version = project.version.toString()

			from(components["java"])
			versionMapping {
				usage("java-api") {
					fromResolutionOf("runtimeClasspath")
				}
			}

			artifact(tasks["dokkaJar"])
			artifact(tasks["sourcesJar"])

			pom {
				name.set(project.name)
				description.set("${project.name} $version - A Declarative graphql client for Kotlin")
				url.set("https://github.com/tpasipanodya/kgraphql-client")
				developers {
					developer {
						name.set("Tafadzwa Pasipanodya")
						email.set("tmpasipanodya@gmail.com")
					}
				}
				scm {
					connection.set("scm:git:git://github.com/tpasipanodya/kgraphql-client.git")
					developerConnection.set("scm:git:ssh://github.com/tpasipanodya/kgraphql-client.git")
					url.set("http://github.com/tpasipanodya/kgraphql-client/tree/main")
				}
			}

		}
	}
}


artifactory {
	setContextUrl("https://tmpasipanodya.jfrog.io/artifactory/")

	publish(delegateClosureOf<PublisherConfig> {

		repository(delegateClosureOf<GroovyObject> {
			setProperty("repoKey", if (isReleaseBuild()) "releases" else "snapshots")
			setProperty("username", System.getenv("ARTIFACTORY_USERNAME"))
			setProperty("password", System.getenv("ARTIFACTORY_PASSWORD"))
			setProperty("maven", true)
		})

		defaults(delegateClosureOf<GroovyObject> {
			invokeMethod("publications", "mavenJava")
		})
	})

	resolve(delegateClosureOf<ResolverConfig> {
		setProperty("repoKey", if (isReleaseBuild()) "releases" else "snapshots")
	})
}

fun isReleaseBuild() = System.getenv("IS_RELEASE_BUILD")?.toBoolean() == true
