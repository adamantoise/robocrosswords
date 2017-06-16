/**
 * TouchImageView.java
 * By: Michael Ortiz
 * Updated By: Patrick Lackemacher
 * Updated By: Babay88
 * Updated By: Adam Rosenfield
 * -------------------
 * Extends Android ImageView to include pinch zooming and panning.
 *
 * Source: https://github.com/MikeOrtiz/TouchImageView
 *
 *
 * Copyright (c) 2012 Michael Ortiz
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 */

package com.adamrosenfield.wordswithcrosses.view;

import java.util.logging.Logger;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;

public class TouchImageView extends ImageView implements ScaleGestureDetector.OnScaleGestureListener {

    protected static Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");

    private Matrix matrix;

    // We can be in one of these 3 states
    private enum Mode
    {
        NONE, DRAG, ZOOM
    }

    private Mode mode = Mode.NONE;

    // Remember some things for zooming
    private PointF last = new PointF();
    private PointF start = new PointF();

    private boolean canScale = true;

    private float scale = 1f;
    private float minScale = 1f;
    private float maxScale = 3f;

    private float[] m;

    private int viewWidth, viewHeight;
    private int clickSlop = 3;
    protected float origWidth, origHeight;
    private int oldMeasuredWidth, oldMeasuredHeight;

    private long lastClickTime = -1;

    private ScaleGestureDetector mScaleDetector;

    private boolean couldBeLongClick;
    private LongClickDetector lastLongClickDetector;

    public TouchImageView(Context context) {
        super(context);
        sharedConstructing(context);
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructing(context);
    }

    private void sharedConstructing(Context context) {
        super.setClickable(true);
        mScaleDetector = new ScaleGestureDetector(context, this);
        matrix = new Matrix();
        m = new float[9];
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);

        setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);
                PointF curr = new PointF(event.getX(), event.getY());

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        last.set(curr);
                        start.set(last);
                        mode = Mode.DRAG;

                        endLongClickDetection();
                        couldBeLongClick = true;
                        lastLongClickDetector = new LongClickDetector();
                        postDelayed(lastLongClickDetector, ViewConfiguration.getLongPressTimeout());
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (mode == Mode.DRAG) {
                            float deltaX = curr.x - last.x;
                            float deltaY = curr.y - last.y;

                            if (couldBeLongClick &&
                                ((int)Math.abs(curr.x - last.x) > clickSlop ||
                                 (int)Math.abs(curr.y - last.y) > clickSlop))
                            {
                                endLongClickDetection();
                            }

                            if (!couldBeLongClick) {
                                last.set(curr.x, curr.y);

                                translate(deltaX, deltaY);
                            }
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        if (mode != Mode.DRAG) {
                            mode = Mode.NONE;
                            break;
                        }
                        mode = Mode.NONE;
                        int xDiff = (int) Math.abs(curr.x - start.x);
                        int yDiff = (int) Math.abs(curr.y - start.y);
                        if (xDiff <= clickSlop && yDiff <= clickSlop) {
                            long now = System.currentTimeMillis();
                            if (now - lastClickTime < ViewConfiguration.getDoubleTapTimeout()) {
                                onDoubleClick(pixelToBitmapPos(curr.x, curr.y));
                            } else {
                                onClick(pixelToBitmapPos(curr.x, curr.y));
                            }

                            lastClickTime = now;
                        }
                        endLongClickDetection();

                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mode = Mode.NONE;
                        endLongClickDetection();
                        break;
                }

                return true; // indicate event was handled
            }

        });
    }

    private void endLongClickDetection() {
        if (couldBeLongClick) {
            couldBeLongClick = false;
            lastLongClickDetector.disable();
            lastLongClickDetector = null;
        }
    }

    // Click callbacks, which can be overridden by subclasses.  By default,
    // they just generate regular click events, which can be received through
    // normal OnClickListener instances (but without the position information).
    protected void onClick(PointF pos) {
        performClick();
    }

    protected void onDoubleClick(PointF pos) {
        performClick();
    }

    protected void onLongClick(PointF pos) {
        performLongClick();
    }

    public void translate(float dx, float dy) {
        float fixTransX = getFixDragTrans(dx, viewWidth, origWidth * scale);
        float fixTransY = getFixDragTrans(dy, viewHeight, origHeight * scale);
        matrix.postTranslate(fixTransX, fixTransY);

        onMatrixChanged();
    }

    public void setTranslate(float x, float y) {
        setScaleAndTranslate(scale, x, y);
    }

    public void setCanScale(boolean canScale) {
        this.canScale = canScale;
    }

    public void setMinScale(float minScale) {
        this.minScale = minScale;
    }

    public void setMaxScale(float maxScale) {
        this.maxScale = maxScale;
    }

    public void setClickSlop(int clickSlop) {
        this.clickSlop = clickSlop;
    }

    public void setScaleAndTranslate(float newScale, float tx, float ty) {
        scale = newScale;
        matrix.setScale(newScale, newScale);
        matrix.postTranslate(tx, ty);
        onMatrixChanged();
    }

    protected void onScaleEnd(float scale) {
        // No-op by default, can be overridden by subclasses
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        if (!canScale) {
            return false;
        }
        mode = Mode.ZOOM;
        endLongClickDetection();
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();
        float origScale = scale;
        scale *= scaleFactor;
        if (scale > maxScale) {
            scale = maxScale;
            scaleFactor = maxScale / origScale;
        } else if (scale < minScale) {
            scale = minScale;
            scaleFactor = minScale / origScale;
        }

        if (origWidth * scale <= viewWidth || origHeight * scale <= viewHeight) {
            matrix.postScale(scaleFactor, scaleFactor, viewWidth / 2.0f, viewHeight / 2.0f);
        } else {
            matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
        }

        onMatrixChanged();
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        onScaleEnd(scale);
    }

    private void onMatrixChanged() {
        fixTrans();
        setImageMatrix(matrix);
        invalidate();
    }

    private void fixTrans() {
        matrix.getValues(m);
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];

        float fixTransX = getFixTrans(transX, viewWidth, origWidth * scale);
        float fixTransY = getFixTrans(transY, viewHeight, origHeight * scale);

        if (fixTransX != 0 || fixTransY != 0) {
            matrix.postTranslate(fixTransX, fixTransY);
        }
    }

    private float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;

        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans) {
            return -trans + minTrans;
        }
        if (trans > maxTrans) {
            return -trans + maxTrans;
        }
        return 0;
    }

    private float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (oldMeasuredWidth == viewWidth && oldMeasuredHeight == viewHeight ||
            viewWidth == 0 ||
            viewHeight == 0)
        {
            return;
        }

        oldMeasuredHeight = viewHeight;
        oldMeasuredWidth = viewWidth;

        // Re-centers the image on device rotation
        //centerImage();
    }

    public void centerImage()
    {
        // Fit to screen.
        Drawable drawable = getDrawable();
        if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)
            return;
        int bmWidth = drawable.getIntrinsicWidth();
        int bmHeight = drawable.getIntrinsicHeight();

        //Log.d("bmSize", "bmWidth: " + bmWidth + " bmHeight : " + bmHeight);

        float scaleX = (float)viewWidth / (float)bmWidth;
        float scaleY = (float)viewHeight / (float)bmHeight;
        scale = Math.min(scaleX, scaleY);
        matrix.setScale(scale, scale);

        // Center the image
        float redundantXSpace = ((float)viewWidth - (scale * (float)bmWidth)) * 0.5f;
        float redundantYSpace = ((float)viewHeight - (scale * (float)bmHeight)) * 0.5f;

        matrix.postTranslate(redundantXSpace, redundantYSpace);

        origWidth = bmWidth;
        origHeight = bmHeight;
        setImageMatrix(matrix);

        fixTrans();
    }

    public PointF pixelToBitmapPos(float x, float y) {
        Matrix invMatrix = new Matrix();
        if (matrix.invert(invMatrix)) {
            float[] p = new float[]{x, y};
            invMatrix.mapPoints(p);
            return new PointF(p[0], p[1]);
        } else {
            // Should not happen
            LOG.warning("pixelToBitmapPos: Failed to invert matrix!");
            return new PointF(0.0f, 0.0f);
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);

        origWidth = drawable.getIntrinsicWidth();
        origHeight = drawable.getIntrinsicHeight();
    }

    private class LongClickDetector implements Runnable {
        private boolean enabled = true;

        public void disable() {
            enabled = false;
        }

        public void run() {
            if (enabled) {
                mode = Mode.NONE;
                onLongClick(pixelToBitmapPos(last.x, last.y));
            }
        }
    }
}
