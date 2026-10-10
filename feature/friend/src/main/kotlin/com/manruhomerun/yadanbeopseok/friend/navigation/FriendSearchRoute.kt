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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.friend.screen.FriendSearchScreen
import com.manruhomerun.yadanbeopseok.friend.viewmodel.FriendSearchViewModel

/** 친구 찾기 화면과 검색 ViewModel, 받은 요청 목록 이동을 연결합니다. */
@Composable
fun FriendSearchRoute(
    onBackClick: () -> Unit,
    onReceivedRequestsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FriendSearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var hasOpenedReceivedRequests by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.userMessage) {
        val message = uiState.userMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearUserMessage()
    }

    Box(modifier = modifier.fillMaxSize()) {
        FriendSearchScreen(
            uiState = uiState,
            onBackClick = onBackClick,
            onQueryChange = viewModel::updateQuery,
            onSearch = viewModel::search,
            onSendFriendRequest = viewModel::sendFriendRequest,
            onReceivedRequestsClick = {
                // 전환 중 클릭과 연속 클릭으로 백스택을 중복 제거하지 않습니다.
                if (!hasOpenedReceivedRequests && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    hasOpenedReceivedRequests = true
                    onReceivedRequestsClick()
                }
            },
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
