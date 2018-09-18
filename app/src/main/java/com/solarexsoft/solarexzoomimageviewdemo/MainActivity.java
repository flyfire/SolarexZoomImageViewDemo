package com.solarexsoft.solarexzoomimageviewdemo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.solarexsoft.solarexzoomimageview.SolarexZoomImageView;

public class MainActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private int[] mImgs = {R.mipmap.kkx0, R.mipmap.kkx1, R.mipmap.kkx2};
    private ImageView[] mImageViews = new ImageView[mImgs.length];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewPager = findViewById(R.id.vp_main);

        mViewPager.setAdapter(new PagerAdapter() {

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                SolarexZoomImageView solarexZoomImageView = new SolarexZoomImageView(getApplicationContext());
                solarexZoomImageView.setImageResource(mImgs[position]);
                container.addView(solarexZoomImageView);
                mImageViews[position] = solarexZoomImageView;
                return solarexZoomImageView;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object
                    object) {
                container.removeView(mImageViews[position]);
            }

            @Override
            public int getCount() {
                return mImgs.length;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }
        });
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
            }
        });
    }
}
