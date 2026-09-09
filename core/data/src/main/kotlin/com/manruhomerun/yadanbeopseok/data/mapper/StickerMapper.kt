package com.manruhomerun.yadanbeopseok.data.mapper

import com.manruhomerun.yadanbeopseok.common.InvalidResponseException
import com.manruhomerun.yadanbeopseok.model.Sticker
import com.manruhomerun.yadanbeopseok.model.StickerPack
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelStickerResponseDto

/**
 * 여행의 스티커 조회 응답을 앱 내부 스티커팩으로 변환합니다.
 *
 * 미획득 상태에서는 null을 반환합니다.
 * 획득 여부와 스티커팩 존재 여부가 다르면 응답 오류로 처리합니다.
 * 각 스티커의 소속 팩 ID는 상위 스티커팩에서 전달합니다.
 */
internal fun TravelStickerResponseDto.toStickerPack(): StickerPack? {
    val pack = stickerPack

    if (hasSticker != (pack != null)) {
        throw InvalidResponseException(
            message = "Sticker ownership does not match stickerPack.",
        )
    }

    if (pack == null) return null

    val packId = pack.id.toString()

    return StickerPack(
        id = packId,
        name = pack.name,
        stickers = pack.stickers.map { sticker ->
            Sticker(
                id = sticker.id.toString(),
                stickerPackId = packId,
                imageUrl = sticker.image,
            )
        },
    )
}
