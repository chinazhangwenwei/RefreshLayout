package com.example.admin.refreshlayoutdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;


/**
 * Created by admin on 2017/3/20.
 */

public class MyHeadView extends FrameLayout implements IHeadView {
    private static final String TAG = "MyHeadView";
    private View content;
    private ProgressBar progressBar;

    public MyHeadView(Context context) {
        this(context, null);
    }

    public MyHeadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyHeadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParams(context);
    }

    private void initParams(Context context) {
        content = View.inflate(context, R.layout.head_layout, null);
    }

    @Override
    public void refreshReady() {
        Log.d(TAG, "refreshReady: ");
    }

    @Override
    public void refreshing() {
        Log.d(TAG, "refreshing: ");
    }

    @Override
    public void refreshOver() {
        Log.d(TAG, "refreshOver: ");
    }

    @Override
    public int getHeadViewHeight() {
        return getMeasuredHeight();
    }
}
