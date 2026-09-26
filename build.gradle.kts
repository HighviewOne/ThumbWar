plugins {
    id("com.android.application") version "9.4.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

detekt {
    autoCorrect = false // report only; CI must not rewrite sources
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/detekt.yml"))
    // The plugin is applied here at the root, whose default source dirs don't exist
    source.setFrom(files("app/src/main/java", "app/src/test/java"))
}

// detekt 1.23 is built against Kotlin 2.0.21 and refuses to run on the newer compiler the build
// uses; give its own classpath the version it expects (it only parses, so this is safe)
configurations.matching { it.name == "detekt" }.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") useVersion("2.0.21")
    }
}
