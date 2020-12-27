plugins {
    kotlin("multiplatform") version "1.4.20"
}

repositories {
    mavenCentral()
    // For kotlinx-datetime
    maven("https://kotlin.bintray.com/kotlinx/")
}

kotlin {
    macosX64("native") {
        // For kotlinx-cli
        compilations["main"].enableEndorsedLibs = true
        binaries {
            executable {
                entryPoint = "com.kdnakt.kttpd.main"
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.1.1")
            }
        }
    }
}

task("e2etest") {
    doLast {
        exec {
            commandLine("/bin/bash", "./run_test.sh")
        }
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "6.7.1"
    distributionType = Wrapper.DistributionType.BIN
}