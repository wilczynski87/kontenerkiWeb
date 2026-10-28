@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(21)
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    jvm()

    wasmJs {
        browser {
            commonWebpackConfig {
                devServer = (devServer ?: org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.DevServer()).apply {
                    proxy = mutableListOf(
                        org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.DevServer.Proxy(
                            context = mutableListOf("/api"),
                            target = "http://localhost:8100",
                            changeOrigin = true,
                            pathRewrite = mutableMapOf("^/api" to ""),
                        ),
                    )
                }
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.startup)
            implementation(libs.ktor.client.android)
            implementation(libs.ktor.client.logging)
            implementation(libs.tink.android)
            implementation(libs.androidx.datastore)
            implementation(libs.datastore.preferences)
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.googleid)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material)
            implementation(libs.compose.ui)
            implementation(libs.compose.resources)
//            implementation(libs.compose.ui.tooling)
//            implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.10.0")
            implementation(libs.compose.material.icons.extended)

            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.datetime)
//            implementation(libs.kotlinx.coroutines.core)

            // Ktor dependencies
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)

            // Kotlinx Serialization
            implementation(libs.jetbrains.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
//            implementation(libs.kotlinx.coroutines.test)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
//            implementation(libs.compose.resources)
//            implementation(libs.kotlinx.coroutines.core)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.ktor.client.cio)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }

    sourceSets.named("commonMain") {
        kotlin.srcDir(layout.buildDirectory.dir("generated/serverUri"))
    }
}

val generateServerUri = tasks.register("generateServerUri") {
    val outputDir = layout.buildDirectory.dir("generated/serverUri")
    val serverUri = providers.provider { readServerUri(rootProject.projectDir) }
    inputs.property("serverUri", serverUri)
    outputs.dir(outputDir)
    doLast {
        val host = serverUri.get()
        val dir = outputDir.get().asFile.resolve("com/kontenery/config")
        dir.mkdirs()
        dir.resolve("RemoteApiUrl.kt").writeText(
            """
            package com.kontenery.config

            /** Host z SERVER_URI, wstawiony przy kompilacji. */
            internal const val REMOTE_API_URL = "http://$host:8100"
            """.trimIndent() + "\n",
        )
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    dependsOn(generateServerUri)
}

fun readServerUri(projectDir: java.io.File): String {
    System.getenv("SERVER_URI")?.trim()?.takeIf { it.isNotEmpty() }?.let { return validateServerUri(it) }
    val envFile = projectDir.resolve(".env")
    if (!envFile.isFile) {
        error("Missing SERVER_URI. Ustaw zmienną środowiskową albo wpisz SERVER_URI w ${envFile.path}")
    }
    val raw = envFile.readLines()
        .map { it.trim() }
        .firstOrNull { it.startsWith("SERVER_URI=") && !it.startsWith("#") }
        ?.substringAfter("=")
        ?.trim()
        ?.trim('"')
        ?.trim('\'')
        .orEmpty()
    if (raw.isEmpty()) {
        error("Missing SERVER_URI in ${envFile.path}")
    }
    return validateServerUri(raw)
}

fun validateServerUri(value: String): String {
    require(!value.contains("://") && !value.contains("/") && !value.contains(":")) {
        "SERVER_URI ma być samym hostem, bez schematu i portu"
    }
    return value
}

android {
    namespace = "com.kontenery"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.kontenery"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        val googleOAuthClientId = project.findProperty("GOOGLE_OAUTH_CLIENT_ID")?.toString().orEmpty()
        buildConfigField("String", "GOOGLE_OAUTH_CLIENT_ID", "\"$googleOAuthClientId\"")
    }
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
//    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.kontenery.MainKt"
        jvmArgs += "-Dkotlinx.coroutines.fast.service.loader=false"
    }
}
