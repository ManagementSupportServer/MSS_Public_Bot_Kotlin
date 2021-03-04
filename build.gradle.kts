import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.*

val projectMainClass: String by extra("jp.mss.MSS_Public_Bot_Kotlin.Main")
val projectVersion: String by extra("1.0.0")
val projectBuildDate: String by extra(SimpleDateFormat("yy.MMdd.HHmm").format(Date()))

val exposedVersion: String by extra("0.28.1")
val ktorVersion: String by extra("1.4.1")

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", "1.4.21"))
    }
}

plugins {
    java
    kotlin("jvm") version "1.4.21"
    groovy
    application
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "jp.mss"
version = projectVersion
application {
    // mainClass.set(projectMainClass)
    mainClassName = projectMainClass
}

repositories {
    mavenCentral()
    jcenter()

    maven("https://dl.bintray.com/sedmelluq/com.sedmelluq")
    maven("https://kotlin.bintray.com/ktor")
    maven("https://repository.aoichaan0513.jp")
    maven("https://maven.codelibs.org")
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    implementation(kotlin("script-util"))
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.4.2")

    // 必須ライブラリ
    implementation("net.dv8tion", "JDA", "4.2.0_229")

    // データ管理ライブラリ
    implementation("org.json", "json", "20201115")
    // implementation("com.google.code.gson", "gson", "2.8.6")
    // implementation("com.fasterxml.jackson.core", "jackson-core", "2.12.0")
    // implementation("org.dom4j", "dom4j", "2.1.3")
    implementation("com.opencsv", "opencsv", "5.3")

    // データベースライブラリ
    // implementation("mysql", "mysql-connector-java", "8.0.22")
    // implementation("org.jetbrains.exposed", "exposed-core", exposedVersion)
    // implementation("org.jetbrains.exposed", "exposed-dao", exposedVersion)
    // implementation("org.jetbrains.exposed", "exposed-jdbc", exposedVersion)
    // implementation("org.jetbrains.exposed", "exposed-jodatime", exposedVersion)

    // ログライブラリ
    implementation("ch.qos.logback", "logback-classic", "1.2.3")
    implementation("org.projectlombok", "lombok", "1.18.16")

    // ツールライブラリ
    implementation("org.apache.commons", "commons-lang3", "3.11")
    implementation("commons-cli", "commons-cli", "1.4")
    implementation("commons-logging", "commons-logging", "1.2")
    implementation("jp.aoichaan0513", "Kotlin_Utils", "1.1.9")
    implementation("jp.aoichaan0513", "JDA_Utils", "1.0.11")

    // その他
    implementation("com.squareup.okhttp3", "okhttp", "4.9.0")
    implementation("com.github.kevinsawicki", "http-request", "6.0")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()

val processResources: ProcessResources by tasks
processResources.from(sourceSets.main.get().resources.srcDirs) {
    filter {
        it.replace(
            mapOf(
                "%PROJECT_VERSION%" to projectVersion,
                "%PROJECT_BUILD_DATE%" to projectBuildDate
            )
        )
    }
}

val jar: Jar by tasks
jar.apply {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to projectMainClass,
                "Class-Path" to "."
            )
        )
    }

    jar.from(
        configurations.compile.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    )
}


fun String.replace(map: Map<String, Any>): String {
    var str = this

    for ((key, value) in map)
        str = str.replace(key, value.toString())

    return str
}