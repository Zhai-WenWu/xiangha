package amodule.article.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;
import com.sina.sinavideo.sdk.VDVideoViewController;
import com.sina.sinavideo.sdk.VDVideoViewListeners;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.article.activity.VideoDetailActivity;
import amodule.dish.activity.DetailDish;
import amodule.dish.view.DishHeaderView;
import amodule.dish.view.DishVideoImageView;
import amodule.dish.view.VideoDredgeVipView;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.tools.AdPlayIdConfig;
import third.video.VideoPlayerController;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/6/20 11:55.
 * E_mail : ztanzeyu@gmail.com
 */

public class VideoHeaderView extends RelativeLayout {
    //转码状态1-未转，2-正转，3-已转
    private final static String STATUS_UNTRANSCOD = "1";
    private final static String STATUS_TRANSCODING = "2";
    private final static String STATUS_TRANSCODED = "3";
    private Activity activity;
    private Context context;

    private RelativeLayout dishVidioLayout;
    private FrameLayout adLayout;

    private VideoPlayerController mVideoPlayerController = null;//视频控制器
    private DishHeaderView.DishHeaderVideoCallBack callBack;

    private Map<String, String> mapAd;//广告数据
    private boolean isAutoPaly = false;//默认自动播放
    private boolean isOnResuming = false;//默认自动播放
    private XHAllAdControl xhAllAdControl;
    private int num = 4;
    private String status = "1";

    public VideoHeaderView(Context context) {
        super(context);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_video_header_oneimage, null);
        addView(view);
    }

    public VideoHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_video_header_oneimage, null);
        addView(view);
    }

    public VideoHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_video_header_oneimage, null);
        addView(view);
    }

    public void initView(Activity activity) {
        this.activity = activity;
        this.context = activity.getBaseContext();
        isAutoPaly = "wifi".equals(ToolsDevice.getNetWorkSimpleType(activity));
        //大图处理
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ToolsDevice.getWindowPx(activity).widthPixels * 9 / 16);
        setLayoutParams(params);
        dishVidioLayout = (RelativeLayout) findViewById(R.id.video_layout);
        dredgeVipLayout = (RelativeLayout) findViewById(R.id.video_dredge_vip_layout);
    }

    public void setData(Map<String, String> data, DishHeaderView.DishHeaderVideoCallBack callBack, Map<String, String> detailPermissionMap) {
        if (callBack == null) {
            this.callBack = new DishHeaderView.DishHeaderVideoCallBack() {
                @Override
                public void videoImageOnClick() {
                }

                @Override
                public void getVideoControl(VideoPlayerController mVideoPlayerController, RelativeLayout dishVidioLayout, View view_oneImage) {
                }
            };
        } else this.callBack = callBack;

        try {
            Map<String, String> videoData = StringManager.getFirstMap(data.get("video"));
            status = videoData.get("status");
            videoData.put("title", data.get("title"));
            Map<String, String> videoUrlData = StringManager.getFirstMap(videoData.get("videoUrl"));
            String url = "";
            if (videoUrlData.isEmpty()) {
                Toast.makeText(getContext(), "视频播放失败", Toast.LENGTH_SHORT).show();
                return;
            }
            if (videoUrlData.containsKey("D1080p") && !TextUtils.isEmpty(videoUrlData.get("D1080p"))) {
                url = videoUrlData.get("D1080p");
            } else if (videoUrlData.containsKey("D720p") && !TextUtils.isEmpty(videoUrlData.get("D720p"))) {
                url = videoUrlData.get("D720p");
            } else if (videoUrlData.containsKey("D480p") && !TextUtils.isEmpty(videoUrlData.get("D480p"))) {
                url = videoUrlData.get("D480p");
            }
            videoData.put("url", url);
            setSelfVideo(videoData,detailPermissionMap);
        } catch (Exception e) {
            Toast.makeText(getContext(), "视频播放失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void initVideoAd() {
        adLayout = (FrameLayout) findViewById(R.id.video_ad_layout);

        ArrayList<String> list = new ArrayList<>();
        String key = AdPlayIdConfig.ARTICLE_TIEPIAN;
        list.add(key);

        xhAllAdControl = new XHAllAdControl(list, new XHAllAdControl.XHBackIdsDataCallBack() {
            @Override
            public void callBack(Map<String, String> maps) {
                String temp = maps.get(AdPlayIdConfig.ARTICLE_TIEPIAN);
                mapAd = StringManager.getFirstMap(temp);
                Log.i("tzy", "needVideoControl time = " + System.currentTimeMillis());
                if (mapAd != null && mapAd.size() > 0
                        && mVideoPlayerController != null) {
                    mVideoPlayerController.setShowAd(true);
                }
                if (isAutoPaly && mVideoPlayerController != null)
                    mVideoPlayerController.setOnClick();
            }
        }, activity, "wz_tiepian");

    }

    /**
     * 处理广告展示
     *
     * @param map
     * @param view
     */
    private void setVideoAdData(final Map<String, String> map, final View view) {
        xhAllAdControl.onAdBind(0, view, "");
        final TextView mNum = (TextView) view.findViewById(R.id.ad_gdt_video_num);
        final ImageView mImageView = (ImageView) view.findViewById(R.id.ad_video_img);
        String imgUrl = null;
        if (map.containsKey("imgUrl")) imgUrl = map.get("imgUrl");
        if (TextUtils.isEmpty(imgUrl)) return;

        view.findViewById(R.id.ad_gdt_video_hint_layout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                view.setVisibility(View.GONE);
                mVideoPlayerController.setShowAd(false);
                mVideoPlayerController.setOnClick();
            }
        });
        view.findViewById(R.id.ad_vip_lead).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCommon.openUrl(activity, StringManager.api_openVip, true);
            }
        });

        mImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                xhAllAdControl.onAdClick(0, "");
            }
        });
        //初始化倒计时
        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mNum.setText("" + msg.what);
                if (msg.what == 0) {
                    view.setVisibility(View.GONE);
                    if (!mVideoPlayerController.isPlaying()) {
                        mVideoPlayerController.setShowAd(false);
                        if (isOnResuming) mVideoPlayerController.setOnClick();
                    }
                }
            }
        };
        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(XHApplication.in())
                .load(imgUrl)
                .setRequestListener(new RequestListener<GlideUrl, Bitmap>() {
                    @Override
                    public boolean onResourceReady(Bitmap arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3, boolean arg4) {
                        return false;
                    }

                    @Override
                    public boolean onException(Exception arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3) {
                        mImageView.setVisibility(View.GONE);
                        return false;
                    }
                })
                .build();
        if (bitmapRequest != null)
            bitmapRequest.into(new SubBitmapTarget() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                    view.setVisibility(View.VISIBLE);
                    mImageView.setVisibility(View.VISIBLE);
                    bitmap.getHeight();
                    mImageView.setImageBitmap(bitmap);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (; num > 0; num--) {
                                handler.sendEmptyMessage(num);
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            handler.sendEmptyMessage(0);
                        }
                    }).start();
                }
            });
    }

    private boolean setSelfVideo(final Map<String, String> selfVideoMap, Map<String, String> permissionMap) {
        if (STATUS_TRANSCODED.equals(status))
            initVideoAd();
        boolean isUrlVaild = false;
        String videoUrl = selfVideoMap.get("url");
        String img = selfVideoMap.get("videoImg");
        if (!TextUtils.isEmpty(videoUrl)
                && videoUrl.startsWith("http")) {
            mVideoPlayerController = new VideoPlayerController(activity, dishVidioLayout, img);

            if(permissionMap != null && permissionMap.containsKey("video")){
                Map<String,String> videoPermionMap = StringManager.getFirstMap(permissionMap.get("video"));
                Map<String,String> commonMap = StringManager.getFirstMap(videoPermionMap.get("common"));
                Map<String,String> timeMap = StringManager.getFirstMap(videoPermionMap.get("fields"));
                if(!TextUtils.isEmpty(timeMap.get("time"))){
//                    limitTime = Integer.parseInt(timeMap.get("time"));
                    setVipPermision(commonMap);
                }
            }else{
                isContinue = true;
                isHaspause = false;
                dredgeVipLayout.setVisibility(GONE);
                mVideoPlayerController.setControlLayerVisibility(true);
                VDVideoViewController.getInstance(activity).setSeekPause(false);
            }

            DishVideoImageView dishVideoImageView = new DishVideoImageView(activity);
            dishVideoImageView.setData(img,"");

            mVideoPlayerController.setNewView(dishVideoImageView);
            mVideoPlayerController.initVideoView2(videoUrl, selfVideoMap.get("title"), dishVideoImageView);
            mVideoPlayerController.setStatisticsPlayCountCallback(new VideoPlayerController.StatisticsPlayCountCallback() {
                @Override
                public void onStatistics() {
                    XHClick.mapStat(getContext(), "a_ShortVideoDetail", "视频内容", "播放视频");
                    callBack.videoImageOnClick();
                }
            });
            //被点击回调
            mVideoPlayerController.setMediaViewCallBack(new VideoPlayerController.MediaViewCallBack() {
                @Override
                public void onclick() {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            setVideoAdData(mapAd, adLayout);
                        }
                    });
                }
            });
            callBack.getVideoControl(mVideoPlayerController, dishVidioLayout, this);
            callBack.videoImageOnClick();
            //转码未完成，重新设置点击
            if (!STATUS_TRANSCODED.equals(status)) {
                dishVideoImageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Tools.showToast(getContext(), "视频转码中，请稍后再试");
                    }
                });
            }
            isUrlVaild = true;
        }
        return isUrlVaild;
    }
    boolean isContinue = false;
    boolean isHaspause = false;
    long currentTime = 0;
    int limitTime = 0;
    private RelativeLayout dredgeVipLayout;
    private VideoDredgeVipView vipView;
    private void setVipPermision(final Map<String, String> common){
        if(StringManager.getBooleanByEqualsValue(common,"isShow")
                ) return;
        final String url = common.get("url");
        if(TextUtils.isEmpty(url)) return;
        vipView = new VideoDredgeVipView(getContext());
        dredgeVipLayout.addView(vipView);
        vipView.setTipMessaText(common.get("text"));
        vipView.setDredgeVipClick(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(url)){
                    AppCommon.openUrl(activity,url,true);
                    return;
                }
            }
        });
        mVideoPlayerController.setOnProgressUpdateListener(new VDVideoViewListeners.OnProgressUpdateListener() {
            @Override
            public void onProgressUpdate(long current, long duration) {
                int currentS = Math.round(current/1000f);
                if(isHaspause){
                    VDVideoViewController.getInstance(activity).setSeekPause(true);
                    mVideoPlayerController.onPause();
                    mVideoPlayerController.onResume();
                    return;
                }
                if(currentS > limitTime && !isContinue){
                    currentTime = current;
                    dredgeVipLayout.setVisibility(VISIBLE);
                    VDVideoViewController.getInstance(activity).setSeekPause(true);
                    mVideoPlayerController.onPause();
                    mVideoPlayerController.onResume();
                    isHaspause = true;
                }
            }

            @Override
            public void onDragProgess(long current, long duration) {
                int currentS = Math.round(current/1000f);
                if(isHaspause){
                    VDVideoViewController.getInstance(activity).setSeekPause(true);
                    mVideoPlayerController.onPause();
                    mVideoPlayerController.onResume();
                    return;
                }
                if(currentS > limitTime && !isContinue){
                    currentTime = current;
                    dredgeVipLayout.setVisibility(VISIBLE);
                    VDVideoViewController.getInstance(activity).setSeekPause(true);
                    mVideoPlayerController.onPause();
                    mVideoPlayerController.onResume();

                    isHaspause = true;
                }
            }
        });
    }

    public void onResume() {
        isOnResuming = true;
    }

    public void onPause() {
        isOnResuming = false;
    }

    public void setLoginStatus(){
        if(vipView != null){
            vipView.setLogin();
        }
    }
}
