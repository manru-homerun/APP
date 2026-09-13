package com.manruhomerun.yadanbeopseok.mypage.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTextField
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDivider
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanError
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.Gender
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.NICKNAME_MAX_LENGTH
import com.manruhomerun.yadanbeopseok.model.UserProfile
import com.manruhomerun.yadanbeopseok.mypage.viewmodel.ProfileEditUiState
import com.manruhomerun.yadanbeopseok.mypage.viewmodel.ProfileNicknameState
import com.manruhomerun.yadanbeopseok.ui.component.YadanTeamSelectionItem
import com.manruhomerun.yadanbeopseok.ui.component.YadanTeamSelectionItemStyle
import com.manruhomerun.yadanbeopseok.ui.component.YadanUserAvatar
import com.manruhomerun.yadanbeopseok.ui.component.YadanUserAvatarSize
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import kotlinx.datetime.todayIn

/** H·02 프로필 수정 화면입니다. */
@Composable
fun ProfileEditScreen(
    uiState: ProfileEditUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onNicknameChange: (String) -> Unit,
    onNicknameCheckRetry: () -> Unit,
    onTeamSelected: (KboTeam) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground),
    ) {
        YadanTopAppBar(
            title = "프로필 수정",
            onNavigationClick = {
                if (!uiState.isSaving) {
                    onBackClick()
                }
            },
            modifier = Modifier.statusBarsPadding(),
        )

        when {
            uiState.isLoading -> {
                ProfileEditLoadingContent(
                    modifier = Modifier.weight(1f),
                )
            }

            uiState.profile == null -> {
                ProfileEditErrorContent(
                    message = uiState.errorMessage
                        ?: "프로필을 불러오지 못했습니다.",
                    onRetryClick = onRetryClick,
                    modifier = Modifier.weight(1f),
                )
            }

            else -> {
                ProfileEditContent(
                    uiState = uiState,
                    onNicknameChange = onNicknameChange,
                    onNicknameCheckRetry = onNicknameCheckRetry,
                    onTeamSelected = onTeamSelected,
                    onSaveClick = onSaveClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** 프로필 정보를 처음 불러오는 동안 진행 상태를 표시합니다. */
@Composable
private fun ProfileEditLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = YadanPrimary)
    }
}

/** 프로필 조회 실패 문구와 재시도 버튼을 표시합니다. */
@Composable
private fun ProfileEditErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "프로필 정보를 확인할 수 없습니다",
            style = YadanTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
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
                .fillMaxWidth(),
        )
    }
}

/** 편집 가능한 프로필 내용과 하단 저장 버튼을 표시합니다. */
@Composable
private fun ProfileEditContent(
    uiState: ProfileEditUiState,
    onNicknameChange: (String) -> Unit,
    onNicknameCheckRetry: () -> Unit,
    onTeamSelected: (KboTeam) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = checkNotNull(uiState.profile)
    val currentDate = remember {
        Clock.System.todayIn(TimeZone.currentSystemDefault())
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            YadanUserAvatar(
                user = profile.copy(
                    nickname = uiState.normalizedNickname,
                    favoriteTeam = uiState.selectedTeam,
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                size = YadanUserAvatarSize.LARGE,
            )

            ProfileEditSectionLabel(
                text = "닉네임",
                trailingText = "${uiState.nicknameLength} / $NICKNAME_MAX_LENGTH",
                modifier = Modifier.padding(top = 24.dp),
            )

            YadanTextField(
                value = uiState.nickname,
                onValueChange = onNicknameChange,
                modifier = Modifier.padding(top = 8.dp),
                placeholder = "닉네임을 입력해주세요",
                enabled = !uiState.isSaving,
                isError = uiState.hasNicknameError,
                errorMessage = uiState.nicknameValidationMessage
                    ?: "닉네임을 확인해주세요",
            )

            NicknameValidationMessage(
                uiState = uiState,
                onRetryClick = onNicknameCheckRetry,
                modifier = Modifier.padding(
                    start = 4.dp,
                    top = 8.dp,
                    end = 4.dp,
                ),
            )

            ProfileEditSectionLabel(
                text = "응원 구단",
                modifier = Modifier.padding(top = 24.dp),
            )

            TeamSelectionGrid(
                selectedTeam = uiState.selectedTeam,
                enabled = !uiState.isSaving,
                onTeamSelected = onTeamSelected,
                modifier = Modifier.padding(top = 10.dp),
            )

            ProfileEditSectionLabel(
                text = "기본 정보",
                modifier = Modifier.padding(top = 24.dp),
            )

            YadanCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
            ) {
                ProfileLinkedInfoRow(
                    label = "연령대",
                    value = profile.ageGroupLabel(currentDate),
                )

                HorizontalDivider(
                    thickness = 1.dp,
                    color = YadanDivider,
                )

                ProfileLinkedInfoRow(
                    label = "성별",
                    value = profile.gender.toDisplayName(),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        YadanButton(
            text = "저장",
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    top = 12.dp,
                    end = 20.dp,
                    bottom = 16.dp,
                )
                .navigationBarsPadding(),
            enabled = uiState.isSaveEnabled,
            isLoading = uiState.isSaving,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            },
        )
    }
}

/** 프로필 편집 항목의 이름과 선택적인 보조 문구를 표시합니다. */
@Composable
private fun ProfileEditSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = YadanTypography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = YadanTextPrimary,
        )

        if (trailingText != null) {
            Text(
                text = trailingText,
                style = YadanTypography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = YadanTextMuted,
            )
        }
    }
}

/** 닉네임 중복 확인 상태와 재시도 동작을 표시합니다. */
@Composable
private fun NicknameValidationMessage(
    uiState: ProfileEditUiState,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val message = uiState.nicknameValidationMessage

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (message != null) {
            when (uiState.nicknameState) {
                ProfileNicknameState.CHECKING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = YadanPrimary,
                        strokeWidth = 2.dp,
                    )
                }

                ProfileNicknameState.AVAILABLE -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                        tint = YadanPrimaryInk,
                    )
                }

                else -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                        tint = YadanError,
                    )
                }
            }

            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = YadanTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color =
                    if (uiState.nicknameState == ProfileNicknameState.AVAILABLE) {
                        YadanPrimaryInk
                    } else if (uiState.hasNicknameError) {
                        YadanError
                    } else {
                        YadanTextMuted
                    },
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        if (uiState.canRetryNicknameCheck) {
            TextButton(
                onClick = onRetryClick,
                enabled = !uiState.isSaving,
            ) {
                Text(
                    text = "다시 확인",
                    style = YadanTypography.labelMedium,
                    color = YadanPrimaryInk,
                )
            }
        }
    }
}

/** HTML 순서대로 KBO 구단을 5열로 표시합니다. */
@Composable
private fun TeamSelectionGrid(
    selectedTeam: KboTeam?,
    enabled: Boolean,
    onTeamSelected: (KboTeam) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        KboTeam.entries.chunked(TEAM_COLUMN_COUNT).forEach { teams ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                teams.forEach { team ->
                    YadanTeamSelectionItem(
                        team = team,
                        selected = selectedTeam == team,
                        onClick = {
                            onTeamSelected(team)
                        },
                        modifier = Modifier.weight(1f),
                        style = YadanTeamSelectionItemStyle.COMPACT,
                        enabled = enabled,
                    )
                }

                repeat(TEAM_COLUMN_COUNT - teams.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/** 카카오 계정에서 연결된 읽기 전용 기본 정보를 표시합니다. */
@Composable
private fun ProfileLinkedInfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 14.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = YadanTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = YadanTextSecondary,
        )

        Text(
            text = value,
            style = YadanTypography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = YadanTextPrimary,
        )

        Text(
            text = "카카오",
            modifier = Modifier.padding(start = 10.dp),
            style = YadanTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = YadanTextMuted,
        )
    }
}

/** 생년월일을 현재 만 나이에 해당하는 연령대로 변환합니다. */
private fun UserProfile.ageGroupLabel(currentDate: LocalDate): String {
    val birthDate = birthDate ?: return "-"
    if (birthDate > currentDate) return "-"

    val age = birthDate.periodUntil(currentDate).years
    return "${age / 10 * 10}대"
}

/** 서버 성별 값을 화면 표시 문구로 변환합니다. */
private fun Gender?.toDisplayName(): String =
    when (this) {
        Gender.MALE -> "남성"
        Gender.FEMALE -> "여성"
        null -> "-"
    }

private const val TEAM_COLUMN_COUNT = 5

private val previewProfile = UserProfile(
    id = "user-profile-edit",
    nickname = "준호",
    favoriteTeam = KboTeam.LOTTE,
    gender = Gender.MALE,
    birthDate = LocalDate(1998, 5, 17),
)

@Preview(name = "H02 프로필 수정", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ProfileEditScreenPreview() {
    YadanbeopseokTheme {
        ProfileEditScreen(
            uiState = ProfileEditUiState(
                profile = previewProfile,
                originalNickname = "준호",
                nickname = "준호",
                nicknameState = ProfileNicknameState.UNCHANGED,
                selectedTeam = KboTeam.LOTTE,
                isLoading = false,
            ),
            onBackClick = {},
            onRetryClick = {},
            onNicknameChange = {},
            onNicknameCheckRetry = {},
            onTeamSelected = {},
            onSaveClick = {},
        )
    }
}

@Preview(name = "H02 닉네임 중복", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ProfileEditDuplicatedPreview() {
    YadanbeopseokTheme {
        ProfileEditScreen(
            uiState = ProfileEditUiState(
                profile = previewProfile,
                originalNickname = "준호",
                nickname = "야구왕",
                nicknameState = ProfileNicknameState.DUPLICATED,
                selectedTeam = KboTeam.LOTTE,
                isLoading = false,
            ),
            onBackClick = {},
            onRetryClick = {},
            onNicknameChange = {},
            onNicknameCheckRetry = {},
            onTeamSelected = {},
            onSaveClick = {},
        )
    }
}

@Preview(name = "H02 닉네임 확인 중", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ProfileEditCheckingPreview() {
    YadanbeopseokTheme {
        ProfileEditScreen(
            uiState = ProfileEditUiState(
                profile = previewProfile,
                originalNickname = "준호",
                nickname = "야구여행",
                nicknameState = ProfileNicknameState.CHECKING,
                selectedTeam = KboTeam.LOTTE,
                isLoading = false,
            ),
            onBackClick = {},
            onRetryClick = {},
            onNicknameChange = {},
            onNicknameCheckRetry = {},
            onTeamSelected = {},
            onSaveClick = {},
        )
    }
}

@Preview(name = "H02 저장 중", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ProfileEditSavingPreview() {
    YadanbeopseokTheme {
        ProfileEditScreen(
            uiState = ProfileEditUiState(
                profile = previewProfile,
                originalNickname = "준호",
                nickname = "준호",
                nicknameState = ProfileNicknameState.UNCHANGED,
                selectedTeam = KboTeam.KIA,
                isLoading = false,
                isSaving = true,
            ),
            onBackClick = {},
            onRetryClick = {},
            onNicknameChange = {},
            onNicknameCheckRetry = {},
            onTeamSelected = {},
            onSaveClick = {},
        )
    }
}

@Preview(name = "H02 불러오는 중", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ProfileEditLoadingPreview() {
    YadanbeopseokTheme {
        ProfileEditScreen(
            uiState = ProfileEditUiState(),
            onBackClick = {},
            onRetryClick = {},
            onNicknameChange = {},
            onNicknameCheckRetry = {},
            onTeamSelected = {},
            onSaveClick = {},
        )
    }
}

@Preview(name = "H02 불러오기 실패", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ProfileEditErrorPreview() {
    YadanbeopseokTheme {
        ProfileEditScreen(
            uiState = ProfileEditUiState(
                isLoading = false,
                errorMessage = "인터넷 연결을 확인한 후 다시 시도해주세요.",
            ),
            onBackClick = {},
            onRetryClick = {},
            onNicknameChange = {},
            onNicknameCheckRetry = {},
            onTeamSelected = {},
            onSaveClick = {},
        )
    }
}
