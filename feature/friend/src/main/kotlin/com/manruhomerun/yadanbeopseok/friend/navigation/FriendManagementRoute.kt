package com.manruhomerun.yadanbeopseok.friend.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.friend.screen.FriendManagementScreen
import com.manruhomerun.yadanbeopseok.friend.viewmodel.FriendManagementViewModel

/** F·01·F·02 화면과 친구 관리 ViewModel, 내비게이션 콜백을 연결합니다. */
@Composable
fun FriendManagementRoute(
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FriendManagementViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onResume()
    }

    LaunchedEffect(uiState.userMessage) {
        val message = uiState.userMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearUserMessage()
    }

    Box(modifier = modifier.fillMaxSize()) {
        FriendManagementScreen(
            uiState = uiState,
            onBackClick = onBackClick,
            onSearchClick = onSearchClick,
            onTabSelected = viewModel::selectTab,
            onAcceptRequest = viewModel::acceptRequest,
            onRejectRequest = viewModel::rejectRequest,
            onCancelRequest = viewModel::cancelRequest,
            onDeleteFriend = viewModel::deleteFriend,
            onRetryFriends = viewModel::retryFriends,
            onRetryReceivedRequests = viewModel::retryReceivedRequests,
            onRetrySentRequests = viewModel::retrySentRequests,
            modifier = Modifier.fillMaxSize(),
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(
                    horizontal = 20.dp,
                    vertical = 12.dp,
                ),
        )
    }
}
