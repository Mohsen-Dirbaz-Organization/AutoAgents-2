import org.jetbrains.intellij.tasks.BuildSearchableOptionsTask

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.IDE.plugin"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.mockito:mockito-core:5.5.0")
}

// Configure IntelliJ Platform Plugin
intellij {
    version.set("2023.3.2")
    type.set("IC")
    downloadSources.set(true)
    plugins.set(listOf("java"))
    updateSinceUntilBuild.set(false)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        sinceBuild.set("223")
        untilBuild.set("241.*")

        changeNotes.set("""
            <h3>1.0.0</h3>
            <ul>
                <li>Initial release of AutoAgents plugin</li>
                <li>Multi-agent AI code generation</li>
                <li>Trust verification system</li>
                <li>Memory state management</li>
                <li>Administrative history tracking</li>
            </ul>
        """.trimIndent())
    }

    runIde {
        jvmArgs = listOf("-Xmx2048m", "-XX:+UseG1GC", "-XX:SoftRefLRUPolicyMSPerMB=50")
    }

    withType<BuildSearchableOptionsTask> {
        enabled = false
    }
}

tasks.test {
    useJUnitPlatform()
}
