package dev.themeinerlp.skinserver.service

import dev.themeinerlp.skinserver.utils.Constants
import dev.themeinerlp.skinserver.utils.HeadView
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.ByteArrayOutputStream


@Service
class RenderService {

    /**
     * Renders a head as a ByteArray
     */
    fun renderHeadFromByteArray(size: Int, rotation: HeadView, content: ByteArray, layer: Boolean): ByteArray {
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        val original = ImageIO.read(content.inputStream())

        if (original.height == Constants.SMALL_HEIGHT_INDICATOR) {
            graphics.scale(size.toDouble().div(original.width.toDouble()) * 8 ,size.toDouble().div(original.height.toDouble()).div(2) * 8)
            graphics.drawImage(original.getSubimage(rotation.leftHead, rotation.topHead, 8, 8), 0,0, null)
            if (layer) graphics.drawImage(original.getSubimage(rotation.overlayLeft, rotation.overlayTop, 8, 8), 0,0, null)
            graphics.dispose()
        } else {
            graphics.scale(size.toDouble().div(original.width.toDouble())  * 8 ,size.toDouble().div(original.height.toDouble())  * 8)
            graphics.drawImage(original.getSubimage(rotation.leftHead, rotation.topHead, 8, 8), 0,0, null)
            if (layer) graphics.drawImage(original.getSubimage(rotation.overlayLeft, rotation.overlayTop, 8, 8), 0,0, null)
            graphics.dispose()
        }

        graphics.dispose()
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, "png", baos)
        return baos.toByteArray()
    }

    /**
     * Renders a Skin as a ByteArray
     */
    fun renderSkinFromByteArray(size: Int,content: ByteArray): ByteArray {
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val original = ImageIO.read(content.inputStream())
        val graphics = image.createGraphics()
        graphics.scale(size.toDouble().div(original.width.toDouble()),size.toDouble().div(original.height.toDouble()))
        graphics.drawImage(original,0,0,null)
        graphics.dispose()
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, "png", baos)
        return baos.toByteArray()
    }
}