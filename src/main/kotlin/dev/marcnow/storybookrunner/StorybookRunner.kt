package dev.marcnow.storybookrunner

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import org.jetbrains.plugins.terminal.TerminalToolWindowManager

object StorybookRunner {
    fun runInTerminal(project: Project, glob: String) {
        val manager = TerminalToolWindowManager.getInstance(project)
        val widget = manager.createLocalShellWidget(project.basePath, "Storybook")
        val cmd = buildStorybookCommand(glob)
        widget.executeCommand(cmd)
    }

    private fun buildStorybookCommand(glob: String): String {
        return if (SystemInfo.isWindows) {
            // Execute via cmd explicitly so the command works even when the IDE terminal uses PowerShell.
            "cmd /d /s /c \"set \\\"STORYBOOK_STORY_GLOB=$glob\\\" && npm run storybook\""
        } else {
            "env STORYBOOK_STORY_GLOB='$glob' npm run storybook"
        }
    }

}
