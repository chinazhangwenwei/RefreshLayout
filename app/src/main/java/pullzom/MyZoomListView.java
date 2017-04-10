package pullzom;


import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.example.admin.refreshlayoutdemo.R;


/**
 * Created by admin on 2017/3/28.
 */

public class MyZoomListView extends ListView implements AbsListView.OnScrollListener {
    private AbsListView.OnScrollListener scrollListener;
    private int scaleWidth = 16;
    private int scaleHeight = 9;
    private float ration = 1.0f;
    private float maxScale = 2.0f;
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

    private boolean isRefreshing = false;
    private boolean isLoading = false;


//    private int offHeight;

    private ZoomRationScrollListener headRationScroll;
    private ZoomRefreshListener refreshListener;
    private ZoomLoadMoreListener loadMoreListener;


    public MyZoomListView(Context context) {
        this(context, null);
    }

    public MyZoomListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyZoomListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private static final String TAG = "MyPullToZoomListView";

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
//        offHeight = touchSlop;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyZoomListView);
        scaleWidth = typedArray.getInt(R.styleable.MyZoomListView_ration_head_width, scaleWidth);
        scaleHeight = typedArray.getInt(R.styleable.MyZoomListView_ration_head_height, scaleHeight);
        maxScale = typedArray.getFloat(R.styleable.MyZoomListView_ration_max_scale, maxScale);
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

    public void addZoomScrollListener(ZoomRationScrollListener headRationScroll) {
        this.headRationScroll = headRationScroll;
    }

    public void addZoomRefreshListener(ZoomRefreshListener refreshListener) {
        this.refreshListener = refreshListener;
    }

    public void addLoadMoreListener(ZoomLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    public void setRefreshOk() {
        isLoading = false;
        isRefreshing = false;
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

        int distanceY = headViewHeight - headViewContainer.getBottom();
        if (distanceY > 0 && (distanceY < headViewHeight)) {
            int realY = (int) (distanceY * 0.65);
            headViewContainer.scrollTo(0, -realY);
            if (headRationScroll != null) {
                headRationScroll.zoomHeadRation(headViewContainer.getBottom() * 1.0f / headViewHeight);
            }
        } else if (headViewContainer.getScrollY() != 0) {
            headViewContainer.scrollTo(0, 0);
        }

        Log.d(TAG, "onScroll: firstVisibleItem" + firstVisibleItem);
        Log.d(TAG, "onScroll: visibleItemCount" + visibleItemCount);
        Log.d(TAG, "onScroll: totalItemCount" + totalItemCount);
        if (firstVisibleItem + visibleItemCount == totalItemCount) {
            if (loadMoreListener != null) {
                if (!isRefreshing && !isLoading) {
                    loadMoreListener.zoomLoadMore();
                    isLoading = true;
                }
            }
        }


    }

    public void addHeadContentView(View headContent) {
        if (headViewContainer != null) {
            headViewContainer.addView(headContent);
        } else {
            throw new NullPointerException("headContainer为空，请先保证headViewContainer存在");
        }
    }

    private int mActionId;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex = -1;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onTouchEvent: ACTION_DOWN");
                mActionId = ev.getPointerId(0);
                if (animator != null) {
                    animator.isRunning();
                    animator.end();
                }
                lastPressX = ev.getX();
                lastPressY = ev.getY();
                tempInitY = lastPressY;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onTouchEvent: ACTION_MOVE");
                pointerIndex = ev.findPointerIndex(mActionId);
                if (pointerIndex == -1) {
                    return false;
                }
                final float currentY = ev.getY(pointerIndex);
                Log.d(TAG, "onTouchEvent: ACTION_MOVE" + currentY);
                Log.d(TAG, "onTouchEvent: ACTION_MOVE" + headViewContainer.getBottom() + "_______" + headViewHeight + "__________");
                if (Math.abs(currentY - tempInitY) > touchSlop) {
                    if (headViewContainer.getBottom() >= headViewHeight) {
                        scale = (headViewContainer.getBottom() + (currentY - lastPressY)) * 1.0f / headViewHeight;
                        ViewGroup.LayoutParams localLayoutParams = headViewContainer
                                .getLayoutParams();
//                        localLayoutParams.width = (int) (headViewWidth * scale);
                        Log.d(TAG, "onTouchEvent: " + scale);
                        if (scale < 1.0f) {
                            scale = 1.0f;
                            return super.onTouchEvent(ev);
                        }
                        scale = Math.min(scale, maxScale);
                        localLayoutParams.height = (int) (headViewHeight * scale);
                        headViewContainer.setLayoutParams(localLayoutParams);
                        lastPressY = currentY;
                        return true;
                    }
                }
                lastPressY = currentY;

                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d(TAG, "onTouchEvent: ACTION_POINTER_DOWN");
                final int index = ev.getActionIndex();
                if (index == -1) {
                    return false;
                }
                lastPressY = (int) ev.getY(index);
                tempInitY = lastPressY;
                mActionId = ev.getPointerId(index);
                if (mActionId < 0) {
                    return false;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.d(TAG, "onTouchEvent: ACTION_POINTER_UP");
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "onTouchEvent: ACTION_CANCEL");
                mActionId = -1;
                if (scale > 1.0f) {
                    endScale();
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActionId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActionId = ev.getPointerId(newPointerIndex);
            lastPressY = (int) ev.getY(newPointerIndex);
            tempInitY = (int) ev.getY(newPointerIndex);
        }
    }


    private ValueAnimator animator;

    private void endScale() {

        Log.d(TAG, "animator" + scale);
        float tempScale = scale;
        if (tempScale > 1.8) {
            if (refreshListener != null) {
                if (!isRefreshing && !isLoading) {
                    isRefreshing = true;
                    refreshListener.zoomRefreshListener();
                }
            }
        }
        scale = 1.0f;
        animator = ValueAnimator.ofFloat(tempScale, 1.0f);
        animator.setDuration(300l);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                ViewGroup.LayoutParams localLayoutParams = headViewContainer
                        .getLayoutParams();
                localLayoutParams.height = (int) (headViewHeight * value);
                headViewContainer.setLayoutParams(localLayoutParams);

            }
        });

        animator.start();
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) {
            if (animator.isRunning()) {
                animator.end();
                animator = null;
            }
        }
        super.onDetachedFromWindow();
    }

    interface ZoomRationScrollListener {
        /**
         * 头部滚动的百分比回掉
         *
         * @param ration
         */
        void zoomHeadRation(float ration);
    }

    interface ZoomRefreshListener {
        /**
         * 刷新回掉
         */
        void zoomRefreshListener();
    }

    interface ZoomLoadMoreListener {
        /**
         * 加载更多的回掉
         */
        void zoomLoadMore();
    }

}
