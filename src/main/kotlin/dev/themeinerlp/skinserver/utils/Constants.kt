package dev.themeinerlp.skinserver.utils

import java.net.InetAddress

object Constants {
    val DEFAULT_ADDRESSES = ArrayList(InetAddress.getAllByName(InetAddress.getLocalHost().canonicalHostName).map { it.hostAddress })
}