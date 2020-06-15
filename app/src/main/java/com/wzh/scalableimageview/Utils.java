package com.wzh.scalableimageview;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.TypedValue;

public class Utils {

    public static float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    // 不能用Resources.getSystem()，这里要去取资源
    public static Bitmap getAvatar(Resources resources, int width) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, R.drawable.xiaoxin, options);
        options.inJustDecodeBounds = false;
        options.inDensity = options.outWidth;
        options.inTargetDensity = width;
        return BitmapFactory.decodeResource(resources, R.drawable.xiaoxin, options);
    }

    // 返回camera Z轴的位置
    public static float getZForCamera() {
        return - 6 * Resources.getSystem().getDisplayMetrics().density;
    }
}
