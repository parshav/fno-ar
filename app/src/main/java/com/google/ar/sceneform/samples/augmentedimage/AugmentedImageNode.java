/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ar.sceneform.samples.augmentedimage;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.rendering.ViewSizer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Node for rendering an augmented image. The image is framed by placing the virtual picture frame
 * at the corners of the augmented image trackable.
 */
@SuppressWarnings({"AndroidApiChecker"})
public class AugmentedImageNode extends AnchorNode {

    private static final String TAG = "AugmentedImageNode";

    // The augmented image represented by this node.
    private AugmentedImage image;

    // Models of the 4 corners.  We use completable futures here to simplify
    // the error handling and asynchronous loading.  The loading is started with the
    // first construction of an instance, and then used when the image is set.
    private static CompletableFuture<ModelRenderable> ulCorner;
    private static CompletableFuture<ModelRenderable> urCorner;
    private static CompletableFuture<ModelRenderable> lrCorner;
    private static CompletableFuture<ModelRenderable> llCorner;

    private static CompletableFuture<ViewRenderable> something;
    private boolean hasBeenSet = false;

    public AugmentedImageNode(Context context) {
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
            something =
                ViewRenderable.builder()
                    .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
                    .setView(context, R.layout.custom_view)
                    .setSizer(new ViewSizer() {
                        @Override
                        public Vector3 getSize(View view) {
                            return new Vector3(0.1f, 0.1f, 0.1f);
                        }
                    })
                    .build();
        }
    }

    /**
     * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
     * created based on an Anchor created from the image. The corners are then positioned based on the
     * extents of the image. There is no need to worry about world coordinates since everything is
     * relative to the center of the image, which is the parent node of the corners.
     */
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    public void setImage(AugmentedImage image) {
        this.image = image;

        // If any of the models are not loaded, then recurse when all are loaded.
//        if (!ulCorner.isDone() || !urCorner.isDone() || !llCorner.isDone() || !lrCorner.isDone()) {
        if (!something.isDone()) {
            CompletableFuture.allOf(something)
                .thenAccept((Void aVoid) -> setImage(image))
                .handle((as, s) -> {

                    WebView webView = null;
                    try {
                        webView = something.get().getView().findViewById(R.id.web_view);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
//                    webView.setWebViewClient(new AutoPlayVideoWebViewClient());
                    webView.loadUrl("https://thumbs.gfycat.com/ObviousSeriousHectorsdolphin.webp");
//                    webView.loadUrl(DataHelper.WEBVIEW_LINK);

                   /* try {
                        VideoView videoView = something.get().getView().findViewById(R.id.video_view);
                        String UrlPath = "android.resource://" + context.getPackageName() + "/" + R.raw.vid;
                        videoView.setVideoURI(Uri.parse(UrlPath));
                        videoView.start();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                    return null;
                })
                .exceptionally(
                    throwable -> {
                        Log.e(TAG, "Exception loading", throwable);
                        return null;
                    });
        }

        // Set the anchor based on the center of the image.
//        setAnchor(image.createAnchor(image.getCenterPose()));

        // Make the 4 corner nodes.
//        Vector3 localPosition = new Vector3();
//        Node cornerNode;

        if (!hasBeenSet) {

            hasBeenSet = true;

            // Set the anchor based on the center of the image.
            setAnchor(image.createAnchor(image.getCenterPose()));

            // Make the 4 corner nodes.
            Vector3 localPosition = new Vector3();
            Node cornerNode;

            ViewRenderable theView = something.getNow(null);

            localPosition.set(0.0f, 0.0f, 0.0f);
            cornerNode = new Node();
            cornerNode.setParent(this);
            cornerNode.setLocalPosition(localPosition);
            cornerNode.setLocalRotation(Quaternion.axisAngle(new Vector3(1f, 0f, 0f), 90f));
            cornerNode.setRenderable(theView);
        }
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
    }

    public AugmentedImage getImage() {
        return image;
    }
}
