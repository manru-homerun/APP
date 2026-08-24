package com.manruhomerun.yadanbeopseok.travel.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCardStyle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelStepIndicator

/**
 * B·01부터 B·06까지 여행 만들기 화면에서 공통으로 사용하는 화면 골격입니다.
 *
 * 상단 앱 바, 진행 단계, 화면 제목과 설명, 스크롤 본문 및 하단 버튼 영역을
 * 공통으로 구성합니다. 각 단계 화면은 [content]와 [bottomBar]만 전달합니다.
 */
@Composable
internal fun TravelCreationScaffold(
    currentStep: TravelCreationStep,
    title: String,
    description: String?,
    onNavigationClick: () -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    val isFirstStep = currentStep == TravelCreationStep.GAME_SELECTION

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        YadanTopAppBar(
            title = "여행 만들기",
            onNavigationClick = onNavigationClick,
            navigationContentDescription = if (isFirstStep) {
                "여행 만들기 닫기"
            } else {
                "이전 단계로 돌아가기"
            },
            navigationIcon = if (isFirstStep) {
                {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "여행 만들기 닫기",
                    )
                }
            } else {
                null
            },
        )

        YadanTravelStepIndicator(
            currentStepIndex = currentStep.ordinal,
            stepNames = travelCreationStepNames,
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                start = 24.dp,
                top = 6.dp,
                end = 24.dp,
                bottom = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(key = "travel_creation_header") {
                TravelCreationHeader(
                    title = title,
                    description = description,
                )
            }

            content()
        }

        bottomBar()
    }
}

/**
 * 여행 만들기 단계 화면의 제목과 안내 문구를 표시합니다.
 */
@Composable
private fun TravelCreationHeader(
    title: String,
    description: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        Text(
            text = title,
            style = YadanTypography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
            ),
            color = YadanTextPrimary,
        )

        if (!description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = YadanTypography.bodyMedium,
                color = YadanTextSecondary,
            )
        }
    }
}

/**
 * 여행 만들기의 단계와 표시 순서입니다.
 */
internal enum class TravelCreationStep(
    val label: String,
) {
    GAME_SELECTION("경기 선택"),
    COMPANION_CONDITION("동행 조건"),
    THEME_SELECTION("목적·스타일"),
    COMPANION_SELECTION("동행자"),
    DATE_SELECTION("여행 기간"),
    SPOT_SELECTION("관광지 담기"),
}

private val travelCreationStepNames =
    TravelCreationStep.entries.map { step -> step.label }


@Preview(
    name = "B02 여행 만들기 공통 구조",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelCreationScaffoldPreview() {
    YadanbeopseokTheme {
        TravelCreationScaffold(
            currentStep = TravelCreationStep.COMPANION_CONDITION,
            title = "배려가 필요한 동행이 있나요?",
            description = "선택하면 베리어프리 정보로 동선을 맞춰드려요. 복수 선택할 수 있어요.",
            onNavigationClick = {},
            bottomBar = {
                YadanButton(
                    text = "다음",
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 24.dp,
                            top = 11.dp,
                            end = 24.dp,
                            bottom = 20.dp,
                        ),
                )
            },
        ) {
            item(key = "child") {
                TravelCreationPreviewCard(
                    text = "아이 동반",
                    selected = false,
                )
            }

            item(key = "elderly") {
                TravelCreationPreviewCard(
                    text = "어르신 동반",
                    selected = true,
                )
            }

            item(key = "wheelchair") {
                TravelCreationPreviewCard(
                    text = "휠체어 이용",
                    selected = false,
                )
            }
        }
    }
}

@Composable
private fun TravelCreationPreviewCard(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    YadanCard(
        modifier = modifier.fillMaxWidth(),
        style = if (selected) {
            YadanCardStyle.SELECTED
        } else {
            YadanCardStyle.DEFAULT
        },
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = YadanTypography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = YadanTextPrimary,
        )
    }
}
