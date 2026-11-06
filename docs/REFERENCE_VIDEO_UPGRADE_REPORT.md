# Bubble Night: Reference Video Upgrade Report

Date: 6 November 2026  
Repository: https://github.com/agentpocket0-gif/bubble-night  
Working branch: `reference-video-upgrade`  
Base branch: `premium-complete-upgrade`

## Scope reviewed

Two user-provided screen recordings were reviewed:

- The current Bubble Night recording (about 36 seconds) shows the existing home, campaign/orbit map, difficulty selection, level gameplay and results.
- The reference bubble-shooter recording (about 2 minutes 21 seconds) shows a brighter sky-blue playfield, a much denser glossy bubble cluster, a dotted aiming path, more prominent projectile effects, a bottom booster tray and stronger visual feedback.

This is a feature and visual reference review. It does not copy the reference game's name, logo, characters, sprites, music, sound recordings or proprietary artwork.

## Existing project facts verified in source

- This repository is a native Android application written in Kotlin and Jetpack Compose. It is **not** a Godot project.
- The catalogue contains 600 base levels arranged as 12 groups of 50.
- Each base level currently has Easy, Medium and Hard variants, for 1,800 variant boards and associated solution certificates.
- The game already has local sound cues for launch, bounce, match, drop, victory and defeat, played with Android SoundPool. Vibration is optional.
- The game board already draws glossy-looking spheres with radial gradients, highlights, rims and shadows. It uses Compose Canvas, which gives a 2.5D visual style rather than true real-time 3D models.
- The current game data model supports colour bubbles, rocks and stars. It does not yet define bomb or laser power-up mechanics.

## Changes committed on this branch

1. **Brighter visual theme**
   - Changed the main app theme from dark to light.
   - Replaced the navy/near-black background palette with a light sky-blue palette and darker readable text.
   - Updated the gameplay board gradient, board decoration, bubble colours and shadow treatment.
   - Changed the aiming preview to a dotted orange path for stronger contrast.

2. **One visible difficulty choice**
   - Removed the Easy / Medium / Hard picker from the campaign map, free-play, daily challenge, Zen selection and statistics screens.
   - Removed the difficulty row from level preparation.
   - New profiles now default to Medium as the internal standard variant.

   **Compatibility note:** the old profile format still stores its previous difficulty, and historical progress is still partitioned by difficulty. This UI pass hides the selector but is not yet a full historical progress merge or a profile migration. Existing saves and active sessions have deliberately not been force-reset.

3. **Denser first level**
   - Updated Level 1 to use one shared five-row, 43-bubble layout across the three existing variants.
   - Increased its content revision so old certificates cannot be silently reused.
   - The repository workflow must solve and replay fresh certificates before this level can be considered verified.

4. **Build workflow**
   - Enabled the existing premium certificate-and-build workflow on this branch.
   - Made the workflow check out and push to the branch that triggered it, instead of hard-coding the older upgrade branch.
   - The workflow runs catalogue validation, certificate replay, unit tests, lint and APK builds, then uploads debug and unsigned release APK artifacts if all steps pass.

## Not yet implemented or verified

These items are intentionally not described as complete:

- Real bomb and laser power-ups, including inventory/charges, activation rules, engine events, animations, scoring and tests.
- Dedicated bomb/laser sound cues and richer particle/burst effects.
- A full density and layout review of Levels 2–600. Only Level 1's board layout has been changed in this pass; the 600-level catalogue itself remains intact.
- A safe migration that combines historical Easy, Medium and Hard progress into one Standard campaign without losing stars, scores, achievements or saved sessions.
- True 3D assets/models. The current app is Kotlin/Compose and has no Godot 3D renderer. The current visual pass uses native Canvas shading and lighting effects.
- Real-device checks for touch feel, readability, sound balance, performance, and screen-size behaviour.

## Verification gate

The GitHub Actions run for this branch is the source of truth for build status. A successful workflow must complete:
- Level 1 solve and rebalance
- Strict 600-level catalogue validation
- Certificate replay
- Core, level-tool and app unit tests
- Android lint
- Debug APK and unsigned release APK build

Only after that run succeeds should the generated APK artifacts be treated as build outputs. A successful cloud build still does not replace installing and testing the APK on a physical Android phone.

## Next engineering steps

1. Design the Standard campaign migration and add tests before changing old save data.
2. Expand and review board density across the remaining levels, then regenerate and replay certificates.
3. Add bomb and laser as real engine-supported pieces/power-ups, not decorative controls.
4. Add original sound cues and effects for each new action.
5. Test the resulting APK on a physical Android device and fix any layout or performance issues.

## Current status

This is a first upgrade pass committed to an isolated branch. The main branch has not been changed. The visual theme and Level 1 layout changes are committed; the full 600-level, power-up and save-migration work is still in progress and must pass the verification gates above.
