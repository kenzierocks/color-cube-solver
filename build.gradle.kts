import com.techshroom.inciseblue.commonLib
import net.minecrell.gradle.licenser.LicenseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    application
    kotlin("jvm") version "1.3.31"
    id("com.techshroom.incise-blue") version "0.3.14"
}

configurations.all {
    resolutionStrategy {
        // Refresh SNAPSHOTs often
        cacheChangingModulesFor(5, "minutes")
    }
}

inciseBlue {
    util {
        javaVersion = JavaVersion.VERSION_12
    }
    license()
    ide()
    lwjgl {
        // Until Gradle fixes their stuff
        lwjglVersion = "3.1.3"
        addDependency("")
        addDependency("glfw")
        addDependency("opengl")
        addDependency("openal")
        addDependency("nanovg")
    }
}

configure<LicenseExtension> {
    include("**/*.kt")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    commonLib("org.jetbrains.kotlinx", "kotlinx-coroutines", "1.2.1") {
        implementation(lib("core"))
        implementation(lib("jdk8"))
    }
    implementation(group = "com.techshroom.unplanned", name = "api", version = "0.0.1-SNAPSHOT")
    implementation(group = "com.techshroom.unplanned", name = "implementation", version = "0.0.1-SNAPSHOT")
    implementation(group = "org.slf4j", name = "slf4j-api", version = "1.7.26")
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")
    implementation(group = "ch.qos.logback", name = "logback-core", version = "1.2.3")

    implementation(group = "net.sf.jopt-simple", name = "jopt-simple", version = "5.0.4")

    testImplementation(group = "junit", name = "junit", version = "4.12")
}

tasks.withType<KotlinJvmCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xuse-experimental=kotlin.Experimental")
        jvmTarget = "1.8"
    }
}

application.mainClassName = "net.octyl.colorcube.solver.ColorCubeSolverRunner"
