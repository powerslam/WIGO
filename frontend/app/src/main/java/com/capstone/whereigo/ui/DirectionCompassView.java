package com.capstone.whereigo.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class DirectionCompassView extends View {
    private float cameraYaw = 0f;
    private float pathYaw = 0f;
    private final Paint paintCircle = new Paint();
    private final Paint paintDot = new Paint();
    private final Paint paintArrow = new Paint();

    public DirectionCompassView(Context context) {
        super(context);
        init();
    }

    public DirectionCompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DirectionCompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paintCircle.setColor(Color.GRAY);
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setStrokeWidth(5);

        paintDot.setColor(Color.YELLOW);
        paintDot.setStyle(Paint.Style.FILL);

        paintArrow.setColor(Color.RED);
        paintArrow.setStyle(Paint.Style.FILL);
    }

    public void setYawValues(float cameraYawDeg, float pathYawDeg) {
        this.cameraYaw = cameraYawDeg;
        this.pathYaw = pathYawDeg;
        invalidate();  // UI 다시 그리기
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - 20;

        // 원 그리기
        canvas.drawCircle(centerX, centerY, radius, paintCircle);

        // 방향 점 위치 계산
        float cameraAngleRad = (float) Math.toRadians(cameraYaw);
        float pathAngleRad = (float) Math.toRadians(pathYaw);

        float cameraX = (float) (centerX + radius * Math.sin(cameraAngleRad));
        float cameraY = (float) (centerY - radius * Math.cos(cameraAngleRad));

        float pathX = (float) (centerX + radius * Math.sin(pathAngleRad));
        float pathY = (float) (centerY - radius * Math.cos(pathAngleRad));

        // 내 방향 원 (노란색)
        canvas.drawCircle(cameraX, cameraY, 20, paintDot);

        // 경로 방향 삼각형 (빨간색)
        float arrowSize = 40;
        float arrowX = (float) (centerX + (radius - arrowSize) * Math.sin(pathAngleRad));
        float arrowY = (float) (centerY - (radius - arrowSize) * Math.cos(pathAngleRad));
        canvas.drawCircle(arrowX, arrowY, 15, paintArrow);  // 삼각형 대신 원으로 단순화
    }
}
