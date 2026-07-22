package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Museum
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.SportsBaseball
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCategoryBadge
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCategoryBadgeStyle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconButtonSize
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconToggleButton
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDivider
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanFavorite
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTintStrong
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory

/**
 * 여행지 카드 오른쪽에 표시할 작업 유형입니다.
 */
enum class YadanTravelSpotAction {
    /** 홈 추천 및 찜 목록에서 사용하는 하트 버튼입니다. */
    FAVORITE,

    /** 코스에 여행지를 담을 때 사용합니다. */
    ADD,

    /** 이미 코스에 담긴 여행지를 나타냅니다. */
    ADDED,

    /** 코스에서 여행지를 제거할 때 사용합니다. */
    REMOVE,
}

/**
 * 여행지 추천, 찜 목록, 여행 생성 및 일정 편집에서 사용하는 공통 카드입니다.
 *
 * HTML의 `.spotcard`와 `.spick` 구조를 하나의 컴포넌트로 구성합니다.
 *
 * [YadanTravelSpotAction.FAVORITE]에서는 60dp 이미지를 사용하고,
 * 나머지 작업 유형에서는 HTML의 선택 카드에 맞춰 50dp 이미지를 사용합니다.
 *
 * @param spot 카드에 표시할 여행지입니다.
 * @param onClick 카드 자체를 눌렀을 때 실행할 작업입니다.
 * @param onActionClick 하트 또는 담기·담음·삭제 버튼을 눌렀을 때 실행할 작업입니다.
 * @param modifier 카드의 크기와 배치를 지정할 Modifier입니다.
 * @param action 카드 오른쪽에 표시할 작업 유형입니다.
 * @param enabled 카드와 작업 버튼의 활성화 여부입니다.
 */
@Composable
fun YadanTravelSpotCard(
    spot: TravelSpot,
    onClick: () -> Unit,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    action: YadanTravelSpotAction = YadanTravelSpotAction.FAVORITE,
    enabled: Boolean = true,
) {
    val imageSize =
        if (action == YadanTravelSpotAction.FAVORITE) {
            60.dp
        } else {
            50.dp
        }

    YadanCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            YadanAsyncImage(
                imageUrl = spot.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(imageSize),
                shape = YadanShapes.small,
                placeholderIcon = spot.category.placeholderIcon(),
            )

            Spacer(modifier = Modifier.width(11.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = spot.name,
                    style =
                        YadanTypography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                        ),
                    color = YadanTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                YadanCategoryBadge(
                    text = spot.category.displayName,
                    style = spot.category.travelSpotCardBadgeStyle(),
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            when (action) {
                YadanTravelSpotAction.FAVORITE -> {
                    YadanIconToggleButton(
                        checked = spot.isLiked,
                        onCheckedChange = {
                            onActionClick()
                        },
                        size = YadanIconButtonSize.MEDIUM,
                        enabled = enabled,
                        uncheckedContentColor = YadanTextMuted,
                        checkedContentColor = YadanFavorite,
                    ) { checked ->
                        Icon(
                            imageVector =
                                if (checked) {
                                    Icons.Default.Favorite
                                } else {
                                    Icons.Outlined.FavoriteBorder
                                },
                            contentDescription =
                                if (checked) {
                                    "찜 해제"
                                } else {
                                    "찜하기"
                                },
                        )
                    }
                }

                else -> {
                    YadanTravelSpotActionButton(
                        action = action,
                        onClick = onActionClick,
                        enabled = enabled,
                    )
                }
            }
        }
    }
}

/**
 * 코스 생성 및 일정 편집 카드에 표시하는 작은 작업 버튼입니다.
 *
 * 공통 YadanButton은 주요 화면 작업을 위한 큰 버튼이므로,
 * HTML의 36px `.sp-btn` 비율은 카드 내부 전용 버튼으로 구성합니다.
 */
@Composable
private fun YadanTravelSpotActionButton(
    action: YadanTravelSpotAction,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    val visuals = action.actionVisuals() ?: return

    Row(
        modifier =
            Modifier
                .alpha(
                    if (enabled) {
                        1f
                    } else {
                        0.42f
                    },
                )
                .height(36.dp)
                .widthIn(min = 64.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(visuals.containerColor)
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(horizontal = 13.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        visuals.icon?.let { icon ->
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = visuals.contentColor,
            )

            Spacer(modifier = Modifier.width(3.dp))
        }

        Text(
            text = visuals.text,
            style =
                YadanTypography.bodySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
            color = visuals.contentColor,
            maxLines = 1,
        )
    }
}

/**
 * 작업 유형에 맞는 버튼 문구와 색상을 반환합니다.
 *
 * FAVORITE는 YadanIconToggleButton으로 처리하므로 null을 반환합니다.
 */
private fun YadanTravelSpotAction.actionVisuals(): TravelSpotActionVisuals? =
    when (this) {
        YadanTravelSpotAction.FAVORITE ->
            null

        YadanTravelSpotAction.ADD ->
            TravelSpotActionVisuals(
                text = "담기",
                icon = Icons.Default.Add,
                containerColor = YadanPrimary,
                contentColor = YadanOnPrimary,
            )

        YadanTravelSpotAction.ADDED ->
            TravelSpotActionVisuals(
                text = "담음",
                icon = Icons.Default.Check,
                containerColor = YadanPrimaryTintStrong,
                contentColor = YadanPrimaryInk,
            )

        YadanTravelSpotAction.REMOVE ->
            TravelSpotActionVisuals(
                text = "삭제",
                icon = null,
                containerColor = YadanDivider,
                contentColor = YadanTextMuted,
            )
    }

/**
 * 추천·검색·찜 카드에 표시되는 여행지 태그 스타일을 반환합니다.
 *
 * 일반 여행지는 연한 하늘색을 사용하고,
 * 행사는 조금 더 진한 강조 배경을 사용합니다.
 */
private fun TravelSpotCategory.travelSpotCardBadgeStyle(): YadanCategoryBadgeStyle =
    when (this) {
        TravelSpotCategory.FESTIVAL ->
            YadanCategoryBadgeStyle.FESTIVAL

        else ->
            YadanCategoryBadgeStyle.TINTED
    }

/**
 * 여행지 카테고리에 맞는 기존 디자인 시스템 배지 유형을 반환합니다.
 */
internal fun TravelSpotCategory.badgeStyle(): YadanCategoryBadgeStyle =
    when (this) {
        TravelSpotCategory.FESTIVAL ->
            YadanCategoryBadgeStyle.FESTIVAL

        TravelSpotCategory.STADIUM ->
            YadanCategoryBadgeStyle.GAME

        else ->
            YadanCategoryBadgeStyle.DEFAULT
    }

/**
 * 이미지가 없을 때 카테고리를 나타내는 placeholder 아이콘을 반환합니다.
 */
internal fun TravelSpotCategory.placeholderIcon(): ImageVector =
    when (this) {
        TravelSpotCategory.ACCOMMODATION -> Icons.Outlined.Hotel
        TravelSpotCategory.FESTIVAL -> Icons.Outlined.Celebration
        TravelSpotCategory.EXPERIENCE -> Icons.Outlined.Palette
        TravelSpotCategory.FOOD -> Icons.Outlined.Restaurant
        TravelSpotCategory.HISTORY -> Icons.Outlined.AccountBalance
        TravelSpotCategory.LEISURE -> Icons.AutoMirrored.Outlined.DirectionsBike
        TravelSpotCategory.NATURE -> Icons.Outlined.Park
        TravelSpotCategory.SHOPPING -> Icons.Outlined.ShoppingBag
        TravelSpotCategory.CULTURE -> Icons.Outlined.Museum
        TravelSpotCategory.STADIUM -> Icons.Outlined.SportsBaseball
        TravelSpotCategory.UNKNOWN -> Icons.Outlined.Image
    }

/**
 * 카드 내부 작업 버튼에 필요한 시각 정보를 묶습니다.
 */
private data class TravelSpotActionVisuals(
    val text: String,
    val icon: ImageVector?,
    val containerColor: Color,
    val contentColor: Color,
)

@Preview(
    name = "Yadan travel spot cards",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanTravelSpotCardPreview() {
    val baseSpot =
        TravelSpot(
            id = "spot-1",
            name = "감천문화마을",
            latitude = 35.0975,
            longitude = 129.0106,
            region = Region.BUSAN,
            category = TravelSpotCategory.CULTURE,
            imageUrl = null,
            isLiked = true,
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
            YadanTravelSpotCard(
                spot = baseSpot,
                onClick = {},
                onActionClick = {},
                action = YadanTravelSpotAction.FAVORITE,
            )

            YadanTravelSpotCard(
                spot =
                    baseSpot.copy(
                        id = "spot-2",
                        name = "해운대 해수욕장",
                        category = TravelSpotCategory.NATURE,
                        isLiked = false,
                    ),
                onClick = {},
                onActionClick = {},
                action = YadanTravelSpotAction.ADD,
            )

            YadanTravelSpotCard(
                spot =
                    baseSpot.copy(
                        id = "spot-3",
                        name = "전포 카페거리",
                        category = TravelSpotCategory.FOOD,
                        isLiked = false,
                    ),
                onClick = {},
                onActionClick = {},
                action = YadanTravelSpotAction.ADDED,
            )

            YadanTravelSpotCard(
                spot =
                    baseSpot.copy(
                        id = "spot-4",
                        name = "광안리 해변",
                        category = TravelSpotCategory.NATURE,
                        isLiked = false,
                    ),
                onClick = {},
                onActionClick = {},
                action = YadanTravelSpotAction.REMOVE,
            )
        }
    }
}
