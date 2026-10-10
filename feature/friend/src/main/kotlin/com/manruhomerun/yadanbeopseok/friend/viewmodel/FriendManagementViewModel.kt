package com.manruhomerun.yadanbeopseok.friend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.ApiException
import com.manruhomerun.yadanbeopseok.common.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.HttpURLConnection.HTTP_CONFLICT
import java.net.HttpURLConnection.HTTP_NOT_FOUND
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * F·01 친구 목록과 F·02 친구 요청의 조회·변경 상태를 관리합니다.
 */
@HiltViewModel
class FriendManagementViewModel @Inject constructor(private val friendRepository: FriendRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FriendManagementUiState())
    val uiState: StateFlow<FriendManagementUiState> = _uiState.asStateFlow()

    private var friendsJob: Job? = null
    private var receivedRequestsJob: Job? = null
    private var sentRequestsJob: Job? = null
    private var friendsRequestGeneration = 0L
    private var receivedRequestsGeneration = 0L
    private var sentRequestsGeneration = 0L
    private var receivedCountRequestGeneration = 0L
    private var pendingFriendsRefresh = false
    private var pendingReceivedRequestsRefresh = false
    private var pendingSentRequestsRefresh = false
    private var hasLoadedRequests = false
    private var hasHandledFirstResume = false
    private var hasExpiredSession = false

    init {
        loadFriends()
    }

    /** F·03에서 돌아온 뒤 이미 열어본 목록만 다시 조회합니다. */
    fun onResume() {
        if (!hasHandledFirstResume) {
            hasHandledFirstResume = true
            return
        }

        loadFriends()
        if (hasLoadedRequests) {
            loadReceivedRequests()
            loadSentRequests()
        }
    }

    /** 친구 목록 또는 요청 탭을 선택합니다. */
    fun selectTab(tab: FriendManagementTab) {
        if (_uiState.value.selectedTab == tab) return

        _uiState.update { currentState ->
            currentState.copy(selectedTab = tab)
        }

        if (tab == FriendManagementTab.REQUESTS && !hasLoadedRequests) {
            hasLoadedRequests = true
            loadReceivedRequests()
            loadSentRequests()
        }
    }

    fun retryFriends() {
        loadFriends()
    }

    fun retryReceivedRequests() {
        hasLoadedRequests = true
        loadReceivedRequests()
    }

    fun retrySentRequests() {
        hasLoadedRequests = true
        loadSentRequests()
    }

    /** 받은 친구 요청을 수락합니다. */
    fun acceptRequest(requestId: String) {
        if (!beginRequestAction(requestId)) return

        viewModelScope.launch {
            try {
                ensureActive()
                if (hasExpiredSession) return@launch
                friendRepository.acceptFriendRequest(requestId)
                ensureActive()
                if (hasExpiredSession) return@launch
                removeReceivedRequest(requestId)
                loadFriends()
                loadReceivedRequests()
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                handleSessionExpired()
            } catch (exception: Exception) {
                ensureActive()
                if (!hasExpiredSession) {
                    handleRequestActionFailure(
                        exception = exception,
                        message = "친구 요청을 수락하지 못했습니다.",
                        refresh = {
                            loadFriends()
                            loadReceivedRequests()
                        },
                    )
                }
            } finally {
                finishRequestAction(requestId)
            }
        }
    }

    /** 받은 친구 요청을 거절합니다. */
    fun rejectRequest(requestId: String) {
        if (!beginRequestAction(requestId)) return

        viewModelScope.launch {
            try {
                ensureActive()
                if (hasExpiredSession) return@launch
                friendRepository.rejectFriendRequest(requestId)
                ensureActive()
                if (hasExpiredSession) return@launch
                removeReceivedRequest(requestId)
                loadReceivedRequests()
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                handleSessionExpired()
            } catch (exception: Exception) {
                ensureActive()
                if (!hasExpiredSession) {
                    handleRequestActionFailure(
                        exception = exception,
                        message = "친구 요청을 거절하지 못했습니다.",
                        refresh = ::loadReceivedRequests,
                    )
                }
            } finally {
                finishRequestAction(requestId)
            }
        }
    }

    /** 보낸 친구 요청을 취소합니다. */
    fun cancelRequest(requestId: String) {
        if (!beginRequestAction(requestId)) return

        viewModelScope.launch {
            try {
                ensureActive()
                if (hasExpiredSession) return@launch
                friendRepository.cancelFriendRequest(requestId)
                ensureActive()
                if (hasExpiredSession) return@launch
                removeSentRequest(requestId)
                loadSentRequests()
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                handleSessionExpired()
            } catch (exception: Exception) {
                ensureActive()
                if (!hasExpiredSession) {
                    handleRequestActionFailure(
                        exception = exception,
                        message = "친구 요청을 취소하지 못했습니다.",
                        refresh = ::loadSentRequests,
                    )
                }
            } finally {
                finishRequestAction(requestId)
            }
        }
    }

    /** 선택한 친구를 목록에서 삭제합니다. */
    fun deleteFriend(friendId: String) {
        if (hasExpiredSession || _uiState.value.deletingFriendId != null) return

        _uiState.update { currentState ->
            currentState.copy(
                deletingFriendId = friendId,
                userMessage = null,
            )
        }
        invalidateReadRequests()

        viewModelScope.launch {
            try {
                ensureActive()
                if (hasExpiredSession) return@launch
                friendRepository.deleteFriend(friendId)
                ensureActive()
                if (hasExpiredSession) return@launch
                removeFriend(friendId)
                loadFriends()
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                handleSessionExpired()
            } catch (exception: Exception) {
                ensureActive()
                if (!hasExpiredSession) {
                    if (exception.isStaleFriendState()) {
                        loadFriends()
                    }

                    _uiState.update { currentState ->
                        currentState.copy(
                            userMessage = exception.toFriendErrorMessage(
                                defaultMessage = "친구를 삭제하지 못했습니다.",
                            ),
                        )
                    }
                }
            } finally {
                _uiState.update { currentState ->
                    if (currentState.deletingFriendId == friendId) {
                        currentState.copy(deletingFriendId = null)
                    } else {
                        currentState
                    }
                }
                refreshPendingLists()
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { currentState ->
            currentState.copy(userMessage = null)
        }
    }

    private fun loadFriends() {
        if (hasExpiredSession) return

        _uiState.update { currentState ->
            currentState.copy(
                isFriendsLoading = currentState.friends.isEmpty(),
                friendsErrorMessage = null,
            )
        }

        if (hasPendingChanges()) {
            pendingFriendsRefresh = true
            return
        }

        val generation = ++friendsRequestGeneration
        val countGeneration = ++receivedCountRequestGeneration
        friendsJob?.cancel()
        friendsJob = viewModelScope.launch {
            try {
                val result = friendRepository.getFriends()
                ensureActive()
                if (!isCurrentFriendsRequest(generation)) return@launch

                _uiState.update { currentState ->
                    currentState.copy(
                        friends = result.friends,
                        friendCount = result.friendCount,
                        receivedRequestCount = if (countGeneration == receivedCountRequestGeneration) {
                            result.receivedRequestCount
                        } else {
                            currentState.receivedRequestCount
                        },
                        friendsErrorMessage = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                handleSessionExpired()
            } catch (exception: Exception) {
                ensureActive()
                if (isCurrentFriendsRequest(generation)) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            friendsErrorMessage = exception.toFriendErrorMessage(
                                defaultMessage = "친구 목록을 불러오지 못했습니다.",
                            ),
                        )
                    }
                }
            } finally {
                if (generation == friendsRequestGeneration) {
                    friendsJob = null
                    _uiState.update { it.copy(isFriendsLoading = false) }
                }
            }
        }
    }

    private fun loadReceivedRequests() {
        if (hasExpiredSession) return

        _uiState.update { currentState ->
            currentState.copy(
                isReceivedRequestsLoading = currentState.receivedRequests.isEmpty(),
                receivedRequestsErrorMessage = null,
            )
        }

        if (hasPendingChanges()) {
            pendingReceivedRequestsRefresh = true
            return
        }

        val generation = ++receivedRequestsGeneration
        val countGeneration = ++receivedCountRequestGeneration
        receivedRequestsJob?.cancel()
        receivedRequestsJob = viewModelScope.launch {
            try {
                val result = friendRepository.getReceivedFriendRequests()
                ensureActive()
                if (!isCurrentReceivedRequests(generation)) return@launch

                _uiState.update { currentState ->
                    currentState.copy(
                        receivedRequests = result.requests,
                        receivedRequestCount = if (countGeneration == receivedCountRequestGeneration) {
                            result.receivedRequestCount
                        } else {
                            currentState.receivedRequestCount
                        },
                        receivedRequestsErrorMessage = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                handleSessionExpired()
            } catch (exception: Exception) {
                ensureActive()
                if (isCurrentReceivedRequests(generation)) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            receivedRequestsErrorMessage = exception.toFriendErrorMessage(
                                defaultMessage = "받은 친구 요청을 불러오지 못했습니다.",
                            ),
                        )
                    }
                }
            } finally {
                if (generation == receivedRequestsGeneration) {
                    receivedRequestsJob = null
                    _uiState.update { it.copy(isReceivedRequestsLoading = false) }
                }
            }
        }
    }

    private fun loadSentRequests() {
        if (hasExpiredSession) return

        _uiState.update { currentState ->
            currentState.copy(
                isSentRequestsLoading = currentState.sentRequests.isEmpty(),
                sentRequestsErrorMessage = null,
            )
        }

        if (hasPendingChanges()) {
            pendingSentRequestsRefresh = true
            return
        }

        val generation = ++sentRequestsGeneration
        sentRequestsJob?.cancel()
        sentRequestsJob = viewModelScope.launch {
            try {
                val result = friendRepository.getSentFriendRequests()
                ensureActive()
                if (!isCurrentSentRequests(generation)) return@launch

                _uiState.update { currentState ->
                    currentState.copy(
                        sentRequests = result.requests,
                        sentRequestCount = result.sentRequestCount,
                        sentRequestsErrorMessage = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                handleSessionExpired()
            } catch (exception: Exception) {
                ensureActive()
                if (isCurrentSentRequests(generation)) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            sentRequestsErrorMessage = exception.toFriendErrorMessage(
                                defaultMessage = "보낸 친구 요청을 불러오지 못했습니다.",
                            ),
                        )
                    }
                }
            } finally {
                if (generation == sentRequestsGeneration) {
                    sentRequestsJob = null
                    _uiState.update { it.copy(isSentRequestsLoading = false) }
                }
            }
        }
    }

    private fun beginRequestAction(requestId: String): Boolean {
        if (hasExpiredSession || requestId in _uiState.value.processingRequestIds) return false

        _uiState.update { currentState ->
            currentState.copy(
                processingRequestIds = currentState.processingRequestIds + requestId,
                userMessage = null,
            )
        }
        invalidateReadRequests()
        return true
    }

    private fun finishRequestAction(requestId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                processingRequestIds = currentState.processingRequestIds - requestId,
            )
        }
        refreshPendingLists()
    }

    private fun hasPendingChanges(): Boolean =
        _uiState.value.processingRequestIds.isNotEmpty() || _uiState.value.deletingFriendId != null

    /** 변경 전 조회를 무효화하고, 취소한 조회는 모든 변경이 끝난 뒤 다시 실행합니다. */
    private fun invalidateReadRequests() {
        pendingFriendsRefresh = pendingFriendsRefresh || friendsJob?.isActive == true
        pendingReceivedRequestsRefresh = pendingReceivedRequestsRefresh || receivedRequestsJob?.isActive == true
        pendingSentRequestsRefresh = pendingSentRequestsRefresh || sentRequestsJob?.isActive == true
        friendsRequestGeneration++
        receivedRequestsGeneration++
        sentRequestsGeneration++
        receivedCountRequestGeneration++
        friendsJob?.cancel()
        receivedRequestsJob?.cancel()
        sentRequestsJob?.cancel()
        friendsJob = null
        receivedRequestsJob = null
        sentRequestsJob = null
    }

    /** 마지막 변경이 끝나면 대기 중인 목록을 각각 한 번씩 다시 조회합니다. */
    private fun refreshPendingLists() {
        if (hasExpiredSession || hasPendingChanges() || !viewModelScope.isActive) return

        val refreshFriends = pendingFriendsRefresh
        val refreshReceivedRequests = pendingReceivedRequestsRefresh
        val refreshSentRequests = pendingSentRequestsRefresh
        pendingFriendsRefresh = false
        pendingReceivedRequestsRefresh = false
        pendingSentRequestsRefresh = false
        if (refreshFriends) loadFriends()
        if (refreshReceivedRequests) loadReceivedRequests()
        if (refreshSentRequests) loadSentRequests()
    }

    /** 로그인 이동은 전역 처리에 맡기고, 만료 이후 조회와 결과 반영을 중단합니다. */
    private fun handleSessionExpired() {
        if (hasExpiredSession) return

        hasExpiredSession = true
        invalidateReadRequests()
        pendingFriendsRefresh = false
        pendingReceivedRequestsRefresh = false
        pendingSentRequestsRefresh = false
        _uiState.update {
            it.copy(
                isFriendsLoading = false,
                isReceivedRequestsLoading = false,
                isSentRequestsLoading = false,
                friendsErrorMessage = null,
                receivedRequestsErrorMessage = null,
                sentRequestsErrorMessage = null,
                processingRequestIds = emptySet(),
                deletingFriendId = null,
                userMessage = null,
            )
        }
    }

    private fun isCurrentFriendsRequest(generation: Long): Boolean =
        !hasExpiredSession && generation == friendsRequestGeneration

    private fun isCurrentReceivedRequests(generation: Long): Boolean =
        !hasExpiredSession && generation == receivedRequestsGeneration

    private fun isCurrentSentRequests(generation: Long): Boolean =
        !hasExpiredSession && generation == sentRequestsGeneration

    private fun handleRequestActionFailure(exception: Exception, message: String, refresh: () -> Unit) {
        if (exception.isStaleFriendState()) {
            refresh()
        }

        _uiState.update { currentState ->
            currentState.copy(
                userMessage = exception.toFriendErrorMessage(message),
            )
        }
    }

    private fun removeFriend(friendId: String) {
        _uiState.update { currentState ->
            if (currentState.friends.none { it.id == friendId }) {
                currentState
            } else {
                currentState.copy(
                    friends = currentState.friends.filterNot { friend -> friend.id == friendId },
                    friendCount = currentState.friendCount.decrementIfKnown(),
                )
            }
        }
    }

    private fun removeReceivedRequest(requestId: String) {
        _uiState.update { currentState ->
            if (currentState.receivedRequests.none { it.id == requestId }) {
                currentState
            } else {
                currentState.copy(
                    receivedRequests = currentState.receivedRequests.filterNot { request ->
                        request.id == requestId
                    },
                    receivedRequestCount = currentState.receivedRequestCount.decrementIfKnown(),
                )
            }
        }
    }

    private fun removeSentRequest(requestId: String) {
        _uiState.update { currentState ->
            if (currentState.sentRequests.none { it.id == requestId }) {
                currentState
            } else {
                currentState.copy(
                    sentRequests = currentState.sentRequests.filterNot { request ->
                        request.id == requestId
                    },
                    sentRequestCount = currentState.sentRequestCount.decrementIfKnown(),
                )
            }
        }
    }
}

/** 사용자에게 내부 예외 정보를 노출하지 않는 친구 기능 오류 문구를 만듭니다. */
internal fun Throwable.toFriendErrorMessage(defaultMessage: String): String = when (this) {
    is NetworkConnectionException -> "인터넷 연결을 확인한 후 다시 시도해주세요."
    is NetworkTimeoutException -> "서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요."
    else -> defaultMessage
}

private fun Exception.isStaleFriendState(): Boolean =
    this is ApiException && (statusCode == HTTP_NOT_FOUND || statusCode == HTTP_CONFLICT)

private fun Long?.decrementIfKnown(): Long? = this?.let { count -> (count - 1L).coerceAtLeast(0L) }
