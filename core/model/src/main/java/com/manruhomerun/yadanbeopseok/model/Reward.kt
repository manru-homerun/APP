package com.manruhomerun.yadanbeopseok.model

/** 스티커를 획득하기 위해 필요한 방문 인증 관광지 수입니다. */
const val STICKER_REQUIRED_VERIFIED_SPOT_COUNT = 5

/**
 * 여행에서 획득한 스티커팩과 소속 스티커 목록입니다.
 *
 * @property id 스티커팩의 고유 식별자
 * @property name 스티커팩 이름
 * @property stickers 스티커팩에 포함된 스티커 목록
 */
data class StickerPack(
    val id: String,
    val name: String,
    val stickers: List<Sticker> = emptyList(),
)

/**
 * 스티커 이미지와 소속 스티커팩의 식별 정보입니다.
 *
 * [stickerPackId]는 Mapper에서 상위 스티커팩의 ID를 전달합니다.
 *
 * @property id 스티커의 고유 식별자
 * @property stickerPackId 스티커가 포함된 스티커팩의 식별자
 * @property imageUrl 스티커 이미지 URL
 */
data class Sticker(
    val id: String,
    val stickerPackId: String,
    val imageUrl: String,
)
