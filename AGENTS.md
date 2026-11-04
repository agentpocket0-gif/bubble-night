# AI Agent Instructions for Bubble Night

Read `docs/ARCHITECTURE.md`, `docs/CONTENT.md`, `docs/UX_FLOW.md`, and `README.md` before changing the app.

Bubble Night is a Kotlin and Jetpack Compose Android game with `:core`, `:leveltool`, and `:app` modules. Preserve the deterministic engine, certificates, offline-first design, local saves, accessibility, six original sound cues, MIT notices, and all existing game rules. Keep visible strings in Android resources. Do not add network access, ads, analytics, accounts, tracking, or third-party SDKs without explicit approval. Do not copy competitor artwork, logos, characters, music, sounds, text, or exact layouts; learn only general interaction patterns.

Prefer focused Compose UI improvements. Do not change level JSON, certificates, solver settings, package ID, signing or permissions for a UI task. Any gameplay-content or engine change must re-solve and replay all affected certificates. Add/update tests and run `bash tools/verify.sh`; report only checks that actually ran. For visible UI changes, add a Maestro flow when practical and request real-device evidence. Keep changes on a feature branch and open a Pull Request. Never push directly to `main` or merge automatically.
