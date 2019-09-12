package com;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.fanx.augrel.augmentedimage.ArFNO;

public class AugmentedImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mainn);

//        ArFNO.useTestVideo();
        ArFNO.setVideoUrl("https://s3.amazonaws.com/fanx-hosted-app-assets/test-files/small.mp4");
        ArFNO.setModelFilename("neb_runza.imgdb");
        ArFNO.setToolbarText("Test Title");
        ArFNO.setToolbarColor(Color.CYAN);
        ArFNO.startArActivity(this);
    }
}
