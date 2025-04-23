import gg.jte.ContentType
import java.nio.file.Paths

plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("gg.jte.gradle") version "3.1.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.javalin:javalin:6.6.0")
    implementation("io.javalin:javalin-rendering:6.6.0")
    implementation("gg.jte:jte:3.2.0")
    implementation("org.slf4j:slf4j-simple:2.0.7")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
}

tasks.compileJava {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()
    options.encoding = "UTF-8"
}

application {
    mainClass = "com.github.professorSam.soundboard.SoundBoard"
}

tasks.withType(JavaExec::class) {
    args = listOf("--dev")
}

tasks.shadowJar{
    dependsOn(tasks.precompileJte)
    from(jte.targetDirectory)
}

jte {
    sourceDirectory = Paths.get(project.projectDir.absolutePath, "src", "main", "jte")
    targetDirectory = Paths.get(project.projectDir.absolutePath, "build", "generated", "jte")
    contentType = ContentType.Html
    precompile()
}
