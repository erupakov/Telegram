// Standalone JVM build: no Android SDK needed. Run with `gradle -p divo-api-tests test`.
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "divo-api-tests"
