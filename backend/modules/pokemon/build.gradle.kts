dependencies {
    implementation(project(":modules:shared"))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.graphql)
    implementation(libs.spring.boot.starter.cache)
    implementation(libs.caffeine)
    implementation(libs.spring.retry)
    implementation(libs.spring.aspects)
    runtimeOnly(libs.postgres.driver)

    testImplementation(libs.spring.boot.starter.test)
}
