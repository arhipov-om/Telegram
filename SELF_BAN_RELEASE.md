# Self-ban APK migration and release prerequisites

This branch contains an **incomplete** source implementation. The existing user-provided APK is a repackaged binary and is **not reproducible from this branch yet**. Do not publish a release until source integration and testing are complete.

## Existing installation compatibility

- Application ID: `org.telegram.messenger.beta`
- Last supplied APK versionName: `13.0.3`
- Last supplied APK versionCode: `71840`
- Signing certificate SHA-256: `06fdc5264932543b6df2dda3248f770846cfb26a96bdb9e5dd62bd1acc1fde66`
- ABI: `arm64-v8a`
- Future APKs must use the **same signing private key**, the same application ID, and a greater versionCode.
- The supplied ZIP archives contained APKs and README files, **not** the signing keystore.
- The latest supplied APK was patched, not built from source. Current Java self-ban store has not been integrated with UI/navigation.

## Required secure release configuration

Keep credentials out of Git commits, pull requests, logs and APK assets:

- Signing keystore: provision through an encrypted GitHub Actions secret (base64) or secure release environment.
- Keystore alias, store password and key password: GitHub Actions secrets.
- Telegram API credentials: provision via secrets and ensure BuildVars.java receives the expected values at build time without logging them.
- Firebase configuration: use project-specific google-services.json; never reuse the upstream dummy for a production release.
- Use the repository's documented Android SDK 36 and NDK 27.2.12479018.

## Before enabling automated release

1. Integrate strict channel blocking into all relevant Telegram entry points and add tests.
2. Configure the beta application ID and versionCode greater than 71840.
3. Validate a locally built APK's signing certificate SHA-256 against the value above.
4. Test in-place installation and app-data preservation on a spare device.
5. Build from a pinned upstream revision and review upstream merges; do not auto-publish untested builds.

**No signing key or API secret is stored in this repository.**
