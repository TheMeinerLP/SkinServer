package dev.themeinerlp.skinserver.model

import java.nio.file.Path
import java.nio.file.Paths

class PlayerSkin {
    val username: String
    val folderPath: Path
    val skinFile: Path
    val textureFile: Path

    constructor(username: String) {
        this.username = username;
        this.folderPath = Paths.get("cache", this.username.toLowerCase().substring(0,2))
        this.skinFile = Paths.get(this.folderPath.toString(), "${this.username}.png")
        this.textureFile = Paths.get(this.skinFile.toString(), ".url")
    }

}