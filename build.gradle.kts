import xyz.jpenilla.runtask.task.AbstractRun

plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.gradleup.shadow") version "9.0.0-rc1"
}

version = "${rootProject.property("plugin_version")}-mc${rootProject.property("minecraft_version")}"
group = rootProject.property("maven_group").toString()

base.archivesName.set(rootProject.property("archives_base_name").toString())

repositories {
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "PaperMC-Repo"
    }

    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "Sonatype"
    }

    maven("https://repo.onarandombox.com/content/groups/public/") {
        name = "onarandombox"
    }

    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("${rootProject.property("minecraft_version")}-${rootProject.property("paper_version")}")

    compileOnly("org.mvplugins.multiverse.core:multiverse-core:${rootProject.property("multiverse_core_version")}")
    compileOnly("org.mvplugins.multiverse.netherportals:multiverse-netherportals:${rootProject.property("multiverse_netherportals_version")}")
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    processResources {
        val propertiesToExpand = mapOf(
            "name" to rootProject.property("plugin_name"),
            "description" to rootProject.property("plugin_description"),
            "version" to rootProject.property("plugin_version"),
            "minecraft_version" to rootProject.property("minecraft_version"),
            "author" to rootProject.property("plugin_author"),
            "entrypoint" to rootProject.property("plugin_entrypoint")
        )

        inputs.properties(propertiesToExpand)

        filesMatching("plugin.yml") {
            expand(propertiesToExpand)
        }
    }

    runServer {
        minecraftVersion(rootProject.property("minecraft_version").toString())
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = 21
}

tasks.withType(AbstractRun::class) {
    javaLauncher = javaToolchains.launcherFor {
        vendor = JvmVendorSpec.AZUL // Change this to be whichever JVM vendor you use if you need to.
        languageVersion = JavaLanguageVersion.of(21)
    }
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
