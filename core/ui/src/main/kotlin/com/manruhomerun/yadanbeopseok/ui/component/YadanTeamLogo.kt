package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.ui.R

/**
 * KBO 구단의 공식 로고를 표시하는 공통 UI 컴포넌트입니다.
 *
 * 구단 모델과 Android drawable 리소스의 매핑을 이 컴포넌트에서 관리하여
 * 화면마다 동일한 매핑 코드를 반복하지 않도록 합니다.
 *
 * 서로 다른 종횡비를 가진 공식 로고가 잘리지 않도록 정사각형 영역 안에서
 * [ContentScale.Fit]으로 표시합니다.
 *
 * @param team 표시할 KBO 구단입니다.
 * @param contentDescription 로고의 접근성 설명입니다.
 * 구단명이 옆에 함께 표시된다면 중복 안내를 피하기 위해 null을 전달합니다.
 * 로고만 단독으로 표시한다면 "${team.fullName} 로고"와 같이 전달합니다.
 * @param modifier 로고의 배치나 추가 스타일을 지정하는 Modifier입니다.
 * @param size 로고가 배치되는 정사각형 영역의 크기입니다.
 * 기본값은 HTML의 일반 `.crest` 크기를 참고한 38dp입니다.
 */
@Composable
fun YadanTeamLogo(
    team: KboTeam,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
) {
    Image(
        painter = painterResource(id = team.logoResourceId),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        alignment = Alignment.Center,
        contentScale = ContentScale.Fit,
    )
}

/**
 * 순수 Kotlin 모델인 [KboTeam]을 Android drawable 리소스와 연결합니다.
 *
 * 이 매핑을 core/model에 넣으면 모델 모듈이 Android 리소스에 의존하게 되므로
 * 실제로 로고를 표시하는 core/ui에서 관리합니다.
 */
private val KboTeam.logoResourceId: Int
    get() =
        when (this) {
            KboTeam.LOTTE -> R.drawable.core_ui_lotte
            KboTeam.KIA -> R.drawable.core_ui_kia
            KboTeam.SAMSUNG -> R.drawable.core_ui_samsung
            KboTeam.LG -> R.drawable.core_ui_lg
            KboTeam.DOOSAN -> R.drawable.core_ui_doosan
            KboTeam.KIWOOM -> R.drawable.core_ui_kiwoom
            KboTeam.SSG -> R.drawable.core_ui_ssg
            KboTeam.NC -> R.drawable.core_ui_nc
            KboTeam.HANWHA -> R.drawable.core_ui_hanwha
            KboTeam.KT -> R.drawable.core_ui_kt
        }

@Preview(
    name = "Yadan team logos",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanTeamLogoPreview() {
    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            KboTeam.entries
                .chunked(2)
                .forEach { teams ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        teams.forEach { team ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                YadanTeamLogo(
                                    team = team,
                                    contentDescription = null,
                                )

                                Text(
                                    text = team.displayName,
                                    style = YadanTypography.labelMedium,
                                    color = YadanTextPrimary,
                                )
                            }
                        }
                    }
                }
        }
    }
}
