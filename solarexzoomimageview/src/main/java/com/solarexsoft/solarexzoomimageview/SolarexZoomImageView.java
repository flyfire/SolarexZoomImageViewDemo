package com.solarexsoft.solarexzoomimageview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * <pre>
 *    Author: houruhou
 *    CreatAt: 23:12/2018/9/17
 *    Desc:
 * </pre>
 */
public class SolarexZoomImageView extends ImageView {
    public SolarexZoomImageView(Context context) {
        this(context, null);
    }

    public SolarexZoomImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SolarexZoomImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
