package com.fanx.augrel.augmentedimage

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import com.fanx.augrel.R
import com.fanx.augrel.helpers.SnackbarHelper
import com.google.ar.core.AugmentedImage
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.ux.ArFragment
import java.util.HashMap

internal class AugmentedImageActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private lateinit var fitToScanView: ImageView

    // Augmented image and its associated center pose anchor, keyed by the augmented image in
    // the database.
    private val augmentedImageMap = HashMap<AugmentedImage, AugmentedImageNode>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment
        fitToScanView = findViewById(R.id.image_view_fit_to_scan)

//        arFragment.arSceneView.scene.addOnUpdateListener(::onUpdateFrame)
    }

    override fun onResume() {
        super.onResume()
        if (augmentedImageMap.isEmpty()) {
            fitToScanView.visibility = View.VISIBLE
        }
    }

    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     *
     * @param frameTime - time since last frame.
     */
    private fun onUpdateFrame(frameTime: FrameTime) {
        val frame = arFragment.arSceneView.arFrame ?: return

        // If there is no frame, just return.

        val updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)
        for (augmentedImage in updatedAugmentedImages) {
            when (augmentedImage.trackingState) {
                TrackingState.PAUSED -> {
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    val text = "Detected Image " + augmentedImage.index
                    SnackbarHelper.getInstance().showMessage(this, text)
                }

                TrackingState.TRACKING -> {
                    // Have to switch to UI Thread to update View.
                    fitToScanView.visibility = View.GONE

                    // Create a new anchor for newly found images.
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        val node = AugmentedImageNode(this)
                        node.image = augmentedImage
                        augmentedImageMap[augmentedImage] = node
                        arFragment.arSceneView.scene.addChild(node)
                    }
                }

                TrackingState.STOPPED -> augmentedImageMap.remove(augmentedImage)
                else -> {
                }
            }
        }
    }
}
