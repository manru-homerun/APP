package com.manruhomerun.yadanbeopseok.friend.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanSearchBar
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDivider
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTint
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.friend.viewmodel.FriendSearchUiState
import com.manruhomerun.yadanbeopseok.model.FriendRelationshipStatus
import com.manruhomerun.yadanbeopseok.model.FriendSearchUser
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.UserProfile
import com.manruhomerun.yadanbeopseok.ui.component.YadanUserListItem

/** F·03의 닉네임 검색 결과와 친구 요청 상태를 표시합니다. */
@Composable
fun FriendSearchScreen(
    uiState: FriendSearchUiState,
    onBackClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSendFriendRequest: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        YadanTopAppBar(
            title = "친구 찾기",
            onNavigationClick = onBackClick,
        )

        YadanSearchBar(
            query = uiState.query,
            onQueryChange = onQueryChange,
            placeholder = "닉네임으로 검색",
            onSearch = onSearch,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.height(10.dp))

        when {
            uiState.isLoading -> {
                FriendLoadingContent(modifier = Modifier.weight(1f))
            }

            uiState.errorMessage != null -> {
                FriendMessageContent(
                    title = "검색 결과를 확인할 수 없습니다",
                    message = uiState.errorMessage,
                    actionLabel = "다시 시도",
                    onActionClick = onSearch,
                    modifier = Modifier.weight(1f),
                )
            }

            uiState.searchedQuery == null -> {
                FriendSearchInitialContent(modifier = Modifier.weight(1f))
            }

            uiState.users.isEmpty() -> {
                FriendMessageContent(
                    title = "검색 결과가 없습니다",
                    message = "다른 닉네임으로 검색해보세요.",
                    modifier = Modifier.weight(1f),
                )
            }

            else -> {
                FriendSearchResultContent(
                    uiState = uiState,
                    onSendFriendRequest = onSendFriendRequest,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun FriendSearchResultContent(
    uiState: FriendSearchUiState,
    onSendFriendRequest: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            bottom = 24.dp,
        ),
    ) {
        item(key = "result-header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "'${uiState.searchedQuery}' 검색 결과",
                    style = YadanTypography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = YadanTextPrimary,
                )

                Text(
                    text = "${uiState.resultCount}명",
                    style = YadanTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = YadanTextMuted,
                )
            }
        }

        items(
            items = uiState.users,
            key = { result -> result.user.id },
        ) { result ->
            FriendSearchResultRow(
                result = result,
                isRequesting = result.user.id in uiState.requestingUserIds,
                onSendFriendRequest = onSendFriendRequest,
            )

            HorizontalDivider(
                thickness = 1.dp,
                color = YadanDivider,
            )
        }

        item(key = "search-note") {
            FriendSearchInfoNote(
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun FriendSearchResultRow(
    result: FriendSearchUser,
    isRequesting: Boolean,
    onSendFriendRequest: (String) -> Unit,
) {
    val (buttonText, buttonStyle) =
        when (result.relationshipStatus) {
            FriendRelationshipStatus.NONE -> "친구 신청" to FriendActionButtonStyle.PRIMARY
            FriendRelationshipStatus.REQUEST_SENT -> "요청됨" to FriendActionButtonStyle.WAITING
            FriendRelationshipStatus.REQUEST_RECEIVED -> "받은 요청" to FriendActionButtonStyle.WAITING
            FriendRelationshipStatus.FRIEND -> "친구" to FriendActionButtonStyle.WAITING
            FriendRelationshipStatus.UNKNOWN -> "확인 불가" to FriendActionButtonStyle.WAITING
        }

    val canSendRequest =
        result.relationshipStatus == FriendRelationshipStatus.NONE && !isRequesting

    YadanUserListItem(
        user = result.user,
        trailingContent = {
            FriendActionButton(
                text = if (isRequesting) "요청 중" else buttonText,
                onClick = {
                    onSendFriendRequest(result.user.id)
                },
                style = buttonStyle,
                enabled = canSendRequest,
            )
        },
    )
}

@Composable
private fun FriendSearchInitialContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "찾고 싶은 친구의 닉네임을 입력해주세요.",
            style = YadanTypography.bodyMedium,
            color = YadanTextSecondary,
            textAlign = TextAlign.Center,
        )

        FriendSearchInfoNote(modifier = Modifier.padding(top = 18.dp))
    }
}

@Composable
private fun FriendSearchInfoNote(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = YadanPrimaryTint,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(
                horizontal = 12.dp,
                vertical = 10.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = YadanPrimary,
        )

        Text(
            text = "닉네임으로 검색해 야구 친구를 추가하고, 함께 원정 여행을 떠나보세요.",
            style = YadanTypography.bodySmall,
            color = YadanTextSecondary,
        )
    }
}

private val previewSearchUsers = listOf(
    FriendSearchUser(
        user = UserProfile(id = "user-1", nickname = "한별", favoriteTeam = KboTeam.LOTTE),
        relationshipStatus = FriendRelationshipStatus.NONE,
    ),
    FriendSearchUser(
        user = UserProfile(id = "user-2", nickname = "우진", favoriteTeam = KboTeam.SSG),
        relationshipStatus = FriendRelationshipStatus.REQUEST_SENT,
    ),
)

@Preview(name = "F03 친구 찾기", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun FriendSearchScreenPreview() {
    YadanbeopseokTheme {
        FriendSearchScreen(
            uiState = FriendSearchUiState(
                query = "한별",
                searchedQuery = "한별",
                users = previewSearchUsers,
                resultCount = 2,
            ),
            onBackClick = {},
            onQueryChange = {},
            onSearch = {},
            onSendFriendRequest = {},
        )
    }
}

@Preview(name = "F03 검색 전", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun FriendSearchInitialPreview() {
    YadanbeopseokTheme {
        FriendSearchScreen(
            uiState = FriendSearchUiState(),
            onBackClick = {},
            onQueryChange = {},
            onSearch = {},
            onSendFriendRequest = {},
        )
    }
}
