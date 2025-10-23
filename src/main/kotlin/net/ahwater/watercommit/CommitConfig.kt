package net.ahwater.watercommit

import com.google.gson.Gson

data class CommitType(val name: String, val emoji: String? = null, val description: String? = null)
data class CommitScope(val name: String, val description: String? = null)

data class CommitConfig(
    val types: List<CommitType>,
    val scopes: List<CommitScope>
) {
    companion object {
        fun fromJson(json: String): CommitConfig =
            Gson().fromJson(json, CommitConfig::class.java)

        fun default(): CommitConfig = CommitConfig(
            types = listOf(
                CommitType("feat", "✨", "：新功能，新增页面、组件、API接口等"),
                CommitType("fix", "🐛", "：修复bug，修复逻辑错误、功能错误、代码报错等"),
                CommitType("ui", "🎨", "：ui更新、修改styles样式"),
                CommitType("text", "✏️", "：修改项目里的文本文字、文案描述等"),
                CommitType("refactor", "💎", "：重构某个功能、重写组件结构、逻辑优化"),
                CommitType("perf", "⚡️", "：性能优化(优化算法、优化渲染、减少请求、缓存处理等)"),
                CommitType("docs", "📝", "：更新项目文档、手册、README、注释等"),
                CommitType("chore", "🔧", "：其他杂项任务，更新各种配置文件"),
                CommitType("deps", "📦️", "：更新项目依赖、第三方库"),
                CommitType("revert", "🚑️", "：版本回滚、修复误提交")
            ),
            scopes = listOf(
                CommitScope("api", "接口相关，接口的增删改"),
                CommitScope("map", "地图底层相关，与pages不同"),
                CommitScope("components", "组件相关，更新组件功能、逻辑"),
                CommitScope("chart", "图表相关，更新图表的绘制、option等"),
                CommitScope("pages", "页面相关，新增了xx页面，修改了xx页面功能、逻辑、样式"),
                CommitScope("utils", "工具方法相关"),
                CommitScope("layout", "布局相关"),
                CommitScope("styles", "样式相关，仅更新样式时候选择此项"),
                CommitScope("vitepress", "更新项目文档"),
                CommitScope("store", "状态相关，如Pinia、前端存储"),
                CommitScope("eslint", "eslint相关，修改、更新某些规则"),
                CommitScope("config", "配置文件相关，dockerfile、vite.config等"),
                CommitScope("other", "其他"),
                CommitScope("", "无(谨慎选择)")
            )
        )
    }
}