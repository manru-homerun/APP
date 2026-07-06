/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.manruhomerun.yadanbeopseok

import com.android.utils.associateWithNotNull
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import kotlin.collections.count
import kotlin.collections.dropLastWhile
import kotlin.collections.filter
import kotlin.collections.firstOrNull
import kotlin.collections.flatMap
import kotlin.collections.flatMapTo
import kotlin.collections.flatten
import kotlin.collections.forEach
import kotlin.collections.getValue
import kotlin.collections.groupBy
import kotlin.collections.ifEmpty
import kotlin.collections.isNotEmpty
import kotlin.collections.joinToString
import kotlin.collections.map
import kotlin.collections.mapKeys
import kotlin.collections.mapTo
import kotlin.collections.mapValues
import kotlin.collections.orEmpty
import kotlin.collections.partition
import kotlin.collections.plus
import kotlin.collections.plusAssign
import kotlin.collections.sorted
import kotlin.collections.sortedDescending
import kotlin.collections.sortedWith
import kotlin.collections.toSet
import kotlin.io.readText
import kotlin.io.writeText
import kotlin.text.RegexOption.DOT_MATCHES_ALL
import kotlin.text.appendLine
import kotlin.text.count
import kotlin.text.isBlank
import kotlin.text.isEmpty
import kotlin.text.isNotEmpty
import kotlin.text.lines
import kotlin.text.repeat
import kotlin.text.replace
import kotlin.text.split
import kotlin.text.substringAfterLast
import kotlin.text.substringBeforeLast
import kotlin.text.toRegex
import kotlin.text.trimIndent
import kotlin.text.trimMargin
import kotlin.to

/**
 * `graphDump` 태스크를 사용하여 모듈 종속성 그래프를 생성하고, `graphUpdate` 태스크로 해당 `README.md` 파일을 업데이트합니다.
 * 이 구현은 최적화되어 있지 않으며 필요에 따라 개선될 수 있습니다.
 * - `Graph.invoke`는 종속 프로젝트를 **재귀적으로** 검색합니다(실제로는 스택 오버플로가 발생하지 않습니다).
 * - `Graph.invoke`는 중간 값을 재사용하지 않고 모든 프로젝트에 대해 완전히 다시 실행됩니다.
 * - `Graph.invoke`는 항상 Gradle의 구성 단계에서 실행됩니다(일반적으로 프로젝트당 1ms 미만이 소요됩니다).
 * 생성된 그래프는 `graph.ignoredProjects` 및 `graph.supportedConfigurations` 속성을 사용하여 구성할 수 있습니다.
 */
private class Graph(
    private val root: Project,
    private val dependencies: MutableMap<Project, Set<Pair<Configuration, Project>>> = mutableMapOf(),
    private val plugins: MutableMap<Project, PluginType> = mutableMapOf(),
    private val seen: MutableSet<String> = mutableSetOf(),
) {

    private val ignoredProjects = root.providers.gradleProperty("graph.ignoredProjects")
        .map { it.split(",").toSet() }
        .orElse(emptySet())
    private val supportedConfigurations =
        root.providers.gradleProperty("graph.supportedConfigurations")
            .map { it.split(",").toSet() }
            .orElse(setOf("api", "implementation", "baselineProfile", "testedApks"))

    operator fun invoke(project: Project = root): Graph {
        if (project.path in seen) return this
        seen += project.path
        plugins.putIfAbsent(
            project,
            PluginType.entries.firstOrNull { project.pluginManager.hasPlugin(it.id) } ?: PluginType.Unknown,
        )
        dependencies.compute(project) { _, u -> u.orEmpty() }
        project.configurations
            .matching { it.name in supportedConfigurations.get() }
            .associateWithNotNull { it.dependencies.withType<ProjectDependency>().ifEmpty { null } }
            .flatMap { (c, value) -> value.map { dep -> c to project.project(dep.path) } }
            .filter { (_, p) -> p.path !in ignoredProjects.get() }
            .forEach { (configuration: Configuration, projectDependency: Project) ->
                dependencies.compute(project) { _, u -> u.orEmpty() + (configuration to projectDependency) }
                invoke(projectDependency)
            }
        return this
    }

    fun dependencies(): Map<String, Set<Pair<String, String>>> = dependencies
        .mapKeys { it.key.path }
        .mapValues { it.value.mapTo(mutableSetOf()) { (c, p) -> c.name to p.path } }

    fun plugins() = plugins.mapKeys { it.key.path }
}

/**
 * 선언 순서가 중요
 */
internal enum class PluginType(val id: String, val ref: String, val style: String) {
    AndroidApplication(
        id = "yadanbeopseok.android.application",
        ref = "android-application",
        style = "fill:#CAFFBF,stroke:#000,stroke-width:2px,color:#000",
    ),
    AndroidFeature(
        id = "yadanbeopseok.android.feature",
        ref = "android-feature",
        style = "fill:#FFD6A5,stroke:#000,stroke-width:2px,color:#000",
    ),
    AndroidLibrary(
        id = "yadanbeopseok.android.library",
        ref = "android-library",
        style = "fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000",
    ),
    AndroidTest(
        id = "yadanbeopseok.android.test",
        ref = "android-test",
        style = "fill:#A0C4FF,stroke:#000,stroke-width:2px,color:#000",
    ),
    Jvm(
        id = "yadanbeopseok.jvm.library",
        ref = "jvm-library",
        style = "fill:#BDB2FF,stroke:#000,stroke-width:2px,color:#000",
    ),
    Unknown(
        id = "?",
        ref = "unknown",
        style = "fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000",
    ),
}

internal fun Project.configureGraphTasks() {
    if (!buildFile.exists()) return // Ignore root modules without build file
    val dumpTask = tasks.register<GraphDumpTask>("graphDump") {
        val graph = Graph(this@configureGraphTasks).invoke()
        projectPath.assign(this@configureGraphTasks.path)
        dependencies.assign(graph.dependencies())
        plugins.assign(graph.plugins())
        output.assign(this@configureGraphTasks.layout.buildDirectory.file("mermaid/graph.txt"))
        legend.assign(this@configureGraphTasks.layout.buildDirectory.file("mermaid/legend.txt"))
    }
    tasks.register<GraphUpdateTask>("graphUpdate") {
        projectPath.assign(this@configureGraphTasks.path)
        input.assign(dumpTask.flatMap { it.output })
        legend.assign(dumpTask.flatMap { it.legend })
        output.assign(this@configureGraphTasks.layout.projectDirectory.file("README.md"))
    }
}

@CacheableTask
private abstract class GraphDumpTask : DefaultTask() {

    @get:Input
    abstract val projectPath: Property<String>

    @get:Input
    abstract val dependencies: MapProperty<String, Set<Pair<String, String>>>

    @get:Input
    abstract val plugins: MapProperty<String, PluginType>

    @get:OutputFile
    abstract val output: RegularFileProperty

    @get:OutputFile
    abstract val legend: RegularFileProperty

    override fun getDescription() = "Dumps project dependencies to a mermaid file."

    @TaskAction
    operator fun invoke() {
        output.get().asFile.writeText(mermaid())
        legend.get().asFile.writeText(legend())
        logger.lifecycle(output.get().asFile.toPath().toUri().toString())
    }

    private fun mermaid() = buildString {
        val dependencies: Set<Dependency> = dependencies.get()
            .flatMapTo(mutableSetOf()) { (project, entries) -> entries.map { it.toDependency(project) } }
        // FrontMatter configuration (not supported yet on GitHub.com)
        appendLine(
            // language=YAML
            """
            ---
            config:
              layout: elk
              elk:
                nodePlacementStrategy: SIMPLE
            ---
            """.trimIndent(),
        )
        // Graph declaration
        appendLine("graph TB")
        // Nodes and subgraphs
        val (rootProjects, nestedProjects) = dependencies
            .map { listOf(it.project, it.dependency) }.flatten().toSet()
            .plus(projectPath.get()) // Special case when this specific module has no other dependency
            .groupBy { it.substringBeforeLast(":") }
            .entries.partition { it.key.isEmpty() }

        val orderedGroups = nestedProjects.groupBy {
            if (it.key.count { char -> char == ':' } > 1) it.key.substringBeforeLast(":") else ""
        }

        orderedGroups.forEach { (outerGroup, innerGroups) ->
            if (outerGroup.isNotEmpty()) {
                appendLine("  subgraph $outerGroup")
                appendLine("    direction TB")
            }
            innerGroups.sortedWith(
                compareBy(
                    { (group, _) ->
                        dependencies.filter { dep ->
                            val toGroup = dep.dependency.substringBeforeLast(":")
                            toGroup == group && dep.project.substringBeforeLast(":") != group
                        }.count()
                    },
                    { -it.value.size },
                ),
            ).forEach { (group, projects) ->
                val indent = if (outerGroup.isNotEmpty()) 4 else 2
                appendLine(" ".repeat(indent) + "subgraph $group")
                appendLine(" ".repeat(indent) + "  direction TB")
                projects.sorted().forEach {
                    appendLine(it.alias(indent = indent + 2, plugins.get().getValue(it)))
                }
                appendLine(" ".repeat(indent) + "end")
            }
            if (outerGroup.isNotEmpty()) {
                appendLine("  end")
            }
        }

        rootProjects.flatMap { it.value }.sortedDescending().forEach {
            appendLine(it.alias(indent = 2, plugins.get().getValue(it)))
        }
        // Links
        if (dependencies.isNotEmpty()) appendLine()
        dependencies
            .sortedWith(compareBy({ it.project }, { it.dependency }, { it.configuration }))
            .forEach { appendLine(it.link(indent = 2)) }
        // Classes
        appendLine()
        PluginType.entries.forEach { appendLine(it.classDef()) }
    }

    private fun legend() = buildString {
        appendLine("graph TB")
        listOf(
            "application" to PluginType.AndroidApplication,
            "feature" to PluginType.AndroidFeature,
            "library" to PluginType.AndroidLibrary,
            "jvm" to PluginType.Jvm,
        ).forEach { (name, type) ->
            appendLine(name.alias(indent = 2, type))
        }
        appendLine()
        listOf(
            Dependency("application", "implementation", "feature"),
            Dependency("library", "api", "jvm"),
        ).forEach {
            appendLine(it.link(indent = 2))
        }
        appendLine()
        PluginType.entries.forEach { appendLine(it.classDef()) }
    }

    private class Dependency(val project: String, val configuration: String, val dependency: String)

    private fun Pair<String, String>.toDependency(project: String) =
        Dependency(project, configuration = first, dependency = second)

    private fun String.alias(indent: Int, pluginType: PluginType): String = buildString {
        append(" ".repeat(indent))
        append(this@alias)
        append("[").append(substringAfterLast(":")).append("]:::")
        append(pluginType.ref)
    }

    private fun Dependency.link(indent: Int) = buildString {
        append(" ".repeat(indent))
        append(project).append(" ")
        append(
            when (configuration) {
                "api" -> "-->"
                "implementation" -> "-.->"
                else -> "-.->|$configuration|"
            },
        )
        append(" ").append(dependency)
    }

    private fun PluginType.classDef() = "classDef $ref $style;"
}

@CacheableTask
private abstract class GraphUpdateTask : DefaultTask() {

    @get:Input
    abstract val projectPath: Property<String>

    @get:InputFile
    @get:PathSensitive(NONE)
    abstract val input: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(NONE)
    abstract val legend: RegularFileProperty

    @get:OutputFile
    abstract val output: RegularFileProperty

    override fun getDescription() = "Updates Markdown file with the corresponding dependency graph."

    @TaskAction
    operator fun invoke() = with(output.get().asFile) {
        if (!exists()) {
            createNewFile()
            writeText(
                """
                # `${projectPath.get()}`

                ## Module dependency graph

                <!--region graph--> <!--endregion-->

                """.trimIndent(),
            )
        }
        val mermaid = input.get().asFile.readText().trimTrailingNewLines()
        val legend = legend.get().asFile.readText().trimTrailingNewLines()
        val regex = """(<!--region graph-->)(.*?)(<!--endregion-->)""".toRegex(DOT_MATCHES_ALL)
        val text = readText().replace(regex) { match ->
            val (start, _, end) = match.destructured
            """
            |$start
            |```mermaid
            |$mermaid
            |```
            |
            |<details><summary>📋 Graph legend</summary>
            |
            |```mermaid
            |$legend
            |```
            |
            |</details>
            |$end
            """.trimMargin()
        }
        writeText(text)
    }

    private fun String.trimTrailingNewLines() = lines()
        .dropLastWhile(String::isBlank)
        .joinToString(System.lineSeparator())
}
