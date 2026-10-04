# roost-kotlin

Private Maven distribution and native package checks for Roost. Rust declarations in
`RelayNet/roost` own the generated API. No generated Kotlin implementation source is committed here.

## Consume a release

Use `io.github.relaynet:roost:<exact-version>` from
`https://maven.pkg.github.com/relaynet/roost-kotlin`. The AAR includes package
`io.github.relaynet.roost` and native libraries for arm64-v8a, armeabi-v7a and x86_64.
Its generated POM supplies JNA. The current minimum Android API is 36.

```kotlin
// In settings.gradle.kts repositories:
exclusiveContent {
    forRepository {
        maven {
            url = uri("https://maven.pkg.github.com/relaynet/roost-kotlin")
            credentials {
                username = providers.gradleProperty("roostGithubUser").get()
                password = providers.environmentVariable("ROOST_PACKAGES_TOKEN").get()
            }
        }
    }
    filter { includeModule("io.github.relaynet", "roost") }
}
// In the consuming module, after the version has been published:
implementation("io.github.relaynet:roost:0.16.0")
```

GitHub Packages downloads require authentication. Use read-only package access, exact versions and
Gradle strict dependency verification. Commit reviewed AAR/POM SHA-256 pins and the approved signer
fingerprint `8C936E5EE86B909B53B607EA0EEDE342BC541851`. Download the AAR, POM, signatures, checksums
and public key from the matching release; verify each artifact with Roost's
`scripts/release/verify-artifact.sh`, supplying its four paths and independently reviewed checksum.
Review upgrades instead of refreshing verification metadata blindly. Do not allow a local Maven
repository or the development coordinate in consumer CI or release builds.

`io.github.relaynet:roost-development:<same-version>` remains an on-demand local build for peer
simulations. Both variants share the Kotlin package and cannot be selected together. Production
excludes development certificate issuance. Maven Central setup is not required. Administrators
can delete package versions for the MVP; replacing released bytes remains forbidden.

## Check a candidate

Run `./check.sh <roost-checkout>` or dispatch **Package consumption** on `main` with a full source
SHA. The workflow requires owner initiation, including reruns, and `ROOST_READ_TOKEN` with Contents
read access to Roost. It builds once, checks every shipped ABI and production classes, and exercises
the resulting AAR/POM on one API 36 emulator. The tests cover loading, foreign storage errors,
lost-write recovery and binary Transport Binding conversion. Swift owns the independent CryptoKit
check; Kotlin adds no signature provider. Rust owns protocol scenarios.

The workflow uploads the exact AAR/POM and tool-version notes for 30 days. Run it before publication
and after changes to bindings, packaging, release commands, toolchains, ABI lists, package layout
or consumer repositories. Both platforms must select the same Roost revision and workspace version.
`ROOST_ANDROID_TEST_TASK=:consumer:connectedDebugAndroidTest` selects an already-running emulator.

For manual installation of a signed candidate, first use Roost's `mobile.py fetch` with its source,
workflow attempts and reviewed pins. Keep those environment variables set, then run:

```bash
./check.sh <roost-checkout> <absolute-signed-candidate-directory>
```

This verifies signatures and pins and runs the native calls without rebuilding. Roost's
[publication instructions](https://github.com/RelayNet/roost/blob/main/docs/mobile-publication.md)
cover signing, review, immutable upload and partial retries. These checks do not publish anything.
Published-package resolution and complete Relay validation remain in RelayNet/roost#268.
