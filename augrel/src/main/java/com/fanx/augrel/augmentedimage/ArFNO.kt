package com.fanx.augrel.augmentedimage

import android.content.Context
import android.content.Intent

object ArFNO {

    private const val testUrl = "https://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"

    @JvmStatic
    var videoUrl = ""
    @JvmStatic
    var modelFilename = "runza_ad.imgdb"
    @JvmStatic
    var toolbarColor = 0
    @JvmStatic
    var toolbarText = "AR Experience"
    @JvmStatic
    var toolbarTextColor = 0

    @JvmStatic
    fun useTestVideo() {
        videoUrl = testUrl
    }

    @JvmStatic
    fun startArActivity(withContext: Context) {
        val intent = Intent(withContext, AugmentedImageActivity::class.java)
        withContext.startActivity(intent)
    }
}