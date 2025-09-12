plugins {
    id("com.android.library")
    id("kotlin-android")
    id("jacoco")
}

android {
    namespace = "com.otaliastudios.cameraview"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        targetSdk = 34
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["filter"] = "" +
                "com.otaliastudios.cameraview.tools.SdkExcludeFilter," +
                "com.otaliastudios.cameraview.tools.SdkIncludeFilter"
    }

    buildTypes {
        getByName("debug") {
            isTestCoverageEnabled = true
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.1")
    testImplementation("org.mockito:mockito-inline:2.28.2")

    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("org.mockito:mockito-android:2.28.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    api("androidx.exifinterface:exifinterface:1.3.3")
    api("androidx.lifecycle:lifecycle-common:2.3.1")
    api("com.google.android.gms:play-services-tasks:17.2.1")
    implementation("androidx.annotation:annotation:1.2.0")
    implementation("com.otaliastudios.opengl:egloo:0.6.1")
}

// Code Coverage
val buildDirPath = project.buildDir.absolutePath
val coverageInputDir = "$buildDirPath/coverage_input"
val coverageOutputDir = "$buildDirPath/coverage_output"

// Run unit tests, with coverage enabled in the android { } configuration.
tasks.register("runUnitTests") {
    dependsOn("testDebugUnitTest")
    doLast {
        copy {
            from("$buildDirPath/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
            into("$coverageInputDir/unit_tests")
        }
    }
}

// Run android tests with coverage.
tasks.register("runAndroidTests") {
    dependsOn("connectedDebugAndroidTest")
    doLast {
        copy {
            from("$buildDirPath/outputs/code_coverage/debugAndroidTest/connected")
            include("*coverage.ec")
            into("$coverageInputDir/android_tests")
        }
    }
}

// Merge the two with a jacoco task.
jacoco { toolVersion = "0.8.5" }
tasks.register("computeCoverage", JacocoReport::class) {
    dependsOn("compileDebugSources")
    executionData.from(fileTree(coverageInputDir))
    sourceDirectories.from(android.sourceSets["main"].java.srcDirs)
    additionalSourceDirs.from("$buildDirPath/generated/source/buildConfig/debug")
    additionalSourceDirs.from("$buildDirPath/generated/source/r/debug")
    classDirectories.from(fileTree("$buildDirPath/intermediates/javac/debug") {
        exclude(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "android/**",
            "androidx/**",
            "com/google/**",
            "**/*\$ViewInjector*.*",
            "**/Dagger*Component.class",
            "**/Dagger*Component\$Builder.class",
            "**/*Module_*Factory.class",
            // We don’t test OpenGL filters.
            "**/com/otaliastudios/cameraview/filters/**.*"
        )
    })
    reports.html.required.set(true)
    reports.xml.required.set(true)
    reports.html.outputLocation.set(file("$coverageOutputDir/html"))
    reports.xml.outputLocation.set(file("$coverageOutputDir/xml/report.xml"))
}
