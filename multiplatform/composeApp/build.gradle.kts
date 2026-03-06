import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("app.cash.sqldelight")
}

val secretsFile = rootProject.file("../secrets.properties")
val secrets = Properties()
if (secretsFile.exists()) {
    secrets.load(FileInputStream(secretsFile))
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { target ->
        target.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(compose.components.resources)

                // Navigation
                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha10")

                // Lifecycle / ViewModel
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.8.4")

                // Supabase
                api(project.dependencies.platform("io.github.jan-tennert.supabase:bom:3.2.2"))
                implementation("io.github.jan-tennert.supabase:postgrest-kt")
                implementation("io.github.jan-tennert.supabase:auth-kt")
                implementation("io.github.jan-tennert.supabase:realtime-kt")
                implementation("io.github.jan-tennert.supabase:storage-kt")

                // Ktor
                implementation("io.ktor:ktor-client-core:3.2.2")

                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

                // DateTime
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

                // SQLDelight
                implementation("app.cash.sqldelight:runtime:2.0.2")
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")

                // Image loading
                implementation("io.coil-kt.coil3:coil-compose:3.0.4")
                implementation("io.coil-kt.coil3:coil-network-ktor3:3.0.4")

                // Multiplatform Settings
                implementation("com.russhwolf:multiplatform-settings:1.2.0")
                implementation("com.russhwolf:multiplatform-settings-no-arg:1.2.0")

                // Reorderable (drag-and-drop)
                implementation("sh.calvin.reorderable:reorderable:2.4.3")
            }
        }

        val androidMain by getting {
            dependencies {
                // Ktor engine
                implementation("io.ktor:ktor-client-okhttp:3.2.2")

                // SQLDelight driver
                implementation("app.cash.sqldelight:android-driver:2.0.2")

                // Android
                implementation("androidx.activity:activity-compose:1.9.3")
                implementation("androidx.core:core-ktx:1.15.0")

                // QR Code (ZXing)
                implementation("com.google.zxing:core:3.5.3")

                // Google Auth
                implementation("androidx.credentials:credentials:1.3.0")
                implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
                implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

                // Glance (widget)
                implementation("androidx.glance:glance:1.1.1")
                implementation("androidx.glance:glance-appwidget:1.1.1")
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                // Ktor engine
                implementation("io.ktor:ktor-client-darwin:3.2.2")

                // SQLDelight driver
                implementation("app.cash.sqldelight:native-driver:2.0.2")
            }
        }
    }
}

android {
    namespace = "com.kryptoxotis.nexus"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kryptoxotis.nexus.cmp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        val supabaseUrl = secrets.getProperty("SUPABASE_URL")?.takeIf { it.isNotBlank() } ?: "REDACTED_SUPABASE_URL"
        val supabaseAnonKey = secrets.getProperty("SUPABASE_ANON_KEY")?.takeIf { it.isNotBlank() } ?: "REDACTED_SUPABASE_ANON_KEY"
        val googleWebClientId = secrets.getProperty("GOOGLE_WEB_CLIENT_ID")?.takeIf { it.isNotBlank() } ?: "REDACTED_GOOGLE_CLIENT_ID"

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Generate IosConfig.kt with secrets for iOS (like Android BuildConfig)
val generateIosConfig by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/iosConfig/com/kryptoxotis/nexus")
    outputs.dir(outputDir)
    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()
        val supabaseUrl = secrets.getProperty("SUPABASE_URL")?.takeIf { it.isNotBlank() } ?: ""
        val supabaseAnonKey = secrets.getProperty("SUPABASE_ANON_KEY")?.takeIf { it.isNotBlank() } ?: ""
        val googleWebClientId = secrets.getProperty("GOOGLE_WEB_CLIENT_ID")?.takeIf { it.isNotBlank() } ?: ""
        dir.resolve("IosConfig.kt").writeText("""
            package com.kryptoxotis.nexus

            object IosConfig {
                const val SUPABASE_URL = "$supabaseUrl"
                const val SUPABASE_ANON_KEY = "$supabaseAnonKey"
                const val GOOGLE_WEB_CLIENT_ID = "$googleWebClientId"
            }
        """.trimIndent())
    }
}

kotlin.sourceSets.getByName("iosMain") {
    kotlin.srcDir(generateIosConfig.map { layout.buildDirectory.dir("generated/iosConfig") })
}

sqldelight {
    databases {
        create("NexusDatabase") {
            packageName.set("com.kryptoxotis.nexus.data.local")
        }
    }
}
