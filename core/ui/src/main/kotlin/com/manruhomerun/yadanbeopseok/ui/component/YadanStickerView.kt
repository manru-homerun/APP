package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.Sticker

/**
 * 스티커가 사용되는 화면에 따른 표시 크기입니다.
 */
enum class YadanStickerSize {
    /** 지난 여행 상세와 사진 캔버스에 사용하는 66dp 크기입니다. */
    DEFAULT,

    /** 스티커 획득 완료 화면에 사용하는 108dp 크기입니다. */
    LARGE,

    /** 사진 꾸미기 하단 선택 목록에 사용하는 52dp 크기입니다. */
    TRAY,
}

/**
 * 획득한 스티커 이미지 또는 잠긴 스티커 영역을 표시합니다.
 *
 * D01b 지난 여행 상세, D03 스티커 획득 화면과
 * D04 스티커 사진 꾸미기 화면에서 재사용합니다.
 *
 * @param sticker 표시할 스티커입니다. 잠긴 슬롯에서는 null을 전달할 수 있습니다.
 * @param contentDescription 스티커를 설명하는 접근성 문구입니다.
 * 장식용으로 사용할 때는 null을 전달합니다.
 * @param modifier 컴포넌트의 배치와 추가 효과를 지정합니다.
 * @param size 화면 용도에 따른 스티커 크기입니다.
 * @param selected 사진 꾸미기 목록에서 현재 선택된 스티커인지 나타냅니다.
 * @param locked 획득하지 않은 잠긴 스티커인지 나타냅니다.
 * @param enabled 스티커 활성화 여부입니다.
 * @param onClick 스티커를 선택했을 때 실행할 작업입니다.
 * null이면 클릭할 수 없는 이미지로 표시합니다.
 */
@Composable
fun YadanStickerView(
    sticker: Sticker?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: YadanStickerSize = YadanStickerSize.DEFAULT,
    selected: Boolean = false,
    locked: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val visuals = size.visuals()
    val isLocked = locked || sticker == null
    val resolvedContentDescription =
        when {
            !contentDescription.isNullOrBlank() -> contentDescription
            isLocked -> "잠긴 스티커"
            else -> null
        }

    val clickModifier =
        if (onClick != null) {
            Modifier
                .clip(CircleShape)
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                )
        } else {
            Modifier
        }

    Box(
        modifier =
            modifier
                .size(visuals.diameter)
                .alpha(
                    if (enabled) {
                        1f
                    } else {
                        0.42f
                    },
                )
                .shadow(
                    elevation =
                        if (isLocked) {
                            0.dp
                        } else {
                            visuals.elevation
                        },
                    shape = CircleShape,
                    clip = false,
                )
                .then(clickModifier)
                .semantics {
                    resolvedContentDescription?.let {
                        this.contentDescription = it
                    }

                    if (isLocked) {
                        stateDescription = "잠김"
                    } else if (onClick != null) {
                        this.selected = selected
                    }
                },
        contentAlignment = Alignment.Center,
    ) {
        if (isLocked) {
            YadanLockedSticker(
                iconSize = visuals.lockIconSize,
                modifier = Modifier.matchParentSize(),
            )
        } else {
            YadanStickerImage(
                sticker = checkNotNull(sticker),
                borderWidth = visuals.borderWidth,
                selected = selected,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

/**
 * 스티커 이미지를 원형 테두리와 함께 표시합니다.
 */
@Composable
private fun YadanStickerImage(
    sticker: Sticker,
    borderWidth: Dp,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val selectionModifier =
        if (selected) {
            Modifier
                .border(
                    width = 3.dp,
                    color = YadanPrimary,
                    shape = CircleShape,
                )
                .padding(3.dp)
        } else {
            Modifier
        }

    Box(
        modifier =
            modifier
                .then(selectionModifier)
                .border(
                    width = borderWidth,
                    color = YadanOnPrimary,
                    shape = CircleShape,
                )
                .clip(CircleShape),
    ) {
        /*
         * 접근성 문구는 바깥 YadanStickerView에서 제공하므로
         * 네트워크 이미지에는 별도 설명을 전달하지 않습니다.
         */
        YadanAsyncImage(
            imageUrl = sticker.imageUrl,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            shape = CircleShape,
            contentScale = ContentScale.Fit,
            placeholderIcon = Icons.Outlined.WorkspacePremium,
        )
    }
}

/**
 * 획득하지 않은 스티커를 점선 원과 잠금 아이콘으로 표시합니다.
 */
@Composable
private fun YadanLockedSticker(
    iconSize: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier.drawBehind {
                val borderWidth = 2.dp.toPx()
                val radius =
                    (
                        minOf(
                            size.width,
                            size.height,
                        ) - borderWidth
                        ) / 2f

                drawCircle(
                    color = YadanOnPrimary.copy(alpha = 0.30f),
                    radius = radius,
                    style =
                        Stroke(
                            width = borderWidth,
                            pathEffect =
                                PathEffect.dashPathEffect(
                                    intervals =
                                        floatArrayOf(
                                            5.dp.toPx(),
                                            4.dp.toPx(),
                                        ),
                                ),
                        ),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = YadanOnPrimary.copy(alpha = 0.50f),
        )
    }
}

/**
 * 화면별 스티커 크기와 테두리·그림자 값을 반환합니다.
 */
private fun YadanStickerSize.visuals(): YadanStickerVisuals =
    when (this) {
        YadanStickerSize.DEFAULT ->
            YadanStickerVisuals(
                diameter = 66.dp,
                borderWidth = 4.dp,
                elevation = 6.dp,
                lockIconSize = 24.dp,
            )

        YadanStickerSize.LARGE ->
            YadanStickerVisuals(
                diameter = 108.dp,
                borderWidth = 5.dp,
                elevation = 8.dp,
                lockIconSize = 32.dp,
            )

        YadanStickerSize.TRAY ->
            YadanStickerVisuals(
                diameter = 52.dp,
                borderWidth = 3.dp,
                elevation = 4.dp,
                lockIconSize = 22.dp,
            )
    }

/**
 * 크기별 스티커 표현에 필요한 내부 값입니다.
 */
private data class YadanStickerVisuals(
    val diameter: Dp,
    val borderWidth: Dp,
    val elevation: Dp,
    val lockIconSize: Dp,
)

@Preview(
    name = "Yadan sticker views",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
)
@Composable
private fun YadanStickerViewPreview() {
    val sticker =
        Sticker(
            id = "sticker-sajik",
            stickerPackId = "pack-busan-2026",
            imageUrl = "",
        )
    var selected by remember {
        mutableStateOf(true)
    }

    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                YadanStickerView(
                    sticker = sticker,
                    contentDescription = "사직 직관 스티커",
                )

                YadanStickerView(
                    sticker = sticker,
                    contentDescription = "사직 직관 스티커",
                    size = YadanStickerSize.LARGE,
                )
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            color = YadanTextPrimary,
                            shape = YadanShapes.medium,
                        )
                        .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                YadanStickerView(
                    sticker = sticker,
                    contentDescription = "사직 직관 스티커",
                    size = YadanStickerSize.TRAY,
                    selected = selected,
                    onClick = {
                        selected = !selected
                    },
                )

                YadanStickerView(
                    sticker = null,
                    contentDescription = "미획득 스티커",
                    size = YadanStickerSize.TRAY,
                    locked = true,
                )
            }
        }
    }
}
