---
name: code-review
description: Review Bubble Night pull requests for Kotlin, Compose, gameplay, accessibility, offline behavior, and verification quality.
triggers:
  - /codereview
---

# Bubble Night Pull Request Review

Read `AGENTS.md`, `docs/ARCHITECTURE.md`, `docs/CONTENT.md`, and `docs/UX_FLOW.md` where relevant. Prioritize real defects over style preferences. Check Kotlin correctness, lifecycle/state handling, Compose navigation, save behavior, small-screen and large-font layout, touch targets, accessibility/TalkBack, left-handed mode, precise aim, and reduced motion.

Flag new network access, ads, analytics, accounts, tracking, and third-party SDKs. Check for accidental changes to physics, scoring, RNG, saves, migrations, catalog hashes, certificates, app identity, signing, and licenses. Keep visible UI text in string resources. Require Maestro coverage for visible changes where practical and emulator/device evidence for visual claims.

For every finding include severity, exact file/line, player impact, and a concrete fix. Do not report speculation as fact. Do not rewrite files, push commits, or merge during a review; this workflow is review-only.
