package com.manruhomerun.yadanbeopseok.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.error.ApiException
import com.manruhomerun.yadanbeopseok.common.error.InvalidResponseException
import com.manruhomerun.yadanbeopseok.common.error.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.error.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.error.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.OnboardingAgreementsParams
import com.manruhomerun.yadanbeopseok.data.repository.OnboardingRepository
import com.manruhomerun.yadanbeopseok.data.repository.SaveOnboardingParams
import com.manruhomerun.yadanbeopseok.model.Gender
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.ProfileRegion
import com.manruhomerun.yadanbeopseok.model.TravelStyleScore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * 약관부터 여행 프로필까지 전체 온보딩 입력과 저장 요청을 관리합니다.
 *
 * 약관 동의 화면이 ViewModel을 소유하고 나머지 온보딩 화면은
 * 부모 ViewModelStore를 통해 같은 인스턴스를 공유합니다.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private var nicknameCheckJob: Job? = null

    private val _sessionExpiredEvents = Channel<Unit>(capacity = Channel.BUFFERED)
    val sessionExpiredEvents: Flow<Unit> = _sessionExpiredEvents.receiveAsFlow()

    private val _completionEvents = Channel<Unit>(capacity = Channel.BUFFERED)
    val completionEvents: Flow<Unit> = _completionEvents.receiveAsFlow()

    /** 서비스 이용약관 동의 상태를 변경합니다. */
    fun updateServiceTermsAgreement(agreed: Boolean) {
        _uiState.update {
            it.copy(isServiceTermsAgreed = agreed)
        }
    }

    /** 개인정보 수집·이용 동의 상태를 변경합니다. */
    fun updatePrivacyAgreement(agreed: Boolean) {
        _uiState.update {
            it.copy(isPrivacyAgreementAgreed = agreed)
        }
    }

    /** 모든 필수 약관 동의 상태를 한 번에 변경합니다. */
    fun updateAllAgreements(agreed: Boolean) {
        _uiState.update {
            it.copy(
                isServiceTermsAgreed = agreed,
                isPrivacyAgreementAgreed = agreed,
            )
        }
    }

    /** 닉네임을 저장하고 로컬 규칙을 통과하면 중복 확인을 예약합니다. */
    fun updateNickname(nickname: String) {
        nicknameCheckJob?.cancel()

        val inputState = nickname.toNicknameInputState()

        _uiState.update {
            it.copy(
                nickname = nickname,
                nicknameInputState = inputState,
            )
        }

        if (inputState != NicknameInputState.VALID) {
            return
        }

        scheduleNicknameAvailabilityCheck(
            normalizedNickname = nickname.trim(),
        )
    }

    /** 실패한 현재 닉네임의 중복 확인을 다시 요청합니다. */
    fun retryNicknameAvailabilityCheck() {
        val currentState = _uiState.value

        if (currentState.nicknameInputState != NicknameInputState.CHECK_FAILED) {
            return
        }

        updateNickname(currentState.nickname)
    }

    /**
     * 연속 입력이 끝난 뒤 닉네임 중복 확인 API를 호출합니다.
     *
     * 새 입력이 들어오면 이전 Job을 취소하고, 늦게 도착한 응답도
     * 현재 입력과 일치할 때만 상태에 반영합니다.
     */
    private fun scheduleNicknameAvailabilityCheck(normalizedNickname: String) {
        nicknameCheckJob =
            viewModelScope.launch {
                delay(NICKNAME_CHECK_DEBOUNCE_MILLIS.milliseconds)

                if (_uiState.value.normalizedNickname != normalizedNickname) {
                    return@launch
                }

                updateNicknameStateIfCurrent(
                    normalizedNickname = normalizedNickname,
                    inputState = NicknameInputState.CHECKING,
                )

                try {
                    val isAvailable =
                        onboardingRepository.isNicknameAvailable(
                            nickname = normalizedNickname,
                        )

                    ensureActive()

                    updateNicknameStateIfCurrent(
                        normalizedNickname = normalizedNickname,
                        inputState =
                            if (isAvailable) {
                                NicknameInputState.AVAILABLE
                            } else {
                                NicknameInputState.DUPLICATED
                            },
                    )
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: SessionExpiredException) {
                    ensureActive()

                    updateNicknameStateIfCurrent(
                        normalizedNickname = normalizedNickname,
                        inputState = NicknameInputState.CHECK_FAILED,
                    )
                    _sessionExpiredEvents.send(Unit)
                } catch (exception: Exception) {
                    ensureActive()

                    updateNicknameStateIfCurrent(
                        normalizedNickname = normalizedNickname,
                        inputState = NicknameInputState.CHECK_FAILED,
                    )
                }
            }
    }

    /** 현재 입력과 요청한 닉네임이 일치할 때만 검증 상태를 변경합니다. */
    private fun updateNicknameStateIfCurrent(
        normalizedNickname: String,
        inputState: NicknameInputState,
    ) {
        _uiState.update { currentState ->
            if (currentState.normalizedNickname != normalizedNickname) {
                currentState
            } else {
                currentState.copy(
                    nicknameInputState = inputState,
                )
            }
        }
    }

    /** 사용자가 선택한 성별을 저장합니다. */
    fun selectGender(gender: Gender) {
        _uiState.update {
            it.copy(gender = gender)
        }
    }

    /** 생년월일을 저장하고 가입 연령을 검사합니다. */
    fun updateBirthDate(birthDate: LocalDate?) {
        val currentDate =
            Clock.System.todayIn(
                TimeZone.currentSystemDefault(),
            )

        _uiState.update {
            it.copy(
                birthDate = birthDate,
                birthDateInputState =
                    birthDate.toBirthDateInputState(
                        currentDate = currentDate,
                    ),
            )
        }
    }

    /** 사용자가 선택한 응원 구단을 저장합니다. */
    fun selectTeam(team: KboTeam) {
        _uiState.update {
            it.copy(selectedTeam = team)
        }
    }

    /** 사용자가 선택한 거주 지역을 저장합니다. */
    fun selectResidenceRegion(region: ProfileRegion) {
        require(region.isAvailableForResidence) {
            "Region is not available for residence: ${region.code}"
        }

        _uiState.update {
            it.copy(residenceRegion = region)
        }
    }

    /** 사용자가 선택한 여행 성향 점수를 저장합니다. */
    fun updateTravelStyleScore(score: TravelStyleScore) {
        _uiState.update {
            it.copy(travelStyleScore = score)
        }
    }

    /** 선호 여행 지역의 선택 여부를 전환합니다. */
    fun togglePreferredTravelRegion(
        region: ProfileRegion,
    ) {
        require(region.isAvailableForPreferredTravel) {
            "Region is not available for preferred travel: ${region.code}"
        }

        _uiState.update { currentState ->
            val selectedRegions =
                currentState.preferredTravelRegions

            currentState.copy(
                preferredTravelRegions =
                    if (region in selectedRegions) {
                        selectedRegions - region
                    } else {
                        selectedRegions + region
                    },
            )
        }
    }

    /**
     * 입력한 온보딩 정보를 서버에 저장합니다.
     *
     * 필수 입력이 누락됐거나 요청이 진행 중이면 실행하지 않습니다.
     */
    fun submitOnboarding() {
        val currentState = _uiState.value

        if (!currentState.isOnboardingSubmitEnabled) {
            return
        }

        val params = currentState.toSaveOnboardingParams() ?: return

        _uiState.update {
            it.copy(
                isSubmitting = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            try {
                onboardingRepository.saveOnboarding(params)

                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = null,
                    )
                }

                _completionEvents.send(Unit)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SessionExpiredException) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = null,
                    )
                }

                _sessionExpiredEvents.send(Unit)
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = exception.toOnboardingErrorMessage(),
                    )
                }
            }
        }
    }

    /** 화면에 오류를 표시한 뒤 현재 오류 상태를 제거합니다. */
    fun clearErrorMessage() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }
}

/**
 * 현재 온보딩 상태를 Repository 요청 모델로 변환합니다.
 *
 * 필수 입력이 완료되지 않았다면 null을 반환합니다.
 */
private fun OnboardingUiState.toSaveOnboardingParams():
    SaveOnboardingParams? {
    if (!isOnboardingReadyToSubmit) {
        return null
    }

    val resolvedGender = gender ?: return null
    val resolvedBirthDate = birthDate ?: return null
    val resolvedTeam = selectedTeam ?: return null
    val resolvedResidenceRegion =
        residenceRegion ?: return null

    return SaveOnboardingParams(
        agreements =
            OnboardingAgreementsParams(
                serviceTerms = isServiceTermsAgreed,
                privacyPolicy = isPrivacyAgreementAgreed,
            ),
        nickname = normalizedNickname,
        gender = resolvedGender,
        birthDate = resolvedBirthDate,
        favoriteTeam = resolvedTeam,
        residenceRegion = resolvedResidenceRegion,
        travelStyleScore = travelStyleScore,
        preferredTravelRegions = preferredTravelRegions,
    )
}

/**
 * 온보딩 저장 예외를 사용자 안내 문구로 변환합니다.
 *
 * 내부 예외 정보와 인증 토큰은 화면이나 로그에 노출하지 않습니다.
 */
private fun Exception.toOnboardingErrorMessage(): String =
    when (this) {
        is NetworkConnectionException ->
            "인터넷 연결을 확인한 후 다시 시도해주세요."

        is NetworkTimeoutException ->
            "서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요."

        is SessionExpiredException ->
            "로그인 정보가 만료되었습니다. 다시 로그인해주세요."

        is ApiException ->
            "온보딩 저장에 실패했습니다. 잠시 후 다시 시도해주세요."

        is InvalidResponseException ->
            "온보딩 응답을 확인할 수 없습니다. 잠시 후 다시 시도해주세요."

        else ->
            "온보딩 저장에 실패했습니다. 잠시 후 다시 시도해주세요."
    }

private const val NICKNAME_CHECK_DEBOUNCE_MILLIS = 300L
