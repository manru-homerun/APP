package com.manruhomerun.yadanbeopseok.designsystem.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 여행, 추천 결과, 여행 기록의 히어로 영역에 사용하는
 * 야단법석 기본 그라데이션입니다.
 *
 * HTML의 primary에서 primary-d로 이어지는 대각선 배경에 대응합니다.
 */
val YadanPrimaryGradient =
    Brush.linearGradient(
        colors =
            listOf(
                YadanPrimary,
                YadanPrimaryDark,
            ),
    )

/**
 * 히어로 영역 위에 표시하는 대각선 형태의 흰색 광택입니다.
 *
 * HTML에서는 처음 40% 구간에서 투명하게 사라집니다.
 */
val YadanHeroHighlightGradient =
    Brush.linearGradient(
        0.0f to Color.White.copy(alpha = 0.13f),
        0.4f to Color.Transparent,
        1.0f to Color.Transparent,
    )

/**
 * 히어로 영역 우측 상단에 표시하는 원형 빛 효과입니다.
 */
val YadanHeroGlowGradient =
    Brush.radialGradient(
        0.0f to Color.White.copy(alpha = 0.20f),
        0.68f to Color.Transparent,
        1.0f to Color.Transparent,
    )

/**
 * 이미지 위의 흰색 텍스트와 아이콘이 잘 보이도록
 * 이미지 상단에 표시하는 어두운 그라데이션입니다.
 */
val YadanImageTopScrimGradient =
    Brush.verticalGradient(
        colors =
            listOf(
                Color.Black.copy(alpha = 0.30f),
                Color.Transparent,
            ),
    )

/**
 * 스크롤 콘텐츠와 하단 고정 버튼 영역을 자연스럽게 연결하는
 * 오프 화이트 배경 그라데이션입니다.
 */
val YadanBottomFadeGradient =
    Brush.verticalGradient(
        0.0f to Color.Transparent,
        0.3f to YadanBackground,
        1.0f to YadanBackground,
    )
