package com.manruhomerun.yadanbeopseok.mypage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.AuthRepository
import com.manruhomerun.yadanbeopseok.data.repository.UserRepository
import com.manruhomerun.yadanbeopseok.model.TravelPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * H·01의 사용자 정보 조회, 로그아웃과 회원 탈퇴 상태를 관리합니다.
 *
 * 인증 정보가 삭제되면 MainActivityViewModel이 세션 변경을 감지하여
 * 로그인 화면으로 전환합니다.
 */
@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    init {
        loadMyPage()
    }

    /**
     * 사용자 프로필과 여행 취향을 다시 조회합니다.
     */
    fun retry() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isProcessing) return

        loadMyPage()
    }

    /**
     * 백엔드 로그아웃을 요청하고 로컬 인증 정보를 삭제합니다.
     */
    fun logout(kakaoAccessToken: String) {
        if (!beginAction(MyPageAction.LOGOUT)) return

        viewModelScope.launch {
            try {
                authRepository.logout(kakaoAccessToken = kakaoAccessToken)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                // AuthRepository가 finally에서 로컬 인증 정보를 삭제합니다.
            } finally {
                finishAction(MyPageAction.LOGOUT)
            }
        }
    }

    /**
     * 현재 로그인한 사용자의 회원 탈퇴를 요청합니다.
     */
    fun withdraw() {
        if (!beginAction(MyPageAction.WITHDRAWAL)) return

        viewModelScope.launch {
            var errorMessage: String? = null

            try {
                authRepository.withdraw()
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                // 전역 세션 관찰자가 로그인 화면 전환을 처리합니다.
            } catch (exception: Exception) {
                errorMessage =
                    exception.toSafeErrorMessage(
                        defaultMessage = "회원 탈퇴를 완료하지 못했습니다. 잠시 후 다시 시도해주세요.",
                    )
            } finally {
                finishAction(
                    action = MyPageAction.WITHDRAWAL,
                    errorMessage = errorMessage,
                )
            }
        }
    }

    /**
     * 화면에 표시된 오류 문구를 제거합니다.
     */
    fun clearErrorMessage() {
        _uiState.update { currentState ->
            currentState.copy(errorMessage = null)
        }
    }

    /**
     * 사용자 프로필과 여행 취향을 동시에 조회합니다.
     */
    private fun loadMyPage() {
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            try {
                val (userProfile, travelPreference) =
                    coroutineScope {
                        val profileDeferred = async {
                            userRepository.getMyProfile()
                        }
                        val preferenceDeferred = async {
                            userRepository.getMyTravelPreference()
                        }

                        profileDeferred.await() to preferenceDeferred.await()
                    }

                _uiState.update { currentState ->
                    currentState.copy(
                        userProfile = userProfile,
                        travelPreferenceSummary = travelPreference.toSummary(),
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                _uiState.update { currentState ->
                    currentState.copy(isLoading = false)
                }
            } catch (exception: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage =
                            exception.toSafeErrorMessage(
                                defaultMessage = "마이페이지 정보를 불러오지 못했습니다. 다시 시도해주세요.",
                            ),
                    )
                }
            }
        }
    }

    /**
     * 중복 요청을 막고 계정 작업의 진행 상태를 시작합니다.
     */
    private fun beginAction(action: MyPageAction): Boolean {
        val currentState = _uiState.value

        if (currentState.isLoading || currentState.isProcessing) {
            return false
        }

        _uiState.update { state ->
            state.copy(
                processingAction = action,
                errorMessage = null,
            )
        }

        return true
    }

    /**
     * 지정한 계정 작업이 끝난 경우 진행 상태와 오류 문구를 갱신합니다.
     */
    private fun finishAction(
        action: MyPageAction,
        errorMessage: String? = null,
    ) {
        _uiState.update { currentState ->
            if (currentState.processingAction != action) {
                currentState
            } else {
                currentState.copy(
                    processingAction = null,
                    errorMessage = errorMessage,
                )
            }
        }
    }
}

/**
 * H·01에 표시할 여행 취향 요약 문구를 만듭니다.
 */
private fun TravelPreference.toSummary(): String {
    val travelStyleSummary =
        when (travelStyleScore.value) {
            in 1..3 -> "자연 선호"
            4 -> "중립"
            in 5..7 -> "도시 선호"
            else -> error("TravelStyleScore must be between 1 and 7.")
        }

    return "${residenceRegion.displayName} 거주 · " +
        "$travelStyleSummary · 선호 지역 ${preferredTravelRegions.size}곳"
}

/**
 * 내부 예외 정보를 노출하지 않는 사용자용 문구로 변환합니다.
 */
private fun Exception.toSafeErrorMessage(defaultMessage: String): String =
    when (this) {
        is NetworkConnectionException ->
            "인터넷 연결을 확인한 후 다시 시도해주세요."

        is NetworkTimeoutException ->
            "서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요."

        else ->
            defaultMessage
    }
