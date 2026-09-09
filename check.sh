#!/usr/bin/env bash
# Build one Maven candidate and load it on one Android emulator.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
SOURCE="$(cd "${1:?usage: check.sh <roost-source>}" && pwd -P)"
# The consumer checks the production coordinate only.
unset ROOST_CARGO_FEATURES
"$SOURCE/scripts/build/android-aar.sh"
VERSION="$(awk -F '"' '/^version = / { print $2; exit }' "$SOURCE/Cargo.toml")"
mkdir -p "$ROOT/consumer/src/androidTest/assets"
cp "$SOURCE/crates/roost-protocol/tests/fixtures/evidence_bundles_v1.json" "$ROOT/consumer/src/androidTest/assets/"
"${GRADLE_BIN:-gradle}" --no-daemon -p "$ROOT" \
    -ProostVersion="$VERSION" -ProostMavenRepo="$SOURCE/target/android/maven" \
    "${ROOST_ANDROID_TEST_TASK:-:consumer:pixel6Api36DebugAndroidTest}"
