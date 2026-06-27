plugins {
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(project(":modules:shared"))
    implementation(project(":modules:identity"))
    implementation(project(":modules:pokemon"))
    implementation(project(":modules:questionnaire"))
    implementation(project(":modules:recommendation"))
    implementation(project(":modules:ai"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.graphql)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.cache)
    implementation(libs.caffeine)
    implementation(libs.flyway.starter)
    implementation(libs.flyway.postgres)
    runtimeOnly(libs.postgres.driver)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.spring.graphql.test)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
}
