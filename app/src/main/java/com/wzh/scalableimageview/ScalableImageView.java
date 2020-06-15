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

import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

/**
 * 支持缩放和双向滚到的ImageView
 */
public class ScalableImageView extends View implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final float IMAGE_WIDTH = Utils.dp2px(300); // 图片宽度
    private static final float SCALE = 1.5f; // 放大系数

    private Bitmap bitmap;
    private float smallScale;
    private float bigScale;
    private float offSetX;
    private float offSetY;

    private boolean isBig; // 是否放大了
    private float currentScale; // 属性动画改变的缩放
    private ObjectAnimator scaleAnimator;

    // 手势控制器，Compat表示支持低版本
    private GestureDetectorCompat gesture;

    public ScalableImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        bitmap = Utils.getAvatar(getResources(), (int) IMAGE_WIDTH);
        gesture = new GestureDetectorCompat(context, this);
        // 设置双击监听，源码中已经设置了，可以不写
//        gesture.setOnDoubleTapListener(this);
    }

    // onSizeChanged这时尺寸已经测量完毕
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 两个整数直接除，会忽略小数部分，不精确。所以转成float这样会保留小数部分
        if ((float) bitmap.getWidth() / bitmap.getHeight() > (float) getWidth() / getHeight()) {
            smallScale = (float) getWidth() / bitmap.getWidth();
            bigScale = (float) getHeight() / bitmap.getHeight() * SCALE;
        } else {
            smallScale = (float) getHeight() / bitmap.getHeight();
            bigScale = (float) getWidth() / bitmap.getWidth() * SCALE;
        }

        // 除以2f也是为了保留小数位
        offSetX = (getWidth() - bitmap.getWidth()) / 2f;
        offSetY = (getHeight() - bitmap.getHeight()) / 2f;
    }

    private float getCurrentScale() {
        return currentScale;
    }

    private void setCurrentScale(float currentScale) {
        this.currentScale = currentScale;
        invalidate();
    }

    // 懒加载属性动画
    private ObjectAnimator getScaleAnimator() {
        if (scaleAnimator == null) {
            scaleAnimator = ObjectAnimator.ofFloat(this, "currentScale", 0 , 1f);
        }
        return scaleAnimator;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float scale = smallScale + (bigScale - smallScale) * currentScale; // 当前缩放程度
        canvas.scale(scale, scale, getWidth() / 2f, getHeight() / 2f);
        canvas.drawBitmap(bitmap, offSetX , offSetY, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 让gesture去处理触摸事件，事件的回调是GestureDetectorCompat的第二个参数listener
        return gesture.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        // 相当于MotionEvent.Action_DOWN，所以一定要返回true，表示自己开始处理
        // 手势控制只有onDown的返回值需要重写，其他回调方法的返回值不用处理，给Framework层调试用的
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
        // 相当于滑动控件中的 预按下，只不过手势控制一定有预按下的时间
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        // 单击
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        // 相当于MotionEvent.Action_MOVE，手指移动时调用
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        // 长按，取消长按gesture.setIsLongpressEnabled(false);
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        // 手指快速滑动
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        // 如果设置了双击监听，单击在这里处理
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        // 双击事件
        if (isBig) {
            getScaleAnimator().reverse(); // 缩小动画
        } else {
            getScaleAnimator().start(); // 放大动画
        }
        isBig = !isBig;
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        // 双击发生后，其他后续事件（比如滑动）在这里处理
        return false;
    }
}
