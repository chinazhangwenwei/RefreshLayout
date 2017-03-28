package pullzom;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.example.admin.refreshlayoutdemo.R;


/**
 * Created by admin on 2017/3/28.
 */

public class MyPullToZoomListView extends ListView implements AbsListView.OnScrollListener {
    private AbsListView.OnScrollListener scrollListener;
    private int scaleWidth = 16;
    private int scaleHeight = 9;
    private float ration = 1.0f;
    private Context mContext;

    //listView的默认headView，支持滚动视差和刷新逻辑
    private FrameLayout headViewContainer;
    private int headViewWidth;
    private int headViewHeight;
    private float scale = 1.0f;

    private float lastPressX;
    private float lastPressY;
    private float tempInitY;
    private int touchSlop;


    public MyPullToZoomListView(Context context) {
        this(context, null);
    }

    public MyPullToZoomListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyPullToZoomListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private static final String TAG = "MyPullToZoomListView";

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyPullToZoomListView);
        scaleWidth = typedArray.getInt(R.styleable.MyPullToZoomListView_ration_head_width, scaleWidth);
        scaleHeight = typedArray.getInt(R.styleable.MyPullToZoomListView_ration_head_height, scaleHeight);
        Log.d(TAG, "init: " + scaleHeight);
        Log.d(TAG, "init: " + scaleWidth);
        ration = scaleHeight * 1.0f / scaleWidth;
        typedArray.recycle();
        headViewContainer = new FrameLayout(context);
        addHeaderView(headViewContainer);
        setHeadViewLayout();
        setOnScrollListener(this);

    }

    public void setRation(int rationWidth, int rationHeight) {
        scaleWidth = rationWidth;
        scaleHeight = rationHeight;
        ration = scaleHeight * 1.0f / scaleWidth;
        setHeadViewLayout();
    }

    private void setHeadViewLayout() {
        AbsListView.LayoutParams params = (AbsListView.LayoutParams) headViewContainer.getLayoutParams();
        if (params == null) {
            params = new AbsListView.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
        headViewWidth = params.width = localDisplayMetrics.widthPixels;
        headViewHeight = params.height = (int) (params.width * ration);
        Log.d(TAG, "setHeadViewLayout: " + params.width);
        Log.d(TAG, "setHeadViewLayout: " + params.height);
        headViewContainer.setLayoutParams(params);
    }


    public void addOnScrollListener(AbsListView.OnScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollListener != null) {
            scrollListener.onScrollStateChanged(view, scrollState);
        }
    }


    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (scrollListener != null) {
            scrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
        Log.d(TAG, "onScroll: ___________________");
        int distanceY = headViewHeight - headViewContainer.getBottom();
        if (distanceY > 0 && (distanceY + 8) < headViewHeight) {
            int realY = (int) (distanceY * 0.65);
            headViewContainer.scrollTo(0, -realY);
        } else if (headViewContainer.getScrollY() != 0) {
            headViewContainer.scrollTo(0, 0);
        }


    }

    public void addHeadContentView(View headContent) {
        if (headViewContainer != null) {
            headViewContainer.addView(headContent);
        } else {
            throw new NullPointerException("headContainer为空，请先保证headViewContainer存在");
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
//        int action = ev.getActionMasked();
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                if (animator != null) {
//                    animator.isRunning();
//                    animator.end();
//                }
//                lastPressX = ev.getX();
//                lastPressY = ev.getY();
//                tempInitY = lastPressY;
//                break;
//            case MotionEvent.ACTION_MOVE:
//                final float currentY = ev.getY();
//                if (Math.abs(currentY - tempInitY) > touchSlop) {
//                    if (headViewContainer.getBottom() >= headViewHeight) {
//                        scale = headViewContainer.getBottom() + (currentY - lastPressY) / headViewHeight;
//                        ViewGroup.LayoutParams localLayoutParams = headViewContainer
//                                .getLayoutParams();
//                        localLayoutParams.width = (int) (headViewWidth * scale);
//                        localLayoutParams.height = (int) (headViewHeight * scale);
//                        headViewContainer.setLayoutParams(localLayoutParams);
//                    }
//
//                }
//                lastPressY = currentY;
//
//            break;
//            case MotionEvent.ACTION_POINTER_DOWN:
//                break;
//            case MotionEvent.ACTION_POINTER_UP:
//                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                endScale();
//                break;
//        }
        return super.onTouchEvent(ev);
    }

    private ValueAnimator animator;

    private void endScale() {
        if (animator == null) {
            animator = ValueAnimator.ofFloat(scale, 1.0f);
            animator.setDuration(200l);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (Float) animation.getAnimatedValue();
                    ViewGroup.LayoutParams localLayoutParams = headViewContainer
                            .getLayoutParams();
                    localLayoutParams.width = (int) (headViewWidth * value);
                    localLayoutParams.height = (int) (headViewHeight * value);
                    headViewContainer.setLayoutParams(localLayoutParams);

                }
            });

        }
        animator.start();
    }
}
