plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.7.1"
}

group = "net.ahwater"
version = "0.0.5"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Configure IntelliJ Platform Gradle Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        create("IC", "2025.1.4.1")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        // Add necessary plugin dependencies for compilation here, example:
        // bundledPlugin("com.intellij.java")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "241"
        }

        changeNotes = """
            version: 0.0.5
            release: 2025-11-17
            1.Update the logo, and add a small logo to the contextMenu.
            2.Integrate pop-up boxes, consolidating all operations, including settings, into a single pop-up box.
            3.Supported commit body.
        """.trimIndent()

        description = """
            A plugin designed to provide teams with a unified Git commit convention, featuring built-in common types and scopes.
            It helps developers quickly generate standardized commit messages when submitting code and supports automatic synchronization to remote repositories.
        """.trimIndent()
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    withType<org.jetbrains.intellij.platform.gradle.tasks.BuildSearchableOptionsTask> {
        // 解决 Locale must be default 报错
        jvmArgs = listOf("-Duser.language=en", "-Duser.country=US")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
