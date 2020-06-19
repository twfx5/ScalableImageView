package com.wzh.scalableimageview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;

import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

public class ScalableImageView2 extends View implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, Runnable {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final int IMAGE_WIDTH = (int) Utils.dp2px(300);
    private Bitmap bitmap;
    private float rawOffsetX, rawOffsetY;
    private float smallScale, bigScale;
    private static final float SCALE = 1.5f;
    private float current;
    private ObjectAnimator scaleAnimator;
    private boolean isBig;
    private float offsetX, offsetY;

    private GestureDetectorCompat gesture;
    // 与Scroller的区别：
    // 1 Scroller初始速度下降的很快
    // 2 可以设置过度滚动的距离，8个参数的方法的最后两个参数
    private OverScroller scroller;

    public ScalableImageView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        bitmap = Utils.getAvatar(getResources(), IMAGE_WIDTH);
        gesture = new GestureDetectorCompat(context, this);
        scroller = new OverScroller(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rawOffsetX = (getWidth() - bitmap.getWidth()) / 2f;
        rawOffsetY = (getHeight() - bitmap.getHeight()) / 2f;

        if ((float) bitmap.getWidth() / bitmap.getHeight() > (float) getWidth() / getHeight()) {
            smallScale = (float) getWidth() / bitmap.getWidth();
            bigScale = (float) getHeight() / bitmap.getHeight() * SCALE;
        } else {
            smallScale = (float) getHeight() / bitmap.getHeight();
            bigScale = (float) getWidth() / bitmap.getWidth() * SCALE;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(offsetX * current, offsetY * current);
        float scale = smallScale + (bigScale - smallScale) * current;
        canvas.scale(scale, scale, getWidth() / 2f, getHeight() /2f);
        canvas.drawBitmap(bitmap, rawOffsetX, rawOffsetY, paint);
    }

    public float getCurrent() {
        return current;
    }

    public void setCurrent(float current) {
        this.current = current;
        invalidate();
    }

    private ObjectAnimator getScaleAnimator() {
        if (scaleAnimator == null) {
            scaleAnimator = ObjectAnimator.ofFloat(this, "current", 0, 1);
            scaleAnimator.setDuration(500);
        }
        return scaleAnimator;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gesture.onTouchEvent(event);
    }


    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        if (isBig) {
            getScaleAnimator().reverse();
        } else {
            getScaleAnimator().start();
        }
        isBig = !isBig;
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    // scrollX，scrollY 后面的点 减 前面的点
    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float scrollX,  float scrollY) {
        if (isBig) {
            offsetX -= scrollX;
            offsetY -= scrollY;
            offsetX = Math.min(offsetX, (bitmap.getWidth() * bigScale - getWidth()) / 2); // 最大不能超过
            offsetX = Math.max(offsetX, - (bitmap.getWidth() * bigScale - getWidth()) / 2); // 最小不能小于
            offsetY = Math.min(offsetY, (bitmap.getHeight() * bigScale - getHeight()) / 2);
            offsetY = Math.max(offsetY, - (bitmap.getHeight() * bigScale - getHeight()) / 2);
            invalidate();
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        if (isBig) {
            // scroller相当于一个计算器，它只是负责计算，不能做动画
            // (int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY)
            scroller.fling((int) offsetX, (int) offsetY, (int) v, (int) v1,
                    - (int) (bitmap.getWidth() * bigScale - getWidth()) / 2,
                    (int) (bitmap.getWidth() * bigScale - getWidth()) / 2,
            -(int) (bitmap.getHeight() * bigScale - getHeight()) / 2,
                    (int) (bitmap.getHeight() * bigScale - getHeight()) / 2);
            // postOnAnimation 让run()里面的方法在下一帧执行，可以不写ObjectAnimator而执行动画
            postOnAnimation(this);
        }
        return false;
    }

    // 刷新界面
    @Override
    public void run() {

        //scroller.computeScrollOffset(); // 计算

        // 动画没执行完时，继续动画
        if (scroller.computeScrollOffset()) {
            offsetX = scroller.getCurrX();
            offsetY = scroller.getCurrY();
            invalidate();
            postOnAnimation(this);
        }

    }
}
