#!/usr/bin/env bash
# Build once, or check an existing signed candidate without rebuilding it.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
SOURCE="$(cd "${1:?usage: check.sh <roost-source> [signed-candidate-directory]}" && pwd -P)"
# The consumer checks the production coordinate only.
unset ROOST_CARGO_FEATURES
VERSION="$(awk -F '"' '/^version = / { print $2; exit }' "$SOURCE/Cargo.toml")"
if [[ $# == 2 ]]; then
    CANDIDATE="$(cd "$2" && pwd -P)"
    python3 "$SOURCE/scripts/release/mobile.py" verify "$CANDIDATE"
else
    "$SOURCE/scripts/build/android-aar.sh"
    CANDIDATE="$SOURCE/target/android/maven/io/github/relaynet/roost/$VERSION"
fi
# Resolve only the files handed to publication, with no stale Gradle module metadata.
MAVEN="$ROOT/.candidate/maven"
PACKAGE="$MAVEN/io/github/relaynet/roost/$VERSION"
rm -rf "$ROOT/.candidate"
mkdir -p "$PACKAGE"
cp "$CANDIDATE/roost-$VERSION.aar" "$CANDIDATE/roost-$VERSION.pom" "$PACKAGE/"
BEFORE="$(shasum -a 256 "$PACKAGE/roost-$VERSION.aar" "$PACKAGE/roost-$VERSION.pom")"
"${GRADLE_BIN:-gradle}" --no-daemon -p "$ROOT" \
    -ProostVersion="$VERSION" -ProostMavenRepo="$MAVEN" \
    "${ROOST_ANDROID_TEST_TASK:-:consumer:pixel6Api36DebugAndroidTest}" --rerun
[[ "$(shasum -a 256 "$PACKAGE/roost-$VERSION.aar" "$PACKAGE/roost-$VERSION.pom")" == "$BEFORE" ]]
rm -rf "$ROOT/.release"
mkdir -p "$ROOT/.release"
cp "$PACKAGE/roost-$VERSION.aar" "$PACKAGE/roost-$VERSION.pom" "$ROOT/.release/"
{
    printf '\nAndroid package tools:\n```text\n'
    (cd "$SOURCE" && rustc --version)
    java -version 2>&1
    "${GRADLE_BIN:-gradle}" --version | awk '/^Gradle / {print}'
    cargo ndk --version
    awk '/^Pkg.Revision/ {print "NDK " $3}' "${ANDROID_NDK_HOME:?set ANDROID_NDK_HOME}/source.properties"
    printf 'Android SDK/build tools: 36 / 36.0.0\nABIs: arm64-v8a, armeabi-v7a, x86_64\n'
    printf 'Test task: %s\n```\n' "${ROOST_ANDROID_TEST_TASK:-:consumer:pixel6Api36DebugAndroidTest}"
} > "$ROOT/.release/kotlin-notes.md"
