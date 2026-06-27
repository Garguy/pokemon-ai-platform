rootProject.name = "pokemon-ai-platform"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(
    ":app",
    ":modules:shared",
    ":modules:identity",
    ":modules:pokemon",
    ":modules:questionnaire",
    ":modules:recommendation",
    ":modules:ai"
)
