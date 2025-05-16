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
    private final Paint WhitepaintCircle = new Paint();
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

        WhitepaintCircle.setColor(Color.parseColor("#40FFFFFF"));
        WhitepaintCircle.setStyle(Paint.Style.FILL);

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

        float centerX = width / 2f;
        float centerY = height / 2f;
        float radius = Math.min(centerX, centerY) * 0.9f;  // 화면의 90%까지 크기 활용

        // 배경 원
        canvas.drawCircle(centerX, centerY, radius + radius * 0.1f, WhitepaintCircle);

        // 원 그리기 (비율 기반 반지름)
        paintCircle.setStrokeWidth(radius * 0.03f);
        canvas.drawCircle(centerX, centerY, radius * 0.7f, paintCircle);
        canvas.drawCircle(centerX, centerY, radius * 0.4f, paintCircle);

        // 방향 계산
        float cameraAngleRad = (float) Math.toRadians(cameraYaw);
        float pathAngleRad = (float) Math.toRadians(pathYaw);

        float cameraX = (float) (centerX + radius * Math.sin(cameraAngleRad));
        float cameraY = (float) (centerY + radius * Math.cos(cameraAngleRad));

        float pathX = (float) (centerX + radius * Math.sin(pathAngleRad));
        float pathY = (float) (centerY + radius * Math.cos(pathAngleRad));



        float cameraRadius = radius * 0.85f;

        float cameraX_icon = (float) (centerX + cameraRadius * Math.sin(cameraAngleRad));
        float cameraY_icon = (float) (centerY + cameraRadius * Math.cos(cameraAngleRad));

        // 내 방향 (회전된 아이콘)
        if (bitmapUser != null) {
            float iconSize = radius * 0.4f;

            canvas.save();
            canvas.rotate(-cameraYaw + 180, cameraX_icon, cameraY_icon);  // 회전

            RectF dst = new RectF(
                    cameraX_icon - iconSize / 2,
                    cameraY_icon - iconSize / 2,
                    cameraX_icon + iconSize / 2,
                    cameraY_icon + iconSize / 2
            );

            canvas.drawBitmap(bitmapUser, null, dst, null);
            canvas.restore();
        }

        // 경로 방향 원
        float arrowSize = radius * 0.6f;
        float arrowX = (float) (centerX + (radius - arrowSize) * Math.sin(pathAngleRad));
        float arrowY = (float) (centerY + (radius - arrowSize) * Math.cos(pathAngleRad));
        canvas.drawCircle(arrowX, arrowY, radius * 0.2f, paintArrow); // 원 크기도 비율로 조정
    }
}
