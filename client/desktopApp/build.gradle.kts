plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm("desktop")
    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.desktop.currentOs)
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.compose.material3)
                implementation(libs.compose.navigation)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.pokemonai.desktop.MainKt"
    }
}
