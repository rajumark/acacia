plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.publish)
    kotlin("plugin.serialization") version "2.3.0"
    signing
}

import org.gradle.plugin.compatibility.compatibility

group = "com.acacia"
version = libs.versions.sampleplugin.get()

gradlePlugin {
    website.set("https://github.com/rajumark/acacia")
    vcsUrl.set("https://github.com/rajumark/acacia")
    plugins {
        create("plugin") {
            id = "com.acacia"
            displayName = "Acacia"
            description = "AI-Native Compose DSL Plugin - Transform verbose Compose code into concise, AI-friendly DSL"
            tags = listOf("compose", "dsl", "ai", "android", "jetpack-compose", "code-generation")
            implementationClass = "com.acacia.ShortifyPlugin"
            
            compatibility {
                features {
                    configurationCache = true
                }
            }
        }
    }
}

dependencies {
    compileOnly(gradleKotlinDsl())
    implementation(libs.kotlin.gradle)
    implementation(libs.android.gradle)
    implementation("com.squareup:kotlinpoet:1.17.0")
    implementation("org.ow2.asm:asm:9.6")
    implementation("org.ow2.asm:asm-util:9.6")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}

kotlin {
    jvmToolchain(libs.versions.gradle.jvmToolchain.get().toInt())
}

java {
    withSourcesJar()
    withJavadocJar()
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

/*
// For GItHub Actions CI signing
if (providers.environmentVariable("CI").isPresent) {
    apply(plugin = "signing")
    extensions.configure<SigningExtension> {
        useInMemoryPgpKeys(
            providers.environmentVariable("SIGNING_PGP_KEY").orNull,
            providers.environmentVariable("SIGNING_PGP_PASSWORD").orNull
        )
    }
}
 */
