tasks.jar {
    enabled = true
}

tasks.bootJar {
    enabled = false
}

dependencies {
    implementation(projects.modules.domain)
    // Only need Spring annotations (@Service) for this module
    implementation("org.springframework:spring-context")
    // mysql
    runtimeOnly("mysql:mysql-connector-java:8.0.33")
    testRuntimeOnly("mysql:mysql-connector-java:8.0.33")
}
