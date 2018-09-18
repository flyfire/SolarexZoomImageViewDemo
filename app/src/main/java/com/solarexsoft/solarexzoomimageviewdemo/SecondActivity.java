package com.solarexsoft.solarexzoomimageviewdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.solarexsoft.solarexzoomimageview.SolarexZoomImageView;

/**
 * <pre>
 *    Author: houruhou
 *    CreatAt: 00:20/2018/9/19
 *    Desc:
 * </pre>
 */
public class SecondActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        SolarexZoomImageView szi = findViewById(R.id.szi);
        szi.setImageResource(R.mipmap.ic_launcher);
    }
}
