package dev.themeinerlp.skinserver.model

import dev.themeinerlp.skinserver.config.HeadView
import java.nio.file.Path
import java.nio.file.Paths

data class PlayerSkin(val username: String, val size: Int, val rotation: HeadView = HeadView.Front) {
    val folderPath: Path = Paths.get("cache", this.username.toLowerCase().substring(0, 1))
    val skinFile: Path = Paths.get(this.folderPath.toString(), "${this.username}.png")
    val textureFile: Path = Paths.get(this.skinFile.toString() + ".url")

}