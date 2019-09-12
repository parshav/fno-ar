package com.fanx.augrel.augmentedimage

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.fanx.augrel.R
import com.google.ar.sceneform.ux.ArFragment

internal class AugmentedImageActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private lateinit var backButton: ImageView
    private lateinit var xpTitle: TextView
    private lateinit var toolbar: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.arfno_activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment
        backButton = findViewById(R.id.img_back)
        xpTitle = findViewById(R.id.tv_arxp)
        toolbar = findViewById(R.id.dummy_toolbar)

        xpTitle.setTextColor(if (ArFNO.toolbarTextColor != 0) ArFNO.toolbarTextColor else Color.BLACK)
        xpTitle.text = if (ArFNO.toolbarText.isNotEmpty()) ArFNO.toolbarText else "AR Experience"

        toolbar.setBackgroundColor(if (ArFNO.toolbarColor != 0) ArFNO.toolbarColor else Color.GRAY)

        backButton.setColorFilter(
                if (ArFNO.backButtonColor != 0) ArFNO.backButtonColor
                else Color.BLACK
        )
    }

    override fun onStart() {
        super.onStart()

        backButton.setOnClickListener {
            finish()
        }
    }
}
