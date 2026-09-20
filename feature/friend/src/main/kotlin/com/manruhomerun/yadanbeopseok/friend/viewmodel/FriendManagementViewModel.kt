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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    private var hasLoadedRequests = false
    private var hasHandledFirstResume = false

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
                friendRepository.acceptFriendRequest(requestId)
                removeReceivedRequest(requestId)
                loadFriends()
                loadReceivedRequests()
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                // 전역 세션 관찰자가 로그인 화면 이동을 처리합니다.
            } catch (exception: Exception) {
                handleRequestActionFailure(
                    exception = exception,
                    message = "친구 요청을 수락하지 못했습니다.",
                    refresh = ::loadReceivedRequests,
                )
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
                friendRepository.rejectFriendRequest(requestId)
                removeReceivedRequest(requestId)
                loadReceivedRequests()
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                // 전역 세션 관찰자가 로그인 화면 이동을 처리합니다.
            } catch (exception: Exception) {
                handleRequestActionFailure(
                    exception = exception,
                    message = "친구 요청을 거절하지 못했습니다.",
                    refresh = ::loadReceivedRequests,
                )
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
                friendRepository.cancelFriendRequest(requestId)
                removeSentRequest(requestId)
                loadSentRequests()
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                // 전역 세션 관찰자가 로그인 화면 이동을 처리합니다.
            } catch (exception: Exception) {
                handleRequestActionFailure(
                    exception = exception,
                    message = "친구 요청을 취소하지 못했습니다.",
                    refresh = ::loadSentRequests,
                )
            } finally {
                finishRequestAction(requestId)
            }
        }
    }

    /** 선택한 친구를 목록에서 삭제합니다. */
    fun deleteFriend(friendId: String) {
        if (_uiState.value.deletingFriendId != null) return

        _uiState.update { currentState ->
            currentState.copy(
                deletingFriendId = friendId,
                userMessage = null,
            )
        }

        viewModelScope.launch {
            try {
                friendRepository.deleteFriend(friendId)
                removeFriend(friendId)
                loadFriends()
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                // 전역 세션 관찰자가 로그인 화면 이동을 처리합니다.
            } catch (exception: Exception) {
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
            } finally {
                _uiState.update { currentState ->
                    if (currentState.deletingFriendId == friendId) {
                        currentState.copy(deletingFriendId = null)
                    } else {
                        currentState
                    }
                }
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { currentState ->
            currentState.copy(userMessage = null)
        }
    }

    private fun loadFriends() {
        if (friendsJob?.isActive == true) return

        _uiState.update { currentState ->
            currentState.copy(
                isFriendsLoading = currentState.friends.isEmpty(),
                friendsErrorMessage = null,
            )
        }

        friendsJob = viewModelScope.launch {
            try {
                val result = friendRepository.getFriends()

                _uiState.update { currentState ->
                    currentState.copy(
                        friends = result.friends,
                        friendCount = result.friendCount,
                        receivedRequestCount = result.receivedRequestCount,
                        isFriendsLoading = false,
                        friendsErrorMessage = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                _uiState.update { currentState ->
                    currentState.copy(isFriendsLoading = false)
                }
            } catch (exception: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isFriendsLoading = false,
                        friendsErrorMessage = exception.toFriendErrorMessage(
                            defaultMessage = "친구 목록을 불러오지 못했습니다.",
                        ),
                    )
                }
            }
        }
    }

    private fun loadReceivedRequests() {
        if (receivedRequestsJob?.isActive == true) return

        _uiState.update { currentState ->
            currentState.copy(
                isReceivedRequestsLoading = currentState.receivedRequests.isEmpty(),
                receivedRequestsErrorMessage = null,
            )
        }

        receivedRequestsJob = viewModelScope.launch {
            try {
                val result = friendRepository.getReceivedFriendRequests()

                _uiState.update { currentState ->
                    currentState.copy(
                        receivedRequests = result.requests,
                        receivedRequestCount = result.receivedRequestCount,
                        isReceivedRequestsLoading = false,
                        receivedRequestsErrorMessage = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                _uiState.update { currentState ->
                    currentState.copy(isReceivedRequestsLoading = false)
                }
            } catch (exception: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isReceivedRequestsLoading = false,
                        receivedRequestsErrorMessage = exception.toFriendErrorMessage(
                            defaultMessage = "받은 친구 요청을 불러오지 못했습니다.",
                        ),
                    )
                }
            }
        }
    }

    private fun loadSentRequests() {
        if (sentRequestsJob?.isActive == true) return

        _uiState.update { currentState ->
            currentState.copy(
                isSentRequestsLoading = currentState.sentRequests.isEmpty(),
                sentRequestsErrorMessage = null,
            )
        }

        sentRequestsJob = viewModelScope.launch {
            try {
                val result = friendRepository.getSentFriendRequests()

                _uiState.update { currentState ->
                    currentState.copy(
                        sentRequests = result.requests,
                        sentRequestCount = result.sentRequestCount,
                        isSentRequestsLoading = false,
                        sentRequestsErrorMessage = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                _uiState.update { currentState ->
                    currentState.copy(isSentRequestsLoading = false)
                }
            } catch (exception: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isSentRequestsLoading = false,
                        sentRequestsErrorMessage = exception.toFriendErrorMessage(
                            defaultMessage = "보낸 친구 요청을 불러오지 못했습니다.",
                        ),
                    )
                }
            }
        }
    }

    private fun beginRequestAction(requestId: String): Boolean {
        if (requestId in _uiState.value.processingRequestIds) return false

        _uiState.update { currentState ->
            currentState.copy(
                processingRequestIds = currentState.processingRequestIds + requestId,
                userMessage = null,
            )
        }
        return true
    }

    private fun finishRequestAction(requestId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                processingRequestIds = currentState.processingRequestIds - requestId,
            )
        }
    }

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
            currentState.copy(
                friends = currentState.friends.filterNot { friend -> friend.id == friendId },
                friendCount = currentState.friendCount.decrementIfKnown(),
            )
        }
    }

    private fun removeReceivedRequest(requestId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                receivedRequests = currentState.receivedRequests.filterNot { request ->
                    request.id == requestId
                },
                receivedRequestCount = currentState.receivedRequestCount.decrementIfKnown(),
            )
        }
    }

    private fun removeSentRequest(requestId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                sentRequests = currentState.sentRequests.filterNot { request ->
                    request.id == requestId
                },
                sentRequestCount = currentState.sentRequestCount.decrementIfKnown(),
            )
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
