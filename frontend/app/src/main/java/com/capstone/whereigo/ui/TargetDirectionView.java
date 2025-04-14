package com.capstone.whereigo.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class TargetDirectionView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float angle = 0f; // degree

    public TargetDirectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(0xFFFFC107); // 밝은 노랑
    }

    public void setTargetAngle(float angle) {
        this.angle = angle;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) * 0.9f;

        // 회전 설정
        canvas.save();
        canvas.rotate(angle, centerX, centerY);

        // 사다리꼴 그리기
        Path path = new Path();
        float topWidth = radius * 0.1f;
        float bottomWidth = radius * 0.3f;
        float height = radius * 0.5f;

        path.moveTo(centerX - topWidth / 2, centerY - radius + 10);
        path.lineTo(centerX + topWidth / 2, centerY - radius + 10);
        path.lineTo(centerX + bottomWidth / 2, centerY - radius + 10 + height);
        path.lineTo(centerX - bottomWidth / 2, centerY - radius + 10 + height);
        path.close();

        canvas.drawPath(path, paint);
        canvas.restore();
    }
}
