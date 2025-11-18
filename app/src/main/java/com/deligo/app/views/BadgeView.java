package com.deligo.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.deligo.app.R;

/**
 * Custom badge view to display notification count
 */
public class BadgeView extends View {
    private Paint backgroundPaint;
    private Paint textPaint;
    private int count = 0;
    private static final int MAX_COUNT = 99;

    public BadgeView(Context context) {
        super(context);
        init();
    }

    public BadgeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BadgeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Background paint
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(ContextCompat.getColor(getContext(), R.color.error));
        backgroundPaint.setStyle(Paint.Style.FILL);

        // Text paint
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.white));
        textPaint.setTextSize(dpToPx(10));
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setCount(int count) {
        this.count = count;
        setVisibility(count > 0 ? VISIBLE : GONE);
        invalidate();
    }

    public int getCount() {
        return count;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (count <= 0) {
            return;
        }

        int width = getWidth();
        int height = getHeight();
        float radius = Math.min(width, height) / 2f;

        // Draw background circle
        canvas.drawCircle(width / 2f, height / 2f, radius, backgroundPaint);

        // Draw count text
        String text = count > MAX_COUNT ? "99+" : String.valueOf(count);
        float textY = height / 2f - ((textPaint.descent() + textPaint.ascent()) / 2);
        canvas.drawText(text, width / 2f, textY, textPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = (int) dpToPx(20);
        setMeasuredDimension(size, size);
    }

    private float dpToPx(float dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
    }
}
