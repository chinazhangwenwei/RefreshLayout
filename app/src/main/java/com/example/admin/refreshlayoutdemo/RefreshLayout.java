package com.example.admin.refreshlayoutdemo;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ScrollView;

/**
 * Created by admin on 2017/3/20.
 */

public class RefreshLayout extends FrameLayout {
    private MyHeadView mHeadView;
    private IHeadView iHeadView;

    private View contentView;

    private IRefreshListener mRefreshListener;

    private RefreshDistanceHolder refreshDistanceHolder;

    private int headMaxDistance = 500;


    //event
    private int pressX;
    private int pressY;
    private int tempPressY;
    private ScrollerCompat scrollerCompat;
    private int touchSlop;
    private boolean isSlop;
    private ScrollView scrollView;
    //
    private boolean isRefresh = false;

    private RefreshState refreshState = RefreshState.REFRESH_NORMAL;


    public RefreshLayout(Context context) {
        this(context, null);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParams(context);
    }

    private void initParams(Context context) {
        scrollerCompat = ScrollerCompat.create(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        isSlop = false;
        refreshDistanceHolder = new RefreshDistanceHolder();

        mHeadView = new MyHeadView(context);
        iHeadView = (IHeadView) mHeadView;
        addView(mHeadView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int measureHeight = 0;


        int childCount = getChildCount();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() == View.GONE) {
                continue;
            }
            MarginLayoutParams layoutParams = (MarginLayoutParams) childView.getLayoutParams();
            int childWidthMeasure = MeasureSpec.makeMeasureSpec(width - layoutParams.leftMargin -
                    layoutParams.rightMargin - paddingLeft - paddingRight, MeasureSpec.EXACTLY);

            int childHeightMeasure = getChildMeasureSpec(heightMeasureSpec,
                    layoutParams.topMargin + layoutParams.bottomMargin + paddingTop + paddingBottom,
                    layoutParams.height);
            childView.measure(childWidthMeasure, childHeightMeasure);
            if (i == 0) {
                Log.d(TAG, "onMeasure第一个: " + childView.getMeasuredHeight());
            }
            measureHeight += childView.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
        }
        setMeasuredDimension(width, measureHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        super.onLayout(changed, left, top, right, bottom);
        int childCount = getChildCount();
        int topY = getPaddingTop();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            MarginLayoutParams params = (MarginLayoutParams) childView.getLayoutParams();
            if (i == 0) {
                childView.layout(getPaddingLeft() + params.leftMargin, topY - (params.topMargin + childView.getMeasuredHeight()), childView.getMeasuredWidth(), childView.getMeasuredHeight());
//                headViewHeight = childView.getMeasuredHeight();
//                headView = childView;
                continue;
            } else if (i == 1) {
//                recyclerView = (RecyclerView) childView;
                contentView = childView;
                childView.layout(getPaddingLeft() + params.leftMargin, topY + params.topMargin, childView.getMeasuredWidth(), childView.getMeasuredHeight());
            }

//            else if (i == 2) {
//                childView.layout(getPaddingLeft() + params.leftMargin, topY + params.topMargin, childView.getMeasuredWidth(), childView.getMeasuredHeight());
//            }
            topY += params.topMargin + childView.getMeasuredHeight() + params.bottomMargin;

        }

    }

    private MotionEvent motionEvent;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "dispatchTouchEvent: ACTION_DOWN");
                isCancelTouch = true;
                pressX = (int) ev.getX();
                pressY = (int) ev.getY();
                tempPressY = pressY;
                motionEvent = ev;
//                super.dispatchTouchEvent(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isEnabled()) {
                    return super.dispatchTouchEvent(ev);
                }
                Log.d(TAG, "dispatchTouchEvent: ACTION_MOVE");
                int currentX = (int) ev.getX();
                int currentY = (int) ev.getY();
                int distanceY = currentY - pressY;
                pressY = currentY;
                pressX = currentX;
                if (!isSlop) {
                    if (Math.abs(currentY - tempPressY) > touchSlop) {
                        isSlop = true;
                    } else {
                        return super.dispatchTouchEvent(ev);
                    }
                }
//                if (isRefresh) {
//                    sendCancelTouchEvent();
//                    return true;
//                }

                if ((distanceY > 0 && refreshDistanceHolder.mOffsetY <= headMaxDistance) || distanceY < 0) {
                    distanceY = (int) (distanceY / 1.5f);

                    Log.d(TAG, "dispatchTouchEvent: ACTION_MOVE" + distanceY);
                } else {
                    return super.dispatchTouchEvent(ev);
                }
                //
                if ((!canChildScrollUp() && distanceY > 0) || (distanceY < 0 && refreshDistanceHolder.hasHeaderPullDown())) {

                    if (Math.abs(distanceY) > 100) {

                    } else {
                        sendCancelTouchEvent();
                        moveDistanceY(distanceY);
                    }
                } else {
                    return super.dispatchTouchEvent(ev);
                }
                if (Math.abs(distanceY) > 0)
                    Log.d(TAG, "sendDownEvent___________________________");
                sendDownEvent();

                break;
            case MotionEvent.ACTION_CANCEL:

            case MotionEvent.ACTION_UP:
                isSlop = false;
                if (!isRefresh && refreshDistanceHolder.mOffsetY >= iHeadView.getHeadViewHeight()) {
                    isRefresh = true;
                    setRefreshState();
                } else {

                }

                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean isSendDown = true;

    private void sendDownEvent() {
//        if (!mHasSendDownEvent) {
//            LogUtils.d("sendDownEvent");
//            mHasSendCancelEvent = false;
//            mHasSendDownEvent = true;
        if (isSendDown) {
            isSlop = false;
            final MotionEvent last = motionEvent;
            if (last == null)
                return;

            MotionEvent e = MotionEvent.obtain(last.getDownTime(),
                    last.getEventTime(), MotionEvent.ACTION_DOWN, last.getX(),
                    last.getY(), last.getMetaState());
            super.dispatchTouchEvent(e);
        }
//        }
    }

    @Override
    public void computeScroll() {
        if (scrollerCompat.computeScrollOffset()) {
            int lastY = refreshDistanceHolder.mOffsetY;
            int dy = lastY - scrollerCompat.getCurrY();
            moveDistanceY(dy);
        }
//        super.computeScroll();
    }

    private void setRefreshState() {
        if (refreshDistanceHolder.mOffsetY >= iHeadView.getHeadViewHeight()) {
            scrollerCompat.startScroll(0, refreshDistanceHolder.mOffsetY, 0, iHeadView.getHeadViewHeight());
            iHeadView.refreshing();
        } else {
            scrollerCompat.startScroll(0, refreshDistanceHolder.mOffsetY, 0, 0);
        }
//        scrollerCompat.startScroll(0,);
//        mHeadView.offsetTopAndBottom(iHeadView.getHeadViewHeight() - refreshDistanceHolder.mOffsetY);
//        contentView.offsetTopAndBottom(iHeadView.getHeadViewHeight() - refreshDistanceHolder.mOffsetY);
//        refreshDistanceHolder.mOffsetY = iHeadView.getHeadViewHeight();
//        iHeadView.refreshing();
    }

    private boolean isCancelTouch = false;

    private void sendCancelTouchEvent() {
        if (motionEvent != null) {
            if (isCancelTouch) {
                isCancelTouch = false;
                MotionEvent e = MotionEvent.obtain(
                        motionEvent.getDownTime(),
                        motionEvent.getEventTime()
                                + ViewConfiguration.getLongPressTimeout(),
                        MotionEvent.ACTION_CANCEL, motionEvent.getX(), motionEvent.getY(),
                        motionEvent.getMetaState());
//            motionEvent.setAction(MotionEvent.ACTION_CANCEL);
                super.dispatchTouchEvent(e);
            }
        }
    }

    private static final String TAG = "RefreshLayout";

    private void moveDistanceY(int distanceY) {
        Log.d(TAG, "moveDistanceY: " + distanceY);
        if (Math.abs(distanceY) > 100) {
            Log.d(TAG, "moveDistanceY: 移动距离过大" + distanceY);
        } else {
            refreshDistanceHolder.move(distanceY);
            mHeadView.offsetTopAndBottom(distanceY);
            contentView.offsetTopAndBottom(distanceY);
            ViewCompat.postInvalidateOnAnimation(this);
        }


    }

    public boolean canChildScrollUp() {

        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (contentView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) contentView;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(contentView, -1) || contentView.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(contentView, -1);
        }
    }

    public void setmRefreshListener(IRefreshListener iRefreshListener) {
        mRefreshListener = iRefreshListener;
    }


    public static enum RefreshState {
        REFRESHING, REFRESH_NORMAL

    }
}
