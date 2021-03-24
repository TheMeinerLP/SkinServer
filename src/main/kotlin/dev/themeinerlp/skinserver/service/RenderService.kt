package dev.themeinerlp.skinserver.service

import dev.themeinerlp.skinserver.model.PlayerSkin
import org.jetbrains.skija.Data
import org.jetbrains.skija.Image
import org.jetbrains.skija.Rect
import org.jetbrains.skija.Surface
import org.springframework.stereotype.Service


@Service
class RenderService {

    val scaleSize: Float = 8F
    val smallHeightIndicator = 32

    /**
     * Renders a head as a ByteArray
     */
    fun renderHeadFront(playerSkin: PlayerSkin): ByteArray {
        val surface = Surface.makeRasterN32Premul(playerSkin.size, playerSkin.size)
        Data.makeFromFileName(playerSkin.skinFile.toString()).use {
            val skinImage = Image.makeFromEncoded(it.bytes)
            val small = skinImage.height == smallHeightIndicator
            val rotation = playerSkin.rotation
            if (small) {
                surface.canvas.drawImageRect(
                    skinImage, Rect.makeWH(skinImage.width.toFloat(), skinImage.height.toFloat())
                        .offset(rotation.leftHead,rotation.topHead),
                    Rect.makeWH(playerSkin.size.toFloat(), playerSkin.size.toFloat() / 2).scale(scaleSize, scaleSize )
                )
                surface.canvas.drawImageRect(
                    skinImage, Rect.makeWH(skinImage.width.toFloat(), skinImage.height.toFloat())
                        .offset(rotation.overlayLeft,rotation.overlayTop),
                    Rect.makeWH(playerSkin.size.toFloat(), playerSkin.size.toFloat() / 2 ).scale(scaleSize, scaleSize )
                )
            } else {
                surface.canvas.drawImageRect(
                    skinImage, Rect.makeWH(skinImage.width.toFloat(), skinImage.height.toFloat())
                        .offset(rotation.leftHead,rotation.topHead),
                    Rect.makeWH(playerSkin.size.toFloat(), playerSkin.size.toFloat()).scale(scaleSize, scaleSize )
                )
                surface.canvas.drawImageRect(
                    skinImage, Rect.makeWH(skinImage.width.toFloat(), skinImage.height.toFloat())
                        .offset(rotation.overlayLeft,rotation.overlayTop),
                    Rect.makeWH(playerSkin.size.toFloat(), playerSkin.size.toFloat()).scale(scaleSize, scaleSize )
                )
            }
        }
        return surface.makeImageSnapshot().encodeToData()!!.bytes
    }

    /**
     * Renders a Skin as a ByteArray
     */
    fun renderSkin(playerSkin: PlayerSkin): ByteArray {
        val surface = Surface.makeRasterN32Premul(playerSkin.size, playerSkin.size)
        Data.makeFromFileName(playerSkin.skinFile.toString()).use {
            val skinImage = Image.makeFromEncoded(it.bytes)
            val small = skinImage.height == smallHeightIndicator
            if (small) {
                surface.canvas.drawImageRect(
                    skinImage, Rect.makeWH(skinImage.width.toFloat(), skinImage.height.toFloat()),
                    Rect.makeWH(playerSkin.size.toFloat(), playerSkin.size.toFloat() / 2)
                )
            } else {
                surface.canvas.drawImageRect(
                    skinImage, Rect.makeWH(skinImage.width.toFloat(), skinImage.height.toFloat()),
                    Rect.makeWH(playerSkin.size.toFloat(), playerSkin.size.toFloat())
                )
            }
        }
        return surface.makeImageSnapshot().encodeToData()!!.bytes
    }
}