package dev.themeinerlp.skinserver.utils

import java.net.InetAddress

object Constants {
    val DEFAULT_ADDRESSES = ArrayList(InetAddress.getAllByName(InetAddress.getLocalHost().canonicalHostName).map { it.hostAddress })
    val SMALL_HEIGHT_INDICATOR = 32
    val MOJANG_URL = "https://api.mojang.com/users/profiles/minecraft/"
    val USER_AGENT = "Minecraft-Skin-Server"
    val UUID_REGEX_STRING = "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)"
    val UUID_REGEX = Regex(UUID_REGEX_STRING)
}