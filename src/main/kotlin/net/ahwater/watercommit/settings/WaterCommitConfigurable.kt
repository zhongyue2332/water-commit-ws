package net.ahwater.watercommit.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.BoxLayout

class WaterCommitConfigurable : Configurable {

    private var panel: JPanel? = null
    private var autoAddCheckbox: JCheckBox? = null
    private var autoSyncCheckbox: JCheckBox? = null

    override fun getDisplayName(): String = "Water Commit"

    override fun createComponent(): JComponent {
        val settings = WaterCommitSettings.getInstance()
        panel = JPanel()
        panel!!.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        autoAddCheckbox = JCheckBox("当暂存区为空时自动执行 `git add -A` 后再提交。", settings.autoGitAdd)
        autoSyncCheckbox = JCheckBox("提交完成后自动同步当前分支到远程仓库。", settings.autoSyncRemote)

        panel!!.add(autoAddCheckbox)
        panel!!.add(autoSyncCheckbox)

        return panel!!
    }

    override fun isModified(): Boolean {
        val settings = WaterCommitSettings.getInstance()
        return settings.autoGitAdd != autoAddCheckbox!!.isSelected ||
                settings.autoSyncRemote != autoSyncCheckbox!!.isSelected
    }

    override fun apply() {
        val settings = WaterCommitSettings.getInstance()
        settings.autoGitAdd = autoAddCheckbox!!.isSelected
        settings.autoSyncRemote = autoSyncCheckbox!!.isSelected
    }

    override fun reset() {
        val settings = WaterCommitSettings.getInstance()
        autoAddCheckbox!!.isSelected = settings.autoGitAdd
        autoSyncCheckbox!!.isSelected = settings.autoSyncRemote
    }

    override fun disposeUIResources() {
        panel = null
        autoAddCheckbox = null
        autoSyncCheckbox = null
    }
}