package net.ahwater.watercommit.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "WaterCommitSettings", storages = [Storage("waterCommit.xml")])
class WaterCommitSettings : PersistentStateComponent<WaterCommitSettings.State> {

    companion object {
        fun getInstance(): WaterCommitSettings =
            com.intellij.openapi.application.ApplicationManager.getApplication().getService(WaterCommitSettings::class.java)
    }

    data class State(
        var autoGitAdd: Boolean = true,
        var autoSyncRemote: Boolean = false
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var autoGitAdd: Boolean
        get() = state.autoGitAdd
        set(value) {
            state.autoGitAdd = value
        }

    var autoSyncRemote: Boolean
        get() = state.autoSyncRemote
        set(value) {
            state.autoSyncRemote = value
        }
}