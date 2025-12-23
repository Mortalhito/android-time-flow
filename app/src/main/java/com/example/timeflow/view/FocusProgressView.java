package com.example.timeflow.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class FocusProgressView extends View {
    private Paint bgPaint, progressPaint;
    private RectF rectF = new RectF();
    private float progress = 100f; // 100表示满圆

    public FocusProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#333333")); // 底色灰
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(25f);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(Color.parseColor("#FF4081")); // 进度颜色
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(25f);
        progressPaint.setStrokeCap(Paint.Cap.ROUND); // 圆角笔触
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float center = getWidth() / 2f;
        float radius = center - 30f;
        rectF.set(center - radius, center - radius, center + radius, center + radius);

        // 1. 画背景圆环
        canvas.drawCircle(center, center, radius, bgPaint);
        // 2. 画进度圆弧 (从顶部-90度开始)
        float sweepAngle = (progress / 100f) * 360f;
        canvas.drawArc(rectF, -90, sweepAngle, false, progressPaint);
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }
}