package com.projects.wallet.facerecognition

import android.graphics.Bitmap
import android.graphics.Rect

data class Prediction( var bbox : Rect, var label : String, val score:Double = 1.0)

data class InternalStoragePhoto(
    val name: String,
    val bmp: Bitmap
)
