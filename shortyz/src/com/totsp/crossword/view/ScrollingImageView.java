package com.totsp.crossword.view;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;


public class ScrollingImageView extends AbsoluteLayout
    implements OnGestureListener {
    private static final Logger LOG = Logger.getLogger("com.totsp.crossword");
    private ClickListener ctxListener;
    private Context context;
    private GestureDetector gestureDetector;
    private ImageView imageView;
    private double xScrollPercent;
    private double yScrollPercent;
    private Timer longTouchTimer = new Timer();

    public ScrollingImageView(Context context, AttributeSet as) {
        super(context, as);
        this.context = context;
        gestureDetector = new GestureDetector(this);
        gestureDetector.setIsLongpressEnabled(false);
        imageView = new ImageView(context);
    }

    public void setBitmap(Bitmap bitmap) {
        if (imageView != null) {
            this.removeView(imageView);
        }

        LOG.finest("New Bitmap Size: " + bitmap.getWidth() + " x " +
            bitmap.getHeight());
        imageView.setImageBitmap(bitmap);

        AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(bitmap.getWidth(),
                bitmap.getHeight(), 5, 5);
        this.addView(imageView, params);
    }

    public void setContextMenuListener(ClickListener l) {
        this.ctxListener = l;
    }

    public ImageView getImageView() {
        return this.imageView;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);

        return true;
    }

    public boolean onDown(MotionEvent e) {
        final Point p = new Point();
        p.x = (int) (e.getX() + this.getScrollX());
        p.y = (int) (e.getY() + this.getScrollY());
        this.longTouchTimer = new Timer();
        TimerTask longTouchTask = new TimerTask(){

    		@Override
    		public void run() {
    			LOG.info("LONG FIRED");
    			if(ScrollingImageView.this.ctxListener != null){
    				ScrollingImageView.this.ctxListener.onContextMenu(p);
    				ScrollingImageView.this.longTouched = true;
    			}
    		}
        	
        };
        this.longTouchTimer.schedule(longTouchTask, 750);
        
        return true;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
        float velocityY) {
        // TODO Auto-generated method stub 
        return false;
    }

    public void onLongPress(MotionEvent e) {
        System.out.println("Long.");
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
        float distanceY) {
        System.out.println(distanceX + " " + distanceY);
        this.longTouchTimer.cancel();
        this.longTouched = false;
        int scrollWidth = imageView.getWidth() - this.getWidth() + 10;

        if(imageView.getWidth() + 5 > this.getWidth()){
        
	        if ((this.getScrollX() >= 0) && (this.getScrollX() <= scrollWidth) &&
	                (scrollWidth > 0)) {
	            int moveX = (int) distanceX;
	
	            if (((moveX + this.getScrollX()) >= 0) &&
	                    ((Math.abs(moveX) + Math.abs(this.getScrollX())) <= scrollWidth)) {
	                this.scrollBy(moveX, 0);
	            } else {
	                if (distanceX >= 0) {
	                    int xScroll = scrollWidth -
	                        Math.max(Math.abs(moveX), Math.abs(this.getScrollX()));
	                    this.scrollBy(xScroll, 0);
	                } else {
	                    this.scrollBy(-Math.min(Math.abs(moveX),
	                            Math.abs(this.getScrollX())), 0);
	                }
	            }
	        }
        }

        int scrollHeight = imageView.getHeight() - this.getHeight() + 10;

        if(imageView.getHeight() + 5 > this.getHeight() ){
	        
	        if ((this.getScrollY() >= 0) && (this.getScrollY() <= scrollHeight) &&
	                (scrollHeight > 0)) {
	            int moveY = (int) distanceY;
	
	            if (((moveY + this.getScrollY()) >= 0) &&
	                    ((Math.abs(moveY) + Math.abs(this.getScrollY())) <= scrollHeight)) {
	                this.scrollBy(0, moveY);
	            } else {
	                if (distanceY >= 0) {
	                    this.scrollBy(0,
	                        scrollHeight -
	                        Math.max(Math.abs(moveY), Math.abs(this.getScrollY())));
	                } else {
	                    this.scrollBy(0,
	                        -Math.min(Math.abs(moveY), Math.abs(this.getScrollY())));
	                }
	            }
	        }
        }

        this.xScrollPercent = (double) this.getScrollX() / (double) (this.getWidth());
        this.yScrollPercent = (double) this.getScrollY() / (double) (this.getHeight());

        return true;
    }
    
    public void scrollBy(int x, int y){
    	int scrollWidth = imageView.getWidth() - this.getWidth() + 5;
    	int scrollHeight = imageView.getHeight() - this.getHeight() + 5;
    	if(this.getScrollX() + x < 0 ){
    		x=0;
    	} else if( this.getScrollX() + x > scrollWidth ){
    		x = scrollWidth;
    	}
    	if(this.getScrollY() + y < 0) {
    		y=0;
    	} else if (this.getScrollY() + y > scrollHeight){
    		y = scrollHeight;
    	}
    	System.out.println("scrollBy("+x+","+y+")");
    	super.scrollTo(this.getScrollX() + x, this.getScrollY() +y);
    	if(this.getScrollX() < 0 ){
    		this.scrollTo(0, this.getScrollY());
    	} else if( this.getScrollX() > this.imageView.getWidth() - this.getWidth() + 10 ){
    		this.scrollTo(this.imageView.getWidth() - this.getWidth() + 10, this.getScrollY() );
    	}
    	if(this.getScrollY() < 0 ){
    		this.scrollTo(this.getScrollX(), 0);
    	} else if( this.getScrollY() > this.imageView.getHeight() - this.getHeight() + 10 ){
    		this.scrollTo(this.getScrollX(), this.imageView.getHeight() - this.getHeight() + 10);
    	}
    }

    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub 
    }
    
    private boolean longTouched;

    public boolean onSingleTapUp(MotionEvent e) {
        Point p = new Point();
        p.x = (int) (e.getX() + this.getScrollX());
        p.y = (int) (e.getY() + this.getScrollY());
        this.longTouchTimer.cancel();
        if(this.longTouched == true){
        	this.longTouched = false;
        } else {
        	if(this.ctxListener != null){
        		this.ctxListener.onTap(p);
        	}
        }
        
        return true;
    }

    public void zoomIn() {
        int h = imageView.getHeight();
        int w = imageView.getWidth();
        h *= 2;
        w *= 2;
        imageView.getLayoutParams().height = h;
        imageView.getLayoutParams().width = w;

        //                this.removeAllViews(); 
        //                this.addView(imageView, new LinearLayout.LayoutParams(w, h)); 
    }

    public void zoomOut() {
        this.scrollTo(0, 0);

        int h = imageView.getHeight();
        int w = imageView.getWidth();
        h /= 2;
        w /= 2;
        imageView.getLayoutParams().height = h;
        imageView.getLayoutParams().width = w;

        //                this.removeAllViews(); 
        //                this.addView(imageView, new LinearLayout.LayoutParams(w, h)); 
    }
    
    public void ensureVisible(Point p){
    	int maxScrollX = imageView.getWidth() - this.getWidth() + 5;
    	int x = p.x;
    	int maxScrollY = imageView.getHeight() - this.getHeight() +5 ;
    	int y = p.y;
    	
    	
    	int currentMinX = this.getScrollX();
    	int currentMaxX = this.getWidth() + this.getScrollX();
    	int currentMinY = this.getScrollY();
    	int currentMaxY = this.getHeight() + this.getScrollY();
    	
    	LOG.finest("X range "+currentMinX+" to "+currentMaxX);
    	LOG.finest("Desired X:"+x);
    	LOG.finest("Y range "+currentMinY+" to "+currentMaxY);
    	LOG.finest("Desired Y:"+y);
    	
    	
    	if( x < currentMinX || x > currentMaxX ){
    		this.scrollTo(x > maxScrollX ? maxScrollX : x + 5, this.getScrollY() );
    	}
    	if( y < currentMinY || y > currentMaxY ){
    		LOG.finest("Y adjust");
    		this.scrollTo(this.getScrollX(), y > maxScrollY ? maxScrollY : y + 5);
    	}
    }

    public static interface ClickListener {
        public void onContextMenu(Point e);

        public void onTap(Point e);
    }

    public static class Point {
        int x;
        int y;
        public Point(int x, int y){
        	this.x = x;
        	this.y = y;
        }
        public Point(){
        	
        }
    }
}
