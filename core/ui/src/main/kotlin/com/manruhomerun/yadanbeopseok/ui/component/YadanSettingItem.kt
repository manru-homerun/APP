package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDivider
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTint
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 알림 설정에서 사용하는 토글 설정 항목입니다.
 *
 * HTML의 `.set-row`, `.st`, `.ss`, `.tsw` 구조에 대응합니다.
 * 행 전체를 선택할 수 있게 구성하고 내부 Switch는 시각적 상태만 표시합니다.
 *
 * @param title 설정 이름입니다.
 * @param checked 현재 설정 활성화 여부입니다.
 * @param onCheckedChange 설정 상태가 변경될 때 호출됩니다.
 * @param modifier 항목의 크기와 배치를 지정합니다.
 * @param supportingText 설정에 관한 부가 설명입니다.
 * @param enabled 설정 변경 가능 여부입니다.
 */
@Composable
fun YadanSettingItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true,
) {
    YadanSettingItemContent(
        title = title,
        supportingText = supportingText,
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
                .toggleable(
                    value = checked,
                    enabled = enabled,
                    role = Role.Switch,
                    onValueChange = onCheckedChange,
                ),
        verticalPadding = 13.dp,
        trailingContent = {
            /*
             * 행 전체에서 토글 동작과 접근성을 처리하므로 Switch에는
             * 별도의 클릭 동작과 접근성 정보를 부여하지 않습니다.
             *
             * Material Switch를 축소해 HTML의 약 44x26dp 비율에 맞추고,
             * 행 전체의 터치 영역은 그대로 유지합니다.
             */
            CompositionLocalProvider(
                LocalMinimumInteractiveComponentSize provides 0.dp,
            ) {
                Switch(
                    checked = checked,
                    onCheckedChange = null,
                    modifier =
                        Modifier
                            .scale(0.85f)
                            .clearAndSetSemantics {},
                    enabled = enabled,
                    colors =
                        SwitchDefaults.colors(
                            checkedThumbColor = YadanSurface,
                            checkedTrackColor = YadanPrimary,
                            checkedBorderColor = Color.Transparent,
                            uncheckedThumbColor = YadanSurface,
                            uncheckedTrackColor = YadanOutline,
                            uncheckedBorderColor = Color.Transparent,
                            disabledCheckedThumbColor = YadanSurface,
                            disabledCheckedTrackColor = YadanPrimary,
                            disabledCheckedBorderColor = Color.Transparent,
                            disabledUncheckedThumbColor = YadanSurface,
                            disabledUncheckedTrackColor = YadanOutline,
                            disabledUncheckedBorderColor = Color.Transparent,
                        ),
                )
            }
        },
    )
}

/**
 * 마이페이지와 설정 화면에서 다른 화면으로 이동할 때 사용하는 설정 항목입니다.
 *
 * HTML의 `.mrow`, `.mrow-ic`, `.mrow-tx`, `.mrow-r` 구조에 대응합니다.
 *
 * @param title 메뉴 이름입니다.
 * @param onClick 항목을 눌렀을 때 실행할 작업입니다.
 * @param modifier 항목의 크기와 배치를 지정합니다.
 * @param supportingText 메뉴에 관한 부가 설명입니다.
 * @param enabled 항목 선택 가능 여부입니다.
 * @param leadingContent 왼쪽 아이콘 영역에 표시할 콘텐츠입니다.
 * Material Icon뿐 아니라 추후 SVG 기반 아이콘도 전달할 수 있습니다.
 * @param trailingContent 화살표 앞에 표시할 상태나 배지입니다.
 */
@Composable
fun YadanSettingItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Surface(
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
        color = Color.Transparent,
        contentColor = YadanTextPrimary,
    ) {
        YadanSettingItemContent(
            title = title,
            supportingText = supportingText,
            modifier = Modifier.fillMaxWidth(),
            verticalPadding = 14.dp,
            leadingContent = leadingContent,
            trailingContent = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    trailingContent?.invoke()

                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = YadanTextMuted,
                    )
                }
            },
        )
    }
}

/**
 * 토글 항목과 이동 항목이 공유하는 제목, 설명 및 좌우 콘텐츠 영역입니다.
 */
@Composable
private fun YadanSettingItemContent(
    title: String,
    supportingText: String?,
    modifier: Modifier,
    verticalPadding: Dp,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
            modifier.padding(
                horizontal = 15.dp,
                vertical = verticalPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingContent?.let { content ->
            Surface(
                modifier = Modifier.size(34.dp),
                shape = YadanShapes.small,
                color = YadanPrimaryTint,
                contentColor = YadanPrimary,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    content()
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style =
                    YadanTypography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = YadanTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

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
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
        }

        trailingContent?.invoke()
    }
}

@Preview(
    name = "Yadan setting items",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
)
@Composable
private fun YadanSettingItemPreview() {
    var ticketNotificationEnabled by remember {
        mutableStateOf(true)
    }
    var certificationNotificationEnabled by remember {
        mutableStateOf(true)
    }
    var nearbyGameNotificationEnabled by remember {
        mutableStateOf(false)
    }

    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = "알림 설정",
                style =
                    YadanTypography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = YadanTextMuted,
            )

            YadanCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                YadanSettingItem(
                    title = "예매 일자 알림",
                    supportingText = "응원 구단 예매가 열리면 알려드려요",
                    checked = ticketNotificationEnabled,
                    onCheckedChange = {
                        ticketNotificationEnabled = it
                    },
                )

                HorizontalDivider(
                    thickness = 1.5.dp,
                    color = YadanOutline,
                )

                YadanSettingItem(
                    title = "방문 인증 리마인드",
                    supportingText =
                        "아침·점심·저녁, 관광지 인증을 잊지 않게 알려드려요",
                    checked = certificationNotificationEnabled,
                    onCheckedChange = {
                        certificationNotificationEnabled = it
                    },
                )

                HorizontalDivider(
                    thickness = 1.5.dp,
                    color = YadanOutline,
                )

                YadanSettingItem(
                    title = "지역 경기 알림",
                    supportingText = "선호 지역 근처에서 경기가 열릴 때",
                    checked = nearbyGameNotificationEnabled,
                    onCheckedChange = {
                        nearbyGameNotificationEnabled = it
                    },
                )
            }

            Text(
                text = "마이페이지 메뉴",
                style =
                    YadanTypography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = YadanTextMuted,
            )

            YadanCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                YadanSettingItem(
                    title = "찜한 여행지",
                    supportingText = "저장한 관광지 모아보기",
                    onClick = {},
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(19.dp),
                        )
                    },
                )

                HorizontalDivider(
                    thickness = 1.5.dp,
                    color = YadanDivider,
                )

                YadanSettingItem(
                    title = "내 여행 취향",
                    supportingText =
                        "부산 거주 · 자연 선호 · 선호 지역 2곳",
                    onClick = {},
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(19.dp),
                        )
                    },
                )

                HorizontalDivider(
                    thickness = 1.5.dp,
                    color = YadanDivider,
                )

                YadanSettingItem(
                    title = "친구",
                    supportingText = "동행 초대할 야구 친구 관리",
                    onClick = {},
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
    }
}
