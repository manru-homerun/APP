package com.manruhomerun.yadanbeopseok.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.manruhomerun.yadanbeopseok.designsystem.R

/**
 * 야단법석 앱에서 사용하는 Pretendard 글꼴입니다.
 *
 * 화면에서 사용하는 굵기별 폰트 리소스를 하나의 FontFamily로 묶습니다.
 */
val PretendardFontFamily =
    FontFamily(
        Font(
            resId = R.font.pretendard_regular,
            weight = FontWeight.Normal,
        ),
        Font(
            resId = R.font.pretendard_medium,
            weight = FontWeight.Medium,
        ),
        Font(
            resId = R.font.pretendard_semibold,
            weight = FontWeight.SemiBold,
        ),
        Font(
            resId = R.font.pretendard_bold,
            weight = FontWeight.Bold,
        ),
        Font(
            resId = R.font.pretendard_extrabold,
            weight = FontWeight.ExtraBold,
        ),
    )

/**
 * 모든 텍스트 스타일에 공통으로 Pretendard와 자간 0을 적용합니다.
 */
private fun yadanTextStyle(
    fontWeight: FontWeight,
    fontSize: TextUnit,
    lineHeight: TextUnit,
): TextStyle =
    TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = fontWeight,
        fontSize = fontSize,
        lineHeight = lineHeight,
        letterSpacing = 0.sp,
    )

/**
 * HTML 디자인의 글자 크기와 굵기를 기준으로 구성한 앱 타이포그래피입니다.
 */
val YadanTypography =
    Typography(
        displayLarge = yadanTextStyle(FontWeight.ExtraBold, 34.sp, 42.sp),
        displayMedium = yadanTextStyle(FontWeight.ExtraBold, 30.sp, 38.sp),
        displaySmall = yadanTextStyle(FontWeight.ExtraBold, 28.sp, 36.sp),

        headlineLarge = yadanTextStyle(FontWeight.Bold, 26.sp, 34.sp),
        headlineMedium = yadanTextStyle(FontWeight.Bold, 24.sp, 32.sp),
        headlineSmall = yadanTextStyle(FontWeight.Bold, 22.sp, 30.sp),

        titleLarge = yadanTextStyle(FontWeight.Bold, 20.sp, 28.sp),
        titleMedium = yadanTextStyle(FontWeight.Bold, 18.sp, 26.sp),
        titleSmall = yadanTextStyle(FontWeight.SemiBold, 16.sp, 24.sp),

        bodyLarge = yadanTextStyle(FontWeight.Normal, 16.sp, 24.sp),
        bodyMedium = yadanTextStyle(FontWeight.Normal, 14.sp, 21.sp),
        bodySmall = yadanTextStyle(FontWeight.Normal, 12.sp, 18.sp),

        labelLarge = yadanTextStyle(FontWeight.Bold, 16.sp, 24.sp),
        labelMedium = yadanTextStyle(FontWeight.SemiBold, 13.sp, 18.sp),
        labelSmall = yadanTextStyle(FontWeight.Medium, 11.sp, 16.sp),
    )
