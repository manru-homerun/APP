package com.manruhomerun.yadanbeopseok.friend.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconButtonSize
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconButtonStyle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTabItem
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTabRow
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDivider
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.friend.viewmodel.FriendManagementTab
import com.manruhomerun.yadanbeopseok.friend.viewmodel.FriendManagementUiState
import com.manruhomerun.yadanbeopseok.model.Friend
import com.manruhomerun.yadanbeopseok.model.FriendRequest
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.UserProfile
import com.manruhomerun.yadanbeopseok.ui.component.YadanUserListItem
import com.manruhomerun.yadanbeopseok.ui.displayNickname

/**
 * F·01 친구 목록과 F·02 받은·보낸 친구 요청을 탭으로 표시합니다.
 */
@Composable
fun FriendManagementScreen(
    uiState: FriendManagementUiState,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onTabSelected: (FriendManagementTab) -> Unit,
    onAcceptRequest: (String) -> Unit,
    onRejectRequest: (String) -> Unit,
    onCancelRequest: (String) -> Unit,
    onDeleteFriend: (String) -> Unit,
    onRetryFriends: () -> Unit,
    onRetryReceivedRequests: () -> Unit,
    onRetrySentRequests: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expandedFriendMenuId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDeleteFriendId by rememberSaveable { mutableStateOf<String?>(null) }

    val pendingDeleteFriend = uiState.friends.firstOrNull { friend ->
        friend.id == pendingDeleteFriendId
    }

    LaunchedEffect(pendingDeleteFriendId, uiState.friends) {
        if (pendingDeleteFriendId != null && pendingDeleteFriend == null) {
            pendingDeleteFriendId = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        YadanTopAppBar(
            title = "친구",
            onNavigationClick = onBackClick,
            trailingContent = {
                YadanIconButton(
                    onClick = onSearchClick,
                    style = YadanIconButtonStyle.DEFAULT,
                    size = YadanIconButtonSize.DEFAULT,
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "친구 찾기",
                    )
                }
            },
        )

        YadanTabRow(
            tabs = listOf(
                YadanTabItem(
                    label = "친구",
                    count = uiState.friendCount.toTabCount(),
                ),
                YadanTabItem(
                    label = "요청",
                    count = uiState.receivedRequestCount.toTabCount(),
                ),
            ),
            selectedIndex = uiState.selectedTab.ordinal,
            onTabSelected = { index ->
                FriendManagementTab.entries.getOrNull(index)?.let(onTabSelected)
            },
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        when (uiState.selectedTab) {
            FriendManagementTab.FRIENDS -> {
                FriendListContent(
                    uiState = uiState,
                    expandedFriendMenuId = expandedFriendMenuId,
                    onExpandedFriendMenuChange = { friendId ->
                        expandedFriendMenuId = friendId
                    },
                    onDeleteMenuClick = { friendId ->
                        expandedFriendMenuId = null
                        pendingDeleteFriendId = friendId
                    },
                    onRetryClick = onRetryFriends,
                    modifier = Modifier.weight(1f),
                )
            }

            FriendManagementTab.REQUESTS -> {
                FriendRequestContent(
                    uiState = uiState,
                    onAcceptRequest = onAcceptRequest,
                    onRejectRequest = onRejectRequest,
                    onCancelRequest = onCancelRequest,
                    onRetryReceivedRequests = onRetryReceivedRequests,
                    onRetrySentRequests = onRetrySentRequests,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    pendingDeleteFriend?.let { friend ->
        FriendDeleteDialog(
            friend = friend,
            isDeleting = uiState.deletingFriendId == friend.id,
            onDismiss = {
                if (uiState.deletingFriendId == null) {
                    pendingDeleteFriendId = null
                }
            },
            onConfirm = {
                onDeleteFriend(friend.id)
            },
        )
    }
}

@Composable
private fun FriendListContent(
    uiState: FriendManagementUiState,
    expandedFriendMenuId: String?,
    onExpandedFriendMenuChange: (String?) -> Unit,
    onDeleteMenuClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isFriendsLoading && uiState.friends.isEmpty() -> {
            FriendLoadingContent(modifier)
        }

        uiState.friendsErrorMessage != null && uiState.friends.isEmpty() -> {
            FriendMessageContent(
                title = "친구 목록을 확인할 수 없습니다",
                message = uiState.friendsErrorMessage,
                actionLabel = "다시 시도",
                onActionClick = onRetryClick,
                modifier = modifier,
            )
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 24.dp,
                ),
            ) {
                uiState.friendsErrorMessage?.let { message ->
                    item(key = "friends-error") {
                        FriendInlineRetry(
                            message = message,
                            onRetryClick = onRetryClick,
                        )
                    }
                }

                if (uiState.friends.isEmpty()) {
                    item(key = "friends-empty") {
                        FriendEmptyListMessage(
                            text = "아직 추가한 친구가 없습니다.",
                        )
                    }
                } else {
                    items(
                        items = uiState.friends,
                        key = { friend -> friend.id },
                    ) { friend ->
                        FriendListRow(
                            friend = friend,
                            isMenuExpanded = expandedFriendMenuId == friend.id,
                            isDeleting = uiState.deletingFriendId == friend.id,
                            onMenuClick = {
                                onExpandedFriendMenuChange(friend.id)
                            },
                            onMenuDismiss = {
                                onExpandedFriendMenuChange(null)
                            },
                            onDeleteClick = {
                                onDeleteMenuClick(friend.id)
                            },
                        )

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = YadanDivider,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendListRow(
    friend: Friend,
    isMenuExpanded: Boolean,
    isDeleting: Boolean,
    onMenuClick: () -> Unit,
    onMenuDismiss: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    YadanUserListItem(
        user = friend.user,
        enabled = !isDeleting,
        trailingContent = {
            Box {
                YadanIconButton(
                    onClick = onMenuClick,
                    style = YadanIconButtonStyle.MUTED,
                    size = YadanIconButtonSize.SMALL,
                    enabled = !isDeleting,
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "${friend.user.displayNickname()} 친구 메뉴",
                    )
                }

                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = onMenuDismiss,
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(text = "친구 삭제")
                        },
                        onClick = onDeleteClick,
                    )
                }
            }
        },
    )
}

@Composable
private fun FriendRequestContent(
    uiState: FriendManagementUiState,
    onAcceptRequest: (String) -> Unit,
    onRejectRequest: (String) -> Unit,
    onCancelRequest: (String) -> Unit,
    onRetryReceivedRequests: () -> Unit,
    onRetrySentRequests: () -> Unit,
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
        item(key = "received-header") {
            FriendRequestSectionHeader(
                title = "받은 요청",
                count = uiState.receivedRequestCount,
            )
        }

        when {
            uiState.isReceivedRequestsLoading && uiState.receivedRequests.isEmpty() -> {
                item(key = "received-loading") {
                    FriendSectionLoading()
                }
            }

            uiState.receivedRequestsErrorMessage != null && uiState.receivedRequests.isEmpty() -> {
                item(key = "received-error") {
                    FriendInlineRetry(
                        message = uiState.receivedRequestsErrorMessage,
                        onRetryClick = onRetryReceivedRequests,
                    )
                }
            }

            uiState.receivedRequests.isEmpty() -> {
                item(key = "received-empty") {
                    FriendEmptyListMessage(text = "받은 친구 요청이 없습니다.")
                }
            }

            else -> {
                uiState.receivedRequestsErrorMessage?.let { message ->
                    item(key = "received-refresh-error") {
                        FriendInlineRetry(
                            message = message,
                            onRetryClick = onRetryReceivedRequests,
                        )
                    }
                }

                items(
                    items = uiState.receivedRequests,
                    key = { request -> "received_${request.id}" },
                ) { request ->
                    val isProcessing = request.id in uiState.processingRequestIds

                    YadanUserListItem(
                        user = request.user,
                        trailingContent = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                FriendActionButton(
                                    text = "수락",
                                    onClick = {
                                        onAcceptRequest(request.id)
                                    },
                                    style = FriendActionButtonStyle.PRIMARY,
                                    enabled = !isProcessing,
                                )

                                FriendActionButton(
                                    text = "거절",
                                    onClick = {
                                        onRejectRequest(request.id)
                                    },
                                    style = FriendActionButtonStyle.OUTLINED,
                                    enabled = !isProcessing,
                                )
                            }
                        },
                    )

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = YadanDivider,
                    )
                }
            }
        }

        item(key = "sent-header") {
            FriendRequestSectionHeader(
                title = "보낸 요청",
                count = uiState.sentRequestCount,
                modifier = Modifier.padding(top = 18.dp),
            )
        }

        when {
            uiState.isSentRequestsLoading && uiState.sentRequests.isEmpty() -> {
                item(key = "sent-loading") {
                    FriendSectionLoading()
                }
            }

            uiState.sentRequestsErrorMessage != null && uiState.sentRequests.isEmpty() -> {
                item(key = "sent-error") {
                    FriendInlineRetry(
                        message = uiState.sentRequestsErrorMessage,
                        onRetryClick = onRetrySentRequests,
                    )
                }
            }

            uiState.sentRequests.isEmpty() -> {
                item(key = "sent-empty") {
                    FriendEmptyListMessage(text = "보낸 친구 요청이 없습니다.")
                }
            }

            else -> {
                uiState.sentRequestsErrorMessage?.let { message ->
                    item(key = "sent-refresh-error") {
                        FriendInlineRetry(
                            message = message,
                            onRetryClick = onRetrySentRequests,
                        )
                    }
                }

                items(
                    items = uiState.sentRequests,
                    key = { request -> "sent_${request.id}" },
                ) { request ->
                    val isProcessing = request.id in uiState.processingRequestIds

                    YadanUserListItem(
                        user = request.user,
                        trailingContent = {
                            FriendActionButton(
                                text = "대기중",
                                onClick = {
                                    onCancelRequest(request.id)
                                },
                                style = FriendActionButtonStyle.WAITING,
                                enabled = !isProcessing,
                            )
                        },
                    )

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = YadanDivider,
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendRequestSectionHeader(title: String, count: Long?, modifier: Modifier = Modifier) {
    Text(
        text = if (count == null) title else "$title $count",
        modifier = modifier.padding(vertical = 10.dp),
        style = YadanTypography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
        color = YadanTextPrimary,
    )
}

/** F 그룹의 목록 행에서만 사용하는 작은 작업 버튼입니다. */
@Composable
internal fun FriendActionButton(
    text: String,
    onClick: () -> Unit,
    style: FriendActionButtonStyle,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(8.dp)
    val buttonModifier = modifier
        .height(34.dp)
        .widthIn(min = 54.dp)
    val contentPadding = PaddingValues(horizontal = 11.dp)

    when (style) {
        FriendActionButtonStyle.PRIMARY -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = shape,
                contentPadding = contentPadding,
                colors = ButtonDefaults.buttonColors(
                    containerColor = YadanPrimary,
                    contentColor = Color.White,
                ),
            ) {
                FriendActionButtonText(text)
            }
        }

        FriendActionButtonStyle.OUTLINED -> {
            OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = shape,
                contentPadding = contentPadding,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = YadanTextSecondary),
                border = BorderStroke(1.dp, YadanOutline),
            ) {
                FriendActionButtonText(text)
            }
        }

        FriendActionButtonStyle.WAITING -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = shape,
                contentPadding = contentPadding,
                colors = ButtonDefaults.buttonColors(
                    containerColor = YadanDivider,
                    contentColor = YadanTextMuted,
                ),
            ) {
                FriendActionButtonText(text)
            }
        }
    }
}

@Composable
private fun FriendActionButtonText(text: String) {
    Text(
        text = text,
        style = YadanTypography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
        maxLines = 1,
    )
}

internal enum class FriendActionButtonStyle {
    PRIMARY,
    OUTLINED,
    WAITING,
}

@Composable
private fun FriendDeleteDialog(friend: Friend, isDeleting: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = {
            if (!isDeleting) onDismiss()
        },
        title = {
            Text(text = "친구 삭제")
        },
        text = {
            Text(text = "${friend.user.displayNickname()}님을 친구 목록에서 삭제할까요?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDeleting,
            ) {
                Text(text = if (isDeleting) "삭제 중" else "삭제")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting,
            ) {
                Text(text = "취소")
            }
        },
    )
}

@Composable
internal fun FriendLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = YadanPrimary)
    }
}

@Composable
internal fun FriendMessageContent(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 260.dp)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = YadanTypography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = YadanTextPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = YadanTypography.bodySmall,
            color = YadanTextSecondary,
            textAlign = TextAlign.Center,
        )

        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            FriendActionButton(
                text = actionLabel,
                onClick = onActionClick,
                style = FriendActionButtonStyle.PRIMARY,
            )
        }
    }
}

@Composable
private fun FriendInlineRetry(message: String, onRetryClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            style = YadanTypography.bodySmall,
            color = YadanTextSecondary,
        )

        FriendActionButton(
            text = "다시 시도",
            onClick = onRetryClick,
            style = FriendActionButtonStyle.OUTLINED,
        )
    }
}

@Composable
private fun FriendEmptyListMessage(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        style = YadanTypography.bodyMedium,
        color = YadanTextMuted,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun FriendSectionLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = YadanPrimary,
            strokeWidth = 2.dp,
        )
    }
}

private fun Long?.toTabCount(): Int? = this?.coerceIn(0L, Int.MAX_VALUE.toLong())?.toInt()

private val previewFriends = listOf(
    Friend(
        id = "301",
        user = UserProfile(id = "user-1", nickname = "지우", favoriteTeam = KboTeam.LOTTE),
    ),
    Friend(
        id = "302",
        user = UserProfile(id = "user-2", nickname = "현수", favoriteTeam = KboTeam.KIA),
    ),
)

private val previewReceivedRequests = listOf(
    FriendRequest(
        id = "501",
        user = UserProfile(id = "user-3", nickname = "서연", favoriteTeam = KboTeam.LG),
    ),
    FriendRequest(
        id = "502",
        user = UserProfile(id = "user-4", nickname = "준영", favoriteTeam = KboTeam.NC),
    ),
)

private val previewSentRequests = listOf(
    FriendRequest(
        id = "601",
        user = UserProfile(id = "user-5", nickname = "도윤", favoriteTeam = KboTeam.KT),
    ),
)

@Preview(name = "F01 친구 목록", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun FriendManagementFriendsPreview() {
    YadanbeopseokTheme {
        FriendManagementScreen(
            uiState = FriendManagementUiState(
                friends = previewFriends,
                friendCount = 2,
                receivedRequestCount = 2,
                isFriendsLoading = false,
            ),
            onBackClick = {},
            onSearchClick = {},
            onTabSelected = {},
            onAcceptRequest = {},
            onRejectRequest = {},
            onCancelRequest = {},
            onDeleteFriend = {},
            onRetryFriends = {},
            onRetryReceivedRequests = {},
            onRetrySentRequests = {},
        )
    }
}

@Preview(name = "F02 친구 요청", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun FriendManagementRequestsPreview() {
    YadanbeopseokTheme {
        FriendManagementScreen(
            uiState = FriendManagementUiState(
                selectedTab = FriendManagementTab.REQUESTS,
                receivedRequests = previewReceivedRequests,
                sentRequests = previewSentRequests,
                friendCount = 4,
                receivedRequestCount = 2,
                sentRequestCount = 1,
                isFriendsLoading = false,
            ),
            onBackClick = {},
            onSearchClick = {},
            onTabSelected = {},
            onAcceptRequest = {},
            onRejectRequest = {},
            onCancelRequest = {},
            onDeleteFriend = {},
            onRetryFriends = {},
            onRetryReceivedRequests = {},
            onRetrySentRequests = {},
        )
    }
}

@Preview(name = "F01 로딩", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun FriendManagementLoadingPreview() {
    YadanbeopseokTheme {
        FriendManagementScreen(
            uiState = FriendManagementUiState(),
            onBackClick = {},
            onSearchClick = {},
            onTabSelected = {},
            onAcceptRequest = {},
            onRejectRequest = {},
            onCancelRequest = {},
            onDeleteFriend = {},
            onRetryFriends = {},
            onRetryReceivedRequests = {},
            onRetrySentRequests = {},
        )
    }
}

@Preview(name = "F01 친구 삭제 확인", showBackground = true, widthDp = 390)
@Composable
private fun FriendDeleteDialogPreview() {
    YadanbeopseokTheme {
        FriendDeleteDialog(
            friend = previewFriends.first(),
            isDeleting = false,
            onDismiss = {},
            onConfirm = {},
        )
    }
}
