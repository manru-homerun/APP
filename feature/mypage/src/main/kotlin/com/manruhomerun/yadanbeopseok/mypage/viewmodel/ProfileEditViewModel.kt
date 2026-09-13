package com.manruhomerun.yadanbeopseok.mypage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.UserRepository
import com.manruhomerun.yadanbeopseok.model.KboTeam
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** H·02에서 발생하는 일회성 화면 이벤트입니다. */
sealed interface ProfileEditEvent {
    /** 변경한 프로필이 서버에 저장됐습니다. */
    data object Saved : ProfileEditEvent
}

/** H·02의 프로필 조회, 닉네임 확인, 응원 구단 선택과 저장을 관리합니다. */
@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileEditUiState())
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

    private val _events = Channel<ProfileEditEvent>(Channel.BUFFERED)
    val events: Flow<ProfileEditEvent> = _events.receiveAsFlow()

    private var loadJob: Job? = null
    private var nicknameCheckJob: Job? = null

    init {
        loadProfile()
    }

    /** 프로필 조회를 다시 시도합니다. */
    fun retry() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isSaving) return

        loadProfile()
    }

    /** 닉네임 입력을 변경하고, 유효한 변경값이면 중복 확인을 예약합니다. */
    fun updateNickname(nickname: String) {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isSaving) return

        nicknameCheckJob?.cancel()

        val nicknameState = nickname.toProfileNicknameState(
            originalNickname = currentState.originalNickname,
        )

        _uiState.update {
            it.copy(
                nickname = nickname,
                nicknameState = nicknameState,
                errorMessage = null,
            )
        }

        if (nicknameState == ProfileNicknameState.VALID) {
            scheduleNicknameAvailabilityCheck(nickname.trim())
        }
    }

    /** 실패한 닉네임 중복 확인을 다시 요청합니다. */
    fun retryNicknameAvailabilityCheck() {
        val currentState = _uiState.value
        if (!currentState.canRetryNicknameCheck) return

        scheduleNicknameAvailabilityCheck(currentState.normalizedNickname)
    }

    /** 프로필에 저장할 응원 구단을 변경합니다. */
    fun selectTeam(team: KboTeam) {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isSaving) return

        _uiState.update {
            it.copy(
                selectedTeam = team,
                errorMessage = null,
            )
        }
    }

    /** 검증된 닉네임과 선택한 응원 구단을 서버에 저장합니다. */
    fun saveProfile() {
        val currentState = _uiState.value
        if (!currentState.isSaveEnabled) return

        val selectedTeam = currentState.selectedTeam ?: return
        val normalizedNickname = currentState.normalizedNickname

        nicknameCheckJob?.cancel()

        _uiState.update {
            it.copy(
                isSaving = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            try {
                val updatedProfile = userRepository.updateMyProfile(
                    nickname = normalizedNickname,
                    profileImageUrl = null,
                    favoriteTeam = selectedTeam,
                )

                _uiState.update {
                    it.copy(
                        profile = updatedProfile,
                        originalNickname = normalizedNickname,
                        nickname = normalizedNickname,
                        nicknameState = ProfileNicknameState.UNCHANGED,
                        selectedTeam = updatedProfile.favoriteTeam,
                        isSaving = false,
                    )
                }

                _events.send(ProfileEditEvent.Saved)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                _uiState.update {
                    it.copy(isSaving = false)
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = exception.toProfileEditErrorMessage(
                            defaultMessage = "프로필을 저장하지 못했습니다. 잠시 후 다시 시도해주세요.",
                        ),
                    )
                }
            }
        }
    }

    /** 화면에 표시된 오류 문구를 제거합니다. */
    fun clearErrorMessage() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    /** 현재 로그인한 사용자의 프로필을 조회합니다. */
    private fun loadProfile() {
        loadJob?.cancel()
        nicknameCheckJob?.cancel()

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
            )
        }

        loadJob = viewModelScope.launch {
            try {
                val profile = userRepository.getMyProfile()
                val nickname = profile.nickname?.trim().orEmpty()

                _uiState.update {
                    it.copy(
                        profile = profile,
                        originalNickname = nickname,
                        nickname = nickname,
                        nicknameState = nickname.toProfileNicknameState(nickname),
                        selectedTeam = profile.favoriteTeam,
                        isLoading = false,
                        isSaving = false,
                        errorMessage = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.toProfileEditErrorMessage(
                            defaultMessage = "프로필을 불러오지 못했습니다. 다시 시도해주세요.",
                        ),
                    )
                }
            }
        }
    }

    /** 입력이 잠시 유지되면 서버에 닉네임 중복 여부를 확인합니다. */
    private fun scheduleNicknameAvailabilityCheck(normalizedNickname: String) {
        nicknameCheckJob?.cancel()

        nicknameCheckJob = viewModelScope.launch {
            delay(NICKNAME_CHECK_DEBOUNCE_MILLIS.milliseconds)

            if (_uiState.value.normalizedNickname != normalizedNickname) return@launch

            updateNicknameStateIfCurrent(
                normalizedNickname = normalizedNickname,
                nicknameState = ProfileNicknameState.CHECKING,
            )

            try {
                val isAvailable = userRepository.isNicknameAvailable(normalizedNickname)

                ensureActive()

                updateNicknameStateIfCurrent(
                    normalizedNickname = normalizedNickname,
                    nicknameState =
                        if (isAvailable) {
                            ProfileNicknameState.AVAILABLE
                        } else {
                            ProfileNicknameState.DUPLICATED
                        },
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                ensureActive()

                updateNicknameStateIfCurrent(
                    normalizedNickname = normalizedNickname,
                    nicknameState = ProfileNicknameState.CHECK_FAILED,
                )
            } catch (exception: Exception) {
                ensureActive()

                updateNicknameStateIfCurrent(
                    normalizedNickname = normalizedNickname,
                    nicknameState = ProfileNicknameState.CHECK_FAILED,
                )
            }
        }
    }

    /** 요청한 닉네임이 현재 입력과 같을 때만 중복 확인 결과를 반영합니다. */
    private fun updateNicknameStateIfCurrent(
        normalizedNickname: String,
        nicknameState: ProfileNicknameState,
    ) {
        _uiState.update { currentState ->
            if (currentState.normalizedNickname != normalizedNickname) {
                currentState
            } else {
                currentState.copy(nicknameState = nicknameState)
            }
        }
    }
}

/** 내부 예외 정보를 노출하지 않는 프로필 수정 오류 문구로 변환합니다. */
private fun Exception.toProfileEditErrorMessage(defaultMessage: String): String =
    when (this) {
        is NetworkConnectionException -> "인터넷 연결을 확인한 후 다시 시도해주세요."
        is NetworkTimeoutException -> "서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요."
        else -> defaultMessage
    }

private const val NICKNAME_CHECK_DEBOUNCE_MILLIS = 300L
