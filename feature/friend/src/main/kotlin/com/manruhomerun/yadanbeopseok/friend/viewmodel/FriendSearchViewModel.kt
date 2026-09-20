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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** F·03의 사용자 검색과 친구 요청 상태를 관리합니다. */
@HiltViewModel
class FriendSearchViewModel @Inject constructor(private val friendRepository: FriendRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FriendSearchUiState())
    val uiState: StateFlow<FriendSearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    /** 검색어를 12자로 제한하고 이전 검색 결과를 초기화합니다. */
    fun updateQuery(query: String) {
        val limitedQuery = query.take(MAX_NICKNAME_LENGTH)
        if (_uiState.value.query == limitedQuery) return

        searchJob?.cancel()
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
        val normalizedQuery = _uiState.value.query.trim()

        if (normalizedQuery.isEmpty()) {
            searchJob?.cancel()
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
                friendRepository.sendFriendRequest(userId)
                markRequestAsSent(userId)
                _uiState.update { state ->
                    state.copy(userMessage = "친구 요청을 보냈습니다.")
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                // 전역 세션 관찰자가 로그인 화면 이동을 처리합니다.
            } catch (exception: Exception) {
                _uiState.update { state ->
                    state.copy(
                        userMessage = exception.toFriendErrorMessage(
                            defaultMessage = "친구 요청을 보내지 못했습니다.",
                        ),
                    )
                }

                if (exception is ApiException && exception.statusCode == HTTP_CONFLICT) {
                    _uiState.value.searchedQuery?.let(::loadSearchResult)
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
                val result = friendRepository.searchUsers(
                    nickname = query,
                    limit = SEARCH_RESULT_LIMIT,
                )

                _uiState.update { currentState ->
                    if (currentState.searchedQuery != query) {
                        currentState
                    } else {
                        currentState.copy(
                            users = result.users,
                            resultCount = result.resultCount,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                _uiState.update { currentState ->
                    currentState.copy(isLoading = false)
                }
            } catch (exception: Exception) {
                _uiState.update { currentState ->
                    if (currentState.searchedQuery != query) {
                        currentState
                    } else {
                        currentState.copy(
                            isLoading = false,
                            errorMessage = exception.toFriendErrorMessage(
                                defaultMessage = "사용자를 검색하지 못했습니다.",
                            ),
                        )
                    }
                }
            }
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
