package dev.themeinerlp.skinserver.spec.database

import dev.themeinerlp.skinserver.utils.HeadView
import org.springframework.http.ResponseEntity
import org.springframework.web.multipart.MultipartFile

interface RenderDatabaseHandler {
    fun renderHead(skin: MultipartFile?, size: Int?, rotation: HeadView?, layer: Boolean?): ResponseEntity<Any>
}