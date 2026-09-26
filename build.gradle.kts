plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
}

detekt {
    autoCorrect = false // report only; CI must not rewrite sources
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/detekt.yml"))
}
