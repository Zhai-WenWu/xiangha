package amodule.main.Tools;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.LogManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.main.Main;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqInternet;
import aplug.basic.SubBitmapTarget;
import third.ad.db.bean.XHSelfNativeData;
import third.ad.scrollerAd.XHScrollerSelf;
import third.ad.tools.AdConfigTools;
import third.ad.tools.AdPlayIdConfig;
import third.ad.tools.WelcomeAdTools;

import static android.os.Looper.getMainLooper;
import static third.ad.tools.AdPlayIdConfig.WELCOME;

/**
 * 对于welcome进行管理
 */
public class WelcomeControls {
    public final static int DEFAULT_TIME = 8;
    private Activity activity;
    private TextView textSkip, textLead;
    private RelativeLayout mADLayout;

    private int mAdTime = 8;//默认时间
    private boolean isAdLeadClick = false;
    private boolean isAdLoadOk = false;
    private boolean canShowVipLead = true;
    private Handler mMainHandler = null;
    private final long mAdIntervalTime = 1000;
    public WelcomeCallBack welcomeCallBack;

    private int mOriginalWidth = 1700;
    private int mOriginalHeight = 2060;
    private View mWelcomeView;
    private boolean isShowSkipContainer = false;

    public WelcomeControls(@NonNull Activity act, WelcomeCallBack callBack) {
        this(act, DEFAULT_TIME, callBack);
    }

    /**
     * 初始化
     *
     * @param act
     * @param adShowTime
     * @param callBack
     */
    public WelcomeControls(@NonNull Activity act, int adShowTime, WelcomeCallBack callBack) {
        this.activity = act;
        this.mAdTime = adShowTime;
        this.welcomeCallBack = callBack;
        LogManager.printStartTime("zhangyujian", "WelcomeControls：1111：：");
        mWelcomeView = activity.findViewById(R.id.xh_welcome);
        int imageW, imageH;
        int fixedW = Tools.getPhoneWidth();
        int fixedH = Tools.getPhoneHeight() - activity.getResources().getDimensionPixelSize(R.dimen.dp_105);
        double proportionFixed = fixedH / fixedW;
        double proportionOriginal = mOriginalHeight / mOriginalWidth;
        if (proportionFixed > proportionOriginal) {
            imageW = fixedW;
            imageH = fixedW * mOriginalHeight / mOriginalWidth;
        } else {
            imageH = fixedH;
            imageW = fixedH * mOriginalWidth / mOriginalHeight;
        }
        ImageView welcomeImg = mWelcomeView.findViewById(R.id.image_self);
        ViewGroup.LayoutParams params = welcomeImg.getLayoutParams();
        params.width = imageW;
        params.height = imageH;
        mWelcomeView.setVisibility(View.VISIBLE);
    }

    public void startShow() {
//        activity.findViewById(R.id.xh_welcome).setVisibility(View.VISIBLE);
        initWelcome();
        startCountDown(false);
        WelcomeAdTools.getInstance().handlerAdData(false, new WelcomeAdTools.AdNoDataCallBack() {
            @Override
            public void noAdData() {
                if (LoginManager.isShowAd()) {
                    XHClick.mapStat(activity, "ad_no_show", "开屏", "");
                }
            }
        }, false);
    }

    /**
     * 初始化view
     */
    private void initWelcome() {
        LogManager.printStartTime("zhangyujian", "WelcomeControls:::initWelcome：1111：：");
        // 初始化
        mADLayout = (RelativeLayout) activity.findViewById(R.id.ad_layout);
        textSkip = (TextView) activity.findViewById(R.id.ad_skip);
        textLead = (TextView) activity.findViewById(R.id.ad_vip_lead);
        textLead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("zhangyujian", "展示点击：textLeadtextLead：");
                isAdLeadClick = true;
                closeDialog();
            }
        });
        textSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("zhangyujian", "展示点击：：跳过");
                closeDialog();
            }
        });
        initAd();
    }

    private void initAd() {
        if ("true".equals(FileManager.loadShared(activity, FileManager.xmlFile_appInfo, "once").toString())) {
            mAdTime = DEFAULT_TIME;
//            return;
        }
        //设置广点通广告回调
        WelcomeAdTools.getInstance().setmGdtCallback(
                new WelcomeAdTools.GdtCallback() {
                    @Override
                    public void onAdPresent() {
                        mADLayout.setVisibility(View.GONE);
                        Log.i("zhangyujian", "GdtCallback");
                        if (mAdTime > 5) {
                            endCountDown();
                            mAdTime = 5;
                            startCountDown(false);
                        } else if (mAdTime < 3) {
                            closeDialog();
                            return;
                        }
                        showSkipContainer();
                        isAdLoadOk = true;
                        XHClick.mapStat(activity, "ad_show_index", "开屏", "sdk_gdt");
                    }

                    @Override
                    public void onAdFailed(String reason) {
                    }

                    @Override
                    public void onAdDismissed() {
                        Log.i("zhangyujian", "onAdDismissed");
                        closeDialog();
                    }

                    @Override
                    public void onAdClick() {
                        Log.i("zhangyujian", "onAdClick");
                        closeDialog();
                        XHClick.mapStat(activity, "ad_click_index", "开屏", "sdk_gdt");
                    }

                    @Override
                    public void onADTick(long millisUntilFinished) {
                    }

                    @Override
                    public ViewGroup getADLayout() {
                        return mADLayout;
                    }

                    @Override
                    public View getTextSikp() {
                        return textSkip;
                    }
                });
        //百度开屏
        WelcomeAdTools.getInstance().setBaiduCallback(new WelcomeAdTools.BaiduCallback() {
            @Override
            public void onAdPresent() {
                mADLayout.setVisibility(View.GONE);
                if (mAdTime > 5) {
                    endCountDown();
                    mAdTime = 5;
                    startCountDown(false);
                } else if (mAdTime < 3) {
                    closeDialog();
                    return;
                }
                showSkipContainer();
                isAdLoadOk = true;
                XHClick.mapStat(activity, "ad_show_index", "开屏", "sdk_baidu");
            }

            @Override
            public void onAdDismissed() {
                closeDialog();
            }

            @Override
            public void onAdFailed(String s) {
            }

            @Override
            public void onAdClick() {
                closeDialog();
                XHClick.mapStat(activity, "ad_click_index", "开屏", "sdk_baidu");
            }

            @Override
            public ViewGroup getADLayout() {
                return mADLayout;
            }
        });
        //设置XHBanner回调
        WelcomeAdTools.getInstance().setmXHBannerCallback(
                new WelcomeAdTools.XHBannerCallback() {
                    @Override
                    public void onAdLoadSucceeded(XHSelfNativeData nativeData) {
                        if (nativeData == null) {
                            return;
                        }
                        canShowVipLead = !"1".equals(nativeData.getAdType());
                        String url = nativeData.getBigImage();
                        String loadingUrl = nativeData.getUrl();
                        //处理view
                        mADLayout.setVisibility(View.GONE);
                        mADLayout.removeAllViews();
                        isAdLoadOk = true;
                        View view = LayoutInflater.from(activity).inflate(R.layout.view_ad_inmobi, null,true);
                        final ImageView imageView = (ImageView) view.findViewById(R.id.image);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        mADLayout.addView(view,RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);

                        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(XHApplication.in())
                                .load(url).build();
                        if (bitmapRequest != null)
                            bitmapRequest.into(new SubBitmapTarget() {
                                @Override
                                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                                    if (bitmap != null) {
                                        if (!canShowVipLead) {
                                            mAdTime = DEFAULT_TIME;
                                        }
                                        if (mAdTime > 5) {
                                            endCountDown();
                                            mAdTime = 5;
                                            startCountDown(false);
                                        } else if (mAdTime < 3) {
                                            closeDialog();
                                            return;
                                        }
                                        showSkipContainer();
                                        imageView.setImageBitmap(bitmap);
//                                        UtilImage.setImgViewByWH(imageView, bitmap, ToolsDevice.getWindowPx(activity).widthPixels, 0, false);
                                        XHClick.mapStat(activity, "ad_show_index", "开屏", "xh");

                                        AdConfigTools.getInstance().postStatistics("show", AdPlayIdConfig.WELCOME, nativeData.getPositionId(),"xh", nativeData.getId());
                                        if(nativeData != null && !TextUtils.isEmpty(nativeData.getShowUrl())){
                                            ReqInternet.in().doGet(nativeData.getShowUrl(), new InternetCallback() {
                                                @Override
                                                public void loaded(int i, String s, Object o) {
                                                    super.loaded(i, s, o);
                                                }
                                            });
                                        }
                                    }
                                }
                            });
                        //点击
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //友盟统计
                                XHClick.track(activity, "点击启动页广告");
                                XHClick.mapStat(activity, "ad_click_index", "开屏", "xh");
                                if ("1".equals(nativeData.getDbType())) {
                                    XHScrollerSelf.showSureDownload(nativeData, WELCOME, nativeData.getPositionId(),"xh", nativeData.getId());
                                } else {
                                    AppCommon.openUrl(activity, loadingUrl, true);
                                    AdConfigTools.getInstance().postStatistics("click", AdPlayIdConfig.WELCOME, nativeData.getPositionId(),"xh", nativeData.getId());
                                }
                            }
                        });
                    }
                });
    }

    private void showSkipContainer() {
        if(isShowSkipContainer){
            return;
        }
        isShowSkipContainer = true;
        activity.findViewById(R.id.ad_linear).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.line_1).setVisibility(canShowVipLead ? View.VISIBLE : View.GONE);
        textLead.setVisibility(canShowVipLead ? View.VISIBLE : View.GONE);
        textSkip.setVisibility(View.VISIBLE);
        mADLayout.setVisibility(View.VISIBLE);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setFillAfter(true);
        activity.findViewById(R.id.image).setVisibility(View.GONE);
        mADLayout.startAnimation(alphaAnimation);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                textLead.setVisibility(canShowVipLead ? View.VISIBLE : View.GONE);
                activity.findViewById(R.id.line_1).setVisibility(canShowVipLead ? View.VISIBLE : View.GONE);
                textSkip.setVisibility(View.VISIBLE);
                mADLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    /**
     * 关闭dialog
     */
    private void closeDialog() {
        if(!Main.isShowWelcomeDialog){
            return;
        }
        Log.i("tzy", "closeDialog: ");
        Main.isShowWelcomeDialog = false;//至当前dialog状态
        if (!isExectueFree && welcomeCallBack != null) {
            welcomeCallBack.welcomeFree();
        }
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }
        Log.i("zhangyujian", "closeDialog");
        if (isAdLeadClick) {
            AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), StringManager.getVipUrl(false) + "&vipFrom=开屏广告会员免广告", true);
        }
        if (welcomeCallBack != null) welcomeCallBack.welcomeShowState(false);
        activity.findViewById(R.id.xh_welcome).setVisibility(View.GONE);
    }

    private Runnable mCountDownRun = new Runnable() {
        @Override
        public void run() {
            endCountDown();
//            Log.i("zhangyujian", "Runnable：：：：mAdTime::" + mAdTime + "::isAdLoadOk:" + isAdLoadOk + ":::" + LoginManager.isShowAd());
            if (mAdTime <= 0 || (mAdTime <= 2 && !isAdLoadOk && LoginManager.isShowAd())) {
                closeDialog();
                return;
            }
            layoutCallBack();
//            Log.i("zhangyujian", "Runnable：：：mAdTime:::" + mAdTime);
            mAdTime--;
            startCountDown(true);
        }
    };

    private void endCountDown() {
        if (mMainHandler == null) {
            mMainHandler = new Handler(getMainLooper());
        }
        mMainHandler.removeCallbacksAndMessages(null);
    }

    private void startCountDown(boolean delayed) {
        if (mMainHandler == null) {
            mMainHandler = new Handler(getMainLooper());
        }
        mMainHandler.postDelayed(mCountDownRun, delayed ? mAdIntervalTime : 0);
    }

    private boolean isExectueFree = false;

    private void layoutCallBack() {
        if (!isExectueFree && mAdTime <= 3) {
            isExectueFree = true;
            Log.i("zhangyujian", "layoutCallBack::" + mAdTime);
            if (welcomeCallBack != null) welcomeCallBack.welcomeFree();
        }
    }

    public interface WelcomeCallBack {
        public void welcomeShowState(boolean isShow);

        public void welcomeFree();
    }
}
