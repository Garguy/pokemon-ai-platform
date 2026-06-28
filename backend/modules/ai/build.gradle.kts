dependencies {
    implementation(project(":modules:shared"))
    implementation(project(":modules:pokemon"))
    implementation(project(":modules:recommendation"))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.ai.google.genai.starter)

    testImplementation(libs.spring.boot.starter.test)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.register<Test>("testAiIntegration") {
    description = "Runs real Gemini API tests (requires GEMINI_API_KEY)"
    group = "verification"
    useJUnitPlatform {
        includeTags("integration-ai")
    }
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    outputs.upToDateWhen { false }
}
