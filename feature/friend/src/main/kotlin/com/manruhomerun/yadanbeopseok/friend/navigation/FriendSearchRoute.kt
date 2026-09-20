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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.friend.screen.FriendSearchScreen
import com.manruhomerun.yadanbeopseok.friend.viewmodel.FriendSearchViewModel

/** F·03 화면과 사용자 검색 ViewModel, 뒤로가기 콜백을 연결합니다. */
@Composable
fun FriendSearchRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FriendSearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
