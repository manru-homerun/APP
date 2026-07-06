/*
 * Copyright 2023 The Android Open Source Project
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.manruhomerun.yadanbeopseok

import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.Project
import kotlin.io.resolve

/**
 * `androidTest` 폴더가 없는 경우 [project]에 대한 불필요한 Android 계측 테스트를 비활성화합니다.
 * 그렇지 않으면 이러한 프로젝트는 컴파일, 패키징, 설치 및 실행되지만 결국 다음과 같은 메시지가 표시됩니다.
 * > AVD에서 0개의 테스트 시작
 * 참고: 빌드 유형 및 플레이버에 따라 다른 잠재적 소스 세트를 확인하여 이 기능을 개선할 수 있습니다.
 *
 *  테스트 코드가 하나도 없는 모듈이어도 작업 수행
 *  1.테스트용 APK 컴파일 및 패키징
 *  2.에뮬레이터나 실기기에 설치
 *  3.테스트 실행 시도 (결과는 항상 Starting 0 tests on AVD)
 */

// 동작 시점: 안드로이드 빌드 시스템이 각 변체(Variants, 예: debug/release)를 구성하기 직전(beforeVariants)에 실행됩니다.
internal fun LibraryAndroidComponentsExtension.disableUnnecessaryAndroidTests(
    project: Project,
) = beforeVariants {
    it.androidTest.enable = it.androidTest.enable
        && project.projectDir.resolve("src/androidTest").exists()
}
