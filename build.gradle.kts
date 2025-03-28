plugins {
    alias(libs.plugins.androidApplication) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    dependencies {
        // ✅ Correct way to add classpath in Kotlin DSL
        classpath("com.google.gms:google-services:4.4.2")
    }
}
