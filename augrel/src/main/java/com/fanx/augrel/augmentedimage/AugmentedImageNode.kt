package com.fanx.augrel.augmentedimage

import android.content.Context
import android.util.Log
import com.fanx.augrel.R
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import java.util.concurrent.CompletableFuture


internal class AugmentedImageNode(context: Context) : AnchorNode() {

    companion object {
        private var something: CompletableFuture<ViewRenderable>? = null
        private val TAG = "AugmentedImageNode"
    }

    private var hasBeenSet = false

    init {
        if (something == null) {
            something = ViewRenderable.builder()
                    .setVerticalAlignment(ViewRenderable.VerticalAlignment.BOTTOM)
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
                            /*
                                                        var webView: WebView? = null
                                                        try {
                                                            webView = something!!.get().view.findViewById(R.id.web_view)
                                                        } catch (e: ExecutionException) {
                                                            e.printStackTrace()
                                                        } catch (e: InterruptedException) {
                                                            e.printStackTrace()
                                                        }
                                                        webView!!.settings.javaScriptEnabled = true
                                                        webView.webChromeClient = WebChromeClient()
                                                        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                                        webView.settings.mediaPlaybackRequiresUserGesture = false
                                                        webView.settings.userAgentString = "Mozilla/5.0 (Linux; U; Android 2.0; en-us; Droid Build/ESD20) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17"
                                                        webView.loadUrl("https://clips.vorwaerts-gmbh.de/VfE_html5.mp4")*/
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
                val cornerNode = Node()
                val theView = something!!.getNow(null)
                cornerNode.setParent(this)
                cornerNode.localPosition = Vector3(0f, 0f, 0f)
                cornerNode.localRotation = Quaternion.axisAngle(Vector3(1f, 0f, 0f), 90f)
                cornerNode.renderable = theView
            }
        }
}
