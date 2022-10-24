package dev.themeinerlp.skinserver.spec.database

import dev.themeinerlp.skinserver.utils.HeadView
import org.springframework.http.ResponseEntity
import java.util.*

interface HeadDatabaseHandler {

    fun getHeadByUsername(size: Int?, username: String?, rotation: HeadView?, layer: Boolean?): ResponseEntity<Any>
    fun getHeadByUUID(size: Int?, uuid: UUID?, rotation: HeadView?, layer: Boolean?): ResponseEntity<Any>

}