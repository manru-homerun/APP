package com.manruhomerun.yadanbeopseok.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreProvider
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.savedstate.compose.LocalSavedStateRegistryOwner

/**
 * 여러 NavEntry가 부모 NavEntry의 ViewModelStore를 공유할 수 있도록 하는
 * ViewModelStore decorator를 생성합니다.
 *
 * 부모가 지정되지 않은 NavEntry에는 해당 화면만의 ViewModelStore를 제공하고,
 * 부모가 지정된 NavEntry에는 자신의 ViewModelStore와 부모의 ViewModelStore를
 * 함께 제공합니다.
 */
@Composable
fun <T : Any> rememberSharedViewModelStoreNavEntryDecorator(
    viewModelStoreOwner: ViewModelStoreOwner =
        checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided."
        },
): SharedViewModelStoreNavEntryDecorator<T> {
    val viewModelStoreProvider =
        rememberViewModelStoreProvider(
            parent = viewModelStoreOwner,
        )

    return remember(viewModelStoreProvider) {
        SharedViewModelStoreNavEntryDecorator(
            viewModelStoreProvider = viewModelStoreProvider,
        )
    }
}

/**
 * NavEntry마다 ViewModelStore를 제공하고, 부모가 지정된 NavEntry에는
 * 부모의 ViewModelStoreOwner도 함께 제공하는 decorator입니다.
 *
 * NavEntry가 백스택에서 제거되면 해당 contentKey의 ViewModelStore를
 * 정리하여 ViewModel의 생명주기를 백스택과 일치시킵니다.
 */
class SharedViewModelStoreNavEntryDecorator<T : Any>(
    viewModelStoreProvider: ViewModelStoreProvider,
) : NavEntryDecorator<T>(
    onPop = { contentKey ->
        viewModelStoreProvider.clearKey(contentKey)
    },
    decorate = { entry ->
        val entryViewModelStoreOwner =
            rememberViewModelStoreOwner(
                key = entry.contentKey,
                provider = viewModelStoreProvider,
                savedStateRegistryOwner =
                    LocalSavedStateRegistryOwner.current,
            )

        val parentContentKey = entry.metadata[PARENT_CONTENT_KEY]

        if (parentContentKey == null) {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides
                    entryViewModelStoreOwner,
            ) {
                entry.Content()
            }
        } else {
            val parentViewModelStoreOwner =
                rememberViewModelStoreOwner(
                    key = parentContentKey,
                    provider = viewModelStoreProvider,
                    savedStateRegistryOwner = LocalSavedStateRegistryOwner.current,
                )

            CompositionLocalProvider(
                LocalViewModelStoreOwner provides
                    entryViewModelStoreOwner,
                LocalSharedViewModelStoreOwner provides
                    parentViewModelStoreOwner,
            ) {
                entry.Content()
            }
        }
    },
) {
    companion object {
        /**
         * NavEntry가 공유할 부모 ViewModelStore의 contentKey를 지정합니다.
         */
        fun parent(
            parentContentKey: Any,
        ) = metadata {
            put(
                key = PARENT_CONTENT_KEY,
                value = parentContentKey,
            )
        }
    }
}

/**
 * 부모 NavEntry가 소유한 공유 ViewModelStoreOwner입니다.
 *
 * 부모 메타데이터가 설정된 NavEntry 안에서만 사용할 수 있습니다.
 */
val LocalSharedViewModelStoreOwner =
    staticCompositionLocalOf<ViewModelStoreOwner> {
        error(
            "No shared ViewModelStoreOwner was provided. " +
                "Set parent metadata on this NavEntry.",
        )
    }

private object PARENT_CONTENT_KEY : NavMetadataKey<Any>
