package com.guna.libcolorpicker;

import android.app.Dialog;
import android.app.assist.AssistStructure;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Administrator on 05/08/2016.
 */
public class MyColorPicker extends Dialog {
    private OnColorChangedListener mListener;
    ArrayList<Paint> listPaint = new ArrayList<Paint>();
    String mKey;
    public MyColorPicker(Context context, OnColorChangedListener listener, String key){
        super(context);
        mListener = listener;
        mKey = key;
    }

    private class MyView extends View{
        private float mCENTER_X = 320;
        private float mCENTER_Y = 350;
        private float radius = 280;
        private int stepDegree;
        private OnColorChangedListener mListener;
        private String mKey;
        int[] mColorArr = {Color.RED,Color.GREEN ,Color.BLUE , Color.YELLOW ,Color.CYAN, Color.MAGENTA};
        public MyView(Context context, OnColorChangedListener listener, String key) {
            super(context);
            mListener = listener;
            mKey = key;
        }
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            //float offset = 50;

            float left = mCENTER_X - radius;
            float top = mCENTER_Y - radius;
            float right = mCENTER_X + radius;
            float bottom = mCENTER_Y + radius;
            //float right = 500 - offset;
            // float bottom = 500 - 2* offset;
            RectF recF = new RectF(left,top,right, bottom);

            stepDegree = 360 / mColorArr.length;
            float startAngle = 0;
            float sweepAngle = stepDegree;
            for(int i = 0; i< mColorArr.length; i++){
                Paint paint = new Paint();
                listPaint.add(paint);
                listPaint.get(i).setColor(mColorArr[i]);
                canvas.drawArc(recF,startAngle,sweepAngle,true, listPaint.get(i));

                startAngle += stepDegree;
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            setMeasuredDimension((int)mCENTER_X * 2 ,(int)mCENTER_Y * 2 );
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            double X = event.getX();
            double Y = event.getY();
            double degree;
            String[] colorStr = {"RED","GREEN" ,"BLUE" , "YELLOW" ,"CYAN", "MAGENTA"};

            degree = Math.toDegrees(Math.atan2(Y-mCENTER_Y,X-mCENTER_X));
            if(degree < 0)
                degree += 360;
            Toast.makeText(getContext(),colorStr[(int)degree / stepDegree], Toast.LENGTH_SHORT).show();
            mListener.colorChanged(mKey,mColorArr[(int)degree/stepDegree]);
            return super.onTouchEvent(event);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("CHOOSE COLOR");
        OnColorChangedListener l = new OnColorChangedListener() {
            @Override
            public void colorChanged(String key, int color) {
                mListener.colorChanged(key,color);
                dismiss();
            }
        };
        setContentView(new MyView(getContext(),l,mKey));
    }
}
