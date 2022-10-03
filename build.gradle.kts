plugins {
    val kotlinVersion = "1.7.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.12.0"
}

group = "io.github.itsflicker.itsbot"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.aliyun.com/repository/public")
}

dependencies {
    implementation("com.github.ben-manes.caffeine:caffeine:2.9.3")
    implementation("com.github.Sunshine-wzy:rkon-core:1.2.2")
//    implementation("io.coil-kt:coil:2.2.1")

    shadowLink("com.github.Sunshine-wzy:rkon-core")
}

val ktorVersion = "1.6.7"
fun org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler.ktorApi(id: String, version: String = ktorVersion) {
    api("io.ktor:ktor-$id:$version") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(module = "slf4j-api")
    }
}

kotlin {
    sourceSets["test"].apply {
        dependencies {
            ktorApi("server-test-host")
        }
    }

    sourceSets.all {
        dependencies {
            ktorApi("client-okhttp")
            ktorApi("server-cio")
            ktorApi("http-jvm")
            ktorApi("websockets")
            ktorApi("client-websockets")
            ktorApi("server-core")
            ktorApi("http")
        }
    }
}
