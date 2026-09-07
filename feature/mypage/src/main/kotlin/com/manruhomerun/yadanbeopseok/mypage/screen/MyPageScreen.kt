package com.manruhomerun.yadanbeopseok.mypage.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanMainHeader
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDivider
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanError
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.LoginProvider
import com.manruhomerun.yadanbeopseok.model.UserProfile
import com.manruhomerun.yadanbeopseok.mypage.viewmodel.MyPageAction
import com.manruhomerun.yadanbeopseok.mypage.viewmodel.MyPageUiState
import com.manruhomerun.yadanbeopseok.ui.component.YadanProfileCard
import com.manruhomerun.yadanbeopseok.ui.component.YadanSettingItem

/**
 * H·01 마이 화면입니다.
 *
 * HTML의 프로필 카드, 여행 메뉴와 계정 작업을 표시합니다.
 * 출시 정책 확인을 위한 이용약관과 개인정보 처리방침 메뉴도 제공합니다.
 */
@Composable
fun MyPageScreen(
    uiState: MyPageUiState,
    onRetryClick: () -> Unit,
    onProfileClick: () -> Unit,
    onDibsClick: () -> Unit,
    onTravelPreferenceClick: () -> Unit,
    onFriendsClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onWithdrawalClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isWithdrawalDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground),
    ) {
        YadanMainHeader(
            title = "마이",
            modifier = Modifier.statusBarsPadding(),
        )

        val userProfile = uiState.userProfile

        when {
            uiState.isLoading -> {
                MyPageLoadingContent(
                    modifier = Modifier.weight(1f),
                )
            }

            userProfile == null -> {
                MyPageErrorContent(
                    message = uiState.errorMessage
                        ?: "마이페이지 정보를 불러오지 못했습니다.",
                    onRetryClick = onRetryClick,
                    modifier = Modifier.weight(1f),
                )
            }

            else -> {
                MyPageContent(
                    userProfile = userProfile,
                    travelPreferenceSummary = uiState.travelPreferenceSummary,
                    uiState = uiState,
                    onProfileClick = onProfileClick,
                    onDibsClick = onDibsClick,
                    onTravelPreferenceClick = onTravelPreferenceClick,
                    onFriendsClick = onFriendsClick,
                    onTermsClick = onTermsClick,
                    onPrivacyPolicyClick = onPrivacyPolicyClick,
                    onLogoutClick = onLogoutClick,
                    onWithdrawalClick = {
                        isWithdrawalDialogVisible = true
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    if (isWithdrawalDialogVisible) {
        WithdrawalConfirmationDialog(
            isProcessing = uiState.processingAction == MyPageAction.WITHDRAWAL,
            onDismiss = {
                if (!uiState.isProcessing) {
                    isWithdrawalDialogVisible = false
                }
            },
            onConfirm = onWithdrawalClick,
        )
    }
}

/**
 * 사용자 정보를 처음 불러오는 동안 진행 상태를 표시합니다.
 */
@Composable
private fun MyPageLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = YadanPrimary)
    }
}

/**
 * 사용자 정보 조회 실패 문구와 재시도 버튼을 표시합니다.
 */
@Composable
private fun MyPageErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 280.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "마이페이지 정보를 확인할 수 없습니다",
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
 * H·01의 스크롤 가능한 본문을 표시합니다.
 */
@Composable
private fun MyPageContent(
    userProfile: UserProfile,
    travelPreferenceSummary: String?,
    uiState: MyPageUiState,
    onProfileClick: () -> Unit,
    onDibsClick: () -> Unit,
    onTravelPreferenceClick: () -> Unit,
    onFriendsClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onWithdrawalClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentEnabled = !uiState.isProcessing

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 4.dp,
            end = 20.dp,
            bottom = 24.dp,
        ),
    ) {
        item(key = "profile") {
            YadanProfileCard(
                user = userProfile,
                onClick = onProfileClick,
                enabled = false,
            )
        }

        item(key = "travel-label") {
            MyPageSectionLabel(
                text = "여행",
                modifier = Modifier.padding(
                    start = 2.dp,
                    top = 20.dp,
                    bottom = 9.dp,
                ),
            )
        }

        item(key = "travel-menu") {
            MyPageTravelMenu(
                travelPreferenceSummary = travelPreferenceSummary,
                enabled = contentEnabled,
                onDibsClick = onDibsClick,
                onTravelPreferenceClick = onTravelPreferenceClick,
                onFriendsClick = onFriendsClick,
            )
        }

        item(key = "service-label") {
            MyPageSectionLabel(
                text = "서비스",
                modifier = Modifier.padding(
                    start = 2.dp,
                    top = 20.dp,
                    bottom = 9.dp,
                ),
            )
        }

        item(key = "service-menu") {
            MyPagePolicyMenu(
                enabled = contentEnabled,
                onTermsClick = onTermsClick,
                onPrivacyPolicyClick = onPrivacyPolicyClick,
            )
        }

        item(key = "account-actions") {
            MyPageAccountActions(
                uiState = uiState,
                onLogoutClick = onLogoutClick,
                onWithdrawalClick = onWithdrawalClick,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

/**
 * 마이 화면의 메뉴 그룹 이름을 표시합니다.
 */
@Composable
private fun MyPageSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = YadanTypography.labelMedium.copy(
            fontWeight = FontWeight.ExtraBold,
        ),
        color = YadanTextMuted,
    )
}

/**
 * HTML의 여행 메뉴 카드입니다.
 */
@Composable
private fun MyPageTravelMenu(
    travelPreferenceSummary: String?,
    enabled: Boolean,
    onDibsClick: () -> Unit,
    onTravelPreferenceClick: () -> Unit,
    onFriendsClick: () -> Unit,
) {
    YadanCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        YadanSettingItem(
            title = "찜한 관광지",
            supportingText = "저장한 관광지 모아보기",
            onClick = onDibsClick,
            enabled = enabled,
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                )
            },
        )

        MyPageMenuDivider()

        YadanSettingItem(
            title = "내 여행 취향",
            supportingText = travelPreferenceSummary,
            onClick = onTravelPreferenceClick,
            enabled = false,
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                )
            },
        )

        MyPageMenuDivider()

        YadanSettingItem(
            title = "친구",
            supportingText = "동행 초대할 야구 친구 관리",
            onClick = onFriendsClick,
            enabled = false,
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                )
            },
        )
    }
}

/**
 * 이용약관과 개인정보 처리방침 메뉴를 표시합니다.
 */
@Composable
private fun MyPagePolicyMenu(
    enabled: Boolean,
    onTermsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
) {
    YadanCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        YadanSettingItem(
            title = "이용약관",
            onClick = onTermsClick,
            enabled = enabled,
        )

        MyPageMenuDivider()

        YadanSettingItem(
            title = "개인정보 처리방침",
            onClick = onPrivacyPolicyClick,
            enabled = enabled,
        )
    }
}

/**
 * 같은 메뉴 카드 안의 항목을 구분합니다.
 */
@Composable
private fun MyPageMenuDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = YadanDivider,
    )
}

/**
 * 로그아웃과 회원 탈퇴 작업을 표시합니다.
 */
@Composable
private fun MyPageAccountActions(
    uiState: MyPageUiState,
    onLogoutClick: () -> Unit,
    onWithdrawalClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AccountActionButton(
            text = "로그아웃",
            isProcessing = uiState.processingAction == MyPageAction.LOGOUT,
            enabled = !uiState.isProcessing,
            contentColor = YadanTextMuted,
            onClick = onLogoutClick,
        )

        Text(
            text = "·",
            style = YadanTypography.bodyMedium,
            color = YadanTextMuted,
        )

        AccountActionButton(
            text = "회원 탈퇴",
            isProcessing = uiState.processingAction == MyPageAction.WITHDRAWAL,
            enabled = !uiState.isProcessing,
            contentColor = YadanError,
            onClick = onWithdrawalClick,
        )
    }
}

/**
 * 계정 작업의 진행 상태를 함께 표시하는 텍스트 버튼입니다.
 */
@Composable
private fun AccountActionButton(
    text: String,
    isProcessing: Boolean,
    enabled: Boolean,
    contentColor: Color,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.widthIn(min = 96.dp),
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = contentColor,
            disabledContentColor = contentColor.copy(alpha = 0.45f),
        ),
    ) {
        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = contentColor,
                strokeWidth = 2.dp,
            )

            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = text,
            style = YadanTypography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

/**
 * 회원 탈퇴 실행 전 복구할 수 없는 작업임을 확인합니다.
 */
@Composable
private fun WithdrawalConfirmationDialog(
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "회원 탈퇴",
                style = YadanTypography.titleMedium,
                color = YadanTextPrimary,
            )
        },
        text = {
            Text(
                text = "회원 탈퇴 시 계정과 서비스 데이터가 삭제되며 되돌릴 수 없습니다. 계속하시겠어요?",
                style = YadanTypography.bodyMedium,
                color = YadanTextMuted,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isProcessing,
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = YadanError,
                        strokeWidth = 2.dp,
                    )

                    Spacer(modifier = Modifier.width(6.dp))
                }

                Text(
                    text = "탈퇴",
                    color = YadanError,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isProcessing,
            ) {
                Text(text = "취소")
            }
        },
    )
}

private val previewUserProfile = UserProfile(
    id = "1",
    provider = LoginProvider.KAKAO,
    providerUserId = "kakao-preview",
    nickname = "준호",
    favoriteTeam = KboTeam.LOTTE,
)

@Preview(
    name = "H01 마이",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 760,
)
@Composable
private fun MyPageScreenPreview() {
    YadanbeopseokTheme {
        MyPageScreen(
            uiState = MyPageUiState(
                userProfile = previewUserProfile,
                travelPreferenceSummary = "부산 거주 · 자연 선호 · 선호 지역 2곳",
                isLoading = false,
            ),
            onRetryClick = {},
            onProfileClick = {},
            onDibsClick = {},
            onTravelPreferenceClick = {},
            onFriendsClick = {},
            onTermsClick = {},
            onPrivacyPolicyClick = {},
            onLogoutClick = {},
            onWithdrawalClick = {},
        )
    }
}

@Preview(
    name = "H01 계정 처리 중",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 760,
)
@Composable
private fun MyPageScreenLoadingPreview() {
    YadanbeopseokTheme {
        MyPageScreen(
            uiState = MyPageUiState(
                userProfile = previewUserProfile,
                travelPreferenceSummary = "부산 거주 · 자연 선호 · 선호 지역 2곳",
                isLoading = false,
                processingAction = MyPageAction.LOGOUT,
            ),
            onRetryClick = {},
            onProfileClick = {},
            onDibsClick = {},
            onTravelPreferenceClick = {},
            onFriendsClick = {},
            onTermsClick = {},
            onPrivacyPolicyClick = {},
            onLogoutClick = {},
            onWithdrawalClick = {},
        )
    }
}
