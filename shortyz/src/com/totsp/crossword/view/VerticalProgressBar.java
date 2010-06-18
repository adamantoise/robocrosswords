package com.totsp.crossword.view;

import android.content.Context;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.graphics.Paint.Style;

import android.util.AttributeSet;

import android.view.View;


public class VerticalProgressBar extends View {
    private static final int GRAY = Color.rgb(49, 49, 49);
    private static final int ORANGE = Color.rgb(213, 165, 24);
    private static final int GREEN = Color.rgb(49, 145, 90);
    private int height;
    private int percentComplete;
    private int width;

    public VerticalProgressBar(Context context) {
        super(context);
    }

    public VerticalProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setPercentComplete(int percentComplete) {
        this.percentComplete = percentComplete;
    }

    public int getPercentComplete() {
        return percentComplete;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(GRAY);
        paint.setStyle(Style.FILL_AND_STROKE);

        if (this.percentComplete == 0) {
            canvas.drawRect(0, 0, this.width, this.height, paint);
        } else if (this.percentComplete == 100) {
            paint.setColor(GREEN);
            canvas.drawRect(0, 0, this.width, this.height, paint);
        } else {
            float orangeHeight = ((float) this.percentComplete / 100f) * (float) this.height;

            float grayHeight = (float) this.height - orangeHeight;

            canvas.drawRect(0, 0, this.width, grayHeight, paint);
            paint.setColor(ORANGE);
            canvas.drawRect(0, grayHeight, this.width, this.height, paint);
        }
        paint.setColor(Color.BLACK);
        canvas.drawLine(0, this.height, this.width, this.height, paint);
        canvas.drawLine(0, 0, this.width, 0, paint);
        
    }

    @Override
    protected void onMeasure(int widthSpecId, int heightSpecId) {
        this.height = View.MeasureSpec.getSize(heightSpecId);
        this.width = View.MeasureSpec.getSize(widthSpecId);
        setMeasuredDimension(this.width, this.height);
    }
}
