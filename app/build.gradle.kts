plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.protobuf)
}

android {
    namespace = "com.vocallock"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.vocallock"
        minSdk = 33
        targetSdk = 36
        versionCode = (System.getenv("VERSION_CODE") ?: "1").toInt()
        versionName = System.getenv("VERSION_NAME") ?: "1.0.0-dev"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("Boolean", "ENABLE_LOGGING", "false")
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            buildConfigField("Boolean", "ENABLE_LOGGING", "true")
        }
    }

    // ── Kotlin / Java compatibility ──────────────────────────
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        // Opt-in to coroutine experimental APIs used by DataStore + Flow
        freeCompilerArgs += listOf(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
        )
    }

    // ── Compose ──────────────────────────────────────────────
    buildFeatures {
        compose = true
        buildConfig = true
    }

    // ── Packaging ────────────────────────────────────────────
    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/DEPENDENCIES",
            )
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }

    // ── KSP source sets ──────────────────────────────────────
    // Room generates code into these dirs; they must be on the source path
    applicationVariants.all {
        val variantName = name
        sourceSets {
            getByName(variantName) {
                kotlin.srcDir("build/generated/ksp/$variantName/kotlin")
            }
        }
    }
}

// ── Proto DataStore configuration ────────────────────────────
// Generates Kotlin data classes from .proto files in src/main/proto/
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.get()}"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    // ── Compose BOM ──────────────────────────────────────────
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // ── Core ─────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.startup)
    implementation(libs.bundles.lifecycle)
    implementation(libs.google.material)

    // ── Compose ──────────────────────────────────────────────
    implementation(libs.bundles.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // ── Room — KSP────────────────────────────────────────────
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    // ── Proto DataStore ──────────────────────────────────────
    implementation(libs.bundles.datastore)

    // ── Koin ─────────────────────────────────────────────────
    implementation(libs.bundles.koin)
    implementation(libs.koin.annotations)
    ksp(libs.koin.ksp.compiler)

    // ── Coroutines ───────────────────────────────────────────
    implementation(libs.bundles.coroutines)

    // ── Serialization ────────────────────────────────────────
    implementation(libs.kotlinx.serialization.json)

    // ── Unit tests ───────────────────────────────────────────
    testImplementation(libs.bundles.testing.unit)

    // ── Instrumented tests ───────────────────────────────────
    androidTestImplementation(libs.bundles.testing.android)
}
