package com.manruhomerun.yadanbeopseok.travel.creation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanSectionHeader
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanSectionMetaText
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.UserProfile
import com.manruhomerun.yadanbeopseok.travel.creation.viewmodel.MAX_COMPANION_COUNT
import com.manruhomerun.yadanbeopseok.travel.creation.viewmodel.TravelCompanionSelectionUiState
import com.manruhomerun.yadanbeopseok.ui.component.YadanUserListItem
import com.manruhomerun.yadanbeopseok.ui.component.YadanUserListItemStyle

/**
 * B·04 여행 만들기의 동행자 선택 화면입니다.
 *
 * 친구 목록에서 최대 2명을 선택할 수 있습니다.
 * 동행자를 선택하지 않고 혼자 여행하는 것도 허용합니다.
 */
@Composable
fun TravelCompanionSelectionScreen(
    uiState: TravelCompanionSelectionUiState,
    selectedCompanions: List<UserProfile>,
    onCompanionClick: (UserProfile) -> Unit,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TravelCreationScaffold(
        currentStep = TravelCreationStep.COMPANION_SELECTION,
        title = "누구와 함께 떠나요?",
        description = "친구 중 최대 ${MAX_COMPANION_COUNT}명까지 선택할 수 있어요. 동행자의 취향도 코스에 함께 반영돼요.",
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
                enabled = !uiState.isLoading,
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
        item(key = "companion_header") {
            YadanSectionHeader(
                title = "내 친구",
                trailingContent = {
                    YadanSectionMetaText(
                        text = "${selectedCompanions.size} / $MAX_COMPANION_COUNT 선택",
                    )
                },
            )
        }

        when {
            uiState.isLoading -> {
                item(key = "loading") {
                    CompanionSelectionLoadingContent()
                }
            }

            uiState.errorMessage != null -> {
                item(key = "error") {
                    CompanionSelectionErrorContent(
                        message = uiState.errorMessage,
                        onRetryClick = onRetryClick,
                    )
                }
            }

            uiState.friends.isEmpty() -> {
                item(key = "empty") {
                    CompanionSelectionEmptyContent()
                }
            }

            else -> {
                uiState.friends.forEach { friend ->
                    item(key = "friend_${friend.id}") {
                        val selected = selectedCompanions.any { companion ->
                            companion.id == friend.id
                        }
                        val enabled = selected || selectedCompanions.size < MAX_COMPANION_COUNT

                        YadanUserListItem(
                            user = friend,
                            style = YadanUserListItemStyle.SELECTION_CARD,
                            selected = selected,
                            onClick = {
                                onCompanionClick(friend)
                            },
                            enabled = enabled,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 친구 목록을 불러오는 동안 진행 상태를 표시합니다.
 */
@Composable
private fun CompanionSelectionLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 260.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = YadanPrimary)
    }
}

/**
 * 친구 목록 조회 실패 안내와 재시도 버튼을 표시합니다.
 */
@Composable
private fun CompanionSelectionErrorContent(
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
            text = "친구 목록을 확인할 수 없습니다",
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

/**
 * 선택할 수 있는 친구가 없을 때 안내 문구를 표시합니다.
 */
@Composable
private fun CompanionSelectionEmptyContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 260.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "함께할 친구가 아직 없습니다",
            style = YadanTypography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = YadanTextPrimary,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "동행자를 선택하지 않고 다음 단계로 이동할 수 있습니다.",
            modifier = Modifier.padding(top = 8.dp),
            style = YadanTypography.bodySmall,
            color = YadanTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(
    name = "B04 동행자 선택",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelCompanionSelectionScreenPreview() {
    val friends = listOf(
        UserProfile(
            id = "friend-1",
            nickname = "지우",
            favoriteTeam = KboTeam.LOTTE,
        ),
        UserProfile(
            id = "friend-2",
            nickname = "현수",
            favoriteTeam = KboTeam.KIA,
        ),
        UserProfile(
            id = "friend-3",
            nickname = "민지",
            favoriteTeam = KboTeam.SAMSUNG,
        ),
    )

    YadanbeopseokTheme {
        TravelCompanionSelectionScreen(
            uiState = TravelCompanionSelectionUiState(
                friends = friends,
            ),
            selectedCompanions = listOf(friends.first()),
            onCompanionClick = {},
            onBackClick = {},
            onNextClick = {},
            onRetryClick = {},
        )
    }
}
