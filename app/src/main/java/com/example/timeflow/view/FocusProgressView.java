package com.example.timeflow.view;

import android.animation.ObjectAnimator;
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
    private float progressAnimated = 100f; // 实际绘制用的属性

    public FocusProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#4DFFFFFF"));
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(20f);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(Color.WHITE); // 进度颜色
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(20f);
        progressPaint.setStrokeCap(Paint.Cap.ROUND); // 圆角笔触

        progressAnimated = progress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float center = getWidth() / 2f;
        float radius = center - 40f; // 稍微内缩一点更美观
        rectF.set(center - radius, center - radius, center + radius, center + radius);

        // 1. 画背景圆环 (半透明，看起来更高级)
        canvas.drawCircle(center, center, radius, bgPaint);

        // 2. 画进度圆弧
        // 使用 progressAnimated 确保动画流畅
        float sweepAngle = (progressAnimated / 100f) * 360f;
        canvas.drawArc(rectF, -90, sweepAngle, false, progressPaint);
    }

    public void setProgress(float targetProgress) {
        // 【核心修复】：动画应该从“当前已显示的位置”运动到“目标位置”
        // 并且要把 duration 设短，或者取消动画直接赋值（因为倒计时本身是平滑的）
        if (Math.abs(progressAnimated - targetProgress) < 0.1) return;

        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "progressAnimated", progressAnimated, targetProgress);
        animator.setDuration(900); // 略小于1秒，保证视觉连贯不卡顿
        animator.start();
    }
    // 系统回调：由 ObjectAnimator 自动调用
    public void setProgressAnimated(float value) {
        this.progressAnimated = value;
        invalidate(); // 强制重绘
    }

}