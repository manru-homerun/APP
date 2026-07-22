package com.manruhomerun.yadanbeopseok.ui.theme

import androidx.compose.ui.graphics.Color
import com.manruhomerun.yadanbeopseok.model.KboTeam

/**
 * KBO 구단별 대표 상징색을 반환합니다.
 *
 * 구단명, 구단 칩, 프로필 및 경기 카드 등 구단을 시각적으로
 * 구분해야 하는 공통 UI에서 재사용합니다.
 *
 * core/model은 Compose에 의존하지 않아야 하므로
 * 구단과 UI 색상의 연결은 core/ui에서 관리합니다.
 */
val KboTeam.teamColor: Color
    get() =
        when (this) {
            KboTeam.LOTTE ->
                Color(0xFF041E42)

            KboTeam.KIA ->
                Color(0xFFEA0029)

            KboTeam.SAMSUNG ->
                Color(0xFF074CA1)

            KboTeam.LG ->
                Color(0xFFC30452)

            KboTeam.DOOSAN ->
                Color(0xFF1A1748)

            KboTeam.KIWOOM ->
                Color(0xFF570514)

            KboTeam.SSG ->
                Color(0xFFCE0E2D)

            KboTeam.NC ->
                Color(0xFF315288)

            KboTeam.HANWHA ->
                Color(0xFFFC4E00)

            KboTeam.KT ->
                Color(0xFFED2024)
        }
