package dev.themeinerlp.skinserver.config

enum class HeadView(val leftHead: Float, val topHead: Float, val overlayLeft: Float, val overlayTop: Float) {
    Front(8F,8F,40F,8F),
    Right(0F, 8F, 32F, 8F),
    Left(16F, 8F, 48F, 8F),
    Back(24F, 8F, 56F, 8F),
    Top(8F, 0F, 40F, 0F),
    Bottom(16F, 0F, 48F, 0F),

}