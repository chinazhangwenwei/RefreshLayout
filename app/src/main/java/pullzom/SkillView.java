package pullzom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by admin on 2017/3/30.
 */

public class SkillView extends View {
    private int skillNumber = 5;
    private int baseColor = Color.BLUE;
    private int backColor = Color.CYAN;
    private Paint mPaint;
    private float defalutSize = 200;

    public SkillView(Context context) {
        this(context, null);
    }

    public SkillView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SkillView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParams(context, attrs);
    }

    private void initParams(Context context, AttributeSet attributeSet) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setColor(backColor);
        defalutSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80,
                getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int widthMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                width = (int) Math.min(width, defalutSize);
                break;
        }
        return width;
    }

    private int measureHeight(int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                height = (int) defalutSize;
                break;
        }
        return height;
    }

    private static final String TAG = "SkillView";

    //支持相同padding ，不相同padding 支持没有意义
    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        int widthCenter = getMeasuredWidth() / 2;
        int heightCenter = getMeasuredHeight() / 2;
        int radius;
        if (widthCenter < heightCenter) {
            radius = widthCenter - getPaddingLeft();
        } else {
            radius = heightCenter - getPaddingTop();
        }
        canvas.translate(widthCenter, heightCenter);

        //计算数据
        Point array[] = new Point[skillNumber];
        for (int i = 1; i <= skillNumber; i++) {
            double sinContent = Math.sin(Math.PI * 2 * i / skillNumber + Math.PI / 2);
            double conContent = Math.cos(Math.PI * 2 * i / skillNumber + Math.PI / 2);
            Point tempPoint = new Point();
            tempPoint.x = (float) (radius * conContent);
            tempPoint.y = (float) (radius * sinContent);
            array[i - 1] = tempPoint;
        }
        int scanBackNum = 4;
        Path path = new Path();
//        canvas.rotate(180f);
        //画背景
        for (int j = scanBackNum; j >= 1; j--) {
            path.reset();
            path.moveTo(0, radius * j / scanBackNum);
            for (int i = 0; i < skillNumber; i++) {
                path.lineTo(array[i].x * j / scanBackNum, array[i].y * j / scanBackNum);
            }
            mPaint.setAlpha(255 - (j * 220 / scanBackNum));
            if (j == 1) {
                mPaint.setAlpha(255);
            }
            path.close();
            canvas.drawPath(path, mPaint);
        }

        mPaint.setColor(baseColor);
        canvas.drawLine(0, radius, 0, 0, mPaint);
        //画线
        for (int i = 0; i < skillNumber; i++) {
            canvas.drawLine(array[i].x, array[i].y, 0, 0, mPaint);
        }
    }

    class Point {
        public float x;
        public float y;
    }
}
