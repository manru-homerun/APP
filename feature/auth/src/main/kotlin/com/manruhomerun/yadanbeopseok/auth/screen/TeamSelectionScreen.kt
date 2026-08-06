package com.manruhomerun.yadanbeopseok.auth.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.ui.component.YadanTeamSelectionItem

/**
 * 신규 회원이 응원 구단을 선택하는 화면입니다.
 *
 * 구단 목록은 [KboTeam.entries]를 사용하고, 각 선택 항목은
 * 기존 [YadanTeamSelectionItem]을 재사용합니다.
 */
@Composable
fun TeamSelectionScreen(
    selectedTeam: KboTeam?,
    onTeamSelected: (KboTeam) -> Unit,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(YadanBackground)
                .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        YadanTopAppBar(
            title = "응원 구단",
            onNavigationClick = onBackClick,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 8.dp,
                        bottom = 26.dp,
                    ),
        ) {
            Text(
                text = "어느 팀을 응원하세요?",
                style =
                    YadanTypography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = YadanTextPrimary,
            )

            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text =
                    buildAnnotatedString {
                        append("홈·경기 일정을 ")

                        withStyle(
                            style =
                                SpanStyle(
                                    color = YadanPrimaryInk,
                                    fontWeight = FontWeight.ExtraBold,
                                ),
                        ) {
                            append("응원 구단 위주")
                        }

                        append("로 보여드려요.")
                    },
                style = YadanTypography.bodyMedium,
                color = YadanTextSecondary,
            )

            Spacer(modifier = Modifier.height(18.dp))

            /*
             * 화면 높이가 작거나 글자 크기가 커져도 구단 목록만
             * 스크롤되고 하단 버튼은 고정된 위치를 유지합니다.
             */
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .selectableGroup(),
                contentPadding =
                    PaddingValues(
                        bottom = 18.dp,
                    ),
                horizontalArrangement =
                    androidx.compose.foundation.layout.Arrangement
                        .spacedBy(10.dp),
                verticalArrangement =
                    androidx.compose.foundation.layout.Arrangement
                        .spacedBy(10.dp),
            ) {
                items(
                    items = KboTeam.entries,
                    key = KboTeam::name,
                ) { team ->
                    YadanTeamSelectionItem(
                        team = team,
                        selected = selectedTeam == team,
                        onClick = {
                            onTeamSelected(team)
                        },
                    )
                }
            }

            YadanButton(
                text = "다음",
                onClick = onNextClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedTeam != null,
                trailingIcon = {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )
        }
    }
}

@Preview(
    name = "Team selection",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TeamSelectionScreenPreview() {
    YadanbeopseokTheme {
        TeamSelectionScreen(
            selectedTeam = KboTeam.LOTTE,
            onTeamSelected = {},
            onBackClick = {},
            onNextClick = {},
        )
    }
}
