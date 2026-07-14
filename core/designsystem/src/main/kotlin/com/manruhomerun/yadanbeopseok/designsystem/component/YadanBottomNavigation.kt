package com.manruhomerun.yadanbeopseok.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 야단법석 메인 화면에서 사용하는 공통 하단 내비게이션입니다.
 *
 * 중앙 버튼의 자리를 제외한 일반 탭은 아래쪽 Row에 배치하고,
 * 중앙 버튼은 전체 내비게이션 위에 별도로 겹쳐 표시합니다.
 *
 * 실제 화면 경로와 NavController는 알지 않으며
 * 선택 상태와 클릭 이벤트를 전달받아 시각적인 구성만 담당합니다.
 *
 * 시스템 내비게이션 바 영역은 포함하지 않습니다.
 * 시스템 바 여백은 Scaffold 또는 상위 화면에서 처리합니다.
 *
 * @param centerAction 중앙에 표시할 여행 추가 버튼입니다.
 * @param modifier 하단 내비게이션의 크기와 배치에 사용할 Modifier입니다.
 * @param startItems 중앙 버튼 왼쪽에 표시할 두 개의 일반 탭입니다.
 * @param endItems 중앙 버튼 오른쪽에 표시할 두 개의 일반 탭입니다.
 */
@Composable
fun YadanBottomNavigation(
    centerAction: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    startItems: @Composable RowScope.() -> Unit,
    endItems: @Composable RowScope.() -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(BOTTOM_NAVIGATION_HEIGHT),
    ) {
        /*
         * HTML의 반투명 배경과 위쪽 구분선입니다.
         *
         * 웹의 backdrop-filter는 사용하지 않고
         * 기존 Surface 색상에 투명도만 적용합니다.
         */
        Surface(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(NAVIGATION_CONTAINER_HEIGHT),
            color =
                YadanSurface.copy(
                    alpha = NAVIGATION_CONTAINER_ALPHA,
                ),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                HorizontalDivider(
                    modifier = Modifier.align(Alignment.TopCenter),
                    thickness = NAVIGATION_DIVIDER_THICKNESS,
                    color = YadanOutline,
                )
            }
        }

        /*
         * Row에는 좌우 여백만 적용하여 일반 탭이 전체 62dp 높이를
         * 클릭 영역으로 사용할 수 있게 합니다.
         *
         * HTML의 위 9px와 아래 13px 여백은 각 탭 내부에 적용합니다.
         */
        Row(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(NAVIGATION_CONTAINER_HEIGHT)
                    .padding(
                        horizontal = NAVIGATION_HORIZONTAL_PADDING,
                    ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            startItems()

            /*
             * 중앙 버튼 자리도 일반 탭과 같은 weight를 사용합니다.
             */
            Spacer(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(),
            )

            endItems()
        }

        /*
         * 중앙 버튼은 일반 탭 Row 밖에서 배치합니다.
         *
         * 부모가 58dp 정사각형을 보장하므로 버튼이 높이 제약으로
         * 눌리지 않고 CircleShape가 정확한 원으로 표시됩니다.
         */
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .size(CENTER_ACTION_OUTER_SIZE),
            contentAlignment = Alignment.Center,
        ) {
            centerAction()
        }
    }
}

/**
 * 하단 내비게이션에 표시하는 일반 탭입니다.
 *
 * HTML처럼 별도의 선택 배경 없이 아이콘과 글자 색상만 변경합니다.
 * Compose의 selectable을 사용하여 선택 상태, Ripple 및 접근성을 처리합니다.
 *
 * 선택 영역은 하단 내비게이션 배경의 전체 높이인 62dp이며,
 * 아이콘과 글자에는 HTML의 위 9px, 아래 13px 여백을 적용합니다.
 *
 * [selectedIcon]을 전달하면 선택 상태에서 별도의 채워진 아이콘을
 * 사용할 수 있으므로 추후 SVG 아이콘으로 교체할 수 있습니다.
 *
 * @param selected 현재 선택된 탭인지 여부입니다.
 * @param onClick 탭을 눌렀을 때 실행할 작업입니다.
 * @param label 아이콘 아래에 표시할 탭 이름입니다.
 * @param modifier 탭의 배치에 사용할 Modifier입니다.
 * @param enabled 탭 활성화 여부입니다.
 * @param selectedIcon 선택 상태에서 표시할 아이콘입니다.
 * @param icon 기본 상태에서 표시할 아이콘입니다.
 */
@Composable
fun RowScope.YadanBottomNavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectedIcon: (@Composable () -> Unit)? = null,
    icon: @Composable () -> Unit,
) {
    val enabledContentColor =
        if (selected) {
            YadanPrimary
        } else {
            YadanTextMuted
        }

    val contentColor =
        if (enabled) {
            enabledContentColor
        } else {
            enabledContentColor.copy(
                alpha = DISABLED_ALPHA,
            )
        }

    CompositionLocalProvider(
        LocalContentColor provides contentColor,
    ) {
        Column(
            modifier =
                modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .selectable(
                        selected = selected,
                        interactionSource = null,
                        indication =
                            ripple(
                                bounded = false,
                                radius = 28.dp,
                                color = YadanPrimary,
                            ),
                        enabled = enabled,
                        role = Role.Tab,
                        onClick = onClick,
                    )
                    .padding(
                        top = NAVIGATION_TOP_PADDING,
                        bottom = NAVIGATION_BOTTOM_PADDING,
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier.size(NAVIGATION_ICON_SIZE),
                contentAlignment = Alignment.Center,
            ) {
                if (selected && selectedIcon != null) {
                    selectedIcon()
                } else {
                    icon()
                }
            }

            Spacer(
                modifier = Modifier.height(NAVIGATION_CONTENT_SPACING),
            )

            /*
             * Pretendard와 자간은 기존 타이포그래피를 재사용하고,
             * 하단 탭에만 필요한 HTML의 10px 크기만 조정합니다.
             */
            Text(
                text = label,
                style =
                    YadanTypography.labelSmall.copy(
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = contentColor,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

/**
 * 하단 내비게이션 중앙에 표시하는 여행 추가 버튼입니다.
 *
 * HTML의 `.tab.add .plus`처럼 내비게이션 위로 돌출된 원형 버튼입니다.
 * 일반 탭 Row와 분리되어 항상 58dp 정사각형으로 측정됩니다.
 *
 * 여행 만들기 화면으로 이동하는 처리는 [onClick]에서 담당합니다.
 *
 * @param onClick 중앙 버튼을 눌렀을 때 실행할 작업입니다.
 * @param modifier 버튼의 배치에 사용할 Modifier입니다.
 * @param enabled 버튼 활성화 여부입니다.
 * @param icon 버튼 안에 표시할 아이콘입니다.
 */
@Composable
fun YadanBottomNavigationCenterAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier =
            modifier
                .size(CENTER_ACTION_OUTER_SIZE)
                .alpha(
                    if (enabled) {
                        1f
                    } else {
                        DISABLED_ALPHA
                    },
                )
                .shadow(
                    elevation = CENTER_ACTION_SHADOW_ELEVATION,
                    shape = CircleShape,
                    clip = false,
                    ambientColor = YadanPrimary,
                    spotColor = YadanPrimary,
                ),
        enabled = enabled,
        shape = CircleShape,
        color = YadanPrimary,
        contentColor = YadanOnPrimary,
        border =
            BorderStroke(
                width = CENTER_ACTION_BORDER_WIDTH,
                color = YadanSurface,
            ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier.size(CENTER_ACTION_ICON_SIZE),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }
        }
    }
}

/*
 * HTML의 tabbar 높이와 중앙 버튼의 22px 돌출 영역입니다.
 */
private val NAVIGATION_CONTAINER_HEIGHT = 62.dp
private val CENTER_ACTION_PROTRUSION = 22.dp

private val BOTTOM_NAVIGATION_HEIGHT =
    NAVIGATION_CONTAINER_HEIGHT + CENTER_ACTION_PROTRUSION

/*
 * HTML의 tabbar 내부 여백입니다.
 */
private val NAVIGATION_HORIZONTAL_PADDING = 8.dp
private val NAVIGATION_TOP_PADDING = 9.dp
private val NAVIGATION_BOTTOM_PADDING = 13.dp

/*
 * HTML의 일반 탭 아이콘과 콘텐츠 간격입니다.
 */
private val NAVIGATION_ICON_SIZE = 25.dp
private val NAVIGATION_CONTENT_SPACING = 3.dp

/*
 * HTML의 50px 버튼과 4px 테두리를 합친 중앙 버튼 크기입니다.
 */
private val CENTER_ACTION_OUTER_SIZE = 58.dp
private val CENTER_ACTION_ICON_SIZE = 28.dp
private val CENTER_ACTION_BORDER_WIDTH = 4.dp
private val CENTER_ACTION_SHADOW_ELEVATION = 10.dp

private val NAVIGATION_DIVIDER_THICKNESS = 1.dp

private const val NAVIGATION_CONTAINER_ALPHA = 0.94f
private const val DISABLED_ALPHA = 0.42f

@Preview(
    name = "Yadan bottom navigation",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 84,
)
@Composable
private fun YadanBottomNavigationPreview() {
    YadanbeopseokTheme {
        Surface(
            color = YadanBackground,
        ) {
            YadanBottomNavigation(
                centerAction = {
                    YadanBottomNavigationCenterAction(
                        onClick = {},
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "새 여행 만들기",
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                },
                startItems = {
                    YadanBottomNavigationItem(
                        selected = true,
                        onClick = {},
                        label = "홈",
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    YadanBottomNavigationItem(
                        selected = false,
                        onClick = {},
                        label = "경기",
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsBaseball,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                },
                endItems = {
                    YadanBottomNavigationItem(
                        selected = false,
                        onClick = {},
                        label = "기록",
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    YadanBottomNavigationItem(
                        selected = false,
                        onClick = {},
                        label = "마이",
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                },
            )
        }
    }
}
