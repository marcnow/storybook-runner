package dev.marcnow.storybookrunner

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement

class StoryRunLineMarkerContributor : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        val psiFile = element.containingFile ?: return null
        val file = psiFile.virtualFile ?: return null
        if (!file.name.endsWith(".stories.ts")) return null

        val content = psiFile.text
        val markerOffset = content.indexOf("export default").takeIf { it >= 0 } ?: return null
        if (element.textRange?.startOffset != markerOffset) return null

        return Info(
            AllIcons.Actions.Execute,
            arrayOf(RunStorybookForStoryAction()),
        ) { "Run Storybook for this story" }
    }
}
