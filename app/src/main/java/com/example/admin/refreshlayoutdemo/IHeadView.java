package com.example.admin.refreshlayoutdemo;

/**
 * Created by admin on 2017/3/20.
 */

public interface IHeadView {

    void refreshReady();

    void refreshing();

    void refreshOver();

    int getHeadViewHeight();
}
