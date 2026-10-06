plugins {
    kotlin("jvm") version "2.0.21"
}

// Compiles the Divo network layer straight from the app sources: DTOs, Retrofit services, mappers
// and the HTTP client. A few Android/Telegram classes they touch are stubbed in src/stubs.
val divoSrc = file("../TMessagesProj/src/main/java/org/telegram/divo")

sourceSets {
    main {
        kotlin {
            srcDir("src/stubs/kotlin")
            srcDir(divoSrc)
            include(
                "android/**",
                "org/telegram/messenger/**",
                "org/telegram/divo/**",
                "dal/dto/**",
                "dal/api/**",
                "dal/network/DivoResult.kt",
                "dal/network/DivoApiClient.kt",
                "dal/network/DivoApiConfig.kt",
                "dal/network/DivoLinkErrors.kt",
                "dal/utils/AccessTokenProvider.kt",
                "dal/repository/AuthRepository.kt",
                "entity/**",
                "common/arch/OffsetPaginator.kt",
                "common/utils/AdditionalInfoKeys.kt",
            )
            // Built from the event-creation screen state; not part of the HTTP contract under test
            exclude("dal/dto/event/CreateEventRequest.kt", "dal/api/EventService.kt")
        }
    }
}

// Bytecode for Java 17 so it runs on CI's JDK 17 as well as newer local JDKs
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
