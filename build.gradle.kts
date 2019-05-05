import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.11"
    application
    id("com.github.johnrengelman.shadow") version "2.0.4"
}

group = "com.piotrwilczek"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://kotlin.bintray.com/kotlinx")
}

dependencies {
    compile(kotlin("stdlib-jdk8"))

    val ktorVersion = "1.1.3"
    fun ktor(s: String = "", v: String = ktorVersion) = "io.ktor:ktor$s:$v"
    compile(ktor("-server-core"))
    compile(ktor("-server-netty"))
    compile(ktor("-auth-jwt"))
    compile(ktor("-gson"))
    compile(ktor("-client-apache"))
    compile(ktor("-client-gson"))

    compile("com.auth0:java-jwt:3.4.1")
    compile("org.eclipse.jgit:org.eclipse.jgit:5.2.1.201812262042-r")
    compile("org.apache.tika:tika-core:1.19.1")
    compile("ch.qos.logback:logback-classic:1.2.3")
    compile("io.github.cdimascio:java-dotenv:3.1.7")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    applicationName = "newliner"
    group = "com.piotrwilczek"
    mainClassName = "io.ktor.server.netty.EngineMain"
}

tasks.withType<ShadowJar> {
    baseName = project.name
    classifier = ""
    version = ""
}
