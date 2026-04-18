plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.publish)
    signing
}

group = "com.acacia"
version = libs.versions.sampleplugin.get()

gradlePlugin {
    website = "https://github.com/example/example"
    vcsUrl = "https://github.com/example/example"
    plugins {
        create("plugin") {
            id = "com.acacia"
            displayName = "Acacia"
            description = "AI-Native Compose DSL Plugin"
            tags = listOf("compose", "dsl", "ai")
            implementationClass = "com.acacia.ShortifyPlugin"
        }
    }
}

dependencies {
    compileOnly(gradleKotlinDsl())
    implementation(libs.kotlin.gradle)
    implementation(libs.android.gradle)
}

kotlin {
    jvmToolchain(libs.versions.gradle.jvmToolchain.get().toInt())
}

java {
    withSourcesJar()
    withJavadocJar()
}

/*
signing {
    useInMemoryPgpKeys(
        providers.gradleProperty("signingKey").orNull,
        providers.gradleProperty("signingPassword").orNull
    )
}
*/

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
