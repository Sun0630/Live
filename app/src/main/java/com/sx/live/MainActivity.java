package com.sx.live;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import com.sx.live.fragment.LivePlayFragment;
import com.sx.live.fragment.LivePushFragment;
import com.sx.live.view.SegmentView;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements SegmentView.OnSegmentChangeListener {

    private ArrayList<Fragment> mFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        //初始化Fragment
        initFragments();
        SegmentView sv = (SegmentView) findViewById(R.id.sv);
        sv.setOnSegmentChangeListener(this);
        sv.setSegmentSelected(0);
    }

    private void initFragments() {
        mFragments = new ArrayList<>();
        mFragments.add(new LivePushFragment());

        LivePlayFragment playFragment = new LivePlayFragment();
        playFragment.setLive(false);//点播
        mFragments.add(playFragment);

        playFragment = new LivePlayFragment();
        playFragment.setLive(true);//直播
        mFragments.add(playFragment);

    }

    @Override
    public void onSegmentChange(int selectedIndex) {
        changeFragment(selectedIndex);
    }

    private void changeFragment(int index) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_layout,mFragments.get(index))
                .commit();

    }
}
