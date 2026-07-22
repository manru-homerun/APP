package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCategoryBadge
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconButtonSize
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconButtonStyle
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDivider
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPillShape
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelCertification
import com.manruhomerun.yadanbeopseok.model.TravelPlace
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import kotlinx.datetime.LocalDateTime

/**
 * 여행 일정 장소 항목의 표시 모드입니다.
 */
enum class YadanTravelPlaceItemMode {
    /** 추천 결과 및 저장된 일정에서 사용하는 읽기 전용 모드입니다. */
    VIEW,

    /** 진행 중인 여행에서 방문 인증 상태와 인증 작업을 표시합니다. */
    ACTIVE,

    /** 지난 여행 상세에서 완료된 일정을 간결하게 표시합니다. */
    COMPLETED,

    /** 일정 순서 변경과 장소 삭제가 가능한 편집 모드입니다. */
    EDIT,
}

/**
 * 여행 일정에 포함된 장소 하나를 타임라인 형태로 표시합니다.
 *
 * HTML의 `.trow`, `.tdot`, `.tline`, `.spotrow`와
 * 지난 여행의 `.tl-done` 구조에 대응합니다.
 *
 * @param place 표시할 여행 장소입니다.
 * @param currentUserId 현재 로그인한 야단법석 사용자의 ID입니다.
 * @param isLast 해당 일차에서 마지막 장소인지 나타냅니다.
 * @param onClick 장소를 눌렀을 때 실행할 작업입니다.
 * @param modifier 항목 전체의 크기와 배치를 지정합니다.
 * @param mode 장소 항목의 보기, 진행 중, 완료 또는 편집 모드입니다.
 * @param displayOrder 타임라인 원 안에 표시할 순서입니다.
 * 숙박을 제외한 화면상 순서를 부모에서 계산하여 전달할 수 있습니다.
 * @param supportingText 방문 시간, 체크인 시간, 경기 시작 시간 등 부가 정보입니다.
 * @param isFixed 편집 중 순서를 변경하거나 삭제할 수 없는 장소인지 나타냅니다.
 * 기본적으로 야구장은 고정 장소로 처리합니다.
 * @param onVerifyClick 방문 인증 버튼을 눌렀을 때 실행할 작업입니다.
 * @param onRemoveClick 편집 모드의 삭제 버튼을 눌렀을 때 실행할 작업입니다.
 * @param dragHandleModifier 드래그 정렬 라이브러리의 핸들 Modifier를 연결합니다.
 * @param enabled 장소 항목과 작업 버튼의 활성화 여부입니다.
 */
@Composable
fun YadanTravelPlaceItem(
    place: TravelPlace,
    currentUserId: String,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    mode: YadanTravelPlaceItemMode = YadanTravelPlaceItemMode.VIEW,
    displayOrder: Int = place.order,
    supportingText: String? = null,
    isFixed: Boolean = place.spot.category == TravelSpotCategory.STADIUM,
    onVerifyClick: (() -> Unit)? = null,
    onRemoveClick: (() -> Unit)? = null,
    dragHandleModifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val isCertified = place.isCertifiedBy(currentUserId)
    val isCompleted = mode == YadanTravelPlaceItemMode.COMPLETED

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        horizontalArrangement =
            Arrangement.spacedBy(
                if (isCompleted) {
                    11.dp
                } else {
                    10.dp
                },
            ),
    ) {
        YadanTravelTimelineIndicator(
            place = place,
            isCertified = isCertified,
            displayOrder = displayOrder,
            mode = mode,
            isLast = isLast,
            modifier = Modifier.fillMaxHeight(),
        )

        if (isCompleted) {
            YadanCompletedTravelPlaceContent(
                place = place,
                supportingText = supportingText,
                isLast = isLast,
                onClick = onClick,
                modifier = Modifier.weight(1f),
                enabled = enabled,
            )
        } else {
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(bottom = 9.dp),
            ) {
                YadanTravelPlaceCard(
                    place = place,
                    isCertified = isCertified,
                    supportingText = supportingText,
                    mode = mode,
                    isFixed = isFixed,
                    onClick = onClick,
                    onVerifyClick = onVerifyClick,
                    onRemoveClick = onRemoveClick,
                    modifier = dragHandleModifier,
                    enabled = enabled,
                )
            }
        }
    }
}

/**
 * 장소 종류와 화면 상태에 맞는 타임라인 표시와 연결선을 그립니다.
 */
@Composable
private fun YadanTravelTimelineIndicator(
    place: TravelPlace,
    isCertified: Boolean,
    displayOrder: Int,
    mode: YadanTravelPlaceItemMode,
    isLast: Boolean,
    modifier: Modifier = Modifier,
) {
    val isGame =
        place.spot.category == TravelSpotCategory.STADIUM
    val isStay =
        place.spot.category == TravelSpotCategory.ACCOMMODATION
    val isActiveCertified =
        mode == YadanTravelPlaceItemMode.ACTIVE &&
            isCertified
    val isCompleted =
        mode == YadanTravelPlaceItemMode.COMPLETED
    val isCompletedCertified =
        isCompleted && isCertified
    val isCompletedUncertified =
        isCompleted && !isCertified

    /*
     * 진행 중인 여행에서는 인증한 장소를 체크로 표시합니다.
     * 지난 여행에서는 인증한 일반 장소만 체크로 표시하며,
     * 인증한 야구장은 HTML과 동일하게 야구공 아이콘을 유지합니다.
     */
    val showsCompletionCheck =
        isActiveCertified ||
            (isCompletedCertified && !isGame)

    /*
     * 지난 여행의 미인증 장소와 일반 화면의 미인증 숙박 장소는
     * 외곽선이 있는 흐린 마커로 구분합니다.
     */
    val showsMutedMarker =
        isCompletedUncertified ||
            (
                isStay &&
                    !isActiveCertified &&
                    !isCompleted
                )

    val containerColor =
        when {
            showsMutedMarker -> YadanDivider
            isGame && !isActiveCertified -> YadanTextPrimary
            else -> YadanPrimary
        }

    val contentColor =
        if (showsMutedMarker) {
            YadanTextSecondary
        } else {
            YadanOnPrimary
        }

    val markerBorder =
        if (showsMutedMarker) {
            BorderStroke(
                width = 1.5.dp,
                color = YadanOutline,
            )
        } else {
            null
        }

    /*
     * 지난 여행 타임라인은 HTML의 `.tl-dot` 크기를 적용하고,
     * 나머지 모드는 기존 타임라인 크기를 유지합니다.
     */
    val markerSize =
        if (isCompleted) {
            22.dp
        } else {
            26.dp
        }
    val markerIconSize =
        if (isCompleted) {
            14.dp
        } else {
            16.dp
        }
    val connectorSpacing =
        if (isCompleted) {
            2.dp
        } else {
            3.dp
        }
    val connectorWidth =
        if (isCompleted) {
            2.dp
        } else {
            2.5.dp
        }
    val connectorColor =
        if (isCompleted) {
            lerp(
                start = YadanDivider,
                stop = YadanPrimary,
                fraction = 0.32f,
            )
        } else {
            YadanDivider
        }

    Column(
        modifier = modifier.width(markerSize),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.size(markerSize),
            shape = CircleShape,
            color = containerColor,
            contentColor = contentColor,
            border = markerBorder,
        ) {
            Box(
                contentAlignment = Alignment.Center,
            ) {
                when {
                    showsCompletionCheck -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription =
                                if (isCompleted) {
                                    "방문 완료"
                                } else {
                                    "방문 인증 완료"
                                },
                            modifier = Modifier.size(markerIconSize),
                        )
                    }

                    isGame -> {
                        Icon(
                            imageVector = Icons.Default.SportsBaseball,
                            contentDescription = "야구 경기",
                            modifier = Modifier.size(markerIconSize),
                        )
                    }

                    isStay -> {
                        Icon(
                            imageVector = Icons.Default.Bed,
                            contentDescription = "숙박",
                            modifier = Modifier.size(markerIconSize),
                        )
                    }

                    else -> {
                        Text(
                            text = displayOrder.coerceAtLeast(1).toString(),
                            style =
                                YadanTypography.bodySmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                ),
                        )
                    }
                }
            }
        }

        if (!isLast) {
            Spacer(modifier = Modifier.height(connectorSpacing))

            Box(
                modifier =
                    Modifier
                        .width(connectorWidth)
                        .weight(1f)
                        .clip(YadanPillShape)
                        .background(connectorColor),
            )

            Spacer(modifier = Modifier.height(connectorSpacing))
        }
    }
}

/**
 * 지난 여행 상세에서 사용하는 간결한 완료 장소 내용입니다.
 */
@Composable
private fun YadanCompletedTravelPlaceContent(
    place: TravelPlace,
    supportingText: String?,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .alpha(
                    if (enabled) {
                        1f
                    } else {
                        0.42f
                    },
                )
                .clip(YadanShapes.small)
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(
                    bottom =
                        if (isLast) {
                            0.dp
                        } else {
                            20.dp
                        },
                ),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = place.spot.name,
            style =
                YadanTypography.labelMedium.copy(
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                ),
            color = YadanTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        supportingText
            ?.takeIf { it.isNotBlank() }
            ?.let { text ->
                Text(
                    text = text,
                    style =
                        YadanTypography.labelSmall.copy(
                            fontSize = 10.5.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    color = YadanTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
    }
}

/**
 * 이미지, 장소명, 카테고리와 일반 모드별 작업을 표시하는 카드입니다.
 */
@Composable
private fun YadanTravelPlaceCard(
    place: TravelPlace,
    isCertified: Boolean,
    supportingText: String?,
    mode: YadanTravelPlaceItemMode,
    isFixed: Boolean,
    onClick: () -> Unit,
    onVerifyClick: (() -> Unit)?,
    onRemoveClick: (() -> Unit)?,
    modifier: Modifier,
    enabled: Boolean,
) {
    val isEditMode =
        mode == YadanTravelPlaceItemMode.EDIT
    val isGame =
        place.spot.category == TravelSpotCategory.STADIUM
    val isActiveCertified =
        mode == YadanTravelPlaceItemMode.ACTIVE &&
            isCertified

    val gameBorderModifier =
        if (isGame) {
            Modifier.border(
                width = 1.5.dp,
                color = YadanTextPrimary,
                shape = MaterialTheme.shapes.large,
            )
        } else {
            Modifier
        }

    YadanCard(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .then(gameBorderModifier)
                .alpha(
                    if (isActiveCertified) {
                        0.6f
                    } else {
                        1f
                    },
                ),
        enabled = enabled,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 62.dp)
                    .padding(
                        horizontal = 11.dp,
                        vertical = 13.dp,
                    ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isEditMode) {
                YadanTravelDragHandle(
                    isFixed = isFixed,
                    modifier =
                        if (isFixed) {
                            Modifier
                        } else {
                            modifier
                        },
                )
            }

            YadanAsyncImage(
                imageUrl = place.spot.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                shape = YadanShapes.small,
                placeholderIcon =
                    place.spot.category.placeholderIcon(),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = place.spot.name,
                        modifier = Modifier.weight(1f, fill = false),
                        style =
                            YadanTypography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                            ),
                        color = YadanTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    YadanCategoryBadge(
                        text = place.spot.category.displayName,
                        style = place.spot.category.badgeStyle(),
                    )
                }

                supportingText
                    ?.takeIf { it.isNotBlank() }
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

            YadanTravelPlaceTrailingContent(
                isCertified = isCertified,
                mode = mode,
                isFixed = isFixed,
                onVerifyClick = onVerifyClick,
                onRemoveClick = onRemoveClick,
                enabled = enabled,
            )
        }
    }
}

/**
 * 편집 모드에서 장소 순서를 변경하는 드래그 핸들을 표시합니다.
 */
@Composable
private fun YadanTravelDragHandle(
    isFixed: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(23.dp)
                .alpha(
                    if (isFixed) {
                        0.25f
                    } else {
                        1f
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.DragIndicator,
            contentDescription =
                if (isFixed) {
                    null
                } else {
                    "순서 변경"
                },
            modifier = Modifier.size(19.dp),
            tint = YadanTextMuted,
        )
    }
}

/**
 * 진행 중 또는 편집 모드에 맞는 오른쪽 작업 영역을 표시합니다.
 */
@Composable
private fun YadanTravelPlaceTrailingContent(
    isCertified: Boolean,
    mode: YadanTravelPlaceItemMode,
    isFixed: Boolean,
    onVerifyClick: (() -> Unit)?,
    onRemoveClick: (() -> Unit)?,
    enabled: Boolean,
) {
    when (mode) {
        YadanTravelPlaceItemMode.VIEW,
        YadanTravelPlaceItemMode.COMPLETED -> Unit

        YadanTravelPlaceItemMode.ACTIVE -> {
            if (isCertified) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = YadanPrimaryInk,
                    )

                    Text(
                        text = "인증완료",
                        style =
                            YadanTypography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                            ),
                        color = YadanPrimaryInk,
                        maxLines = 1,
                    )
                }
            } else {
                YadanTravelVerifyButton(
                    onClick = onVerifyClick ?: {},
                    enabled = enabled && onVerifyClick != null,
                )
            }
        }

        YadanTravelPlaceItemMode.EDIT -> {
            if (isFixed) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = YadanTextMuted,
                    )

                    Text(
                        text = "고정",
                        style =
                            YadanTypography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                            ),
                        color = YadanTextMuted,
                        maxLines = 1,
                    )
                }
            } else {
                YadanIconButton(
                    onClick = onRemoveClick ?: {},
                    style = YadanIconButtonStyle.TONAL,
                    size = YadanIconButtonSize.SMALL,
                    enabled = enabled && onRemoveClick != null,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "일정에서 삭제",
                    )
                }
            }
        }
    }
}

/**
 * 진행 중인 여행 카드 안에서 사용하는 작은 방문 인증 버튼입니다.
 */
@Composable
private fun YadanTravelVerifyButton(
    onClick: () -> Unit,
    enabled: Boolean,
) {
    Box(
        modifier =
            Modifier
                .alpha(
                    if (enabled) {
                        1f
                    } else {
                        0.42f
                    },
                )
                .height(40.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(YadanPrimary)
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(horizontal = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "인증하기",
            style =
                YadanTypography.bodySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
            color = YadanOnPrimary,
            maxLines = 1,
        )
    }
}

@Preview(
    name = "Yadan travel place item",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    heightDp = 1040,
)
@Composable
private fun YadanTravelPlaceItemPreview() {
    val currentUserId = "user-1"

    val foodPlace =
        previewTravelPlace(
            id = "place-food",
            name = "돼지국밥 거리",
            category = TravelSpotCategory.FOOD,
            order = 1,
        )
    val stayPlace =
        previewTravelPlace(
            id = "place-stay",
            name = "스테이 광안",
            category = TravelSpotCategory.ACCOMMODATION,
            order = 2,
        )
    val gamePlace =
        previewTravelPlace(
            id = "place-game",
            name = "사직야구장",
            category = TravelSpotCategory.STADIUM,
            order = 3,
            certified = true,
        )
    val certifiedPlace =
        previewTravelPlace(
            id = "place-certified",
            name = "감천문화마을",
            category = TravelSpotCategory.CULTURE,
            order = 1,
            certified = true,
        )

    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
        ) {
            YadanTravelPlaceItem(
                place = foodPlace,
                currentUserId = currentUserId,
                isLast = false,
                onClick = {},
                supportingText = "11:30 · 음식",
            )

            YadanTravelPlaceItem(
                place = stayPlace,
                currentUserId = currentUserId,
                isLast = false,
                onClick = {},
                supportingText = "체크인 21:00",
            )

            YadanTravelPlaceItem(
                place = gamePlace,
                currentUserId = currentUserId,
                isLast = true,
                onClick = {},
                supportingText = "17:00 경기 시작",
            )

            Spacer(modifier = Modifier.height(12.dp))

            YadanTravelPlaceItem(
                place = certifiedPlace,
                currentUserId = currentUserId,
                isLast = true,
                onClick = {},
                mode = YadanTravelPlaceItemMode.ACTIVE,
                supportingText = "13:00 · 문화",
            )

            YadanTravelPlaceItem(
                place = foodPlace,
                currentUserId = currentUserId,
                isLast = true,
                onClick = {},
                mode = YadanTravelPlaceItemMode.ACTIVE,
                supportingText = "11:30 · 음식",
                onVerifyClick = {},
            )

            Spacer(modifier = Modifier.height(12.dp))

            YadanTravelPlaceItem(
                place = foodPlace,
                currentUserId = currentUserId,
                isLast = true,
                onClick = {},
                mode = YadanTravelPlaceItemMode.EDIT,
                onRemoveClick = {},
            )

            YadanTravelPlaceItem(
                place = gamePlace,
                currentUserId = currentUserId,
                isLast = true,
                onClick = {},
                mode = YadanTravelPlaceItemMode.EDIT,
                supportingText = "17:00 경기 시작",
            )

            Spacer(modifier = Modifier.height(12.dp))

            /*
             * 지난 여행의 인증 완료, 미인증, 직관 장소를 함께 확인합니다.
             */
            YadanTravelPlaceItem(
                place = certifiedPlace,
                currentUserId = currentUserId,
                isLast = false,
                onClick = {},
                mode = YadanTravelPlaceItemMode.COMPLETED,
                supportingText = "13:00 · 문화",
            )

            YadanTravelPlaceItem(
                place = foodPlace,
                currentUserId = currentUserId,
                isLast = false,
                onClick = {},
                mode = YadanTravelPlaceItemMode.COMPLETED,
                displayOrder = 2,
                supportingText = "11:30 · 음식",
            )

            YadanTravelPlaceItem(
                place = gamePlace,
                currentUserId = currentUserId,
                isLast = true,
                onClick = {},
                mode = YadanTravelPlaceItemMode.COMPLETED,
                supportingText = "17:00 · 직관",
            )
        }
    }
}

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
        id = id,
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
                                hour = 13,
                                minute = 0,
                                second = 0,
                                nanosecond = 0,
                            ),
                    ),
                )
            } else {
                emptyList()
            },
    )
