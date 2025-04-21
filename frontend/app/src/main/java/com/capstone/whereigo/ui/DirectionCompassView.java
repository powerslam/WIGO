package com.capstone.whereigo.ui;

import com.capstone.whereigo.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class DirectionCompassView extends View {

    private final Paint paintBackground = new Paint();
    private float cameraYaw = 0f;
    private float pathYaw = 0f;
    private Bitmap bitmapUser;
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
        paintBackground.setColor(Color.parseColor("#80CCCCCC"));  // 연회색 + 50% 투명
        paintBackground.setStyle(Paint.Style.FILL);
        paintBackground.setAntiAlias(true);

        paintCircle.setColor(Color.parseColor("#133e33"));
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setStrokeWidth(10);

        paintDot.setColor(Color.YELLOW);
        paintDot.setStyle(Paint.Style.FILL);

        bitmapUser = BitmapFactory.decodeResource(getResources(), R.drawable.user_marker);

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

        float width = getWidth();
        float height = getHeight();

        // 둥근 배경 그리기 (radius는 적당히 조절)
        float cornerRadius = 80f;
        //canvas.drawRoundRect(0, 0, width, height, cornerRadius, cornerRadius, paintBackground);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - 80;

        // 원 그리기
        canvas.drawCircle(centerX, centerY, radius, paintCircle);
        canvas.drawCircle(centerX, centerY, radius-75, paintCircle);

        // 방향 점 위치 계산
        float cameraAngleRad = (float) Math.toRadians(cameraYaw);
        float pathAngleRad = (float) Math.toRadians(pathYaw);

        float cameraX = (float) (centerX + radius * Math.sin(cameraAngleRad));
        float cameraY = (float) (centerY + radius * Math.cos(cameraAngleRad));

        float pathX = (float) (centerX + radius * Math.sin(pathAngleRad));
        float pathY = (float) (centerY + radius * Math.cos(pathAngleRad));


        if (bitmapUser != null) {
            float iconSize = 180f;
            RectF dst2 = new RectF(
                    centerX - iconSize / 2,
                    centerY - iconSize / 2,
                    centerX + iconSize / 2,
                    centerY + iconSize / 2
            );
            canvas.drawBitmap(bitmapUser, null, dst2, null);
        }

        // 내 방향 원 (노란색)
        if (bitmapUser != null) {
            float iconSize = 100f;

            canvas.save(); // 현재 상태 저장

            // 회전 기준점: 아이콘 중심
            canvas.rotate(-cameraYaw + 180, cameraX, cameraY);  // 회전 (주의: Android는 시계방향 기준이므로 음수 처리)

            RectF dst = new RectF(
                    cameraX - iconSize / 2,
                    cameraY - iconSize / 2,
                    cameraX + iconSize / 2,
                    cameraY + iconSize / 2
            );

            canvas.drawBitmap(bitmapUser, null, dst, null);

            canvas.restore(); // 이전 상태로 되돌림 (다른 UI 요소에 영향 X)
        }


        // 경로 방향 삼각형 (빨간색)
        float arrowSize = 80;
        float arrowX = (float) (centerX + (radius - arrowSize) * Math.sin(pathAngleRad));
        float arrowY = (float) (centerY + (radius - arrowSize) * Math.cos(pathAngleRad));
        canvas.drawCircle(arrowX, arrowY, 35, paintArrow);  // 삼각형 대신 원으로 단순화

        // 카메라 방향선
//        float lineLength = radius * 0.9f;
//        float endX = (float) (centerX + lineLength * Math.sin(cameraAngleRad));
//        float endY = (float) (centerY + lineLength * Math.cos(cameraAngleRad));
//
//        paintDot.setStrokeWidth(8);
//        canvas.drawLine(centerX, centerY, endX, endY, paintDot);

    }
}
