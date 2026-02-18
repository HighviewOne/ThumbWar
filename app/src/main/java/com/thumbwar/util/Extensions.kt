package com.thumbwar.util

fun Float.mapRange(fromMin: Float, fromMax: Float, toMin: Float, toMax: Float): Float {
    return toMin + (this - fromMin) / (fromMax - fromMin) * (toMax - toMin)
}
