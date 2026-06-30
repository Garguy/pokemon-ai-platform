plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.library)
    alias(libs.plugins.apollo)
}

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
    }
    iosArm64 {
        binaries.framework { baseName = "shared"; isStatic = true }
    }
    iosSimulatorArm64 {
        binaries.framework { baseName = "shared"; isStatic = true }
    }
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(libs.apollo.runtime)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.compose.navigation)
            implementation(libs.coil.compose)
            implementation(libs.coil.network)
            implementation(libs.composables.ui)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.apollo.testing.support)
            implementation(libs.koin.test)
        }
        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.security.crypto)
            // OkHttp engine needed by Apollo and Coil on Android
            implementation(libs.okhttp)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.coil.network.okhttp)
        }
        val iosMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
        val desktopMain by getting {
            dependencies {
                // java.util.prefs available on JVM — no extra dep needed
            }
        }
    }
}

android {
    namespace = "com.pokemonai.client.shared"
    compileSdk = 37
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.pokemonai.client.shared.resources"
}

apollo {
    service("pokemonai") {
        packageName.set("com.pokemonai.client.graphql")
        schemaFile.set(file("src/commonMain/graphql/schema.graphqls"))
        srcDir("src/commonMain/graphql")
        generateKotlinModels.set(true)
    }
}
