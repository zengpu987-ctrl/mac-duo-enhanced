package com.macduo.android

/// Shared fold angle between the activity slider and the running service.
object FoldState {
    @Volatile var manual = false
    @Volatile var manualFoldDegrees = 0f
    @Volatile var autoFoldDegrees = 0f

    val foldDegrees: Float
        get() = if (manual) manualFoldDegrees else autoFoldDegrees

    val foldProgress: Float
        get() = (foldDegrees / MAX_FOLD_DEGREES).coerceIn(0f, 1f)

    const val MAX_FOLD_DEGREES = 85f
}
