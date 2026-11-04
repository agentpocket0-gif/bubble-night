---
name: bubble-night-game-ux
description: Improve Bubble Night's player journey with evidence-based puzzle-game UX patterns while preserving its offline Android architecture.
---

# Bubble Night Game UX

Read `AGENTS.md`, `docs/ARCHITECTURE.md`, `docs/CONTENT.md`, and `docs/UX_FLOW.md` before editing. Use official store listings/help pages for current research. Existing references include Block Blast, Bubble Witch 3, Toon Blast, and Candy Crush Saga. Learn general interaction patterns only: quick entry to play, clear progression, visible goals and remaining shots, aiming feedback, and progressive teaching. Never copy competitor art, logos, characters, music, sounds, text, or exact layouts.

Evaluate launch/loading, campaign map, preparation, gameplay HUD, results, and return-to-map progression. Prefer focused Compose changes. Keep offline play, game rules, local saves, accessibility, six original sound cues, MIT notices, and Bubble Night's identity. Do not add network, ads, analytics, accounts, tracking, or new SDKs. Keep visible text in Android resources. Do not alter level JSON/certificates for a UI-only task. If gameplay content or engine behavior changes, re-solve and replay all affected certificates.

Add/update tests and a Maestro flow for visible changes where practical. Run `bash tools/verify.sh`, report only checks actually run, and request real-device evidence for touch, layout, sound, performance, and saved-game behavior. Use a feature branch and Pull Request; never merge automatically.
