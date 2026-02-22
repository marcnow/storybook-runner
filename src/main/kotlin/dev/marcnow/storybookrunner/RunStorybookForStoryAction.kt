package dev.marcnow.storybookrunner

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vfs.VfsUtilCore

class RunStorybookForStoryAction : AnAction("Run Storybook for this story"), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        if (!file.name.endsWith(".stories.ts")) return

        val base = project.baseDir ?: return
        val rel = VfsUtilCore.getRelativePath(file, base, '/') ?: return
        val glob = "../$rel" // weil Storybook-Config in .storybook liegt

        StorybookRunner.runInTerminal(project, glob)
    }
}
