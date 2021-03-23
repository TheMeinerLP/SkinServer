package dev.themeinerlp.skinserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SkinServerApplication

fun main(args: Array<String>) {
	runApplication<SkinServerApplication>(*args)
}
