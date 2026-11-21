# Roost Kotlin

The cryptographic core for private, end-to-end encrypted messaging in Kotlin apps.

## Usage

GitHub Packages requires a GitHub account for every download. Add your username and a classic personal access token with the `read:packages` scope to `~/.gradle/gradle.properties`:

```properties
GitHubPackagesUsername=USERNAME
GitHubPackagesPassword=TOKEN
```

Add the repository to `dependencyResolutionManagement` in your `settings.gradle.kts`:

```kotlin
repositories {
    maven("https://maven.pkg.github.com/khaledchehabeddine/roost-kotlin") {
        name = "GitHubPackages"
        credentials(PasswordCredentials::class)
    }
}
```

Add the dependency to your module's `build.gradle.kts`:

```kotlin
implementation("com.chirp:roost:X.Y.Z")
```
