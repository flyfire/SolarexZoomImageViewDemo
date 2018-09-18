package com.solarexsoft.solarexzoomimageview;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * <pre>
 *    Author: houruhou
 *    CreatAt: 23:12/2018/9/17
 *    Desc:
 * </pre>
 */
public class SolarexZoomImageView extends ImageView implements ViewTreeObserver
        .OnGlobalLayoutListener, ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener {
    private boolean mOnce;

    private float mInitScale;  // 初始时缩放的值
    private float mMidScale;   // 双击放大时到达的值
    private float mMaxScale;   // 放大的最大值

    private Matrix mScaleMatrix;
    // 多指触摸时缩放比例
    private ScaleGestureDetector mScaleGestureDetector;

    // 自由移动
    private int mLastPointerCount;
    private float mLastPointerCenterX, mLastPointerCenterY;
    private boolean isCanDrag;
    private int mTouchSlop;

    // 双击放大
    private GestureDetector mGestureDetector;
    private boolean isAutoScale; // 正在进行双击放大缩小的操作

    public SolarexZoomImageView(Context context) {
        this(context, null);
    }

    public SolarexZoomImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SolarexZoomImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setScaleType(ScaleType.MATRIX);
        mScaleMatrix = new Matrix();
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        setOnTouchListener(this);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mGestureDetector = new GestureDetector(context, new GestureDetector
                .SimpleOnGestureListener() {

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // 若当前比例小于mMidScale放大到mMidScale，否则重置为mInitScale
                // 缓慢放大和缩小
                if (isAutoScale) {
                    return true;
                }
                float x = e.getX();
                float y = e.getY();

                float scale = getScale();
                if (scale < mMidScale) {
                    postDelayed(new AutoScaleRunnable(mMidScale, x, y), 16);
                    isAutoScale = true;
                } else {
                    postDelayed(new AutoScaleRunnable(mInitScale, x, y), 16);
                    isAutoScale = true;
                }
                return true;
            }
        });
    }

    private class AutoScaleRunnable implements Runnable {
        private float mTargetScale;
        private float pivotX;
        private float pivotY;

        private final float BIGGER_FACTOR = 1.07f;
        private final float SMALLER_FACTOR = 0.93f;

        private float scaleFactor;

        public AutoScaleRunnable(float targetScale, float pivotX, float pivotY) {
            mTargetScale = targetScale;
            this.pivotX = pivotX;
            this.pivotY = pivotY;

            float scale = getScale();
            if (scale > mTargetScale) {
                scaleFactor = SMALLER_FACTOR;
            } else {
                scaleFactor = BIGGER_FACTOR;
            }
        }

        @Override
        public void run() {
            // 进行缩放
            mScaleMatrix.postScale(scaleFactor, scaleFactor, pivotX, pivotY);
            checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);

            float currentScale = getScale();
            if ((scaleFactor > 1.0f && currentScale < mTargetScale) || (scaleFactor < 1.0f &&
                    currentScale > mTargetScale)) {
                postDelayed(this, 16);
            } else {
                float factor = mTargetScale / currentScale;
                mScaleMatrix.postScale(factor, factor, pivotX, pivotY);
                checkBorderAndCenterWhenScale();
                setImageMatrix(mScaleMatrix);
                isAutoScale = false;
            }
        }
    }

    @Override
    public void onGlobalLayout() {
        // 获取ImageView的图片，进行缩放，只需要进行一次
        if (!mOnce) {
            Drawable drawable = getDrawable();
            if (drawable == null) {
                return;
            }

            int dw = drawable.getIntrinsicWidth();
            int dh = drawable.getIntrinsicHeight();
            int width = getWidth();
            int height = getHeight();

            float scale = 1.0f;
            if (dw > width && dh < height) {
                scale = width * 1.0f / dw;
            }
            if (dw < width && dh > height) {
                scale = height * 1.0f / dh;
            }
            if (dw > width && dh > height) {
                scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
            }
            if (dw < width && dh < height) {
                scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
            }

            mInitScale = scale;
            mMidScale = scale * 2;
            mMaxScale = scale * 4;

            // 将图片移动到控件中心
            float dx = width / 2 - dw / 2;
            float dy = height / 2 - dh / 2;

            mScaleMatrix.postTranslate(dx, dy);
            mScaleMatrix.postScale(mInitScale, mInitScale, width / 2, height / 2);
            setImageMatrix(mScaleMatrix);
            mOnce = true;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        // 缩放区间 mInitScale mMaxScale
        float scaleFactor = detector.getScaleFactor();
        float scale = getScale();

        Drawable drawable = getDrawable();
        if (drawable == null) {
            return true;
        }

        // 想放大或者缩小
        if ((scale < mMaxScale && scaleFactor > 1.0f) || (scale > mInitScale && scaleFactor <
                1.0f)) {
            if (scale * scaleFactor < mInitScale) {
                scaleFactor = mInitScale / scale;
            }
            if (scale * scaleFactor > mMaxScale) {
                scaleFactor = mMaxScale / scale;
            }

            checkBorderAndCenterWhenScale();
            mScaleMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector
                    .getFocusY());
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true; // 双击时不进行移动
        }

        mScaleGestureDetector.onTouchEvent(event);

        float pointerCenterX = 0.0f;
        float pointerCenterY = 0.0f;

        int pointerCount = event.getPointerCount();

        for (int i = 0; i < pointerCount; i++) {
            x += event.getX(i);
            y += event.getY(i);
        }
        x /= pointerCount;
        y /= pointerCount;

        if (mLastPointerCount != pointerCount) {
            isCanDrag = false;
            mLastPointerCenterX = x;
            mLastPointerCenterY = y;
            mLastPointerCount = pointerCount;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = x - mLastPointerCenterX;
                float dy = y - mLastPointerCenterY;

                if (!isCanDrag) {
                    isCanDrag = isMoveAction(dx, dy);
                }

                if (isCanDrag) {
                    RectF rectF = getMatrixRectF();
                    if (getDrawable() != null) {
                        if (rectF.width() < getWidth()) {
                            dx = 0;
                        }
                        if (rectF.height() < getHeight()) {
                            dy = 0;
                        }
                        mScaleMatrix.postTranslate(dx, dy);
                        checkborderAndCenterWhenTranslate();
                        setImageMatrix(mScaleMatrix);
                    }
                }
                mLastPointerCenterX = x;
                mLastPointerCenterY = y;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLastPointerCount = 0;
                break;
        }
        return false;
    }

    private void checkborderAndCenterWhenTranslate() {
        RectF rectF = getMatrixRectF();
        float deltaX, deltaY;

        int width = getWidth();
        int height = getHeight();

        if (rectF.top > 0 && rectF.height() >= height) {
            deltaY = -rectF.top;
        }
        if (rectF.bottom < height && rectF.height() >= height) {
            deltaY = height - rectF.bottom;
        }
        if (rectF.left > 0 && rectF.width() >= width) {
            deltaX = -rectF.left;
        }
        if (rectF.right < width && rectF.width() >= width) {
            deltaX = width - rectF.right;
        }

        mScaleMatrix.postTranslate(deltaX, deltaY);
        setImageMatrix(mScaleMatrix);
    }

    private boolean isMoveAction(float dx, float dy) {
        return Math.sqrt(dx * dx + dy * dy) > mTouchSlop;
    }

    private float getScale() {
        float[] values = new float[9];
        mScaleMatrix.getValues(values);
        return values[Matrix.MSCALE_X];
    }

    private void checkBorderAndCenterWhenScale() {
        RectF rectF = getMatrixRectF();

        float deltaX = 0.0f;
        float deltaY = 0.0f;

        int width = getWidth();
        int height = getHeight();

        if (rectF.width() >= width) {
            if (rectF.left > 0) {
                deltaX = -rectF.left;
            }
            if (rectF.right < width) {
                deltaX = width - rectF.right;
            }
        }

        if (rectF.height() >= height) {
            if (rectF.top > 0) {
                deltaY = -rectF.top;
            }
            if (rectF.bottom < height) {
                deltaY = height - rectF.bottom;
            }
        }

        // 如果放大或缩小后的宽度或高度小于控件的宽度或高度，进行居中处理
        if (rectF.width() < width) {
            deltaX = width / 2f - rectF.right + rectF.width() / 2f;
        }

        if (rectF.height() < height) {
            deltaY = height / 2f - rectF.bottom + rectF.height() / 2f;
        }

        mScaleMatrix.postTranslate(deltaX, deltaY);
        setImageMatrix(mScaleMatrix);
    }

    private RectF getMatrixRectF() {
        Matrix matrix = mScaleMatrix;
        RectF rectF = new RectF();

        Drawable drawable = getDrawable();
        if (drawable != null) {
            rectF.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            // 得到放大或缩小后的大小 l t r b
            matrix.mapRect(rectF);
        }
        return rectF;
    }
}
