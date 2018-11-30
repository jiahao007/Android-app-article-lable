package com.example.liuji.hw;

import android.widget.TextView;

public class ZoomTextView extends ZoomView<TextView> {
    public static final float MIN_TEXT_SIZE = 20.0f;//the min size of word
    public static final float MAX_TEXT_SIZE = 40.0f;//the max size of word

    float scale;
    float textSize;

    public ZoomTextView(TextView view, float scale) {
        super(view);
        this.scale = scale;
        textSize = view.getTextSize();
    }

    //extend word size
    protected void zoomOut()
    {
        textSize += scale;
        if(textSize > MAX_TEXT_SIZE)
            textSize = MAX_TEXT_SIZE;
        view.setTextSize(textSize);
    }
    //shrink word size
    protected void zoomIn()
    {
        textSize -= scale;
        if(textSize < MIN_TEXT_SIZE)
            textSize = MIN_TEXT_SIZE;
        view.setTextSize(textSize);
    }
}
