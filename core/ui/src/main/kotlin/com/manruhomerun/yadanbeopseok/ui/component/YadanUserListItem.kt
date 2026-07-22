package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCardStyle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCheckbox
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconButtonSize
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconButtonStyle
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDivider
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.LoginProvider
import com.manruhomerun.yadanbeopseok.model.UserProfile
import com.manruhomerun.yadanbeopseok.ui.displayNickname

/**
 * 사용자 목록 항목의 시각적 유형입니다.
 */
enum class YadanUserListItemStyle {
    /** 친구 목록, 친구 요청 및 친구 검색에서 사용하는 일반 행입니다. */
    LIST,

    /** 여행 만들기의 동행자 선택에서 사용하는 선택 카드입니다. */
    SELECTION_CARD,
}

/**
 * 사용자 정보와 사용자 관련 작업을 표시하는 공통 목록 항목입니다.
 *
 * [YadanUserAvatar]와 [YadanTeamBadge]를 재사용합니다.
 *
 * @param user 표시할 사용자입니다.
 * @param modifier 항목의 크기와 배치를 지정합니다.
 * @param style 일반 목록 또는 동행자 선택 카드 유형입니다.
 * @param supportingText 사용자 아래에 표시할 부가 설명입니다.
 * @param selected 동행자 선택 카드의 선택 여부입니다.
 * @param onClick 항목을 눌렀을 때 실행합니다.
 * 선택 카드에서는 반드시 전달하고, 오른쪽 버튼만 사용하는 목록에서는
 * null을 전달합니다.
 * @param enabled 항목 활성화 여부입니다.
 * @param trailingContent 일반 목록 오른쪽에 표시할 버튼이나 상태입니다.
 */
@Composable
fun YadanUserListItem(
    user: UserProfile,
    modifier: Modifier = Modifier,
    style: YadanUserListItemStyle = YadanUserListItemStyle.LIST,
    supportingText: String? = null,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    when (style) {
        YadanUserListItemStyle.LIST -> {
            val clickModifier =
                if (onClick != null) {
                    Modifier.clickable(
                        enabled = enabled,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                }

            YadanUserListItemContent(
                user = user,
                supportingText = supportingText,
                showTeamBadge = true,
                trailingContent = trailingContent,
                modifier =
                    modifier
                        .fillMaxWidth()
                        .alpha(
                            if (enabled) {
                                1f
                            } else {
                                0.42f
                            },
                        )
                        .then(clickModifier)
                        .padding(
                            horizontal = 4.dp,
                            vertical = 11.dp,
                        ),
                horizontalSpacing = 12.dp,
            )
        }

        YadanUserListItemStyle.SELECTION_CARD -> {
            val selectionEnabled =
                enabled && onClick != null

            /*
             * 동행자는 여러 명을 선택할 수 있으므로 카드 전체를
             * Checkbox 역할을 가진 복수 선택 항목으로 제공합니다.
             *
             * 클릭 처리는 Modifier.toggleable이 담당하므로
             * 클릭 가능한 YadanCard 오버로드를 사용하지 않습니다.
             */
            YadanCard(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .alpha(
                            if (selectionEnabled) {
                                1f
                            } else {
                                0.42f
                            },
                        )
                        .clip(MaterialTheme.shapes.large)
                        .toggleable(
                            value = selected,
                            enabled = selectionEnabled,
                            role = Role.Checkbox,
                            onValueChange = {
                                onClick?.invoke()
                            },
                        ),
                style =
                    if (selected) {
                        YadanCardStyle.SELECTED
                    } else {
                        YadanCardStyle.DEFAULT
                    },
            ) {
                YadanUserListItemContent(
                    user = user,
                    supportingText = supportingText,
                    showTeamBadge = false,
                    trailingContent = {
                        /*
                         * 카드 전체가 Checkbox 접근성과 클릭을 처리하므로
                         * 체크박스는 현재 선택 상태만 표시합니다.
                         */
                        YadanCheckbox(
                            checked = selected,
                            onCheckedChange = null,
                            enabled = selectionEnabled,
                        )
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 14.dp,
                            ),
                    horizontalSpacing = 13.dp,
                )
            }
        }
    }
}

/**
 * 목록 행과 선택 카드가 공유하는 사용자 정보 영역입니다.
 */
@Composable
private fun YadanUserListItemContent(
    user: UserProfile,
    supportingText: String?,
    showTeamBadge: Boolean,
    trailingContent: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    horizontalSpacing: androidx.compose.ui.unit.Dp,
) {
    val nickname = user.displayNickname()

    Row(
        modifier = modifier,
        horizontalArrangement =
            Arrangement.spacedBy(horizontalSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        /*
         * 사용자 이름을 바로 옆에서 제공하므로 목록 안의 아바타는
         * 장식 요소로 처리해 닉네임이 두 번 읽히지 않게 합니다.
         */
        Box(
            modifier = Modifier.clearAndSetSemantics {},
        ) {
            YadanUserAvatar(
                user = user,
                size = YadanUserAvatarSize.LIST,
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = nickname,
                    modifier = Modifier.weight(1f, fill = false),
                    style =
                        YadanTypography.bodyMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                        ),
                    color = YadanTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (showTeamBadge) {
                    user.favoriteTeam?.let { favoriteTeam ->
                        YadanTeamBadge(
                            team = favoriteTeam,
                        )
                    }
                }
            }

            supportingText
                ?.takeIf { text -> text.isNotBlank() }
                ?.let { text ->
                    Text(
                        text = text,
                        style =
                            YadanTypography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                        color = YadanTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
        }

        trailingContent?.invoke()
    }
}

@Preview(
    name = "Yadan user list items",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
)
@Composable
private fun YadanUserListItemPreview() {
    val lotteUser =
        UserProfile(
            id = "user-lotte",
            provider = LoginProvider.KAKAO,
            providerUserId = "kakao-lotte",
            nickname = "지우",
            favoriteTeam = KboTeam.LOTTE,
        )

    val kiaUser =
        UserProfile(
            id = "user-kia",
            provider = LoginProvider.KAKAO,
            providerUserId = "kakao-kia",
            nickname = "현수",
            favoriteTeam = KboTeam.KIA,
        )

    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            YadanUserListItem(
                user = lotteUser,
                style =
                    YadanUserListItemStyle.SELECTION_CARD,
                supportingText =
                    "부산 거주 · 음식·문화 선호",
                selected = true,
                onClick = {},
            )

            YadanUserListItem(
                user = kiaUser,
                style =
                    YadanUserListItemStyle.SELECTION_CARD,
                supportingText = "사진·야경 선호",
                selected = false,
                onClick = {},
            )

            Column {
                YadanUserListItem(
                    user = lotteUser,
                    trailingContent = {
                        YadanIconButton(
                            onClick = {},
                            style = YadanIconButtonStyle.MUTED,
                            size = YadanIconButtonSize.SMALL,
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "친구 메뉴",
                            )
                        }
                    },
                )

                HorizontalDivider(
                    thickness = 1.5.dp,
                    color = YadanDivider,
                )

                YadanUserListItem(
                    user = kiaUser,
                    supportingText = "사진·야경 선호",
                    trailingContent = {
                        Text(
                            text = "요청됨",
                            style =
                                YadanTypography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                ),
                            color = YadanTextMuted,
                        )
                    },
                )
            }
        }
    }
}
