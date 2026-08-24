package com.manruhomerun.yadanbeopseok.travel.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCardStyle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanSectionHeader
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanSectionMetaText
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTintStrong
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.TravelTheme
import com.manruhomerun.yadanbeopseok.travel.viewmodel.TravelThemeIcon
import com.manruhomerun.yadanbeopseok.travel.viewmodel.TravelThemeItemUiState
import com.manruhomerun.yadanbeopseok.travel.viewmodel.TravelThemeSelectionUiState
import com.manruhomerun.yadanbeopseok.travel.viewmodel.toTravelThemeItemUiState


/**
 * B·03 여행 만들기의 여행 테마 선택 화면입니다.
 */
@Composable
fun TravelThemeSelectionScreen(
    uiState: TravelThemeSelectionUiState,
    selectedThemes: List<TravelTheme>,
    onThemeClick: (TravelTheme) -> Unit,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeItems = remember(uiState.themes) {
        uiState.themes.map { theme ->
            theme.toTravelThemeItemUiState()
        }
    }

    val isNextEnabled = selectedThemes.isNotEmpty() && !uiState.isLoading

    TravelCreationScaffold(
        currentStep = TravelCreationStep.THEME_SELECTION,
        title = "이번 여행, 어떤 테마인가요?",
        description = "주요 동기에 맞춰 코스 분위기가 달라져요. 1개에서 3개까지 골라주세요.",
        onNavigationClick = onBackClick,
        modifier = modifier,
        bottomBar = {
            YadanButton(
                text = "다음",
                onClick = onNextClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        top = 11.dp,
                        end = 24.dp,
                        bottom = 20.dp,
                    ),
                enabled = isNextEnabled,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )
        },
    ) {
        item(key = "theme_header") {
            YadanSectionHeader(
                title = "여행 테마",
                trailingContent = {
                    YadanSectionMetaText(text = "1~3개 선택")
                },
            )
        }

        when {
            uiState.isLoading -> {
                item(key = "loading") {
                    ThemeSelectionLoadingContent()
                }
            }

            uiState.errorMessage != null -> {
                item(key = "error") {
                    ThemeSelectionErrorContent(
                        message = uiState.errorMessage,
                        onRetryClick = onRetryClick,
                    )
                }
            }

            themeItems.isEmpty() -> {
                item(key = "empty") {
                    ThemeSelectionEmptyContent()
                }
            }

            else -> {
                themeItems.chunked(2).forEachIndexed { rowIndex, rowItems ->
                    item(key = "theme_row_$rowIndex") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowItems.forEach { item ->
                                key(item.theme.id) {
                                    ThemeSelectionCard(
                                        item = item,
                                        selected = selectedThemes.any { theme ->
                                            theme.id == item.theme.id
                                        },
                                        onClick = {
                                            onThemeClick(item.theme)
                                        },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }

                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 여행 테마의 아이콘, 이름과 선택 상태를 표시합니다.
 */
@Composable
private fun ThemeSelectionCard(
    item: TravelThemeItemUiState,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardStyle = if (selected) {
        YadanCardStyle.SELECTED
    } else {
        YadanCardStyle.DEFAULT
    }

    val iconContainerColor = if (selected) {
        YadanPrimary
    } else {
        YadanPrimaryTintStrong
    }

    val iconColor = if (selected) {
        YadanOnPrimary
    } else {
        YadanPrimaryInk
    }

    YadanCard(
        onClick = onClick,
        modifier = modifier.semantics {
            this.selected = selected
        },
        style = cardStyle,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(
                    horizontal = 10.dp,
                    vertical = 8.dp,
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = iconContainerColor,
                        shape = MaterialTheme.shapes.medium,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = item.icon.imageVector,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = iconColor,
                )
            }

            Text(
                text = item.theme.name,
                modifier = Modifier.weight(1f),
                style = YadanTypography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = YadanTextPrimary,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ThemeSelectionLoadingContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 260.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = YadanPrimary)
    }
}

@Composable
private fun ThemeSelectionErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 260.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "여행 테마를 확인할 수 없습니다",
            style = YadanTypography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = YadanTextPrimary,
            textAlign = TextAlign.Center,
        )

        Text(
            text = message,
            modifier = Modifier.padding(top = 8.dp),
            style = YadanTypography.bodySmall,
            color = YadanTextSecondary,
            textAlign = TextAlign.Center,
        )

        YadanButton(
            text = "다시 시도",
            onClick = onRetryClick,
            modifier = Modifier
                .padding(top = 20.dp)
                .widthIn(min = 120.dp),
        )
    }
}

@Composable
private fun ThemeSelectionEmptyContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 260.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "선택할 수 있는 여행 테마가 없습니다.",
            style = YadanTypography.bodyMedium,
            color = YadanTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

private val TravelThemeIcon.imageVector: ImageVector
    get() = when (this) {
        TravelThemeIcon.ESCAPE -> Icons.Outlined.Flight
        TravelThemeIcon.RELAXATION -> Icons.Outlined.Spa
        TravelThemeIcon.MEMORY -> Icons.Outlined.FavoriteBorder
        TravelThemeIcon.REFLECTION -> Icons.Outlined.SelfImprovement
        TravelThemeIcon.PHOTOGRAPHY -> Icons.Outlined.PhotoCamera
        TravelThemeIcon.ACTIVITY -> Icons.Outlined.Bolt
        TravelThemeIcon.EXCITEMENT -> Icons.Outlined.AutoAwesome
        TravelThemeIcon.CULTURE -> Icons.Outlined.AccountBalance
        TravelThemeIcon.CELEBRATION -> Icons.Outlined.Celebration
        TravelThemeIcon.SPONTANEOUS -> Icons.Outlined.Explore
        TravelThemeIcon.GENERAL -> Icons.Outlined.AutoAwesome
    }

@Preview(
    name = "B03 여행 테마",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelThemeSelectionScreenPreview() {
    val themes = previewTravelThemes

    YadanbeopseokTheme {
        TravelThemeSelectionScreen(
            uiState = TravelThemeSelectionUiState(themes = themes),
            selectedThemes = listOf(themes[2]),
            onThemeClick = {},
            onBackClick = {},
            onNextClick = {},
            onRetryClick = {},
        )
    }
}

private val previewTravelThemes = listOf(
    TravelTheme(id = "1", name = "지루한 일상 탈출"),
    TravelTheme(id = "2", name = "피로를 푸는 휴식"),
    TravelTheme(id = "3", name = "소중한 사람과 추억"),
    TravelTheme(id = "4", name = "나를 돌아보는 시간"),
    TravelTheme(id = "5", name = "남는 건 사진 뿐"),
    TravelTheme(id = "6", name = "액티비티로 활력 충전"),
    TravelTheme(id = "7", name = "낯선 곳의 설렘"),
    TravelTheme(id = "8", name = "역사·문화 탐방"),
    TravelTheme(id = "9", name = "특별한 기념일"),
    TravelTheme(id = "10", name = "발길이 이끄는 대로"),
)
