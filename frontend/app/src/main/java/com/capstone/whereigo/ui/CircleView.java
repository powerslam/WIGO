package com.capstone.whereigo.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CircleView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0x55FFFFFF); // 반투명 흰색
        paint.setStrokeWidth(6f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radiusOuter = Math.min(centerX, centerY) * 0.9f;
        float radiusInner = radiusOuter * 0.6f;

        canvas.drawCircle(centerX, centerY, radiusOuter, paint);
        canvas.drawCircle(centerX, centerY, radiusInner, paint);
    }
}
