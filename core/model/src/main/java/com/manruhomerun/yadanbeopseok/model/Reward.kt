package com.manruhomerun.yadanbeopseok.model

/**
 * 앱 내부에서 사용하는 스티커 팩 모델입니다.
 *
 * ERD의 sticker_pack과 sticker를 화면에서 쓰기 좋게 묶은 형태입니다.
 */
data class StickerPack(
    val id: String,
    val name: String,
    val year: Int,
    val region: Region,
    val stickers: List<Sticker> = emptyList(),
)

data class Sticker(
    val id: String,
    val stickerPackId: String,
    val imageUrl: String,
)
