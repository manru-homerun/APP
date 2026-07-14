package com.manruhomerun.yadanbeopseok.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 관광지, 음식점, 친구 등을 검색할 때 사용하는 검색 입력창입니다.
 *
 * HTML의 `.searchbar`, `.searchbar.typed`, `.search-clear`에 대응합니다.
 *
 * 검색어가 입력되었거나 검색창에 포커스가 있으면
 * 테두리와 검색 아이콘을 대표 색상으로 강조합니다.
 *
 * @param query 현재 입력된 검색어입니다.
 * @param onQueryChange 검색어가 변경될 때 호출됩니다.
 * @param placeholder 검색어가 없을 때 표시할 안내 문구입니다.
 * @param modifier 검색창의 배치에 사용할 Modifier입니다.
 * @param enabled 검색창의 입력 가능 여부입니다.
 * @param onSearch 키보드의 검색 버튼을 눌렀을 때 호출됩니다.
 * @param clearContentDescription 검색어 지우기 버튼의 접근성 설명입니다.
 */
@Composable
fun YadanSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSearch: () -> Unit = {},
    clearContentDescription: String = "검색어 지우기",
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val isActive =
        enabled && (isFocused || query.isNotEmpty())

    val borderColor =
        if (isActive) {
            YadanPrimary
        } else {
            YadanOutline
        }

    val borderWidth =
        if (isActive) {
            ACTIVE_BORDER_WIDTH
        } else {
            DEFAULT_BORDER_WIDTH
        }

    val searchIconColor =
        if (isActive) {
            YadanPrimary
        } else {
            YadanTextMuted
        }

    val searchBarShape =
        RoundedCornerShape(SEARCH_BAR_CORNER_RADIUS)

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier =
            modifier
                .fillMaxWidth()
                .alpha(
                    if (enabled) {
                        ENABLED_ALPHA
                    } else {
                        DISABLED_ALPHA
                    },
                ),
        enabled = enabled,
        singleLine = true,
        textStyle =
            YadanTypography.bodyMedium.copy(
                fontSize = SEARCH_TEXT_SIZE,
                lineHeight = SEARCH_TEXT_LINE_HEIGHT,
                fontWeight = FontWeight.Bold,
                color = YadanTextPrimary,
            ),
        cursorBrush = SolidColor(YadanPrimary),
        keyboardOptions =
            KeyboardOptions(
                imeAction = ImeAction.Search,
            ),
        keyboardActions =
            KeyboardActions(
                onSearch = {
                    onSearch()
                    keyboardController?.hide()
                },
            ),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Row(
                modifier =
                    Modifier
                        .background(
                            color = YadanSurface,
                            shape = searchBarShape,
                        )
                        .border(
                            width = borderWidth,
                            color = borderColor,
                            shape = searchBarShape,
                        )
                        .padding(
                            horizontal = SEARCH_HORIZONTAL_PADDING,
                            vertical = SEARCH_VERTICAL_PADDING,
                        ),
                horizontalArrangement =
                    Arrangement.spacedBy(SEARCH_CONTENT_GAP),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(SEARCH_ICON_SIZE),
                    tint = searchIconColor,
                )

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style =
                                YadanTypography.bodyMedium.copy(
                                    fontSize = SEARCH_TEXT_SIZE,
                                    lineHeight = SEARCH_TEXT_LINE_HEIGHT,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            color = YadanTextMuted,
                            maxLines = 1,
                        )
                    }

                    innerTextField()
                }

                if (query.isNotEmpty()) {
                    SearchClearButton(
                        onClick = {
                            onQueryChange("")
                        },
                        enabled = enabled,
                        contentDescription = clearContentDescription,
                    )
                }
            }
        },
    )
}

/**
 * 검색어가 입력되었을 때 표시하는 지우기 버튼입니다.
 *
 * 기존 YadanIconButton의 최소 크기보다 작기 때문에
 * HTML의 19dp 크기를 유지하도록 검색창 내부에만 사용합니다.
 */
@Composable
private fun SearchClearButton(
    onClick: () -> Unit,
    enabled: Boolean,
    contentDescription: String,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(SEARCH_CLEAR_BUTTON_SIZE),
        enabled = enabled,
        shape = CircleShape,
        colors =
            IconButtonDefaults.iconButtonColors(
                containerColor = YadanTextMuted,
                contentColor = YadanOnPrimary,
                disabledContainerColor = YadanTextMuted,
                disabledContentColor = YadanOnPrimary,
            ),
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = contentDescription,
            modifier = Modifier.size(SEARCH_CLEAR_ICON_SIZE),
        )
    }
}

private val SEARCH_BAR_CORNER_RADIUS = 13.dp

private val DEFAULT_BORDER_WIDTH = 1.5.dp
private val ACTIVE_BORDER_WIDTH = 2.dp

private val SEARCH_HORIZONTAL_PADDING = 14.dp
private val SEARCH_VERTICAL_PADDING = 12.dp
private val SEARCH_CONTENT_GAP = 9.dp

private val SEARCH_ICON_SIZE = 20.dp
private val SEARCH_CLEAR_BUTTON_SIZE = 19.dp
private val SEARCH_CLEAR_ICON_SIZE = 13.dp

private val SEARCH_TEXT_SIZE = 13.5.sp
private val SEARCH_TEXT_LINE_HEIGHT = 20.sp

private const val ENABLED_ALPHA = 1f
private const val DISABLED_ALPHA = 0.42f

@Preview(
    name = "Yadan search bar",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanSearchBarPreview() {
    var emptyQuery by remember { mutableStateOf("") }
    var typedQuery by remember { mutableStateOf("감천") }

    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "입력 전",
                style = YadanTypography.labelMedium,
                color = YadanTextMuted,
            )

            YadanSearchBar(
                query = emptyQuery,
                onQueryChange = {
                    emptyQuery = it
                },
                placeholder = "관광지·음식을 검색해보세요",
            )

            Text(
                text = "검색어 입력",
                style = YadanTypography.labelMedium,
                color = YadanTextMuted,
            )

            YadanSearchBar(
                query = typedQuery,
                onQueryChange = {
                    typedQuery = it
                },
                placeholder = "관광지·음식을 검색해보세요",
            )

            Text(
                text = "친구 검색",
                style = YadanTypography.labelMedium,
                color = YadanTextMuted,
            )

            YadanSearchBar(
                query = "한별",
                onQueryChange = {},
                placeholder = "닉네임으로 검색해보세요",
            )
        }
    }
}
