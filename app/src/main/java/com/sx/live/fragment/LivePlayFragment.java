package com.sx.live.fragment;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.sx.live.R;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

/**
 * @Author sunxin
 * @Date 2017/7/13 14:50
 * @Description 播放端Fragment，即支持点播也支持直播，一页两用
 */

public class LivePlayFragment extends Fragment implements ITXLivePlayListener {

    private boolean isLive;//是否是直播
    private TXCloudVideoView mPlayerView;
    private ImageView mLoadingView;
    private Button mBtnPlay;
    private View mProgressGroup;
    private TXLivePlayConfig mPlayConfig;
    private TXLivePlayer mTxLivePlayer;

    private boolean mVideoPause;//视频是否停止
    private Button mBtnOrientation;
    private Button mBtnHWDecode;
    private Button mBtnRenderMode;

    private Button mBtnCacheStrategy;
    private LinearLayout mLayoutCacheStrategy;
    private RadioGroup mCacheStrategyRadioGroup;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_play, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        if (isLive) {
//            view.setBackgroundColor(Color.RED);
//        } else {
//            view.setBackgroundColor(Color.YELLOW);
//        }
        init(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayoutCacheStrategy.setVisibility(View.GONE);
            }
        });
    }

    private void init(View view) {
        initView(view);
        initData();
        initListener();
    }

    //是否是播放状态
    private boolean mVideoPlay;

    private void initListener() {
        mVideoPlay = false;
        //判断当前是否在播放一个直播流
        if (isLive) {
            //是，隐藏进度条
            mProgressGroup.setVisibility(View.GONE);
        }
        //点击播放
        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoPlay) {
                    //是播放状态
                    if (!isLive) {
                        //处理点播数据源
                        if (mVideoPause) {
                            mTxLivePlayer.resume();
                            mBtnPlay.setBackgroundResource(R.drawable.play_pause);
                        } else {
                            mTxLivePlayer.pause();
                            mBtnPlay.setBackgroundResource(R.drawable.play_start);
                        }
                        mVideoPause = !mVideoPause;
                    } else {
                        //直播，停止推送
                        stopPlayRtmp();
                        mVideoPlay = !mVideoPlay;
                    }
                } else {
                    if (startPlayRtmp()) {
                        mVideoPlay = !mVideoPlay;
                    }
                }
            }
        });


        //切换横竖屏
        mBtnOrientation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTxLivePlayer == null) {
                    return;
                }
                //判断如果方向为横屏，则切换为竖屏，否则切换为横屏
                if (mCurrentRenderRotation == TXLiveConstants.RENDER_ROTATION_PORTRAIT) {
                    //切换为横屏
                    mCurrentRenderRotation = TXLiveConstants.RENDER_ROTATION_LANDSCAPE;
                    mBtnOrientation.setBackgroundResource(R.drawable.portrait);
                } else if (mCurrentRenderRotation == TXLiveConstants.RENDER_ROTATION_LANDSCAPE) {
                    //切换为竖屏
                    mCurrentRenderRotation = TXLiveConstants.RENDER_ROTATION_PORTRAIT;
                    mBtnOrientation.setBackgroundResource(R.drawable.landscape);
                }

                mTxLivePlayer.setRenderRotation(mCurrentRenderRotation);
            }
        });

        //开启硬件加速
        mBtnHWDecode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHWDecode = !mHWDecode;
                mBtnHWDecode.getBackground().setAlpha(mHWDecode ? 255 : 100);
                if (mHWDecode) {
                    Toast.makeText(getActivity(), "已经开启硬件加速,切换会重启播放流程", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "已经关闭硬件加速,切换会重启播放流程", Toast.LENGTH_SHORT).show();
                }
                if (mVideoPlay) {
                    //若是直播状态
                    stopPlayRtmp();
                    startPlayRtmp();
                    //如果是点播状态，并且处于定制
                    if (mVideoPause) {
                        if (mPlayerView != null) {
                            //继续播放
                            mPlayerView.onResume();
                        }
                        mVideoPause = false;
                    }
                }
            }
        });

        //调整屏幕自适应或全屏
        mBtnRenderMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTxLivePlayer == null) {
                    return;
                }

                if (mCurrentRenderMode == TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION) {
                    //如果是自适应
                    mCurrentRenderMode = TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN;
                    mBtnRenderMode.setBackgroundResource(R.drawable.adjust_mode);
                    mTxLivePlayer.setRenderMode(mCurrentRenderMode);
                } else if (mCurrentRenderMode == TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN) {
                    mCurrentRenderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;
                    mBtnRenderMode.setBackgroundResource(R.drawable.fill_mode);
                    mTxLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);//自适应
                }
            }
        });


        //缓存策略
        mBtnCacheStrategy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayoutCacheStrategy.setVisibility(View.VISIBLE);
            }
        });

        setCacheStrategy(CACHE_STRATEGY_AUTO);
        //缓存模式设置
        mCacheStrategyRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.radio_btn_fast://极速
                        setCacheStrategy(CACHE_STRATEGY_FAST);
                        break;
                    case R.id.radio_btn_smooth://流畅
                        setCacheStrategy(CACHE_STRATEGY_SMOOTH);
                        break;
                    case R.id.radio_btn_auto://自动
                        setCacheStrategy(CACHE_STRATEGY_AUTO);
                        break;
                }
                mLayoutCacheStrategy.setVisibility(View.GONE);
            }
        });

    }

    private static final int CACHE_TIME_FAST = 1;//急速
    private static final int CACHE_TIME_SMOOTH = 5;//流畅
    private static final int CACHE_TIME_AUTO_MAX = 10;//最大时间
    private static final int CACHE_TIME_AUTO_MIN = 5;//最小时间

    /**
     * 设置缓存策略
     *
     * @param cacheStrategy
     */
    private void setCacheStrategy(int cacheStrategy) {
        if (mCurrentCacheStrategy == cacheStrategy) {
            return;
        }
        switch (cacheStrategy) {
            //极速模式
            case CACHE_STRATEGY_FAST:
                mPlayConfig.setAutoAdjustCacheTime(true);
                mPlayConfig.setMaxAutoAdjustCacheTime(CACHE_TIME_FAST);
                mPlayConfig.setMinAutoAdjustCacheTime(CACHE_TIME_FAST);
                mTxLivePlayer.setConfig(mPlayConfig);
                break;
            //流畅模式
            case CACHE_STRATEGY_SMOOTH:
                mPlayConfig.setAutoAdjustCacheTime(false);
                mPlayConfig.setCacheTime(CACHE_TIME_SMOOTH);
                mTxLivePlayer.setConfig(mPlayConfig);
                break;
            //自动模式
            case CACHE_STRATEGY_AUTO:
                mPlayConfig.setAutoAdjustCacheTime(true);
                mPlayConfig.setMaxAutoAdjustCacheTime(CACHE_TIME_AUTO_MAX);
                mPlayConfig.setMinAutoAdjustCacheTime(CACHE_TIME_AUTO_MIN);
                mTxLivePlayer.setConfig(mPlayConfig);
                break;
        }
    }

    private static final int CACHE_STRATEGY_AUTO = 1;
    private static final int CACHE_STRATEGY_FAST = 2;
    private static final int CACHE_STRATEGY_SMOOTH = 3;
    private static final int mCurrentCacheStrategy = CACHE_STRATEGY_AUTO;

    private boolean mHWDecode;


    /**
     * 是否正常播放
     *
     * @return
     */
    private boolean startPlayRtmp() {
        mBtnPlay.setBackgroundResource(R.drawable.play_pause);
        String rtmpUrl = "rtmp://live.hkstv.hk.lxdns.com/live/hks";//   香港卫视
        //让播放器显示的数据显示到videoView上
        mTxLivePlayer.setPlayerView(mPlayerView);
        mTxLivePlayer.setPlayListener(this);
        //设置屏幕方向,默认竖直
        mTxLivePlayer.setRenderRotation(mCurrentRenderRotation);
        //设置自适应屏幕
        mTxLivePlayer.setRenderMode(mCurrentRenderMode);
        mTxLivePlayer.setConfig(mPlayConfig);
        //开始播放
        //0:直播 1：非直播
        int result = mTxLivePlayer.startPlay(rtmpUrl, isLive ? 0 : 1);
        if (result == -2) {
            Toast.makeText(getActivity(), "暂时无法播放此视频", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 停止播放直播数据
     */
    private void stopPlayRtmp() {
        mBtnPlay.setBackgroundResource(R.drawable.play_start);
        if (mTxLivePlayer != null) {
            mTxLivePlayer.setPlayListener(null);
            //清除最后一帧
            mTxLivePlayer.stopPlay(true);
        }
    }

    //视频是自适应屏幕的，分辨率越高，视频宽高越大
    private int mCurrentRenderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;
    //视频方向默认竖直
    private int mCurrentRenderRotation = TXLiveConstants.RENDER_ROTATION_PORTRAIT;


    private void initData() {
        //初始化配置
        mPlayConfig = new TXLivePlayConfig();
        if (mTxLivePlayer == null) {
            mTxLivePlayer = new TXLivePlayer(getActivity());
        }
    }

    private void initView(View view) {
        mPlayerView = (TXCloudVideoView) view.findViewById(R.id.video_view);
        mLoadingView = (ImageView) view.findViewById(R.id.loadingImageView);
        mBtnPlay = (Button) view.findViewById(R.id.btnPlay);
        mProgressGroup = view.findViewById(R.id.play_progress);
        //切换横竖屏
        mBtnOrientation = (Button) view.findViewById(R.id.btnOrientation);
        //开启硬件加速
        mBtnHWDecode = (Button) view.findViewById(R.id.btnHWDecode);
        //调整屏幕
        mBtnRenderMode = (Button) view.findViewById(R.id.btnRenderMode);
        //初始化缓存策略相关控件
        mBtnCacheStrategy = (Button) view.findViewById(R.id.btnCacheStrategy);
        mLayoutCacheStrategy = (LinearLayout) view.findViewById(R.id.layoutCacheStrategy);
        mCacheStrategyRadioGroup = (RadioGroup) view.findViewById(R.id.cacheStrategyRadioGroup);
    }

    public void setLive(boolean live) {
        isLive = live;
    }


    @Override
    public void onPlayEvent(int event, Bundle bundle) {
        if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT) {
            //断网了
            Toast.makeText(getActivity(), "木有网啊~", Toast.LENGTH_SHORT).show();
            stopPlayRtmp();
            mVideoPlay = false;
            mVideoPause = false;
            // TODO: 2017/7/17 如果是点播，断网后把播放进度设置为0
        }
    }

    @Override
    public void onNetStatus(Bundle bundle) {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPlayerView != null) {
            mPlayerView.onResume();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mPlayerView != null) {
            mPlayerView.onStop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTxLivePlayer != null) {
            mTxLivePlayer.stopPlay(true);
        }
        if (mPlayerView != null) {
            mPlayerView.onDestroy();
        }
    }
}
