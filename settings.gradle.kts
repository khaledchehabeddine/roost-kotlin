pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        exclusiveContent {
            forRepository { maven { url = uri(providers.gradleProperty("roostMavenRepo").get()) } }
            filter { includeGroup("io.github.relaynet") }
        }
        google()
        mavenCentral()
    }
}
rootProject.name = "roost-kotlin-consumer"
include(":consumer")
