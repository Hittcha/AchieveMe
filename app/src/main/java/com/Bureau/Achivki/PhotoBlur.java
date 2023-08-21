package com.Bureau.Achivki;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;



public class PhotoBlur implements Transformation {
    private final Context context;
    private final int radius;

    public PhotoBlur (Context context, int radius) {
        this.context = context.getApplicationContext();
        this.radius = Math.max(1, Math.min(25, radius));
    }
    @Override
    public Bitmap transform(Bitmap source) {
        Bitmap blurred = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(blurred);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(source, 0, 0, paint);
        android.graphics.BlurMaskFilter filter = new android.graphics.BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL);
        paint.setMaskFilter(filter);
        canvas.drawBitmap(blurred, 0, 0, paint);
        source.recycle();
        return blurred;
    }

    @Override
    public String key() {
        return "blur(" + radius + ")";
    }
}
