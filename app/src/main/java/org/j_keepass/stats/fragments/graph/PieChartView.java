package org.j_keepass.stats.fragments.graph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.List;

public class PieChartView extends View {
    private Paint paint;
    private Paint textPaint;
    private List<Float> values;
    private List<Integer> colors;
    private List<String> text;
    private int textColor;
    float textSize;

    public PieChartView(Context context, List<Float> values, List<Integer> colors, List<String> text, int textColor, float textSize) {
        super(context);
        paint = new Paint();
        textPaint = new Paint();
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        this.colors = colors;
        this.values = values;
        this.text = text;
        this.textSize = textSize;
        this.textColor = textColor;
    }

    public PieChartView(Context context) {
        super(context);
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float total = 0f;
        for (Float value : values) {
            total += value;
        }

        float startAngle = 0f;
        float radius = Math.min(getWidth(), getHeight()) / 2f - 20; // Padding
        float cx = getWidth() / 2; // Center x
        float cy = getHeight() / 2; // Center y

        for (int i = 0; i < values.size(); i++) {
            float sweepAngle = (values.get(i) / total) * 360; // Calculate angle for each slice
            paint.setColor(colors.get(i));
            float left = cx - radius;
            float top = cy - radius;
            float right = cx + radius;
            float bottom = cy + radius;
            //canvas.drawArc(getWidth() / 2f - radius, getHeight() / 2f - radius, getWidth() / 2f + radius, getHeight() / 2f + radius, startAngle, sweepAngle, true, paint);
            canvas.drawArc(left, top, right, bottom, startAngle, sweepAngle, true, paint);


            // Calculate text position
            float textAngle = startAngle + (sweepAngle / 2);
            float textX = cx + (radius / 2) * (float) Math.cos(Math.toRadians(textAngle));
            float textY = cy + (radius / 2) * (float) Math.sin(Math.toRadians(textAngle));

            // Draw text
            paint.setColor(textColor); // Set text color
            paint.setTextSize(textSize);
            canvas.drawText(text.get(i), textX, textY, paint);
            startAngle += sweepAngle; // Update start angle for next slice
        }
    }
}