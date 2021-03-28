package dev.themeinerlp.skinserver.config

enum class HeadView(val leftHead: Int, val topHead: Int, val overlayLeft: Int, val overlayTop: Int) {
    Front(8, 8, 40, 8),
    Right(0, 8, 32, 8),
    Left(16, 8, 48, 8),
    Back(24, 8, 56, 8),
    Top(8, 0, 40, 0),
    Bottom(16, 0, 48, 0),

}