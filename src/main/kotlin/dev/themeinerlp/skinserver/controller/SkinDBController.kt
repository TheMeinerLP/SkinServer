package dev.themeinerlp.skinserver.controller

import dev.themeinerlp.skinserver.repository.SkinRepository
import org.springframework.web.bind.annotation.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

@RequestMapping("db/skin")
@RestController
class SkinDBController(
    private val skinRepository: SkinRepository
) {

    @RequestMapping(

        "find/by/{uuid:[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}}",
        method = [RequestMethod.GET]
    )
    fun findSkinByUUID(
        @NotEmpty
        @NotNull
        @NotBlank
        @PathVariable(required = true) uuid: String
    ): Any {
        return "Hello"
    }


}