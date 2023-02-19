package com.example.dragtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * @author WangYh
 * @version V1.0
 * @Name: SortView
 * @Package com.example.dragtest
 * @Description: 描述信息
 * @date 2023/2/18 0018
 */
public class SortView extends View {

    private static final String TAG = "SortView";

    int downIndex = 0;
    int allCount = 15;
    Bitmap mBitmap;
    Paint bitmapPaint;
    Paint linePaint;
    RectF bmpRect;
    int[] windowLocation = new int[2];
    float lastX;

    int PADDING = 40;
    float canDragMinX;
    float canDragMaxX;
    float bmpStartX;
    float bmpEndX;

    int screenWidth;
    ArrayList<RectF> allPositions = new ArrayList<>();
    ArrayList<Integer> allIndex = new ArrayList<>();
    int viewHeight;
    boolean notDraw = true;

    boolean isFastMove;
    final int TYPE_LEFT = 0;
    final int TYPE_RIGHT = 1;
    final int TIME_INTIVAL = 16;

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(!isFastMove){
                return;
            }
            switch (msg.what){
                case TYPE_LEFT:
                    fastMoveAndSort(true);
                    mHandler.sendEmptyMessageDelayed(TYPE_LEFT,TIME_INTIVAL);
                    break;
                case TYPE_RIGHT:
                    fastMoveAndSort(false);
                    mHandler.sendEmptyMessageDelayed(TYPE_RIGHT,TIME_INTIVAL);
                    break;

            }
        }
    };

    public SortView(Context context) {
        this(context,null);
    }

    public SortView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SortView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);

    }

    private void init(Context context) {
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        mBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_launcher);
        bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(4);
        bmpRect = new RectF();
        for (int i = 0; i < allCount; i++) {
            allPositions.add(new RectF());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec)*3,MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(0x33008822);
        if(notDraw){
            return;
        }
        for (int i = 0; i < allPositions.size(); i++) {
            bmpRect = allPositions.get(i);
            if(i == downIndex){
                canvas.drawRect(bmpRect,linePaint);
                continue;
            }
            canvas.drawBitmap(mBitmap,null,bmpRect,bitmapPaint);
        }
        bmpRect = allPositions.get(downIndex);
        canvas.drawBitmap(mBitmap,null,bmpRect,bitmapPaint);
        // 画边界线
        linePaint.setColor(Color.RED);
        canvas.drawLine(canDragMinX,0, canDragMinX,viewHeight,linePaint);
        canvas.drawLine(canDragMaxX,0, canDragMaxX,viewHeight,linePaint);
        linePaint.setColor(Color.GREEN);
        canvas.drawLine(bmpStartX,0, bmpStartX,viewHeight,linePaint);
        canvas.drawLine(bmpEndX,0, bmpEndX,viewHeight,linePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int actionMasked = event.getActionMasked();
        switch (actionMasked){
            case MotionEvent.ACTION_DOWN:
                notDraw = false;
                Random random = new Random();
                downIndex = random.nextInt(allCount);
                Log.e(TAG,"downIndex = "+downIndex);
                getLocationOnScreen(windowLocation);
                canDragMinX = -windowLocation[0] + PADDING;
                canDragMaxX = -windowLocation[0] + screenWidth - PADDING;
                lastX = event.getX();
                float start = lastX - (viewHeight >> 1) - downIndex * viewHeight;
                bmpStartX = start;
                allIndex.clear();
                RectF rectF;
                for (int i = 0; i < allPositions.size(); i++) {
                    allIndex.add(i);
                    rectF = allPositions.get(i);
                    rectF.top = 0;
                    rectF.bottom = viewHeight;
                    rectF.left = start;
                    start+=viewHeight;
                    rectF.right = start;
                }
                bmpEndX = start;
                getParent().requestDisallowInterceptTouchEvent(true);
                Log.d(TAG,"ACTION_DOWN : windowLocation[0] = "+windowLocation[0]+" , lastX = "+lastX
                +" , dragMinX = "+ canDragMinX +" , dragMaxX = "+ canDragMaxX
                );
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                float curX = event.getX();
                boolean fastLeft = curX + windowLocation[0] < 100;
                boolean fastRight = curX + windowLocation[0] > screenWidth - 100;
                Log.d(TAG,"fastLeft = "+fastLeft+" , fastRight = "+fastRight);
                if(fastLeft || fastRight){
                    if(!isFastMove){
                        isFastMove = true;
                        mHandler.sendEmptyMessageDelayed(fastLeft?TYPE_LEFT:TYPE_RIGHT,TIME_INTIVAL);
                    }
                }else{
                    if(isFastMove){
                        mHandler.removeCallbacksAndMessages(null);
                        isFastMove = false;
                    }
                    moveAndSort(curX);
                }
                break;
            case MotionEvent.ACTION_UP:
                notDraw = true;
                isFastMove = false;
                mHandler.removeCallbacksAndMessages(null);
                Log.e(TAG,"result = "+Arrays.toString(allIndex.toArray()));
                invalidate();
                break;
        }
        return true;
    }

    private void fastMoveAndSort(boolean left) {
        int dx = left?-8:8;
        moveAndSort(lastX+dx);
    }

    private void moveAndSort(float curX) {
        float dx = curX - lastX;
        RectF rectF = allPositions.get(downIndex);
        rectF.left += dx;
        rectF.right += dx;
        if(rectF.left < canDragMinX){
            rectF.left = canDragMinX;
            rectF.right = rectF.left + viewHeight;
        }
        if(rectF.right > canDragMaxX){
            rectF.right = canDragMaxX;
            rectF.left = rectF.right - viewHeight;
        }

        lastX = Math.min(Math.max(curX, canDragMinX +(viewHeight>>1)), canDragMaxX -(viewHeight>>1));
        int index = allIndex.indexOf(downIndex);
        Log.d(TAG,"moveAndSort downIndex ' index is "+index);
        boolean toLeft = dx < 0;
        if(isFastMove){
            offsetPositions(dx);
        }
        int nextIndex = toLeft?index-1:index+1;
        if(nextIndex < 0 || nextIndex >= allIndex.size()){
            invalidate();
            return;
        }
        RectF nextRect = allPositions.get(allIndex.get(nextIndex));
        float center = (nextRect.left + nextRect.right) / 2;
        if(toLeft && rectF.left < center){
            nextRect.left += viewHeight;
            nextRect.right += viewHeight;
            Collections.swap(allIndex,index,nextIndex);
            Log.w(TAG,"moveAndSort : swap to left "+index+" -> "+nextIndex+" , newIndex = "+ Arrays.toString(allIndex.toArray()));
        }

        if(!toLeft && rectF.right > center){
            nextRect.left -= viewHeight;
            nextRect.right -= viewHeight;
            Collections.swap(allIndex,index,nextIndex);
            Log.w(TAG,"moveAndSort : swap to right "+index+" -> "+nextIndex+" , newIndex = "+ Arrays.toString(allIndex.toArray()));
        }
        invalidate();
    }

    private void offsetPositions(float dx) {
        if(allIndex.isEmpty()){
            return;
        }
        int realPosition;
        RectF rectF;
        int firstIndex = allIndex.get(0);
        int endIndex = allIndex.get(allIndex.size()-1);
        boolean atFirst = firstIndex == downIndex;
        boolean atEnd = endIndex == downIndex;
        if(atFirst || atEnd){
            int nextIdx = atFirst?allIndex.get(1):allIndex.get(allIndex.size()-2);
            RectF nextR = allPositions.get(nextIdx);
            RectF downR = allPositions.get(downIndex);
            if((atFirst && downR.right <= nextR.left) || (atEnd && downR.left >= nextR.right)){
                Log.d(TAG,"offsetPositions left or right return!");
                return;
            }
        }
        for (int i = 0; i < allIndex.size(); i++) {
            realPosition = allIndex.get(i);
            if(realPosition == downIndex){
                continue;
            }
            rectF = allPositions.get(realPosition);
            rectF.left -= dx;
            rectF.right -= dx;
        }
    }
}
