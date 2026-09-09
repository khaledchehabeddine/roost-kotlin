import com.android.build.api.dsl.ManagedVirtualDevice

plugins {
    // AGP 9.0+ provides built-in Kotlin support; applying org.jetbrains.kotlin.android
    // is now an error (see https://kotl.in/gradle/agp-built-in-kotlin).
    id("com.android.application")
}

val roostVersion = providers.gradleProperty("roostVersion").get()

android {
    namespace = "io.github.relaynet.roost.consumer"
    compileSdk = 36

    // The smoke test uses the legacy android.test instrumentation framework, which moved out of
    // android.jar into optional libraries; pull them onto the compile classpath.
    useLibrary("android.test.runner")
    useLibrary("android.test.base")

    defaultConfig {
        applicationId = "io.github.relaynet.roost.consumer"
        minSdk = 36
        targetSdk = 36
        versionCode = 1
        versionName = roostVersion
        testInstrumentationRunner = "android.test.InstrumentationTestRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        managedDevices {
            // AGP 9: `localDevices` is monomorphic; the polymorphic typed create moved to `allDevices`.
            allDevices {
                create<ManagedVirtualDevice>("pixel6Api36") {
                    device = "Pixel 6"
                    apiLevel = 36
                    systemImageSource = "aosp-atd"
                }
            }
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("io.github.relaynet:roost:$roostVersion")
}
