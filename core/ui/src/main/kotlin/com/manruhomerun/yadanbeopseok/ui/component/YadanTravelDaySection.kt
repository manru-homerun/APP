package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTint
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelCertification
import com.manruhomerun.yadanbeopseok.model.TravelDay
import com.manruhomerun.yadanbeopseok.model.TravelPlace
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import kotlinx.datetime.LocalDateTime

/**
 * 여행의 하루 일정과 해당 일차에 포함된 장소 목록을 표시합니다.
 *
 * HTML의 `.cdayblock`, `.dayhdr`, `.timeline`, `.addrow` 구조에 대응합니다.
 * 장소 항목은 기존 [YadanTravelPlaceItem]을 재사용합니다.
 *
 * @param currentUserId 현재 로그인한 야단법석 사용자의 ID입니다.
 * @param travelDay 표시할 일차와 장소 목록입니다.
 * @param dateText 해당 일차의 날짜 문구입니다.
 * 예: `5.22 (금)`
 * @param onPlaceClick 장소 카드를 눌렀을 때 실행할 작업입니다.
 * @param modifier 섹션 전체의 크기와 배치를 지정합니다.
 * @param mode 장소 목록의 보기, 진행 중 또는 편집 모드입니다.
 * @param supportingText 장소별 방문 시간이나 경기 시간 문구를 반환합니다.
 * @param isPlaceFixed 편집 중 고정할 장소인지 반환합니다.
 * 기본적으로 야구장을 고정합니다.
 * @param onVerifyClick 진행 중인 여행에서 방문 인증을 요청합니다.
 * @param onRemoveClick 편집 중 장소 삭제를 요청합니다.
 * @param dragHandleModifier 장소별 드래그 핸들 Modifier를 반환합니다.
 * @param onAddPlaceClick 편집 모드에서 관광지 추가 버튼을 눌렀을 때 실행합니다.
 * null이면 추가 버튼을 표시하지 않습니다.
 * @param enabled 섹션 내부의 카드와 버튼 활성화 여부입니다.
 */
@Composable
fun YadanTravelDaySection(
    travelDay: TravelDay,
    currentUserId: String,
    dateText: String,
    onPlaceClick: (TravelPlace) -> Unit,
    modifier: Modifier = Modifier,
    mode: YadanTravelPlaceItemMode = YadanTravelPlaceItemMode.VIEW,
    supportingText: (TravelPlace) -> String? = { null },
    isPlaceFixed: (TravelPlace) -> Boolean = { place ->
        place.spot.category == TravelSpotCategory.STADIUM
    },
    onVerifyClick: ((TravelPlace) -> Unit)? = null,
    onRemoveClick: ((TravelPlace) -> Unit)? = null,
    dragHandleModifier: (TravelPlace) -> Modifier = { Modifier },
    onAddPlaceClick: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    /*
     * API 응답 순서와 관계없이 TravelPlace.order를 기준으로
     * 일정 순서를 안정적으로 표시합니다.
     */
    val orderedPlaces =
        travelDay.places.sortedBy { place ->
            place.order
        }

    /*
     * HTML의 장소 개수는 숙박을 제외하고 계산합니다.
     * 야구장은 방문 장소 개수에 포함됩니다.
     */
    val visitPlaceCount =
        orderedPlaces.count { place ->
            place.spot.category != TravelSpotCategory.ACCOMMODATION
        }

    val showAddPlaceButton =
        mode == YadanTravelPlaceItemMode.EDIT &&
            onAddPlaceClick != null

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        YadanTravelDayHeader(
            day = travelDay.day,
            dateText = dateText,
            placeCount = visitPlaceCount,
        )

        var displayOrder = 0

        orderedPlaces.forEachIndexed { index, place ->
            val isAccommodation =
                place.spot.category ==
                    TravelSpotCategory.ACCOMMODATION

            if (!isAccommodation) {
                displayOrder += 1
            }

            /*
             * 편집 모드에서 추가 버튼이 있다면 마지막 장소의 연결선을
             * 추가 버튼 왼쪽의 + 표시까지 이어줍니다.
             */
            val isLast =
                index == orderedPlaces.lastIndex &&
                    !showAddPlaceButton

            key(place.id) {
                YadanTravelPlaceItem(
                    place = place,
                    currentUserId = currentUserId,
                    isLast = isLast,
                    onClick = {
                        onPlaceClick(place)
                    },
                    mode = mode,
                    displayOrder = displayOrder,
                    supportingText = supportingText(place),
                    isFixed = isPlaceFixed(place),
                    onVerifyClick =
                        onVerifyClick?.let { onVerify ->
                            {
                                onVerify(place)
                            }
                        },
                    onRemoveClick =
                        onRemoveClick?.let { onRemove ->
                            {
                                onRemove(place)
                            }
                        },
                    dragHandleModifier =
                        dragHandleModifier(place),
                    enabled = enabled,
                )
            }
        }

        if (showAddPlaceButton) {
            YadanTravelDayAddPlaceRow(
                day = travelDay.day,
                onClick = onAddPlaceClick,
                enabled = enabled,
            )
        }
    }
}

/**
 * 일차, 날짜와 숙박을 제외한 방문 장소 개수를 표시합니다.
 */
@Composable
private fun YadanTravelDayHeader(
    day: Int,
    dateText: String,
    placeCount: Int,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
    ) {
        Text(
            text = "DAY ${day.coerceAtLeast(1)}",
            modifier = Modifier.alignByBaseline(),
            style =
                YadanTypography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
            color = YadanPrimaryInk,
        )

        if (dateText.isNotBlank()) {
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = dateText,
                modifier = Modifier.alignByBaseline(),
                style =
                    YadanTypography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = YadanTextPrimary,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "${placeCount}곳",
            modifier = Modifier.alignByBaseline(),
            style =
                YadanTypography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
            color = YadanTextMuted,
        )
    }
}

/**
 * 편집 모드에서 일차 마지막에 표시하는 관광지 추가 행입니다.
 */
@Composable
private fun YadanTravelDayAddPlaceRow(
    day: Int,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        YadanTravelDayAddMarker()

        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(bottom = 8.dp),
        ) {
            YadanTravelDayAddButton(
                day = day,
                onClick = onClick,
                enabled = enabled,
            )
        }
    }
}

/**
 * 기존 타임라인 표시와 같은 크기의 점선 + 마커입니다.
 */
@Composable
private fun YadanTravelDayAddMarker() {
    Box(
        modifier =
            Modifier
                .size(26.dp)
                .drawBehind {
                    val strokeWidth = 2.dp.toPx()

                    drawCircle(
                        color = YadanOutline,
                        radius =
                            size.minDimension
                                .minus(strokeWidth)
                                .div(2f),
                        style =
                            Stroke(
                                width = strokeWidth,
                                pathEffect =
                                    PathEffect.dashPathEffect(
                                        intervals =
                                            floatArrayOf(
                                                4.dp.toPx(),
                                                3.dp.toPx(),
                                            ),
                                    ),
                            ),
                    )
                },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = YadanTextMuted,
        )
    }
}

/**
 * 편집 모드의 `DAY n에 관광지 추가` 버튼입니다.
 *
 * HTML 비율을 유지하면서 Android의 최소 터치 영역인 48dp를 확보합니다.
 */
@Composable
private fun YadanTravelDayAddButton(
    day: Int,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .alpha(
                    if (enabled) {
                        1f
                    } else {
                        0.42f
                    },
                )
                .clip(shape)
                .background(YadanPrimaryTint)
                .border(
                    border =
                        BorderStroke(
                            width = 1.5.dp,
                            color =
                                YadanPrimary.copy(
                                    alpha = 0.45f,
                                ),
                        ),
                    shape = shape,
                )
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(
                    horizontal = 12.dp,
                    vertical = 12.dp,
                ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = YadanPrimaryInk,
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = "DAY ${day.coerceAtLeast(1)}에 관광지 추가",
            style =
                YadanTypography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
            color = YadanPrimaryInk,
            maxLines = 1,
        )
    }
}

@Preview(
    name = "Travel day - View",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    heightDp = 390,
)
@Composable
private fun YadanTravelDaySectionViewPreview() {
    YadanTravelDaySectionPreviewContent(
        mode = YadanTravelPlaceItemMode.VIEW,
    )
}

@Preview(
    name = "Travel day - Active",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    heightDp = 390,
)
@Composable
private fun YadanTravelDaySectionActivePreview() {
    YadanTravelDaySectionPreviewContent(
        mode = YadanTravelPlaceItemMode.ACTIVE,
    )
}

@Preview(
    name = "Travel day - Edit",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    heightDp = 410,
)
@Composable
private fun YadanTravelDaySectionEditPreview() {
    YadanTravelDaySectionPreviewContent(
        mode = YadanTravelPlaceItemMode.EDIT,
    )
}

/**
 * 모드별 Preview에서 공통으로 사용하는 화면입니다.
 */
@Composable
private fun YadanTravelDaySectionPreviewContent(
    mode: YadanTravelPlaceItemMode,
) {
    val travelDay = previewTravelDay()

    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
        ) {
            YadanTravelDaySection(
                travelDay = travelDay,
                currentUserId = "user-1",
                dateText = "5.22 (금)",
                onPlaceClick = {},
                mode = mode,
                supportingText = { place ->
                    when (place.spot.category) {
                        TravelSpotCategory.FOOD ->
                            "11:30 · 음식"

                        TravelSpotCategory.CULTURE ->
                            "13:00 · 문화"

                        TravelSpotCategory.ACCOMMODATION ->
                            "체크인 21:00"

                        TravelSpotCategory.STADIUM ->
                            "17:00 경기 시작"

                        else ->
                            null
                    }
                },
                onVerifyClick = {},
                onRemoveClick = {},
                onAddPlaceClick =
                    if (mode == YadanTravelPlaceItemMode.EDIT) {
                        {}
                    } else {
                        null
                    },
            )
        }
    }
}

/**
 * Preview에서 사용하는 하루 일정을 생성합니다.
 */
private fun previewTravelDay(): TravelDay =
    TravelDay(
        day = 1,
        places =
            listOf(
                previewTravelPlace(
                    id = "food",
                    name = "돼지국밥 거리",
                    category = TravelSpotCategory.FOOD,
                    order = 1,
                    certified = true,
                ),
                previewTravelPlace(
                    id = "culture",
                    name = "감천문화마을",
                    category = TravelSpotCategory.CULTURE,
                    order = 2,
                ),
                previewTravelPlace(
                    id = "stay",
                    name = "스테이 광안",
                    category = TravelSpotCategory.ACCOMMODATION,
                    order = 3,
                ),
                previewTravelPlace(
                    id = "game",
                    name = "사직야구장",
                    category = TravelSpotCategory.STADIUM,
                    order = 4,
                ),
            ),
    )

/**
 * Preview에서 사용하는 여행 장소를 생성합니다.
 */
private fun previewTravelPlace(
    id: String,
    name: String,
    category: TravelSpotCategory,
    order: Int,
    certified: Boolean = false,
): TravelPlace =
    TravelPlace(
        id = "place-$id",
        spot =
            TravelSpot(
                id = "spot-$id",
                name = name,
                latitude = 35.0,
                longitude = 129.0,
                region = Region.BUSAN,
                category = category,
                imageUrl = null,
            ),
        day = 1,
        order = order,
        certifications =
            if (certified) {
                listOf(
                    TravelCertification(
                        id = "certification-$id",
                        userId = "user-1",
                        certificatedAt =
                            LocalDateTime(
                                year = 2026,
                                month = 5,
                                day = 22,
                                hour = 11,
                                minute = 30,
                                second = 0,
                                nanosecond = 0,
                            ),
                    ),
                )
            } else {
                emptyList()
            },
    )
