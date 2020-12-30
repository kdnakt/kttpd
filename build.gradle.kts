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

task("e2e_default_port") {
    doLast {
        exec {
            executable("./run_test_default_port.sh")
        }
    }
}

task("e2e_log_level") {
    doLast {
        exec {
            executable("./run_test_log_level.sh")
        }
    }
}

task("e2e_all") {
    doLast {
        exec {
            executable("./run_test.sh")
        }
    }
}

task("e2etest") {
    dependsOn("e2e_default_port",
            "e2e_log_level",
            "e2e_all")
}

tasks["build"].finalizedBy("e2etest")
tasks["nativeTestBinaries"].finalizedBy("e2etest")

tasks.withType<Wrapper> {
    gradleVersion = "6.7.1"
    distributionType = Wrapper.DistributionType.BIN
}