plugins {
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    java
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    group = "com.pokemonai"
    version = "0.1.0-SNAPSHOT"

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:4.1.0")
            mavenBom("org.testcontainers:testcontainers-bom:2.0.5")
        }
    }

    dependencies {
        // Force Gradle to use the BOM-managed junit-platform-launcher (6.x) instead of
        // its own bundled 1.8.2, which is incompatible with junit-platform-engine 6.x.
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform {
            if (name != "testAiIntegration") {
                excludeTags("integration-ai")
            }
        }
    }
}
