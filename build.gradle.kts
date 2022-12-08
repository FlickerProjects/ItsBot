plugins {
    val kotlinVersion = "1.7.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.13.2"
}

group = "io.github.itsflicker.itsbot"
version = "0.2.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.aliyun.com/repository/public")
//    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation("org.jetbrains.skiko:skiko-awt-runtime-windows-x64:0.7.37")
//    implementation("org.jetbrains.skiko:skiko-awt-runtime-linux-x64:0.7.20")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.2")
    implementation("com.github.Sunshine-wzy:rkon-core:1.2.2")
    implementation("top.e404:skiko-util:1.0.0") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.jetbrains.skiko")
    }

    shadowLink("com.github.Sunshine-wzy:rkon-core")
    shadowLink("top.e404:skiko-util")
}

mirai {
    jvmTarget = JavaVersion.VERSION_11
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