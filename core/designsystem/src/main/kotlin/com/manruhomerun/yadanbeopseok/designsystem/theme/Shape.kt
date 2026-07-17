package com.manruhomerun.yadanbeopseok.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 야단법석 화면에서 사용하는 공통 모서리 형태입니다.
 *
 * HTML 디자인에서 반복되는 모서리 반경의 비율을
 * Material3의 크기 단계에 맞게 구성합니다.
 */
val YadanShapes =
    Shapes(
        extraSmall = RoundedCornerShape(6.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(14.dp),
        large = RoundedCornerShape(18.dp),
        extraLarge = RoundedCornerShape(22.dp),
    )

/**
 * 필터, 선택 칩과 같이 양 끝이 둥근 컴포넌트에 사용합니다.
 *
 * HTML의 border-radius 999px 표현에 대응합니다.
 */
val YadanPillShape = RoundedCornerShape(percent = 50)
