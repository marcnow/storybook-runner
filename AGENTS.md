# AGENTS.md

This file gives coding agents project-specific instructions for `storybook-runner`.
Scope: this file applies to the full repository tree from this directory downward.

## Precedence and Overrides

- Direct user/developer/system instructions override this document.
- If nested `AGENTS.md` files are added later, the file closest to the changed file takes precedence.
- Keep this file in sync with user preferences stated in chat. When the user adds a durable workflow preference, update `AGENTS.md` in the same change set.

## Project Snapshot

- Type: IntelliJ Platform plugin (Kotlin, Gradle Kotlin DSL)
- Plugin id: `dev.marc.storybook-runner`
- Main package: `dev.marcnow.storybookrunner`
- Core behavior:
  - Adds actions to run Storybook for a selected story or folder.
  - Adds a run line marker in `*.stories.ts` files.
  - Starts Storybook in the IDE terminal with `STORYBOOK_STORY_GLOB`.

## Environment and Commands

- Use the Gradle wrapper from repo root.
- On Windows use:
  - `.\gradlew.bat build`
  - `.\gradlew.bat verifyPlugin`
  - `.\gradlew.bat runIde`
- On macOS/Linux use:
  - `./gradlew build`
  - `./gradlew verifyPlugin`
  - `./gradlew runIde`

Run before finishing non-trivial changes:

1. `build`
2. `verifyPlugin`

## Where to Change What

- Action wiring and plugin metadata:
  - `src/main/resources/META-INF/plugin.xml`
- Storybook command execution:
  - `src/main/kotlin/dev/marcnow/storybookrunner/StorybookRunner.kt`
- Line marker behavior:
  - `src/main/kotlin/dev/marcnow/storybookrunner/StoryRunLineMarkerContributor.kt`
- Context menu behavior:
  - `src/main/kotlin/dev/marcnow/storybookrunner/RunStorybookFromContextAction.kt`
  - `src/main/kotlin/dev/marcnow/storybookrunner/RunStorybookForStoryAction.kt`

## Class Responsibility Map

- `StorybookRunner`: builds and executes the terminal command that starts Storybook with `STORYBOOK_STORY_GLOB`.
- `RunStorybookForStoryAction`: run-line-marker action for one selected `*.stories.ts` file.
- `RunStorybookFromContextAction`: project-tree context action; resolves glob for either one story file or a folder.
- `StoryRunLineMarkerContributor`: places the green run icon in story files and binds it to `RunStorybookForStoryAction`.

When adding a new class, add one line here describing its responsibility.

## Guardrails

- Keep the plugin dumb-aware where appropriate (`DumbAware`) to avoid indexing-time issues.
- Preserve cross-shell behavior when touching command construction:
  - `cmd.exe`
  - POSIX-like shells (`bash`, `zsh`, `sh`, `fish`)
  - PowerShell fallback
- Keep glob semantics compatible with `.storybook`-relative paths.
- Avoid adding new heavy dependencies unless necessary.
- Do not commit generated or machine-local artifacts when avoidable.
  - Examples: `.intellijPlatform/`, build outputs, IDE runtime caches.

## Coding Style

- Prefer the simplest working solution first. Avoid complex automation when a small explicit step is enough.
- Follow existing Kotlin style and keep functions small and explicit.
- Prefer early returns and null-safe handling (`?: return`) in action handlers.
- Keep user-facing action text concise and consistent with existing wording.

## Validation Checklist

For changes to run actions or glob resolution:

1. Confirm action visibility/enabled state is correct for:
   - single `*.stories.ts` file
   - folder containing stories
   - unrelated file/folder
2. Confirm terminal command launches and includes the expected `STORYBOOK_STORY_GLOB`.
3. Confirm no regression in run line marker placement.

## Commit Guidance

- Make focused commits with imperative messages.
- Include a short test note in commit body for behavioral changes (what was verified).
