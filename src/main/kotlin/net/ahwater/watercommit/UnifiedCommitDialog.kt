package net.ahwater.watercommit

import net.ahwater.watercommit.settings.WaterCommitSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.*

class UnifiedCommitDialog(
    project: Project?,
    private val types: List<CommitType>,
    private val scopes: List<CommitScope>,
) : DialogWrapper(project) {

    var selectedType: CommitType? = null
    var selectedScope: CommitScope? = null
    var subject: String = ""
    var body: String = ""
    private val settings = WaterCommitSettings.getInstance()

    private val typeCombo = ComboBox(
        DefaultComboBoxModel(
            types.map { "${it.emoji ?: ""} ${it.name} - ${it.description}" }.toTypedArray()
        )
    ).apply {
        preferredSize = Dimension(420, 30)
        isEditable = false
    }

    private val scopeCombo = ComboBox(
        DefaultComboBoxModel(
            scopes.map { "${it.name} - ${it.description}" }.toTypedArray()
        )
    ).apply {
        preferredSize = Dimension(420, 30)
        isEditable = false
    }

    private val subjectField = JBTextField().apply {
        preferredSize = Dimension(420, 30)
    }

    private val bodyArea = JBTextArea().apply {
        lineWrap = true
        wrapStyleWord = true
        border = BorderFactory.createCompoundBorder(
            subjectField.border,                  // 保留原始边框
            JBUI.Borders.empty(4, 8)              // 上下左右各 4px 内边距
        )
    }

    private val autoGitAddCheck = JCheckBox("当暂存区为空时自动执行 `git add -A` 后再提交。", settings.autoGitAdd)
    private val autoSyncRemoteCheck = JCheckBox("提交完成后自动同步当前分支到远程仓库。", settings.autoSyncRemote)


    init {
        title = "开始填写提交信息"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        fun addComponentWithSpacing(comp: JComponent, topMargin: Int = 10) {
            val wrapper = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                border = JBUI.Borders.emptyTop(topMargin)
                add(comp)
            }
            panel.add(wrapper)
        }

        // 添加控件时统一 margin
        addComponentWithSpacing(labelAndComp("提交类型(type)：", typeCombo, true))
        addComponentWithSpacing(labelAndComp("作用域(scope)：", scopeCombo, true))
        addComponentWithSpacing(labelAndComp("提交标题(subject)：", subjectField, true))

        val scroll = JBScrollPane(bodyArea).apply {
            preferredSize = Dimension(420, 150)  // 固定高度
            maximumSize = Dimension(420, 150)
            minimumSize = Dimension(420, 150)
            border = JBUI.Borders.empty()
        }
        addComponentWithSpacing(labelAndComp("提交详情(body)：", scroll))

        addComponentWithSpacing(wrapCheckBox(autoGitAddCheck), 10)
        addComponentWithSpacing(wrapCheckBox(autoSyncRemoteCheck), 4)

        return panel
    }

    /**
     * 带 label 的组件
     */
    private fun labelAndComp(label: String, comp: JComponent, isRequired: Boolean = false): JPanel {
        val jLabel = if (isRequired) {
            JLabel("<html><font color='red'>*</font> $label</html>")
        } else {
            JLabel(label)
        }
        jLabel.border = JBUI.Borders.emptyBottom(4)
        val p = JPanel(BorderLayout())
        p.add(jLabel, BorderLayout.NORTH)
        p.add(comp, BorderLayout.CENTER)
        return p
    }

    private fun wrapCheckBox(cb: JCheckBox): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            cb.alignmentX = Component.LEFT_ALIGNMENT
            add(cb)
            maximumSize = Dimension(Int.MAX_VALUE, cb.preferredSize.height)
        }
    }

    override fun doOKAction() {
        val typeStr = typeCombo.selectedItem as? String ?: ""
        selectedType = types.find { typeStr.startsWith("${it.emoji ?: ""} ${it.name}") }

        val scopeStr = scopeCombo.selectedItem as? String ?: ""
        selectedScope = scopes.find { scopeStr.startsWith(it.name) }

        subject = subjectField.text.trim()

        if (subject.isEmpty()) {
            JOptionPane.showMessageDialog(
                contentPane,
                "请输入提交标题(subject)！",
                "错误提示",
                JOptionPane.WARNING_MESSAGE
            )
            return  // 阻止关闭弹框
        }

        body = bodyArea.text.trim()

        settings.autoGitAdd = autoGitAddCheck.isSelected
        settings.autoSyncRemote = autoSyncRemoteCheck.isSelected

        super.doOKAction()
    }
}