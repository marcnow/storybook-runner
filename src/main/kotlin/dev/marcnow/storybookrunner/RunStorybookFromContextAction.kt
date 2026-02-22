package dev.marcnow.storybookrunner

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile

class RunStorybookFromContextAction : AnAction("Run Storybook", null, AllIcons.Actions.Execute), DumbAware {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project
        val selected = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        val target = selected?.singleOrNull()

        val visible = project != null && target != null && resolveGlob(project.baseDir, target) != null
        e.presentation.isEnabledAndVisible = visible
        e.presentation.icon = AllIcons.Actions.Execute
        e.presentation.text = when {
            target == null -> "Run Storybook"
            target.isDirectory -> "Run Storybook for stories in folder"
            else -> "Run Storybook for this story"
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val target = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.singleOrNull() ?: return
        val glob = resolveGlob(project.baseDir, target) ?: return
        StorybookRunner.runInTerminal(project, glob)
    }

    private fun resolveGlob(baseDir: VirtualFile?, target: VirtualFile): String? {
        val base = baseDir ?: return null
        return if (target.isDirectory) {
            if (!containsStoriesFile(target)) return null
            val relDir = VfsUtilCore.getRelativePath(target, base, '/') ?: return null
            if (relDir.isEmpty()) "../**/*.stories.ts" else "../$relDir/**/*.stories.ts"
        } else {
            if (!target.name.endsWith(".stories.ts")) return null
            val relFile = VfsUtilCore.getRelativePath(target, base, '/') ?: return null
            "../$relFile"
        }
    }

    private fun containsStoriesFile(dir: VirtualFile): Boolean {
        var found = false
        VfsUtilCore.iterateChildrenRecursively(dir, null) { file ->
            if (!file.isDirectory && file.name.endsWith(".stories.ts")) {
                found = true
                false
            } else {
                true
            }
        }
        return found
    }
}
