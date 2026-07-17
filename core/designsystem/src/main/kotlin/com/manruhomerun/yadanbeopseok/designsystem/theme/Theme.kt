package com.manruhomerun.yadanbeopseok.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 야단법석의 고정 라이트 색상 체계입니다.
 *
 * 시스템 테마나 동적 색상의 영향을 받지 않고
 * HTML 디자인의 스카이 블루와 오프 화이트 색상을 유지합니다.
 */
private val YadanLightColorScheme =
    lightColorScheme(
        primary = YadanPrimary,
        onPrimary = YadanOnPrimary,
        primaryContainer = YadanPrimaryTint,
        onPrimaryContainer = YadanPrimaryInk,
        inversePrimary = YadanPrimaryTintStrong,

        secondary = YadanPrimaryDark,
        onSecondary = YadanOnPrimary,
        secondaryContainer = YadanPrimaryTintStrong,
        onSecondaryContainer = YadanPrimaryInk,

        tertiary = YadanTextSecondary,
        onTertiary = YadanSurface,
        tertiaryContainer = YadanDivider,
        onTertiaryContainer = YadanTextPrimary,

        background = YadanBackground,
        onBackground = YadanTextPrimary,

        surface = YadanSurface,
        onSurface = YadanTextPrimary,
        surfaceVariant = YadanDivider,
        onSurfaceVariant = YadanTextSecondary,
        surfaceTint = YadanPrimary,

        inverseSurface = YadanTextPrimary,
        inverseOnSurface = YadanSurface,

        error = YadanError,
        onError = YadanOnError,
        errorContainer = YadanError.copy(alpha = 0.12f),
        onErrorContainer = YadanError,

        outline = YadanOutline,
        outlineVariant = YadanDivider,
        scrim = Color.Black,

        surfaceBright = YadanSurface,
        surfaceContainerLowest = YadanSurface,
        surfaceContainerLow = YadanBackground,
        surfaceContainer = YadanBackground,
        surfaceContainerHigh = YadanDivider,
        surfaceContainerHighest = YadanOutline,
        surfaceDim = YadanDivider,

        primaryFixed = YadanPrimaryTint,
        primaryFixedDim = YadanPrimaryTintStrong,
        onPrimaryFixed = YadanPrimaryInk,
        onPrimaryFixedVariant = YadanPrimaryDark,

        secondaryFixed = YadanPrimaryTint,
        secondaryFixedDim = YadanPrimaryTintStrong,
        onSecondaryFixed = YadanPrimaryInk,
        onSecondaryFixedVariant = YadanPrimaryDark,

        tertiaryFixed = YadanDivider,
        tertiaryFixedDim = YadanOutline,
        onTertiaryFixed = YadanTextPrimary,
        onTertiaryFixedVariant = YadanTextSecondary,
    )

/**
 * 야단법석 앱 전체에 공통 디자인 토큰을 제공합니다.
 *
 * MaterialTheme는 색상과 타이포그래피를 전달하는 기반으로만 사용하며,
 * 실제 컴포넌트의 외형은 HTML 디자인에 맞게 별도로 구현합니다.
 */
@Composable
fun YadanbeopseokTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = YadanLightColorScheme,
        typography = YadanTypography,
        shapes = YadanShapes,
        content = content,
    )
}
