# Project rules

## Product goal

This project is a very minimal Android/Kotlin foosball referee timer.

- The app starts at 00:00.
- The screen shows only the current time and a minimal reset interaction.
- Reset happens via a dedicated reset button, screen tap, or volume +/- buttons.
- The app has no complex settings or extra screens in the first version.
- The timer uses haptic feedback as the primary signal instead of loud audio.

## Required behavior

- Reset event: 3 short vibration pulses in quick succession.
- Long vibration at 10, 15, 30 and 90 seconds.
- Short vibration at 8, 9, 13, 14, 28, 29, 88 and 89 seconds.
- Background color transitions:
  - start: light green
  - from 5s: yellow
  - from 10s: orange
  - from 15s: red
  - from 30s: blue
  - from 90s: white
- The display should remain readable even in bright or dark conditions.
- Interaction should be simple enough for one-handed referee use.

## Communication style

- Keep all answers as short and concise as possible.
- Provide details only when explicitly asked.
- Prefer direct, minimal responses over long explanations.

## Git and branch safety

- Never commit directly to `main` or `dev`.
- Always create a new feature branch before making commits.
- If the current branch is `main` or `dev`, stop and switch to a feature branch before continuing.
- Do not merge or push directly into protected branches.
- Use pull requests for all changes.
- Never bypass branch protection or force-push to `main` or `dev`.

## Scope and change discipline

- Stay within the current project scope.
- Do not add unrelated features or "nice-to-haves" unless explicitly requested.
- Keep changes small, focused, and testable.
- Keep edits focused and avoid unrelated file churn.
- Do not add broad scope or unrelated features.

## Architecture and code quality

- Prefer the smallest architecture that solves the problem.
- Add a new module or abstraction only when it clearly reduces complexity.
- Use a simple Android/Kotlin app structure: `MainActivity` for UI and input, a timer service for ticking, and vibration helpers for feedback.
- Keep timer logic separate from UI rendering and haptic effects.
- Prefer small, explicit state transitions over complex frameworks.
- Keep the user interface intentionally minimal and readable.
- Keep the project minimal, explicit, and maintainable.

## Validation and testing

- Run the smallest relevant validation command for each change.
- Do not claim work is complete without validation evidence.
- Add or update unit tests for timer thresholds, color transitions, and vibration triggers when logic changes.
- Prefer real behavior tests over mock-heavy tests.
- Avoid mock-heavy tests when real behavior can be tested directly.
- Keep tests small, clear, and focused on one behavior.
- Update documentation when behavior or structure changes.
