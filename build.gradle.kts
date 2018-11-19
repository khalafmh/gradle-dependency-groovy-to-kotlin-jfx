import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.codehaus.groovy.tools.javac.JavacJavaCompiler
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.3.10"
val coroutinesVersion = "1.0.0"
val tornadofxVersion = "1.7.17"
val controlsfxVersion = "9.0.0"
val kotlintestVersion = "3.1.10"
val slf4jVersion = "1.7.25"

plugins {
    java
    kotlin("jvm") version "1.3.10"
    id("com.github.johnrengelman.shadow") version "4.0.2"
    id("no.tornado.fxlauncher") version "1.0.20"
}

group = "com.mahdialkhalaf"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    testImplementation(group = "junit", name = "junit", version = "4.12")

    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8", version = kotlinVersion)
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = kotlinVersion)

    implementation(group = "org.kodein.di", name = "kodein-di-generic-jvm", version = "5.3.0")

    implementation(group = "no.tornado", name = "tornadofx", version = tornadofxVersion)
    implementation(group = "no.tornado", name = "tornadofx-controlsfx", version = "0.1.1")
    implementation(group = "org.controlsfx", name = "controlsfx", version = controlsfxVersion)

    implementation(group = "io.reactivex.rxjava2", name = "rxkotlin", version = "2.2.0")
    implementation(group = "io.reactivex.rxjava2", name = "rxjava", version = "2.1.12")
    implementation(group = "com.github.thomasnield", name = "rxkotlinfx", version = "2.2.2")

    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = coroutinesVersion)
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-javafx", version = coroutinesVersion)
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-reactive", version = coroutinesVersion)

//    implementation(group = "com.opencsv", name = "opencsv", version = "4.0")
//    implementation(group = "org.apache.poi", name = "poi", version = "4.0.0")
//    implementation(group = "org.apache.poi", name = "poi-ooxml", version = "4.0.0")

    testImplementation(group = "io.kotlintest", name = "kotlintest-runner-junit5", version = kotlintestVersion)
    testImplementation(group = "org.slf4j", name = "slf4j-simple", version = slf4jVersion)
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    named<Wrapper>("wrapper") {
        gradleVersion = "5.0-rc-2"
        distributionType = Wrapper.DistributionType.ALL
    }

    jar {
        manifest.attributes(
            mapOf(
                "Main-Class" to "com.mahdialkhalaf.tools.AppKt"
            )
        )
    }

    shadowJar {
        mustRunAfter(embedApplicationManifest)
//        archiveName = "${project.name}.jar"
    }

    fxlauncher {
        val space = "%20"
        applicationVendor = "SomeVendor"
        applicationUrl = "file://host/some${space}directory/"
        applicationMainClass = "com.example.App"
        acceptDowngrade = true
        cacheDir = "USERLIB/$applicationVendor/${project.name}/"
    }

    val embedShadowJar by registering(Copy::class) {
        dependsOn(embedApplicationManifest, shadowJar)
        group = "fxlauncher"
        from("build/libs")
        into("build/fxlauncher")
        include(shadowJar.get().archiveName)
    }
    val deployLauncher by registering(Copy::class) {
        dependsOn(embedShadowJar)
        group = "fxlauncher"
        from("build/fxlauncher")
        into(fxlauncher.applicationUrl)
    }
}