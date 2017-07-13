package com.sx.live.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @Author sunxin
 * @Date 2017/7/13 14:50
 * @Description 播放端Fragment，即支持点播也支持直播，一页两用
 */

public class LivePlayFragment extends Fragment {

    private boolean isLive;//是否是直播

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isLive) {
            view.setBackgroundColor(Color.RED);
        } else {
            view.setBackgroundColor(Color.YELLOW);
        }
    }

    public void setLive(boolean live) {
        isLive = live;
    }
}
