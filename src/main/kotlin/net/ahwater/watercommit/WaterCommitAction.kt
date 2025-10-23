package net.ahwater.watercommit

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import net.ahwater.watercommit.settings.WaterCommitSettings
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class WaterCommitAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val basePath = project.basePath ?: return
        println("Current git dir: $basePath")
        val gitDir = java.io.File(basePath, ".git")
        if (!gitDir.exists()) {
            showWarn(project, "未检测到Git仓库，请先初始化仓库")
            return
        }

        val config = loadCommitRc(basePath)

        ApplicationManager.getApplication().invokeLater {
            val typeNames = config.types.map { "${it.emoji ?: ""} ${it.name}${it.description}" }.toTypedArray()
            val typeDialog = ComboBoxDialog(project, "请选择提交类型 (type)：", typeNames)
            if (!typeDialog.showAndGet()) return@invokeLater
            val selectedTypeStr = typeDialog.selectedItem ?: return@invokeLater
            val type = config.types.find { "${it.emoji ?: ""} ${it.name}" in selectedTypeStr } ?: return@invokeLater

            val scopeNames = config.scopes.map { "${it.name}：${it.description}" }.toTypedArray()
            val scopeDialog = ComboBoxDialog(project, "请选择作用范围 (scope)：", scopeNames)
            if (!scopeDialog.showAndGet()) return@invokeLater
            val selectedScopeStr = scopeDialog.selectedItem ?: ""
            val scope = config.scopes.find { it.name in selectedScopeStr }?.name ?: ""

            val inputDialog = InputDialog(project, "请输入提交信息（Subject）：")
            if (!inputDialog.showAndGet()) return@invokeLater
            val message = inputDialog.inputText ?: return@invokeLater

            val scopeText = if (scope.isEmpty()) "" else "($scope)"
            val finalMessage = "${type.emoji ?: ""} ${type.name}$scopeText: $message"

            ProgressManager.getInstance().runProcessWithProgressSynchronously({
                try {
                    commitProcess(project, basePath, finalMessage)
                } catch (ex: Exception) {
                    showError(project, "❌ 提交失败：${ex.message}")
                }
            }, "Water Commit 执行中...", true, project)
        }
    }

    class ComboBoxDialog(project: Project?, title: String, items: Array<String>) : DialogWrapper(project) {
        var selectedItem: String? = null
        private val comboBox = JComboBox(items).apply {
            preferredSize = Dimension(420, 30) // 自定义宽度
            isEditable = false
        }

        init {
            setTitle(title)
            init()
        }

        override fun createCenterPanel(): JComponent {
            val panel = JPanel(BorderLayout())
            panel.add(comboBox, BorderLayout.CENTER)
            return panel
        }

        override fun doOKAction() {
            selectedItem = comboBox.selectedItem as? String
            super.doOKAction()
        }
    }

    class InputDialog(project: Project?, title: String) : DialogWrapper(project) {
        var inputText: String? = null
        private val textField = JTextField().apply {
            preferredSize = Dimension(420, 30)
        }

        init {
            setTitle(title)
            init()
        }

        override fun createCenterPanel(): JComponent {
            val panel = JPanel(BorderLayout())
            panel.add(textField, BorderLayout.CENTER)
            return panel
        }

        override fun doOKAction() {
            inputText = textField.text.trim()
            super.doOKAction()
        }
    }

    private fun commitProcess(project: Project, cwd: String, message: String) {
        val autoGitAdd = getBooleanConfig(project, "waterCommit.autoGitAdd", true)
        val autoSyncRemote = getBooleanConfig(project, "waterCommit.autoSyncRemote", false)

        val staged = exec(listOf("git", "diff", "--cached", "--name-only"), cwd).trim()
        if (staged.isEmpty()) {
            val changed = exec(listOf("git", "status", "--porcelain"), cwd).trim()
            println("*****git status result:*****\n$changed")
            if (changed.isEmpty()) {
                showInfo(project, "😄 没有可提交的更改。")
                return
            }
            if (autoGitAdd) {
                exec(listOf("git", "add", "-A"), cwd)
            } else {
                showWarn(project, "暂存区为空，请将文件添加暂存区或将 waterCommit.autoGitAdd 配置为 true。")
                return
            }
        }

        exec(listOf("git", "commit", "-m", message), cwd)

        if (autoSyncRemote) {
            syncToRemote(project, cwd, message)
        } else {
            showInfo(project, "✅ 提交成功：$message")
        }
    }

    private fun syncToRemote(project: Project, cwd: String, finalMessage: String) {
        try {
            val remotes = exec(listOf("git", "remote"), cwd).split("\n").filter { it.isNotBlank() }
            if (remotes.isEmpty()) {
                showWarn(project, "提交成功，检测到未配置远程仓库，已跳过同步。")
                return
            }
            val remoteName = if (remotes.contains("origin")) "origin" else remotes.first()
            val branch = exec(listOf("git", "rev-parse", "--abbrev-ref", "HEAD"), cwd).trim()
            val remoteBranches = exec(listOf("git", "ls-remote", "--heads", remoteName), cwd)

            if (remoteBranches.contains("refs/heads/$branch")) {
                exec(listOf("git", "push", remoteName, branch), cwd)
            } else {
                exec(listOf("git", "push", "-u", remoteName, branch), cwd)
            }

            showInfo(project, "✅ 提交成功，分支已同步：$finalMessage")
        } catch (ex: Exception) {
            showError(project, "❌ 推送失败：${ex.message}")
        }
    }

    private fun exec(command: List<String>, cwd: String): String {
        val process = ProcessBuilder(command).directory(java.io.File(cwd)).redirectErrorStream(true).start()
        val result = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw RuntimeException("Command failed: ${command.joinToString(" ")}\n$result")
        }
        return result
    }

    private fun showInfo(project: Project, msg: String) =
        ApplicationManager.getApplication().invokeLater {
            Messages.showInfoMessage(project, msg, "Water Commit 提示")
        }

    private fun showWarn(project: Project, msg: String) =
        ApplicationManager.getApplication().invokeLater {
            Messages.showWarningDialog(project, msg, "Water Commit 提示")
        }

    private fun showError(project: Project, msg: String) =
        ApplicationManager.getApplication().invokeLater {
            Messages.showErrorDialog(project, msg, "Water Commit 错误")
        }

    private fun getBooleanConfig(project: Project, key: String, default: Boolean): Boolean {
        val settings = WaterCommitSettings.getInstance()
        return when (key) {
            "waterCommit.autoGitAdd" -> settings.autoGitAdd
            "waterCommit.autoSyncRemote" -> settings.autoSyncRemote
            else -> default
        }
    }

    private fun loadCommitRc(basePath: String): CommitConfig {
        val rcFile = java.io.File(basePath, ".commitrc")
        if (!rcFile.exists()) return CommitConfig.default()
        return try {
            val json = rcFile.readText()
            CommitConfig.fromJson(json)
        } catch (e: Exception) {
            ApplicationManager.getApplication().invokeLater {
                Messages.showErrorDialog(
                    ".commitrc 文件读取失败，将使用预设配置，请检查文件格式是否为正确的 JSON。",
                    "Water Commit 配置错误"
                )
            }
            CommitConfig.default()
        }
    }
}