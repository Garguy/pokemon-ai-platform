dependencies {
    implementation(project(":modules:shared"))
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.graphql)
    runtimeOnly(libs.postgres.driver)
}
