package dev.themeinerlp.skinserver.model

import java.nio.file.Path
import java.nio.file.Paths

class PlayerSkin(val username: String, val size: Int) {
    val folderPath: Path = Paths.get("cache", this.username.toLowerCase().substring(0,1))
    val skinFile: Path = Paths.get(this.folderPath.toString(),"${this.username}.png")
    val textureFile: Path = Paths.get(this.skinFile.toString()  + ".url")

}