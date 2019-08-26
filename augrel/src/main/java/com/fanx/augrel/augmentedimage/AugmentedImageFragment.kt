package com.fanx.augrel.augmentedimage

import android.app.ActivityManager
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fanx.augrel.R
import com.fanx.augrel.helpers.SnackbarHelper
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import java.io.IOException
import java.util.HashMap

internal class AugmentedImageFragment : ArFragment() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var externalTexture: ExternalTexture
    private lateinit var videoRenderable: ModelRenderable
    private lateinit var videoAnchorNode: AnchorNode

    private var activeAugmentedImage: AugmentedImage? = null

    private val augmentedImageMap = HashMap<AugmentedImage, AnchorNode>()

    private var isPlaying = false

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        // Check for Sceneform being supported on this device.  This check will be integrated into
        // Sceneform eventually.
        /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later")
            SnackbarHelper.getInstance()
                    .showError(activity, "Sceneform requires Android N or later")
        }*/

        val openGlVersionString = (context!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .deviceConfigurationInfo
                .glEsVersion
        if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 or later")
            SnackbarHelper.getInstance()
                    .showError(activity, "Sceneform requires OpenGL ES 3.0 or later")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaPlayer = MediaPlayer()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        // Turn off the plane discovery since we're only looking for images
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        arSceneView.planeRenderer.isEnabled = false
        arSceneView.isLightEstimationEnabled = false
        initializeSession()
        createArScene()
        return view
    }

    override fun getSessionConfiguration(session: Session): Config {
        val config = super.getSessionConfiguration(session).apply {
            focusMode = Config.FocusMode.AUTO
        }
        if (!setupAugmentedImageDatabase(config, session)) {
            SnackbarHelper.getInstance()
                    .showError(activity, "Could not setup augmented image database")
        }
        return config
    }

    private fun setupAugmentedImageDatabase(config: Config, session: Session): Boolean {

        var augmentedImageDatabase: AugmentedImageDatabase? = null

        val assetManager = if (context != null) context!!.assets else null
        if (assetManager == null) {
            Log.e(TAG, "Context is null, cannot intitialize image database.")
            return false
        }

        // This is an alternative way to initialize an AugmentedImageDatabase instance,
        // load a pre-existing augmented image database.
        try {
            context!!.assets
                    .open(ArFNO.modelFilename)
                    .use { stream ->
                        augmentedImageDatabase = AugmentedImageDatabase.deserialize(session, stream)
                    }
        } catch (e: IOException) {
            Log.e(TAG, "IO exception loading augmented image database.", e)
            return false
        }

        config.augmentedImageDatabase = augmentedImageDatabase
        return true
    }

    private fun createArScene() {
        // Create an ExternalTexture for displaying the contents of the video.
        externalTexture = ExternalTexture().also {
            mediaPlayer.setSurface(it.surface)
        }

        // Create a renderable with a material that has a parameter of type 'samplerExternal' so that
        // it can display an ExternalTexture.
        ModelRenderable.builder()
                .setSource(requireContext(), R.raw.augmented_video_model)
                .build()
                .thenAccept { renderable ->
                    videoRenderable = renderable
                    renderable.isShadowCaster = false
                    renderable.isShadowReceiver = false
                    renderable.material.setExternalTexture("videoTexture", externalTexture)
                }
                .exceptionally { throwable ->
                    Log.e(TAG, "Could not create ModelRenderable", throwable)
                    return@exceptionally null
                }

        videoAnchorNode = AnchorNode().apply {
            setParent(arSceneView.scene)
        }
    }

    private fun playbackArVideo(augmentedImage: AugmentedImage) {

//        if (!isPlaying) {
        requireContext().assets.openFd(ArFNO.videoFilename)
                .use { descriptor ->
                    mediaPlayer.setDataSource(descriptor)
                }.also {
                    mediaPlayer.isLooping = true
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                }
        isPlaying = true
//        }
        videoAnchorNode.apply {
            anchor = augmentedImage.createAnchor(augmentedImage.centerPose)
            localScale = Vector3(
                    augmentedImage.extentX, // width
                    1.0f,
                    augmentedImage.extentZ
            ) // height
            localPosition = Vector3(0f, 0f, 0f)
            localRotation = Quaternion.axisAngle(Vector3(0f, 0f, 1f), 90f)
        }
        activeAugmentedImage = augmentedImage

        externalTexture.surfaceTexture.setOnFrameAvailableListener {
            it.setOnFrameAvailableListener(null)
            videoAnchorNode.renderable = videoRenderable
        }
    }

    private fun dismissArVideo() {
        videoAnchorNode.anchor?.detach()
        videoAnchorNode.renderable = null
        activeAugmentedImage = null
        mediaPlayer.reset()
    }

    override fun onUpdate(frameTime: FrameTime?) {
        super.onUpdate(frameTime)
        val frame = arSceneView.arFrame
        if (frame == null || frame.camera.trackingState != TrackingState.TRACKING) {
            return
        }

        val updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)
        // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
        // but not yet tracked.
        for (augmentedImage in updatedAugmentedImages) {
            if (activeAugmentedImage != augmentedImage &&
                    augmentedImage.trackingState == TrackingState.TRACKING && !isPlaying
            ) {
                try {
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        dismissArVideo()
                        playbackArVideo(augmentedImage)
                        augmentedImageMap[augmentedImage] = videoAnchorNode
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Could not play video [${augmentedImage.name}]", e)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        dismissArVideo()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    companion object {

        private const val TAG = "AugmentedImageFragment"

        // This is a pre-created database containing the sample image.
//        private const val SAMPLE_IMAGE_DATABASE = "runza_ad.imgdb"

        // Do a runtime check for the OpenGL level available at runtime to avoid Sceneform crashing the
        // application.
        private const val MIN_OPENGL_VERSION = 3.0
    }
}
