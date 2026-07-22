package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.LoginProvider
import com.manruhomerun.yadanbeopseok.model.UserProfile
import com.manruhomerun.yadanbeopseok.ui.displayNickname
import com.manruhomerun.yadanbeopseok.ui.normalizedNickname
import com.manruhomerun.yadanbeopseok.ui.theme.teamColor

/**
 * 사용자 아바타의 표시 크기입니다.
 */
enum class YadanUserAvatarSize(
    internal val value: Dp,
) {
    /** 동행자 및 친구 목록에서 사용하는 크기입니다. */
    LIST(44.dp),

    /** 마이페이지 프로필 카드에서 사용하는 크기입니다. */
    PROFILE(56.dp),

    /** 프로필 수정 화면에서 사용하는 큰 크기입니다. */
    LARGE(76.dp),
}

/**
 * 사용자의 프로필 이미지 또는 닉네임 첫 글자를 표시합니다.
 *
 * 프로필 이미지가 있으면 기존 [YadanAsyncImage]를 재사용합니다.
 * 이미지가 없으면 응원 구단 대표 색상 위에 닉네임 첫 글자를 표시합니다.
 *
 * @param user 표시할 사용자입니다.
 * @param modifier 아바타의 크기와 배치를 지정합니다.
 * @param size 아바타의 용도별 크기입니다.
 */
@Composable
fun YadanUserAvatar(
    user: UserProfile,
    modifier: Modifier = Modifier,
    size: YadanUserAvatarSize = YadanUserAvatarSize.LIST,
) {
    val nickname = user.normalizedNickname()
    val displayName = user.displayNickname()
    val initial = nickname?.take(1) ?: "?"

    val backgroundColor =
        user.favoriteTeam?.teamColor
            ?: YadanPrimary

    val initialTextStyle =
        when (size) {
            YadanUserAvatarSize.LIST ->
                YadanTypography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                )

            YadanUserAvatarSize.PROFILE ->
                YadanTypography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                )

            YadanUserAvatarSize.LARGE ->
                YadanTypography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                )
        }

    Box(
        modifier =
            modifier
                .size(size.value)
                .clip(CircleShape)
                .background(backgroundColor)
                .clearAndSetSemantics {
                    contentDescription = "$displayName 프로필"
                },
        contentAlignment = Alignment.Center,
    ) {
        if (user.profileImageUrl.isNullOrBlank()) {
            Text(
                text = initial,
                style = initialTextStyle,
                color = YadanOnPrimary,
                maxLines = 1,
            )
        } else {
            YadanAsyncImage(
                imageUrl = user.profileImageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                placeholderIcon = Icons.Outlined.Person,
            )
        }
    }
}

@Preview(
    name = "Yadan user avatars",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
)
@Composable
private fun YadanUserAvatarPreview() {
    val lotteUser =
        UserProfile(
            id = "user-lotte",
            provider = LoginProvider.KAKAO,
            providerUserId = "kakao-lotte",
            nickname = "지우",
            favoriteTeam = KboTeam.LOTTE,
        )

    val hanwhaUser =
        UserProfile(
            id = "user-hanwha",
            provider = LoginProvider.KAKAO,
            providerUserId = "kakao-hanwha",
            nickname = "현수",
            favoriteTeam = KboTeam.HANWHA,
        )

    val defaultUser =
        UserProfile(
            id = "user-default",
            provider = LoginProvider.KAKAO,
            providerUserId = "kakao-default",
        )

    YadanbeopseokTheme {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                YadanUserAvatar(
                    user = lotteUser,
                    size = YadanUserAvatarSize.LIST,
                )

                Text(
                    text = "목록",
                    style = YadanTypography.labelSmall,
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                YadanUserAvatar(
                    user = hanwhaUser,
                    size = YadanUserAvatarSize.PROFILE,
                )

                Text(
                    text = "프로필",
                    style = YadanTypography.labelSmall,
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                YadanUserAvatar(
                    user = defaultUser,
                    size = YadanUserAvatarSize.LARGE,
                )

                Text(
                    text = "큰 크기",
                    style = YadanTypography.labelSmall,
                )
            }
        }
    }
}
