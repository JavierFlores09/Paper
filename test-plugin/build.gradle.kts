version = "1.0.0-SNAPSHOT"

dependencies {
    compileOnly(project(":paper-api"))
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("--enable-preview")
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "apiversion" to "\"${rootProject.providers.gradleProperty("apiVersion").get()}\"",
    )
    inputs.properties(props)
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}
