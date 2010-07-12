package com.totsp.crossword.view;

import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

import com.totsp.crossword.view.ScrollingImageView.AuxTouchHandler;


public class MultitouchHandler implements OnScaleGestureListener, AuxTouchHandler{
    private ScaleGestureDetector scaleDetector;
    private ScrollingImageView view;
    
    public void init(ScrollingImageView view) {
    	this.view = view;
    	this.scaleDetector = new ScaleGestureDetector(view.getContext(), this);
    }

    
    public boolean onTouchEvent(MotionEvent ev) {
    	scaleDetector.onTouchEvent(ev);
        boolean result = scaleDetector.isInProgress();
        if(!result ){
        	result = ev.getPointerCount() > 1;
        }
        System.out.println("Result: "+result+ " in progress "+scaleDetector.isInProgress());
        
        return result;
        //return scaleDetector.isInProgress();
    }

	public boolean onScale(ScaleGestureDetector detector) {
		view.zoom(detector.getScaleFactor(), (int) detector.getFocusX(), (int) detector.getFocusY());
		return true;
	}

	public boolean onScaleBegin(ScaleGestureDetector detector) {
		System.out.println("Begin");
		return true;
	}

	public void onScaleEnd(ScaleGestureDetector detector) {
		view.zoomEnd();
	}
	
	public boolean inProgress(){
		return scaleDetector.isInProgress();
	}
}
