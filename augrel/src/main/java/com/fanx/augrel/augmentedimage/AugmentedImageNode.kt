package com.fanx.augrel.augmentedimage

import android.content.Context
import android.util.Log
import android.webkit.WebView
import com.fanx.augrel.R
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class AugmentedImageNode(context: Context) : AnchorNode() {

    private var hasBeenSet = false

    companion object {
        private var something: CompletableFuture<ViewRenderable>? = null
        private val TAG = "AugmentedImageNode"
    }

    init {
        if (something == null) {
            something = ViewRenderable.builder()
                    .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
                    .setView(context, R.layout.custom_view)
                    .setSizer { view -> Vector3(0.1f, 0.1f, 0.1f) }
                    .build()
        }
    }

    var image: AugmentedImage? = null
        set(image) {
            field = image
            if (!something!!.isDone) {
                CompletableFuture.allOf(something!!)
                        .thenAccept { this.image = image }
                        .handle<Any> { _, _ ->

                            var webView: WebView? = null
                            try {
                                webView = something!!.get().view.findViewById(R.id.web_view)
                            } catch (e: ExecutionException) {
                                e.printStackTrace()
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }

                            webView!!.settings.javaScriptEnabled = true
                            webView.settings.mediaPlaybackRequiresUserGesture = false
                            webView.loadUrl("https://thumbs.gfycat.com/ObviousSeriousHectorsdolphin.webp")
                            null
                        }
                        .exceptionally { throwable ->
                            Log.e(TAG, "Exception loading", throwable)
                            null
                        }
            }

            if (!hasBeenSet) {

                hasBeenSet = true
                anchor = image?.createAnchor(image.centerPose)
                val localPosition = Vector3()
                val cornerNode = Node()

                val theView = something!!.getNow(null)

                localPosition.set(0.0f, 0.0f, 0.0f)
                cornerNode.setParent(this)
                cornerNode.localPosition = localPosition
                cornerNode.localRotation = Quaternion.axisAngle(Vector3(1f, 0f, 0f), 90f)
                cornerNode.renderable = theView
            }
        }
}
