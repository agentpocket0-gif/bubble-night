# AI Agent and Android UI Test Setup

This pilot adds two separate quality tools. Neither tool is bundled into the Android APK.

## OpenHands: pull request review

OpenHands reviews a Pull Request and posts findings. This workflow does not write source files or merge a PR.

Before using it, open GitHub repository settings:

1. Go to **Settings -> Secrets and variables -> Actions**.
2. Add a repository secret named `LLM_API_KEY`. Use a provider key you are allowed to use. Never put it in source code or a chat message.
3. Add a repository variable named `OPENHANDS_LLM_MODEL`, using the model identifier required by your provider and OpenHands/LiteLLM.
4. Optionally add `OPENHANDS_LLM_BASE_URL` for a compatible custom endpoint.

To request a review, open a Pull Request and add the `review-this` label. The reviewer inspects the diff and posts comments. The workflow is pinned to an OpenHands Extensions commit. It has read-only repository contents permission; it can write review comments but cannot change source files.

**Cost:** OpenHands is open source, but model inference is not automatically free. Check your provider's current free-tier limits and billing before adding a key. The repository does not include or store a model key.

## Maestro: Android UI smoke test

Maestro checks the app through Android's display/accessibility layer. The first flow verifies a fresh launch, Campaign Map, Easy/Medium/Hard choices, Level 1 selection, Level Preparation, and entry into Gameplay.

The `maestro-ui-smoke.yml` workflow uses a GitHub Actions Android emulator. It builds the debug APK, installs it in the emulator, and runs the Maestro flow when relevant changes are pushed to `premium-complete-upgrade`.

This is an emulator smoke test, not proof that every device behaves identically. A real-phone check remains necessary for touch feel, layout, sound, performance, and saved-game behavior.

## Why OpenHands + Maestro instead of OpenManus alone?

OpenManus is a general-purpose agent framework, not a ready-made Android game UX specialist. Bubble Night is Kotlin/Jetpack Compose, so a code reviewer plus an Android UI runner is a closer fit:

- OpenHands: inspect and review Kotlin/Compose changes.
- Maestro: launch the built app, tap, scroll, and assert the actual UI journey.
- Bubble Night project instructions: preserve its game rules, offline design, accessibility, and visual identity.

## Safe rollout

This setup is on a separate pilot branch. Do not merge it while the current `premium-complete-upgrade` workflow is running: that workflow uses `cancel-in-progress: true`, so a new push to that branch can cancel its active run.

After the current build completes, review and merge the pilot Pull Request into `premium-complete-upgrade`. The existing premium build will run for the merge commit. The Maestro workflow will run on the next relevant push to that branch.

No workflow merges changes automatically. Keep app changes in Pull Requests and merge only after CI and visual evidence have been reviewed.
