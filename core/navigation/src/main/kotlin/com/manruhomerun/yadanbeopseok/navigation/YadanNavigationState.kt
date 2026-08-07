package com.manruhomerun.yadanbeopseok.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.manruhomerun.yadanbeopseok.navigation.route.TopLevelNavKey

/**
 * 앱의 Nav3 백스택과 최상위 탭 이동 상태를 관리합니다.
 *
 * Feature에서는 백스택을 직접 변경하지 않고 [Navigator]를 통해
 * 화면 이동을 요청합니다.
 */
@Stable
class YadanNavigationState internal constructor(
    private val mutableBackStack: NavBackStack<NavKey>,
) : Navigator {

    init {
        require(mutableBackStack.isNotEmpty()) {
            "Navigation back stack must contain at least one NavKey."
        }
    }

    /**
     * NavDisplay에 전달할 현재 백스택입니다.
     *
     * 외부에서는 읽기만 가능하며 실제 변경은 Navigator 함수에서 처리합니다.
     */
    val backStack: List<NavKey>
        get() = mutableBackStack

    /**
     * 현재 화면의 NavKey입니다.
     */
    val currentKey: NavKey
        get() = mutableBackStack.last()

    /**
     * 현재 선택된 하단 내비게이션 탭입니다.
     *
     * 로그인이나 온보딩처럼 최상위 탭에 진입하기 전에는 null입니다.
     */
    val currentTopLevelKey: TopLevelNavKey?
        get() = mutableBackStack
            .lastOrNull { key -> key is TopLevelNavKey } as? TopLevelNavKey

    /**
     * 현재 백스택에서 뒤로 이동할 수 있는지 나타냅니다.
     */
    val canNavigateBack: Boolean
        get() = mutableBackStack.size > 1

    /**
     * 현재 화면에서 하단 내비게이션을 표시할지 나타냅니다.
     *
     * 최상위 탭의 시작 화면에서만 표시하며 상세 화면이나
     * 여행 만들기 화면에서는 숨깁니다.
     */
    val shouldShowBottomNavigation: Boolean
        get() = currentKey is TopLevelNavKey

    /**
     * 현재 화면 위에 새로운 화면을 추가합니다.
     *
     * 같은 화면이 연속으로 중복 추가되는 것을 방지합니다.
     * 최상위 화면이 전달되면 탭 이동으로 처리합니다.
     */
    override fun navigate(key: NavKey) {
        if (key is TopLevelNavKey) {
            navigateToTopLevel(key)
            return
        }

        if (currentKey != key) {
            mutableBackStack.add(key)
        }
    }

    /**
     * 현재 화면을 제거하고 이전 화면으로 이동합니다.
     *
     * 최상위 탭의 시작 화면에서는 이전에 방문한 탭으로 돌아가며,
     * 더 이상 돌아갈 화면이 없으면 아무 동작도 하지 않습니다.
     */
    override fun navigateBack() {
        if (!canNavigateBack) return

        mutableBackStack.removeAt(mutableBackStack.lastIndex)
    }

    /**
     * 하단 내비게이션의 최상위 탭으로 이동합니다.
     *
     * 각 탭의 백스택은 하나의 NavBackStack 안에서 구간별로 유지됩니다.
     * 이미 방문한 탭을 선택하면 해당 탭의 기존 상세 화면까지 복원합니다.
     * 현재 선택된 탭을 다시 누르면 탭의 시작 화면으로 이동합니다.
     */
    override fun navigateToTopLevel(key: TopLevelNavKey) {
        val currentTopLevelKey = currentTopLevelKey

        if (currentTopLevelKey == key) {
            popToTopLevelRoot(key)
            return
        }

        val targetStartIndex = mutableBackStack.indexOf(key)

        if (targetStartIndex == -1) {
            // 로그인 등의 백스택만 존재한다면 인증 흐름을 제거합니다.
            if (currentTopLevelKey == null) {
                mutableBackStack.clear()
            }

            mutableBackStack.add(key)
            return
        }

        val targetEndIndex = findTopLevelSegmentEnd(targetStartIndex)
        val targetSegment = mutableBackStack
            .subList(targetStartIndex, targetEndIndex)
            .toList()

        // 선택한 탭의 기존 백스택 구간을 맨 뒤로 옮겨 활성화합니다.
        mutableBackStack
            .subList(targetStartIndex, targetEndIndex)
            .clear()
        mutableBackStack.addAll(targetSegment)
    }

    /**
     * 현재 화면을 새로운 화면으로 교체합니다.
     *
     * 최상위 탭 시작 화면은 탭 백스택의 기준점이므로 제거하지 않고,
     * 그 위에 새로운 화면을 추가합니다.
     */
    override fun replaceCurrent(key: NavKey) {
        if (currentKey == key) return

        if (key is TopLevelNavKey) {
            if (currentKey !is TopLevelNavKey) {
                mutableBackStack.removeAt(mutableBackStack.lastIndex)
            }

            navigateToTopLevel(key)
            return
        }

        if (currentKey is TopLevelNavKey) {
            mutableBackStack.add(key)
        } else {
            mutableBackStack[mutableBackStack.lastIndex] = key
        }
    }

    /**
     * 모든 이동 기록을 제거하고 새로운 시작 화면으로 이동합니다.
     *
     * 로그인 완료, 온보딩 완료, 로그아웃 및 세션 만료 처리에 사용합니다.
     */
    override fun resetTo(key: NavKey) {
        mutableBackStack.clear()
        mutableBackStack.add(key)
    }

    /**
     * 선택된 최상위 탭 위에 쌓인 상세 화면을 모두 제거합니다.
     */
    private fun popToTopLevelRoot(key: TopLevelNavKey) {
        val rootIndex = mutableBackStack.indexOfLast { backStackKey ->
            backStackKey == key
        }

        if (rootIndex < mutableBackStack.lastIndex) {
            mutableBackStack
                .subList(rootIndex + 1, mutableBackStack.size)
                .clear()
        }
    }

    /**
     * 최상위 탭의 백스택 구간이 끝나는 인덱스를 찾습니다.
     */
    private fun findTopLevelSegmentEnd(startIndex: Int): Int =
        (startIndex + 1 until mutableBackStack.size)
            .firstOrNull { index ->
                mutableBackStack[index] is TopLevelNavKey
            }
            ?: mutableBackStack.size
}

/**
 * 구성 변경과 프로세스 재생성에도 백스택이 복원되는
 * [YadanNavigationState]를 생성합니다.
 *
 * [initialKey]는 앱 시작 시 인증 상태에 따라 로그인 화면 또는
 * 홈 화면 등의 시작 NavKey를 전달합니다.
 */
@Composable
fun rememberYadanNavigationState(
    initialKey: NavKey,
): YadanNavigationState {
    val backStack = rememberNavBackStack(initialKey)

    return remember(backStack) {
        YadanNavigationState(
            mutableBackStack = backStack,
        )
    }
}
