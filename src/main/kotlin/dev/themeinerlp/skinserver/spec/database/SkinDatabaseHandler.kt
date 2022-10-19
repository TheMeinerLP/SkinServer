package dev.themeinerlp.skinserver.spec.database

import org.springframework.http.ResponseEntity
import java.util.*

interface SkinDatabaseHandler {

    fun getSkinByUsername(size: Int?, username: String?): ResponseEntity<Any>
    fun getSkinByUUID(size: Int?, uuid: UUID?): ResponseEntity<Any>

}