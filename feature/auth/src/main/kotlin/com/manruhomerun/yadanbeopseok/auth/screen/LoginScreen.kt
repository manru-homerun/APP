package com.manruhomerun.yadanbeopseok.auth.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanKakaoLoginButton
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 카카오 로그인을 시작하는 야단법석의 첫 화면입니다.
 *
 * 이 화면은 로그인 UI와 클릭 이벤트 전달만 담당합니다.
 * 카카오 SDK 호출과 백엔드 로그인 처리는 상위 Route와 ViewModel에서 처리합니다.
 *
 * @param onKakaoLoginClick 카카오 로그인 버튼을 눌렀을 때 실행할 작업입니다.
 * @param modifier 화면 크기와 배치를 지정할 Modifier입니다.
 * @param loginEnabled 중복 로그인 요청을 방지하기 위한 버튼 활성화 상태입니다.
 */
@Composable
fun LoginScreen(
    onKakaoLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    loginEnabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(YadanPrimary),
    ) {
        /*
         * HTML 배경에 흐리게 배치된 야구공과 야구장 장식입니다.
         * 화면 동작에는 관여하지 않으므로 접근성 설명은 제공하지 않습니다.
         */
        Icon(
            imageVector = Icons.Filled.SportsBaseball,
            contentDescription = null,
            modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(
                        x = 70.dp,
                        y = 40.dp,
                    )
                    .size(290.dp),
            tint = YadanOnPrimary.copy(alpha = 0.09f),
        )

        Icon(
            imageVector = Icons.Filled.Stadium,
            contentDescription = null,
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .offset(
                        x = (-40).dp,
                        y = (-150).dp,
                    )
                    .size(150.dp),
            tint = YadanOnPrimary.copy(alpha = 0.07f),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(
                        horizontal = 20.dp,
                        vertical = 24.dp,
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(
                modifier = Modifier.weight(1f),
            )

            Text(
                text = "직관에서 시작하는 여행",
                style =
                    YadanTypography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = YadanOnPrimary.copy(alpha = 0.78f),
                textAlign = TextAlign.Center,
            )

            Spacer(
                modifier = Modifier.height(16.dp),
            )

            Text(
                text = "야단\n법석",
                style =
                    YadanTypography.displayLarge.copy(
                        fontSize = 72.sp,
                        lineHeight = 68.sp,
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = YadanOnPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(
                modifier = Modifier.height(26.dp),
            )

            Canvas(
                modifier =
                    Modifier
                        .width(120.dp)
                        .height(2.dp),
            ) {
                drawLine(
                    color = YadanOnPrimary.copy(alpha = 0.55f),
                    start = Offset(x = 0f, y = size.height / 2f),
                    end =
                        Offset(
                            x = size.width,
                            y = size.height / 2f,
                        ),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        intervals =
                            floatArrayOf(
                                8.dp.toPx(),
                                6.dp.toPx(),
                            ),
                    ),
                )
            }

            Spacer(
                modifier = Modifier.height(26.dp),
            )

            Text(
                text =
                    "응원하는 팀의 경기를 고르면,\n" +
                        "구장 주변 여행 코스를 취향에 맞춰 짜드려요.",
                modifier = Modifier.width(290.dp),
                style =
                    YadanTypography.bodyLarge.copy(
                        fontSize = 15.sp,
                        lineHeight = 25.sp,
                    ),
                color = YadanOnPrimary.copy(alpha = 0.92f),
                textAlign = TextAlign.Center,
            )

            Spacer(
                modifier = Modifier.weight(1f),
            )

            YadanKakaoLoginButton(
                onClick = onKakaoLoginClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = loginEnabled,
            )
        }
    }
}

@Preview(
    name = "Login",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun LoginScreenPreview() {
    YadanbeopseokTheme {
        LoginScreen(
            onKakaoLoginClick = {},
        )
    }
}
