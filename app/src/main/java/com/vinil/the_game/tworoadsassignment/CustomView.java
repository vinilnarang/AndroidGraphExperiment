package com.vinil.the_game.tworoadsassignment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by the_game on 6/12/15.
 */
public class CustomView extends View {
    public Paint mPaint;
    private float x,y,radius;

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.colorAccent));
        x=50;y=50;radius=30;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        x=canvas.getWidth()/2;
        y=canvas.getHeight()/2;
        canvas.drawCircle(x,y,radius,mPaint);
    }

    public void reDraw(int rad){
        radius=rad;
        invalidate();
    }
}
