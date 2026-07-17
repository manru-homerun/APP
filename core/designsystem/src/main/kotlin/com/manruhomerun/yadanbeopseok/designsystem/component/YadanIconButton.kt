package com.manruhomerun.yadanbeopseok.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDivider
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanFavorite
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryDark
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 야단법석 아이콘 버튼의 시각적 유형입니다.
 */
enum class YadanIconButtonStyle {
    /**
     * 일반 화면과 상단 앱 바에서 사용하는 기본 유형입니다.
     *
     * HTML의 `.iconbtn`, `.bell` 유형에 대응합니다.
     */
    DEFAULT,

    /**
     * 더보기처럼 강조하지 않는 작업에 사용하는 유형입니다.
     *
     * HTML의 `.fmore` 유형에 대응합니다.
     */
    MUTED,

    /**
     * 연한 배경이 있는 보조 아이콘 버튼입니다.
     *
     * HTML의 달력 이전·다음 버튼에 대응합니다.
     */
    TONAL,

    /**
     * 어두운 배경 위에서 사용하는 흰색 아이콘 버튼입니다.
     */
    ON_DARK,
}

/**
 * 야단법석 아이콘 버튼의 크기입니다.
 */
enum class YadanIconButtonSize {
    /**
     * 상단 앱 바와 알림 버튼에 사용하는 기본 크기입니다.
     *
     * 컨테이너 42dp, 아이콘 24dp입니다.
     */
    DEFAULT,

    /**
     * 달력 이동 버튼에 사용하는 중간 크기입니다.
     *
     * 컨테이너 34dp, 아이콘 19dp입니다.
     */
    MEDIUM,

    /**
     * 친구 목록의 더보기 버튼에 사용하는 작은 크기입니다.
     *
     * 컨테이너 30dp, 아이콘 20dp입니다.
     */
    SMALL,
}

/**
 * 야단법석 화면에서 공통으로 사용하는 아이콘 버튼입니다.
 *
 * Material3 [IconButton]의 클릭 처리, Ripple 및 접근성을 사용하고
 * 시각적인 크기와 색상은 HTML 디자인에 맞게 적용합니다.
 *
 * [icon]에는 Material [Icon] 또는 추후 추가할 SVG 기반 아이콘을
 * 전달할 수 있습니다. 아이콘의 `contentDescription`은 호출하는
 * 화면에서 버튼의 기능에 맞게 지정해야 합니다.
 *
 * @param onClick 버튼을 눌렀을 때 실행할 작업입니다.
 * @param modifier 버튼의 배치에 사용할 Modifier입니다.
 * @param style 버튼의 배경색과 콘텐츠 색상 유형입니다.
 * @param size 버튼 컨테이너와 내부 아이콘의 크기입니다.
 * @param enabled 버튼 활성화 여부입니다.
 * @param icon 버튼에 표시할 아이콘입니다.
 */
@Composable
fun YadanIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: YadanIconButtonStyle = YadanIconButtonStyle.DEFAULT,
    size: YadanIconButtonSize = YadanIconButtonSize.DEFAULT,
    enabled: Boolean = true,
    icon: @Composable () -> Unit,
) {
    val dimensions = iconButtonDimensions(size)

    val (containerColor, contentColor) =
        when (style) {
            YadanIconButtonStyle.DEFAULT ->
                Color.Transparent to YadanTextPrimary

            YadanIconButtonStyle.MUTED ->
                Color.Transparent to YadanTextMuted

            YadanIconButtonStyle.TONAL ->
                YadanDivider to YadanTextSecondary

            YadanIconButtonStyle.ON_DARK ->
                Color.Transparent to YadanOnPrimary
        }

    /*
     * HTML의 일반 아이콘 버튼은 원형이고,
     * 달력 이동 버튼만 10dp 모서리의 사각형입니다.
     */
    val shape =
        when (style) {
            YadanIconButtonStyle.TONAL ->
                RoundedCornerShape(TONAL_CORNER_RADIUS)

            else ->
                CircleShape
        }

    /*
     * HTML 체크박스 및 버튼과 동일한 비활성 투명도를 사용합니다.
     * Material의 비활성 색상은 원래 색상으로 유지하고
     * 버튼 전체 투명도는 Modifier.alpha에서 처리합니다.
     */
    val buttonAlpha =
        if (enabled) {
            ENABLED_ALPHA
        } else {
            DISABLED_ALPHA
        }

    IconButton(
        onClick = onClick,
        modifier =
            modifier
                .alpha(buttonAlpha)
                .size(dimensions.containerSize),
        enabled = enabled,
        shape = shape,
        colors =
            IconButtonDefaults.iconButtonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = containerColor,
                disabledContentColor = contentColor,
            ),
    ) {
        /*
         * 전달받은 아이콘 종류와 관계없이 HTML에 지정된
         * 아이콘 크기를 유지하도록 최대 영역을 제한합니다.
         */
        Box(
            modifier = Modifier.size(dimensions.iconSize),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
    }
}

/**
 * 선택 상태를 가지는 야단법석 아이콘 버튼입니다.
 *
 * 찜, 북마크처럼 선택 여부에 따라 아이콘과 색상이 달라지는
 * 아이콘 버튼에 사용합니다.
 *
 * 기존 [YadanIconButtonSize]와 크기 계산을 재사용하며,
 * 선택 상태와 접근성 처리는 Material3 [IconToggleButton]에 맡깁니다.
 *
 * @param checked 현재 선택 여부입니다.
 * @param onCheckedChange 선택 상태가 변경될 때 호출됩니다.
 * @param modifier 버튼 배치에 사용할 Modifier입니다.
 * @param size 버튼 컨테이너와 아이콘 크기입니다.
 * @param enabled 버튼 활성화 여부입니다.
 * @param uncheckedContentColor 선택되지 않았을 때의 아이콘 색상입니다.
 * @param checkedContentColor 선택되었을 때의 아이콘 색상입니다.
 * @param icon 현재 선택 상태를 전달받아 표시할 아이콘입니다.
 */
@Composable
fun YadanIconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: YadanIconButtonSize = YadanIconButtonSize.MEDIUM,
    enabled: Boolean = true,
    uncheckedContentColor: Color = YadanTextMuted,
    checkedContentColor: Color = YadanPrimary,
    icon: @Composable (checked: Boolean) -> Unit,
) {
    val dimensions = iconButtonDimensions(size)
    val buttonAlpha =
        if (enabled) {
            ENABLED_ALPHA
        } else {
            DISABLED_ALPHA
        }

    IconToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier =
            modifier
                .alpha(buttonAlpha)
                .size(dimensions.containerSize),
        enabled = enabled,
        shape = CircleShape,
        colors =
            IconButtonDefaults.iconToggleButtonColors(
                containerColor = Color.Transparent,
                contentColor = uncheckedContentColor,
                disabledContainerColor = Color.Transparent,
                disabledContentColor =
                    if (checked) {
                        checkedContentColor
                    } else {
                        uncheckedContentColor
                    },
                checkedContainerColor = Color.Transparent,
                checkedContentColor = checkedContentColor,
            ),
    ) {
        Box(
            modifier = Modifier.size(dimensions.iconSize),
            contentAlignment = Alignment.Center,
        ) {
            icon(checked)
        }
    }
}

/**
 * 아이콘 버튼 크기에 맞는 컨테이너와 아이콘 크기를 반환합니다.
 */
private fun iconButtonDimensions(
    size: YadanIconButtonSize,
): YadanIconButtonDimensions =
    when (size) {
        YadanIconButtonSize.DEFAULT ->
            YadanIconButtonDimensions(
                containerSize = DEFAULT_CONTAINER_SIZE,
                iconSize = DEFAULT_ICON_SIZE,
            )

        YadanIconButtonSize.MEDIUM ->
            YadanIconButtonDimensions(
                containerSize = MEDIUM_CONTAINER_SIZE,
                iconSize = MEDIUM_ICON_SIZE,
            )

        YadanIconButtonSize.SMALL ->
            YadanIconButtonDimensions(
                containerSize = SMALL_CONTAINER_SIZE,
                iconSize = SMALL_ICON_SIZE,
            )
    }

/**
 * 아이콘 버튼의 컨테이너와 아이콘 크기를 함께 보관합니다.
 */
private data class YadanIconButtonDimensions(
    val containerSize: Dp,
    val iconSize: Dp,
)

private val DEFAULT_CONTAINER_SIZE = 42.dp
private val DEFAULT_ICON_SIZE = 24.dp

private val MEDIUM_CONTAINER_SIZE = 34.dp
private val MEDIUM_ICON_SIZE = 19.dp

private val SMALL_CONTAINER_SIZE = 30.dp
private val SMALL_ICON_SIZE = 20.dp

private val TONAL_CORNER_RADIUS = 10.dp

private const val ENABLED_ALPHA = 1f
private const val DISABLED_ALPHA = 0.42f

@Preview(
    name = "Yadan icon buttons - HTML examples",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanIconButtonPreview() {
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
                text = "밝은 배경",
                style = MaterialTheme.typography.labelMedium,
                color = YadanTextMuted,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButtonPreviewItem(
                    label = "기본 42dp",
                ) {
                    YadanIconButton(
                        onClick = {},
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "알림",
                        )
                    }
                }

                IconButtonPreviewItem(
                    label = "보조 34dp",
                ) {
                    YadanIconButton(
                        onClick = {},
                        style = YadanIconButtonStyle.TONAL,
                        size = YadanIconButtonSize.MEDIUM,
                    ) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "이전 달",
                        )
                    }
                }

                IconButtonPreviewItem(
                    label = "더보기 30dp",
                ) {
                    YadanIconButton(
                        onClick = {},
                        style = YadanIconButtonStyle.MUTED,
                        size = YadanIconButtonSize.SMALL,
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "더보기",
                        )
                    }
                }
            }

            Text(
                text = "어두운 배경",
                style = MaterialTheme.typography.labelMedium,
                color = YadanTextMuted,
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            color = YadanPrimaryDark,
                            shape = MaterialTheme.shapes.medium,
                        )
                        .padding(12.dp),
                contentAlignment = Alignment.Center,
            ) {
                YadanIconButton(
                    onClick = {},
                    style = YadanIconButtonStyle.ON_DARK,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                    )
                }
            }
        }
    }
}

/**
 * Preview에서 아이콘 버튼과 용도를 함께 표시합니다.
 */
@Composable
private fun IconButtonPreviewItem(
    label: String,
    content: @Composable () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        content()

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = YadanTextMuted,
        )
    }
}

@Preview(
    name = "Yadan icon toggle button",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanIconToggleButtonPreview() {
    YadanbeopseokTheme {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf(false, true).forEach { checked ->
                YadanIconToggleButton(
                    checked = checked,
                    onCheckedChange = {},
                    checkedContentColor = YadanFavorite,
                ) { selected ->
                    Icon(
                        imageVector =
                            if (selected) {
                                Icons.Default.Favorite
                            } else {
                                Icons.Outlined.FavoriteBorder
                            },
                        contentDescription =
                            if (selected) "찜 해제" else "찜하기",
                    )
                }
            }
        }
    }
}
