package dev.marcnow.storybookrunner

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import org.jetbrains.plugins.terminal.TerminalToolWindowManager
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale

object StorybookRunner {
    fun runInTerminal(project: Project, glob: String) {
        val setupResult = ensureStorybookMainSetup(project.basePath)
        when (setupResult) {
            SetupResult.MISSING_MAIN_TS -> notify(
                project,
                NotificationType.WARNING,
                "`.storybook/main.ts` not found. Story glob fallback setup was skipped.",
            )
            SetupResult.MISSING_STORIES_BLOCK -> notify(
                project,
                NotificationType.WARNING,
                "No `stories: [...]` block found in `.storybook/main.ts`. Fallback setup was skipped.",
            )
            is SetupResult.FAILED -> notify(
                project,
                NotificationType.ERROR,
                "Failed to update `.storybook/main.ts`: ${setupResult.message}",
            )
            SetupResult.OK -> Unit
        }

        val manager = TerminalToolWindowManager.getInstance(project)
        val widget = manager.createLocalShellWidget(project.basePath, "Storybook")
        val cmd = buildStorybookCommand(glob)
        widget.executeCommand(cmd)
    }

    private fun ensureStorybookMainSetup(basePath: String?): SetupResult {
        if (basePath == null) return SetupResult.FAILED("Project base path is unavailable")

        return try {
            val mainTsPath = Path.of(basePath, ".storybook", "main.ts")
            if (!Files.exists(mainTsPath)) return SetupResult.MISSING_MAIN_TS

            val original = Files.readString(mainTsPath)
            val storiesRegex = Regex("""stories\s*:\s*\[(?<value>[\s\S]*?)\]""")
            val storiesMatch = storiesRegex.find(original) ?: return SetupResult.MISSING_STORIES_BLOCK
            val oldStoriesValue = storiesMatch.groups["value"]?.value?.trim()?.trimEnd(',').orEmpty()

            val fallbackExpression = when {
                oldStoriesValue.isBlank() -> "'../src/**/*.stories.ts'"
                oldStoriesValue.contains(",") -> "[${oldStoriesValue}]"
                else -> oldStoriesValue
            }

            var updated = original
            if (!updated.contains("STORYBOOK_STORY_GLOB")) {
                val declaration = "const storyGlob = process.env['STORYBOOK_STORY_GLOB'] ?? $fallbackExpression;\n\n"
                val configRegex = Regex("""const\s+config\s*:\s*StorybookConfig\s*=""")
                updated = if (configRegex.containsMatchIn(updated)) {
                    updated.replaceFirst(configRegex, "${declaration}const config: StorybookConfig =")
                } else {
                    declaration + updated
                }
            }

            updated = updated.replaceFirst(storiesRegex, "stories: [storyGlob],")

            if (updated != original) {
                Files.writeString(mainTsPath, updated)
            }
            SetupResult.OK
        } catch (e: Exception) {
            SetupResult.FAILED(e.message ?: "Unknown error")
        }
    }

    private fun notify(project: Project, type: NotificationType, content: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Storybook Runner")
            .createNotification(content, type)
            .notify(project)
    }

    private fun buildStorybookCommand(glob: String): String {
        val shellPath = (
            System.getenv("SHELL")
                ?: System.getenv("ComSpec")
                ?: ""
            ).lowercase(Locale.ROOT)
        return if (SystemInfo.isWindows) {
            when {
                shellPath.contains("cmd.exe") -> "set \"STORYBOOK_STORY_GLOB=$glob\" && npm run storybook"
                shellPath.contains("bash") || shellPath.contains("zsh") || shellPath.contains("sh") || shellPath.contains("fish") ->
                    "env STORYBOOK_STORY_GLOB='$glob' npm run storybook"
                else -> "\$env:STORYBOOK_STORY_GLOB='$glob'; npm run storybook"
            }
        } else {
            "env STORYBOOK_STORY_GLOB='$glob' npm run storybook"
        }
    }

    private sealed interface SetupResult {
        data object OK : SetupResult
        data object MISSING_MAIN_TS : SetupResult
        data object MISSING_STORIES_BLOCK : SetupResult
        data class FAILED(val message: String) : SetupResult
    }
}
