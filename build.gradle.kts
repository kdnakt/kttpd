plugins {
    kotlin("multiplatform") version "1.4.20"
}

repositories {
    mavenCentral()
}

kotlin {
    macosX64("native") {
        binaries {
            executable {
                entryPoint = "com.kdnakt.kttpd.main"
            }
        }
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "6.7.1"
    distributionType = Wrapper.DistributionType.BIN
}