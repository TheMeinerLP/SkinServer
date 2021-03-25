package dev.themeinerlp.skinserver.service

import dev.themeinerlp.skinserver.config.HeadView
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
    fun renderHeadFromByteArray(size: Int,rotation: HeadView, content: ByteArray, layer: Boolean): ByteArray {
        val surface = Surface.makeRasterN32Premul(size, size)
        Data.makeFromBytes(content).use {
            val skinImage = Image.makeFromEncoded(it.bytes)
            val small = skinImage.height == smallHeightIndicator
            if (small) {
                surface.canvas.drawImageRect(
                    skinImage, Rect.makeWH(skinImage.width.toFloat(), skinImage.height.toFloat())
                        .offset(rotation.leftHead, rotation.topHead),
                    Rect.makeWH(size.toFloat(), size.toFloat() / 2).scale(scaleSize, scaleSize)
                )
                if (layer) {
                    surface.canvas.drawImageRect(
                        skinImage, Rect.makeWH(skinImage.width.toFloat(), skinImage.height.toFloat())
                            .offset(rotation.overlayLeft, rotation.overlayTop),
                        Rect.makeWH(size.toFloat(), size.toFloat() / 2).scale(scaleSize, scaleSize)
                    )
                }

            } else {
                surface.canvas.drawImageRect(
                    skinImage, Rect.makeWH(skinImage.width.toFloat(), skinImage.height.toFloat())
                        .offset(rotation.leftHead, rotation.topHead),
                    Rect.makeWH(size.toFloat(), size.toFloat()).scale(scaleSize, scaleSize)
                )
                if (layer) {
                    surface.canvas.drawImageRect(
                        skinImage, Rect.makeWH(skinImage.width.toFloat(), skinImage.height.toFloat())
                            .offset(rotation.overlayLeft, rotation.overlayTop),
                        Rect.makeWH(size.toFloat(), size.toFloat()).scale(scaleSize, scaleSize)
                    )
                }
            }
        }
        return surface.makeImageSnapshot().encodeToData()!!.bytes
    }

    /**
     * Renders a Skin as a ByteArray
     */
    fun renderSkinFromByteArray(size: Int,content: ByteArray): ByteArray {
        val surface = Surface.makeRasterN32Premul(size,size)
        Data.makeFromBytes(content).use {
            val skinImage = Image.makeFromEncoded(it.bytes)
            val small = skinImage.height == smallHeightIndicator
            if (small) {
                surface.canvas.drawImageRect(
                    skinImage, Rect.makeWH(skinImage.width.toFloat(), skinImage.height.toFloat()),
                    Rect.makeWH(size.toFloat(), size.toFloat() / 2)
                )
            } else {
                surface.canvas.drawImageRect(
                    skinImage, Rect.makeWH(skinImage.width.toFloat(), skinImage.height.toFloat()),
                    Rect.makeWH(size.toFloat(), size.toFloat())
                )
            }
        }
        return surface.makeImageSnapshot().encodeToData()!!.bytes
    }
}