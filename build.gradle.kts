import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.10")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.10")
    implementation("ch.qos.logback:logback-classic:1.2.9")
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.4")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.4")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.4")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("com.sun.mail:javax.mail:1.6.2")
}

sourceSets {
    main {
        kotlin.srcDirs("src/main/kotlin")
    }
    test {
        kotlin.srcDirs("src/test/kotlin")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.register<JavaExec>("run") {
    main = "EmailReplyBot"
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.withType<ShadowJar> {
    archiveFileName.set("bot.jar")
    destinationDirectory.set(project.projectDir)
    manifest {
        attributes["Main-Class"] = "EmailReplyBot"
    }
}

tasks.register("bot") {
    dependsOn("shadowJar")
}
