package com.fanx.augrel.augmentedimage

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import com.fanx.augrel.R
import com.google.ar.sceneform.ux.ArFragment

internal class AugmentedImageActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private lateinit var fitToScanView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.arfno_activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment
        fitToScanView = findViewById(R.id.image_view_fit_to_scan)
    }

    override fun onResume() {
        super.onResume()
        fitToScanView.visibility = View.VISIBLE
    }
}
