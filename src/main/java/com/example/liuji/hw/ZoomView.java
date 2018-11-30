package com.example.liuji.hw;

import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public abstract class ZoomView<V extends View>
{
    protected V view;
    private static final int NONE = 0;//there is no point is touched
    private static final int DRAG = 1;//there is one point is touched
    private static final int ZOOM = 2;//two points are touched
    
    private int mode = NONE;
    float oldDist; //the distance between two points
    
    public ZoomView(V view)
    {
        this.view = view;
        setTouchListener();
    }

    private void setTouchListener()
    {
        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                //judge the gesture and do according operation
                switch (event.getAction() & MotionEvent.ACTION_MASK)
                {
                    case MotionEvent.ACTION_DOWN:
                        mode = DRAG;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        oldDist = spacing(event);
                        if(oldDist > 10f)
                        {
                            mode = ZOOM;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE://the distance between the original point and the moving point
                        if(mode == ZOOM)
                        {
                            float newDist = spacing(event);
                            if(newDist > oldDist)
                            {
                                zoomOut();
                                return true;
                            }
                            if(newDist < oldDist)
                            {
                                zoomIn();
                                return true;
                            }
                        }
                        break;
                }
                return  false;
            }
            /**
             * calculate the distance between the two point
             * @param event
             * @return
             */
            private float spacing(MotionEvent event)
            {
                float x = event.getX(0) - event.getX(1);
                float y = event.getY(0) - event.getY(1);
                return  (float)Math.sqrt(x * x + y * y);
            }
        });
    }
    protected abstract void zoomOut();
    protected abstract void zoomIn();
}
