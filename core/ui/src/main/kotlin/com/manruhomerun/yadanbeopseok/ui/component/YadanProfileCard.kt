package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.LoginProvider
import com.manruhomerun.yadanbeopseok.model.UserProfile

/**
 * 마이페이지 상단에 사용자 정보를 표시하는 프로필 카드입니다.
 *
 * HTML의 `.my-prof`, `.my-av`, `.my-info`, `.my-name`, `.my-edit`
 * 구조에 대응합니다.
 *
 * 기존 [YadanUserAvatar]와 [YadanTeamBadge]를 재사용합니다.
 *
 * @param user 표시할 사용자 프로필입니다.
 * @param onClick 카드를 눌러 프로필 수정 화면으로 이동할 때 실행합니다.
 * @param modifier 카드의 크기와 배치를 지정합니다.
 * @param enabled 카드 선택 가능 여부입니다.
 */
@Composable
fun YadanProfileCard(
    user: UserProfile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val displayName =
        user.nickname
            ?.trim()
            ?.takeIf { nickname -> nickname.isNotEmpty() }
            ?: "사용자"

    YadanCard(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .alpha(
                    if (enabled) {
                        1f
                    } else {
                        0.42f
                    },
                ),
        enabled = enabled,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            /*
             * 카드 안에서 닉네임을 별도로 표시하므로 아바타의 프로필 설명은
             * TalkBack에서 닉네임이 중복 안내되지 않도록 제거합니다.
             */
            Box(
                modifier = Modifier.clearAndSetSemantics {},
            ) {
                YadanUserAvatar(
                    user = user,
                    size = YadanUserAvatarSize.PROFILE,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = displayName,
                        modifier = Modifier.weight(1f, fill = false),
                        style =
                            YadanTypography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                            ),
                        color = YadanTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    user.favoriteTeam?.let { favoriteTeam ->
                        YadanTeamBadge(
                            team = favoriteTeam,
                        )
                    }
                }

                Text(
                    text = "프로필 수정",
                    style =
                        YadanTypography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = YadanTextMuted,
                    maxLines = 1,
                )
            }

            Icon(
                imageVector =
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = YadanTextMuted,
            )
        }
    }
}

@Preview(
    name = "Yadan profile cards",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
)
@Composable
private fun YadanProfileCardPreview() {
    val completeUser =
        UserProfile(
            id = "user-complete",
            provider = LoginProvider.KAKAO,
            providerUserId = "kakao-complete",
            nickname = "준호",
            favoriteTeam = KboTeam.LOTTE,
        )

    val longNameUser =
        UserProfile(
            id = "user-long-name",
            provider = LoginProvider.KAKAO,
            providerUserId = "kakao-long-name",
            nickname = "야구원정여행을좋아하는사용자",
            favoriteTeam = KboTeam.KIA,
        )

    val incompleteUser =
        UserProfile(
            id = "user-incomplete",
            provider = LoginProvider.KAKAO,
            providerUserId = "kakao-incomplete",
        )

    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            YadanProfileCard(
                user = completeUser,
                onClick = {},
            )

            YadanProfileCard(
                user = longNameUser,
                onClick = {},
            )

            YadanProfileCard(
                user = incompleteUser,
                onClick = {},
            )

            YadanProfileCard(
                user = completeUser,
                onClick = {},
                enabled = false,
            )
        }
    }
}
