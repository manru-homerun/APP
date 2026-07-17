package com.manruhomerun.yadanbeopseok.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.R
import com.manruhomerun.yadanbeopseok.designsystem.theme.KakaoContent
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 카카오 공식 로그인 이미지를 사용하는 버튼입니다.
 *
 * 버튼의 배경, 아이콘 및 문구는 공식 PNG에 포함되어 있으므로
 * 별도의 색상이나 텍스트 스타일을 적용하지 않습니다.
 *
 * @param onClick 버튼을 눌렀을 때 실행할 작업입니다.
 * @param modifier 버튼의 크기와 배치를 지정할 Modifier입니다.
 * @param enabled 버튼 활성화 여부입니다.
 */
@Composable
fun YadanKakaoLoginButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val buttonAlpha =
        if (enabled) {
            ENABLED_ALPHA
        } else {
            DISABLED_ALPHA
        }

    Image(
        painter =
            painterResource(
                id = R.drawable.kakao_login_large_wide,
            ),
        contentDescription = "카카오 로그인",
        contentScale = ContentScale.Fit,
        modifier =
            modifier
                // 공식 이미지의 600:90 비율을 유지합니다.
                .aspectRatio(KAKAO_LOGIN_BUTTON_ASPECT_RATIO)
                .alpha(buttonAlpha)
                /*
                 * 공식 이미지의 모서리 비율에 맞춰
                 * 클릭 리플이 이미지 밖으로 퍼지지 않게 합니다.
                 */
                .clip(KakaoLoginButtonShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication =
                        ripple(
                            bounded = true,
                            color = KakaoContent,
                        ),
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                ),
    )
}

private const val KAKAO_LOGIN_BUTTON_ASPECT_RATIO = 600f / 90f
private const val KAKAO_BUTTON_CORNER_PERCENT = 12
private const val ENABLED_ALPHA = 1f
private const val DISABLED_ALPHA = 0.42f

private val KakaoLoginButtonShape =
    RoundedCornerShape(
        percent = KAKAO_BUTTON_CORNER_PERCENT,
    )

@Preview(
    name = "Yadan Kakao login button",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
)
@Composable
private fun YadanKakaoLoginButtonPreview() {
    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            YadanKakaoLoginButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )

            YadanKakaoLoginButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
            )
        }
    }
}
