package com.fanx.augrel.augmentedimage

import android.content.Context
import android.content.Intent

object ArFNO {

    @JvmStatic
    var videoFilename = "lion_chroma.mp4"
    @JvmStatic
    var modelFilename = "runza_ad.imgdb"

    @JvmStatic
    fun startArActivity(withContext: Context) {
        val intent = Intent(withContext, AugmentedImageActivity::class.java)
        withContext.startActivity(intent)
    }
}