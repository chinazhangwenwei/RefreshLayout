package com.example.admin.refreshlayoutdemo;

/**
 * Created by admin on 2017/3/20.
 */

public class RefreshDistanceHolder {
    public int mOffsetY;

    public void move(int deltaY) {
        mOffsetY += deltaY;
    }

    public boolean hasHeaderPullDown() {
        return mOffsetY > 0;
    }


    public boolean isOverHeader(int deltaY) {
        return mOffsetY < -deltaY;

    }
}
