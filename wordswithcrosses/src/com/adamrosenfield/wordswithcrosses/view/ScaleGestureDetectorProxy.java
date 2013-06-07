package com.adamrosenfield.wordswithcrosses.view;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import android.annotation.TargetApi;
import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public abstract class ScaleGestureDetectorProxy
{
    public abstract boolean onTouchEvent(MotionEvent event);

    public static ScaleGestureDetectorProxy create(Context context, TouchImageView view)
    {
        if (android.os.Build.VERSION.SDK_INT >= 8)
        {
            try
            {
                // Construct the listener object
                Class<?> clazz = Class.forName("com.adamrosenfield.wordswithcrosses.view.TouchImageView$ScaleListener");
                Constructor<?> constructor = clazz.getConstructor(TouchImageView.class);
                Object listener = constructor.newInstance(view);

                // Construct the detector proxy
                clazz = Class.forName("com.adamrosenfield.wordswithcrosses.view.ScaleGestureDetectorProxy$ScaleGestureDetectorProxyFroyo");
                constructor = clazz.getConstructor(Context.class, Class.forName("android.view.ScaleGestureDetector$OnScaleGestureListener"));
                return (ScaleGestureDetectorProxy)constructor.newInstance(context, listener);
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (NoSuchMethodException e)
            {
                e.printStackTrace();
            }
            catch (SecurityException e)
            {
                e.printStackTrace();
            }
            catch (InstantiationException e)
            {
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
            catch (InvocationTargetException e)
            {
                e.printStackTrace();
            }
        }

        return new ScaleGestureDetectorProxyDefault();
    }

    public static class ScaleGestureDetectorProxyDefault extends ScaleGestureDetectorProxy
    {
        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            return false;
        }
    }

    @TargetApi(8)
    public static class ScaleGestureDetectorProxyFroyo extends ScaleGestureDetectorProxy
    {
        private ScaleGestureDetector mDetector;

        public ScaleGestureDetectorProxyFroyo(Context context, Object listener)
        {
            mDetector = new ScaleGestureDetector(context, (ScaleGestureDetector.OnScaleGestureListener)listener);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            return mDetector.onTouchEvent(event);
        }
    }
}
