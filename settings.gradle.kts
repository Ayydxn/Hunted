pluginManagement {
    repositories {
        maven("https://repo.papermc.io/repository/maven-public/") {
            name = "PaperMC-Repo"
        }

        gradlePluginPortal()
    }
}

rootProject.name = "Paper Plugin Template"
