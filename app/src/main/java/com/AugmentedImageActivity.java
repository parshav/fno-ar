package com;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.fanx.augrel.augmentedimage.ArFNO;

public class AugmentedImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mainn);

        ArFNO.useTestVideo();
        ArFNO.startArActivity(this);
    }
}
