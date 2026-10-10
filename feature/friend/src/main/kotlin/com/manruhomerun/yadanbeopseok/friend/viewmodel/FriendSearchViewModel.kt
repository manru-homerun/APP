package com.manruhomerun.yadanbeopseok.friend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.ApiException
import com.manruhomerun.yadanbeopseok.common.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.FriendRepository
import com.manruhomerun.yadanbeopseok.model.FriendRelationshipStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.HttpURLConnection.HTTP_CONFLICT
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** F·03의 사용자 검색과 친구 요청 상태를 관리합니다. */
@HiltViewModel
class FriendSearchViewModel @Inject constructor(private val friendRepository: FriendRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FriendSearchUiState())
    val uiState: StateFlow<FriendSearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var searchRequestGeneration = 0L
    private val searchMutex = Mutex()
    private var hasExpiredSession = false

    /** 검색어를 12자로 제한하고 이전 검색 결과를 초기화합니다. */
    fun updateQuery(query: String) {
        if (hasExpiredSession) return

        val limitedQuery = query.take(MAX_NICKNAME_LENGTH)
        if (_uiState.value.query == limitedQuery) return

        cancelSearch()
        _uiState.update { currentState ->
            currentState.copy(
                query = limitedQuery,
                searchedQuery = null,
                users = emptyList(),
                resultCount = 0,
                isLoading = false,
                errorMessage = null,
            )
        }
    }

    /** 입력한 닉네임으로 사용자를 검색합니다. */
    fun search() {
        if (hasExpiredSession) return

        val normalizedQuery = _uiState.value.query.trim()

        if (normalizedQuery.isEmpty()) {
            cancelSearch()
            _uiState.update { currentState ->
                currentState.copy(
                    query = "",
                    searchedQuery = null,
                    users = emptyList(),
                    resultCount = 0,
                    isLoading = false,
                    errorMessage = null,
                )
            }
            return
        }

        loadSearchResult(normalizedQuery)
    }

    /** 검색 결과의 사용자에게 친구 요청을 전송합니다. */
    fun sendFriendRequest(userId: String) {
        if (hasExpiredSession) return

        val currentState = _uiState.value
        val searchUser = currentState.users.firstOrNull { result -> result.user.id == userId }

        if (searchUser?.relationshipStatus != FriendRelationshipStatus.NONE) return
        if (userId in currentState.requestingUserIds) return

        _uiState.update { state ->
            state.copy(
                requestingUserIds = state.requestingUserIds + userId,
                userMessage = null,
            )
        }

        viewModelScope.launch {
            try {
                // 검색과 전송 모두 API 호출부터 상태 반영까지 같은 잠금으로 보호합니다.
                searchMutex.withLock {
                    ensureActive()
                    if (hasExpiredSession) return@withLock

                    friendRepository.sendFriendRequest(userId)
                    ensureActive()
                    if (hasExpiredSession) return@withLock

                    markRequestAsSent(userId)
                    _uiState.update { state ->
                        state.copy(userMessage = "친구 요청을 보냈습니다.")
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                handleSessionExpired()
            } catch (exception: Exception) {
                ensureActive()
                if (!hasExpiredSession) {
                    _uiState.update { state ->
                        state.copy(
                            userMessage = exception.toFriendErrorMessage(
                                defaultMessage = "친구 요청을 보내지 못했습니다.",
                            ),
                        )
                    }

                    if (exception is ApiException && exception.statusCode == HTTP_CONFLICT) {
                        // 잠금 해제 후 현재 유효한 검색만 다시 조회합니다.
                        val state = _uiState.value
                        state.searchedQuery?.takeIf { it == state.query.trim() }?.let(::loadSearchResult)
                    }
                }
            } finally {
                _uiState.update { state ->
                    state.copy(requestingUserIds = state.requestingUserIds - userId)
                }
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { currentState ->
            currentState.copy(userMessage = null)
        }
    }

    private fun loadSearchResult(query: String) {
        if (hasExpiredSession) return

        val generation = ++searchRequestGeneration
        searchJob?.cancel()
        _uiState.update { currentState ->
            currentState.copy(
                query = query,
                searchedQuery = query,
                users = emptyList(),
                resultCount = 0,
                isLoading = true,
                errorMessage = null,
            )
        }

        searchJob = viewModelScope.launch {
            try {
                searchMutex.withLock {
                    ensureActive()
                    if (!isCurrentSearch(generation, query)) return@withLock

                    val result = friendRepository.searchUsers(
                        nickname = query,
                        limit = SEARCH_RESULT_LIMIT,
                    )
                    ensureActive()
                    if (isCurrentSearch(generation, query)) {
                        _uiState.update { currentState ->
                            currentState.copy(
                                users = result.users,
                                resultCount = result.resultCount,
                                errorMessage = null,
                            )
                        }
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                handleSessionExpired()
            } catch (exception: Exception) {
                ensureActive()
                if (isCurrentSearch(generation, query)) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            errorMessage = exception.toFriendErrorMessage(
                                defaultMessage = "사용자를 검색하지 못했습니다.",
                            ),
                        )
                    }
                }
            } finally {
                if (generation == searchRequestGeneration) {
                    searchJob = null
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    /** 검색만 취소하며 이미 진행 중인 친구 요청 전송은 유지합니다. */
    private fun cancelSearch() {
        searchRequestGeneration++
        searchJob?.cancel()
        searchJob = null
    }

    private fun isCurrentSearch(generation: Long, query: String): Boolean =
        !hasExpiredSession && generation == searchRequestGeneration && _uiState.value.searchedQuery == query

    /** 로그인 이동은 전역 처리에 맡기고, 만료 이후 추가 조회와 전송을 차단합니다. */
    private fun handleSessionExpired() {
        if (hasExpiredSession) return

        hasExpiredSession = true
        cancelSearch()
        _uiState.update {
            it.copy(isLoading = false, errorMessage = null, requestingUserIds = emptySet(), userMessage = null)
        }
    }

    private fun markRequestAsSent(userId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                users = currentState.users.map { result ->
                    if (result.user.id == userId) {
                        result.copy(relationshipStatus = FriendRelationshipStatus.REQUEST_SENT)
                    } else {
                        result
                    }
                },
            )
        }
    }
}

private const val MAX_NICKNAME_LENGTH = 12
private const val SEARCH_RESULT_LIMIT = 20
