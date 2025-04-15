package com.capstone.whereigo.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class RotationIndicatorView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float angle = 0f; // degree

    public RotationIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(0xFFFFEB3B); // 노란색
    }

    public void setRotationAngle(float angle) {
        this.angle = angle;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radiusOuter = Math.min(centerX, centerY) * 0.9f;
        float radiusInner = radiusOuter * 0.6f;

        float sweepAngle = 30f; // 30도 폭의 섹터
        float startAngle = angle - (sweepAngle / 2);

        RectF oval = new RectF(centerX - radiusOuter, centerY - radiusOuter,
                centerX + radiusOuter, centerY + radiusOuter);
        canvas.drawArc(oval, startAngle, sweepAngle, true, paint);
    }
}
