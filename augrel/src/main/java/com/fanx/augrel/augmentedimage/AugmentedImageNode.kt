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
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class AugmentedImageNode(context: Context) : AnchorNode() {

    // The augmented image represented by this node.
    /**
     * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
     * created based on an Anchor created from the image. The corners are then positioned based on the
     * extents of the image. There is no need to worry about world coordinates since everything is
     * relative to the center of the image, which is the parent node of the corners.
     */
    // If any of the models are not loaded, then recurse when all are loaded.
    //        if (!ulCorner.isDone() || !urCorner.isDone() || !llCorner.isDone() || !lrCorner.isDone()) {
    //                    webView.setWebViewClient(new AutoPlayVideoWebViewClient());
    //                    webView.loadUrl(DataHelper.WEBVIEW_LINK);
    /* try {
                        VideoView videoView = something.get().getView().findViewById(R.id.video_view);
                        String UrlPath = "android.resource://" + context.getPackageName() + "/" + R.raw.vid;
                        videoView.setVideoURI(Uri.parse(UrlPath));
                        videoView.start();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/// Set the anchor based on the center of the image.
    //        setAnchor(image.createAnchor(image.getCenterPose()));
    // Make the 4 corner nodes.
    //        Vector3 localPosition = new Vector3();
    //        Node cornerNode;
    // Set the anchor based on the center of the image.
    // Make the 4 corner nodes.
    /* // Upper left corner.
        localPosition.set(-0.5f * image.getExtentX(), 0.0f, -0.5f * image.getExtentZ());
        cornerNode = new Node();
        cornerNode.setParent(this);
        cornerNode.setLocalPosition(localPosition);
        cornerNode.setRenderable(ulCorner.getNow(null));

        // Upper right corner.
        localPosition.set(0.5f * image.getExtentX(), 0.0f, -0.5f * image.getExtentZ());
        cornerNode = new Node();
        cornerNode.setParent(this);
        cornerNode.setLocalPosition(localPosition);
        cornerNode.setRenderable(urCorner.getNow(null));

        // Lower right corner.
        localPosition.set(0.5f * image.getExtentX(), 0.0f, 0.5f * image.getExtentZ());
        cornerNode = new Node();
        cornerNode.setParent(this);
        cornerNode.setLocalPosition(localPosition);
        cornerNode.setRenderable(lrCorner.getNow(null));

        // Lower left corner.
        localPosition.set(-0.5f * image.getExtentX(), 0.0f, 0.5f * image.getExtentZ());
        cornerNode = new Node();
        cornerNode.setParent(this);
        cornerNode.setLocalPosition(localPosition);
        cornerNode.setRenderable(llCorner.getNow(null));*/
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
    private var hasBeenSet = false

    init {
        // Upon construction, start loading the models for the corners of the frame.
        /*    if (ulCorner == null) {
      ulCorner =
          ModelRenderable.builder()
              .setSource(context, Uri.parse("models/frame_upper_left.sfb"))
              .build();
      urCorner =
          ModelRenderable.builder()
              .setSource(context, Uri.parse("models/frame_upper_right.sfb"))
              .build();
      llCorner =
          ModelRenderable.builder()
              .setSource(context, Uri.parse("models/frame_lower_left.sfb"))
              .build();
      lrCorner =
          ModelRenderable.builder()
              .setSource(context, Uri.parse("models/frame_lower_right.sfb"))
              .build();
    }*/
        if (something == null) {
            something = ViewRenderable.builder()
                    .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
                    .setView(context, R.layout.custom_view)
                    .setSizer { view -> Vector3(0.1f, 0.1f, 0.1f) }
                    .build()
        }
    }

    companion object {

        private val TAG = "AugmentedImageNode"

        // Models of the 4 corners.  We use completable futures here to simplify
        // the error handling and asynchronous loading.  The loading is started with the
        // first construction of an instance, and then used when the image is set.
        private val ulCorner: CompletableFuture<ModelRenderable>? = null
        private val urCorner: CompletableFuture<ModelRenderable>? = null
        private val lrCorner: CompletableFuture<ModelRenderable>? = null
        private val llCorner: CompletableFuture<ModelRenderable>? = null

        private var something: CompletableFuture<ViewRenderable>? = null
    }
}
