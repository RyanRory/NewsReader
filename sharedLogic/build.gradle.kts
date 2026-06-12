import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

val ktorVersion = "2.3.12"
val koinVersion = "3.5.6"
val sqldelightVersion = "2.3.2"

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SharedLibrary"
            isStatic = true
        }
    }
    
    android {
       namespace = "com.uptick.newsreader.sharedLibrary"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        commonMain.dependencies {
            // Ktor
            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            implementation("io.ktor:ktor-client-logging:$ktorVersion")

            // Koin
            implementation("io.insert-koin:koin-core:$koinVersion")

            // Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

            // SQLDelight
            implementation("app.cash.sqldelight:runtime:$sqldelightVersion")
            implementation("app.cash.sqldelight:coroutines-extensions:$sqldelightVersion")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
            implementation("io.insert-koin:koin-test:4.2.0")
        }

        androidMain.dependencies {
            implementation("app.cash.sqldelight:android-driver:$sqldelightVersion")
            implementation("app.cash.sqldelight:sqlite-driver:${sqldelightVersion}")
        }

        iosMain.dependencies {
            implementation("app.cash.sqldelight:native-driver:$sqldelightVersion")
            implementation("io.ktor:ktor-client-darwin:${ktorVersion}")
        }
    }
}

sqldelight {
    databases {
        create("NewsDatabase") {
            packageName.set("com.uptick.newsreader.db")
        }
    }
}