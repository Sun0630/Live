package com.sx.live.fragment;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.sx.live.R;
import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;

/**
 * @Author sunxin
 * @Date 2017/7/13 14:50
 * @Description 推流端Fragment，
 */

public class LivePushFragment extends Fragment implements ITXLivePushListener, SeekBar.OnSeekBarChangeListener, RadioGroup.OnCheckedChangeListener {

    private TXCloudVideoView mCaptureView;
    private Button mPlay;
    private TXLivePusher mTxLivePusher;
    private TXLivePushConfig mTxLivePushConfig;
    private Button mBtnCameraChange;
    private Button mTouchFocus;
    private Button mFlashLight;

    //定义推送状态
    private boolean mVideoPublish;

    private boolean mFlashTurnOn = false;
    private boolean mIsTouchFocus = true;
    private boolean mIsFrontCamera = true;
    private Button mBtnFaceBeauty;
    private LinearLayout mFaceBeautyLayout;
    private SeekBar mFaceBeautySeekBar;
    private SeekBar mWhiteFaceSeekBar;
    private Button mBtnBitRate;
    private LinearLayout mBitRateLayout;
    private RadioGroup mRgBitRate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_live_push, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFaceBeautyLayout.setVisibility(View.GONE);
                mBitRateLayout.setVisibility(View.GONE);
            }
        });
        init(view);
    }

    private void init(View view) {
        initView(view);
        initData();
        initListener();
    }

    private void initView(View view) {
        mCaptureView = (TXCloudVideoView) view.findViewById(R.id.video_view);
        mPlay = (Button) view.findViewById(R.id.btnPlay);
        //切换摄像头
        mBtnCameraChange = (Button) view.findViewById(R.id.btnCameraChange);
        //对焦按钮
        mTouchFocus = (Button) view.findViewById(R.id.btnTouchFoucs);
        //闪光灯开关
        mFlashLight = (Button) view.findViewById(R.id.btnFlash);
        //美颜
        mBtnFaceBeauty = (Button) view.findViewById(R.id.btnFaceBeauty);
        mFaceBeautyLayout = (LinearLayout) view.findViewById(R.id.layoutFaceBeauty);
        mFaceBeautySeekBar = (SeekBar) view.findViewById(R.id.beauty_seekbar);
        mWhiteFaceSeekBar = (SeekBar) view.findViewById(R.id.whitening_seekbar);

        //修改码率
        mBtnBitRate = (Button) view.findViewById(R.id.btnBitrate);
        mBitRateLayout = (LinearLayout) view.findViewById(R.id.layoutBitrate);
        mRgBitRate = (RadioGroup) view.findViewById(R.id.resolutionRadioGroup);

    }

    private void initData() {
        //1,初始化推流器
        mTxLivePusher = new TXLivePusher(getContext());
        //2，初始化推流配置
        mTxLivePushConfig = new TXLivePushConfig();

    }

    private void initListener() {
        //开始进行推流
        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mVideoPublish) {
                    //1,修正码率
                    fitBitRate(R.id.radio_btn_auto);
                    //2，开始推流
                    startRTMP();
                    //3，将状态设置为true
                    mVideoPublish = true;
                } else {
                    //停止推流
                    stopPublishRtmp();
                    mVideoPublish = false;
                }
            }
        });

        //更改摄像头
        mBtnCameraChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsFrontCamera = !mIsFrontCamera;
                //判断是否正在推流中
                if (mTxLivePusher.isPushing()) {
                    //直接切换摄像头
                    mTxLivePusher.switchCamera();
                } else {
                    mTxLivePushConfig.setFrontCamera(mIsFrontCamera);
                }
                //切换图片
                mBtnCameraChange.setBackgroundResource(mIsFrontCamera ? R.drawable.camera_change : R.drawable.camera_change2);
            }
        });

        //对焦
        mTouchFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("--------点击对焦--------");
                //判断是否是自动对焦
                if (mIsFrontCamera) {
                    //前置摄像头不支持对焦
                    return;
                }
                mIsTouchFocus = !mIsTouchFocus;
                mTxLivePushConfig.setTouchFocus(mIsTouchFocus);
                mTouchFocus.setBackgroundResource(mIsTouchFocus ? R.drawable.manual : R.drawable.automatic);
                //判断如果处于推送中，对摄像头进行预览效果处理
                if (mTxLivePusher.isPushing()) {
                    mTxLivePusher.stopCameraPreview(false);
                    mTxLivePusher.startCameraPreview(mCaptureView);
                }
                Toast.makeText(getContext(), mIsTouchFocus ? "开启手动对焦" : "开启自动对焦", Toast.LENGTH_SHORT).show();
                System.out.println(mIsTouchFocus ? "开启手动对焦" : "开启自动对焦");
            }
        });

        //设置闪光灯
        mFlashLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlashTurnOn = !mFlashTurnOn;
                if (!mTxLivePusher.turnOnFlashLight(mFlashTurnOn)) {
                    //如果闪光灯打开失败
                    Toast.makeText(getActivity(), "未正常打开闪光灯:1,前置摄像头不支持闪光灯.2,没有开启摄像头", Toast.LENGTH_SHORT).show();
                }

                mFlashLight.setBackgroundResource(mFlashTurnOn ? R.drawable.flash_on : R.drawable.flash_off);
            }
        });

        //美颜开关
        mBtnFaceBeauty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFaceBeautyLayout.setVisibility(View.VISIBLE);
            }
        });
        //设置seekBar
        mFaceBeautySeekBar.setOnSeekBarChangeListener(this);
        mWhiteFaceSeekBar.setOnSeekBarChangeListener(this);

        //修改码率
        mBtnBitRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBitRateLayout.setVisibility(View.VISIBLE);
            }
        });
        mRgBitRate.setOnCheckedChangeListener(this);

    }



    /**
     * 开始推流
     */
    private void startRTMP() {
        //1,定义推流的地址
//        rtmp://live.hkstv.hk.lxdns.com/live/hks   香港卫视
        String rtmpUrl = "rtmp://2000.livepush.myqcloud.com/live/2000_4eb4da7079af11e69776e435c87f075e?bizid=2000";
        mCaptureView.setVisibility(View.VISIBLE);
        //2,设置推流监听
        mTxLivePusher.setPushListener(this);
        //3,开启摄像头,将摄像头获取到的数据显示到videoview上
        mTxLivePusher.startCameraPreview(mCaptureView);
        //4,开始推流
        mTxLivePusher.startPusher(rtmpUrl);
        //切换按钮状态
        mPlay.setBackgroundResource(R.drawable.play_pause);
    }

    /**
     * 修正码率
     * @param checkedId
     */
    private void fitBitRate(int checkedId) {

        switch (checkedId){
            case R.id.radio_btn_auto:
                if (mTxLivePusher != null) {
                    //推送器不为空
                    //1.设置画质
                    mTxLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_360_640);
                    //开启自适应码率
                    mTxLivePushConfig.setAutoAdjustBitrate(true);
                    //2，设置最大码率
                    mTxLivePushConfig.setMaxVideoBitrate(1000);
                    //3,设置最小
                    mTxLivePushConfig.setMinVideoBitrate(500);
                    //4,设置正常
                    mTxLivePushConfig.setVideoBitrate(700);
                    //将config配置给推送器
                    mTxLivePusher.setConfig(mTxLivePushConfig);
                }
                mBtnBitRate.setBackgroundResource(R.drawable.auto_bitrate);
                break;
            case R.id.radio_btn_fix_360p:
                if (mTxLivePusher != null) {
                    mTxLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_360_640);
                    //关闭自适应码率
                    mTxLivePushConfig.setAutoAdjustBitrate(false);
                    mTxLivePushConfig.setVideoBitrate(700);
                    mTxLivePusher.setConfig(mTxLivePushConfig);
                }
                mBtnBitRate.setBackgroundResource(R.drawable.fix_bitrate);
                break;
            case R.id.radio_btn_fix_540p:
                if (mTxLivePusher != null) {
                    mTxLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_540_960);
                    //关闭自适应码率
                    mTxLivePushConfig.setAutoAdjustBitrate(false);
                    mTxLivePushConfig.setVideoBitrate(100);
                    mTxLivePusher.setConfig(mTxLivePushConfig);
                }
                mBtnBitRate.setBackgroundResource(R.drawable.fix_bitrate);
                break;

            case R.id.radio_btn_fix_720p:
                if (mTxLivePusher != null) {
                    mTxLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_720_1280);
                    //关闭自适应码率
                    mTxLivePushConfig.setAutoAdjustBitrate(false);
                    mTxLivePushConfig.setVideoBitrate(1500);
                    mTxLivePusher.setConfig(mTxLivePushConfig);
                }
                mBtnBitRate.setBackgroundResource(R.drawable.fix_bitrate);
                break;
        }
    }



    @Override
    public void onPushEvent(int event, Bundle param) {
        //判断网络
        if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT) {
            //网络断开，停止推流
            Toast.makeText(getActivity(), "网络断开了", Toast.LENGTH_SHORT).show();
            mVideoPublish = false;
            stopPublishRtmp();
        }
    }

    /**
     * 停止推流
     */
    private void stopPublishRtmp() {
        //关闭摄像头预览
        //true ：表示删除摄像头最后一张图片预览
        mTxLivePusher.stopCameraPreview(true);
        mTxLivePusher.setPushListener(null);
        mCaptureView.setVisibility(View.GONE);
        mPlay.setBackgroundResource(R.drawable.play_start);
    }

    @Override
    public void onNetStatus(Bundle bundle) {

    }

    @Override
    public void onResume() {
        super.onResume();
        //videoView生命周期管理
        if (mCaptureView != null) {
            mCaptureView.onResume();
        }

        if (mVideoPublish && !mTxLivePusher.isPushing()) {
            mTxLivePusher.startCameraPreview(mCaptureView);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mCaptureView != null) {
            mCaptureView.onStop();
        }
        //留有残影
        mTxLivePusher.stopCameraPreview(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCaptureView != null) {
            mCaptureView.onDestroy();
        }
    }

    private int mBeautyLevel;
    private int mWhiteLevel;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()){
            case R.id.beauty_seekbar://美颜
                mBeautyLevel = progress;
                break;
            case R.id.whitening_seekbar://美白
                mWhiteLevel = progress;
                break;
        }

        if (mTxLivePusher != null) {
            boolean beautyFilter = mTxLivePusher.setBeautyFilter(mBeautyLevel, mWhiteLevel);
            if (!beautyFilter){
                Toast.makeText(getActivity(), "当前设备不支持美颜", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /**
     * 码率选择
     * @param group
     * @param checkedId
     */
    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        fitBitRate(checkedId);
    }
}
