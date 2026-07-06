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

import com.manruhomerun.yadanbeopseok.configureGraphTasks
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 모든 하위 모듈에 의존성 그래프 그리기 기능(Graph.kt의 기능들) 을 자동으로 장착해 주는 설치 기사
 */
class RootPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        require(target.path == ":")
        target.subprojects { configureGraphTasks() }
    }
}
