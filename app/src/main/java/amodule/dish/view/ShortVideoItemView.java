package amodule.dish.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.listener.StandardVideoAllCallBack;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import acore.logic.AppCommon;
import acore.logic.FavoriteHelper;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.logic.stat.StatModel;
import acore.logic.stat.StatisticsManager;
import acore.logic.stat.intefaces.OnClickListenerStat;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.KeyboardDialog;
import acore.widget.multifunction.IconTextSpan;
import amodule._common.conf.FavoriteTypeEnum;
import amodule._common.conf.GlobalAttentionModule;
import amodule._common.conf.GlobalCommentModule;
import amodule._common.conf.GlobalFavoriteModule;
import amodule._common.conf.GlobalGoodModule;
import amodule._common.conf.GlobalVariableConfig;
import amodule.article.view.BottomDialog;
import amodule.comment.CommentDialog;
import amodule.dish.activity.ShortVideoDetailActivity;
import amodule.dish.adapter.RvVericalVideoItemAdapter;
import amodule.dish.video.module.ShortVideoDetailModule;
import amodule.main.Main;
import amodule.main.view.item.BaseItemView;
import amodule.user.Broadcast.UploadStateChangeBroadcasterReceiver;
import amodule.user.activity.FriendHome;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.DefaultInternetCallback;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import aplug.basic.SubBitmapTarget;
import aplug.player.ShortVideoPlayer;
import third.share.activity.ShareActivityDialog;

import static acore.logic.stat.StatConf.STAT_TAG;

/**
 * 短视频itemView
 */
public class ShortVideoItemView extends BaseItemView implements SeekBar.OnSeekBarChangeListener {

    private static final int INNER_PLAY_STATE_START = 1;
    private static final int INNER_PLAY_STATE_PLAYING = 2;
    private static final int INNER_PLAY_STATE_PAUSE = 3;
    private static final int INNER_PLAY_STATE_STOP = 4;
    private static final int INNER_PLAY_STATE_AUTO_COMPLETE = 5;

    private final int FIXED_TEXT_COUNT = 50;

    private int mInnerPlayState;

    private int mScreenW, mScreenH;
    private int mFixedHW;

    private Context context;
    private ImageView mThumbImg;
    private RelativeLayout mThumbContainer;
    private RelativeLayout mVideoLayout;
    private ConstraintLayout mTitleLayout;
    private ImageView mBackImg;
    private ImageView mHeaderImg;
    private TextView mUserName;
    private ImageView mAttentionImage;
    private ImageView mLikeImg;
    private ImageView mMoreImg;
    private View mEmptyView;
    private ConstraintLayout mInfoLayout;
    private LinearLayout mLayoutTopic;
    private LinearLayout mLayoutAddress;
    private TextView mTopicText;
    private TextView mAddressText;
    private TextView mTitleText;
    private ConstraintLayout mBottomLayout;
    private View mBottomCommentLayout;
    private View mBottomShareLayout;
    private View mBottomGoodLayout;
    private ImageView mCommentImg;
    private TextView mCommentNumText;
    private ImageView mShareImg;
    private TextView mShareNum;
    private ImageView mGoodImg;
    private TextView mGoodText;
    private ProgressBar mBottomProgress;
    private TextView mCommentHint;

    private ShortVideoDetailModule mData;//全部
    private boolean mIsSelf;

    private AtomicBoolean mGoodLoading;
    private AtomicBoolean mAttentionLoading;
    private AtomicBoolean mFavLoading;
    private AtomicBoolean mDelLoading;
    private boolean mNeedChangePauseToStartEnable;
    private boolean mPauseToStartEnable;
    private boolean mEffectStaticEnable;
    private boolean mStaticEnable;
    private String mVideoUrl;
    private String mTopicClickUrl;
    private String mAddressClickUrl;

    private String mSendText;

    private int mPos;
    private ShortVideoPlayer mPlayerView;
    private float duration;
    private CommentDialog mCommentDialog;

    private View mGuideView;

    private OnPlayPauseClickListener mOnPlayPauseListener;
    private OnSeekBarTrackingTouchListener mOnSeekBarTrackingTouchListener;
    private int position;
    private int playNum = 0;//播放数量

    private Handler mMainHandler;
    private boolean isCompleteCallback = true;
    private TextView mHavePrizeText;

    public ShortVideoItemView(Context context) {
        this(context, null);
    }

    public ShortVideoItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.item_short_video_view, this, true);
        initView();
    }

    public void initView() {
        mThumbImg = findViewById(R.id.image_thumb);
        mThumbContainer = findViewById(R.id.thumb_container);
        mVideoLayout = findViewById(R.id.surface_container);
        mTitleLayout = findViewById(R.id.layout_title);
        mBackImg = findViewById(R.id.image_back);
        mHeaderImg = findViewById(R.id.image_user_header);
        mUserName = findViewById(R.id.text_user_name);
        mAttentionImage = findViewById(R.id.img_attention);
        mLikeImg = findViewById(R.id.image_like);
        mMoreImg = findViewById(R.id.image_more);
        mEmptyView = findViewById(R.id.view_empty);
        mInfoLayout = findViewById(R.id.layout_info);
        mLayoutTopic = findViewById(R.id.layout_topic);
        mLayoutAddress = findViewById(R.id.layout_address_inner);
        mAddressText = findViewById(R.id.text_address);
        mTopicText = findViewById(R.id.text_topic);
        mHavePrizeText = findViewById(R.id.tv_have_prize);
        mTitleText = findViewById(R.id.text_title);
        mBottomLayout = findViewById(R.id.layout_bottom_info);
        mBottomProgress = findViewById(R.id.bottom_progressbar);
        mBottomCommentLayout = findViewById(R.id.layout_bottom_comment);
        mCommentImg = mBottomCommentLayout.findViewById(R.id.image3);
        mCommentNumText = mBottomCommentLayout.findViewById(R.id.text3);
        mCommentHint = findViewById(R.id.comment_hint);
        mBottomShareLayout = findViewById(R.id.layout_bottom_share);
        mShareImg = mBottomShareLayout.findViewById(R.id.image2);
        mShareNum = mBottomShareLayout.findViewById(R.id.text2);
        mBottomGoodLayout = findViewById(R.id.layout_bottom_good);
        mGoodImg = mBottomGoodLayout.findViewById(R.id.image1);
        mGoodText = mBottomGoodLayout.findViewById(R.id.text1);
        mGuideView = findViewById(R.id.guide_view);

        mPlayerView = findViewById(R.id.short_video);

        mPlayerView.setShowFullAnimation(false);
        mPlayerView.setIsTouchWiget(false);
        initData();

        addListener();
    }

    private void initData() {
        mGoodLoading = new AtomicBoolean(false);
        mAttentionLoading = new AtomicBoolean(false);
        mFavLoading = new AtomicBoolean(false);
        mDelLoading = new AtomicBoolean(false);
        DisplayMetrics dm = ToolsDevice.getWindowPx(getContext());
        mScreenW = dm.widthPixels;
        mScreenH = dm.heightPixels;
        mFixedHW = 667 / 375;
    }

    private void addListener() {
        mBackImg.setTag(STAT_TAG, "返回");
        mBackImg.setOnClickListener(mOnClickListenerStat);
        mHeaderImg.setTag(STAT_TAG, "用户头像");
        mHeaderImg.setOnClickListener(mOnClickListenerStat);
        mUserName.setTag(STAT_TAG, "用户昵称");
        mUserName.setOnClickListener(mOnClickListenerStat);
        mAttentionImage.setTag(STAT_TAG, "关注");
        mAttentionImage.setOnClickListener(mOnClickListenerStat);
        mLikeImg.setTag(STAT_TAG, "收藏");
        mLikeImg.setOnClickListener(mOnClickListenerStat);
        mMoreImg.setTag(STAT_TAG, "更多");
        mMoreImg.setOnClickListener(mOnClickListenerStat);

        mEmptyView.setOnClickListener(mOnClickListenerStat);

        mBottomCommentLayout.setTag(STAT_TAG, "评论");
        mBottomCommentLayout.setOnClickListener(mOnClickListenerStat);
        mBottomGoodLayout.setTag(STAT_TAG, "点赞");
        mBottomGoodLayout.setOnClickListener(mOnClickListenerStat);
        mBottomShareLayout.setTag(STAT_TAG, "分享");
        mBottomShareLayout.setOnClickListener(mOnClickListenerStat);

        mBottomLayout.setOnClickListener(mOnClickListenerStat);
        mLayoutTopic.setTag(STAT_TAG, "话题");
        mLayoutTopic.setOnClickListener(mOnClickListenerStat);
        mLayoutAddress.setTag(STAT_TAG, "地址");
        mLayoutAddress.setOnClickListener(mOnClickListenerStat);

        mPlayerView.setStandardVideoAllCallBack(new StandardVideoAllCallBack() {
            @Override
            public void onClickStartThumb(String url, Object... objects) {
            }

            @Override
            public void onClickBlank(String url, Object... objects) {
            }

            @Override
            public void onClickBlankFullscreen(String url, Object... objects) {
            }

            @Override
            public void onPrepared(String url, Object... objects) {
                mPlayerView.changePlayBtnState(false);
                switchPlayState();
            }

            @Override
            public void onClickStartIcon(String url, Object... objects) {
            }

            @Override
            public void onClickStartError(String url, Object... objects) {
            }

            @Override
            public void onClickStop(String url, Object... objects) {
                mInnerPlayState = INNER_PLAY_STATE_STOP;
            }

            @Override
            public void onClickStopFullscreen(String url, Object... objects) {
            }

            @Override
            public void onClickResume(String url, Object... objects) {
                mInnerPlayState = INNER_PLAY_STATE_PLAYING;
            }

            @Override
            public void onClickResumeFullscreen(String url, Object... objects) {
            }

            @Override
            public void onClickSeekbar(String url, Object... objects) {

            }

            @Override
            public void onClickSeekbarFullscreen(String url, Object... objects) {
            }

            @Override
            public void onAutoComplete(String url, Object... objects) {
                mInnerPlayState = INNER_PLAY_STATE_AUTO_COMPLETE;
                changeThumbImageState(true);
                playNum++;
                if (playCompleteCallBack != null && position >= 0) {
                    playCompleteCallBack.videoComplete(position,isCompleteCallback);
                }
            }

            @Override
            public void onEnterFullscreen(String url, Object... objects) {
            }

            @Override
            public void onQuitFullscreen(String url, Object... objects) {
            }

            @Override
            public void onQuitSmallWidget(String url, Object... objects) {
            }

            @Override
            public void onEnterSmallWidget(String url, Object... objects) {
            }

            @Override
            public void onTouchScreenSeekVolume(String url, Object... objects) {
            }

            @Override
            public void onTouchScreenSeekPosition(String url, Object... objects) {
            }

            @Override
            public void onTouchScreenSeekLight(String url, Object... objects) {
            }

            @Override
            public void onPlayError(String url, Object... objects) {
                changeThumbImageState(true);
            }
        });

        mPlayerView.setOnProgressChangedCallback((progress, secProgress, currentTime, totalTime) -> {

//                Log.e("TAG_Player", "onProgressChanged: progress = " + progress + "  currentTime = " + currentTime);
            Log.i("tzy", "addListener: currentTime"+currentTime);
            if(totalTime - currentTime > 0 && totalTime - currentTime <= 5000){
                Log.i("tzy", "addListener: 看过视频了");
                todayWatchVideo();
            }
            duration = totalTime;
            if (progress == 0 && currentTime == 0) {
                if (mNeedChangePauseToStartEnable) {
                    mNeedChangePauseToStartEnable = false;
                    mPauseToStartEnable = true;
                }
                return;
            } else {
                mNeedChangePauseToStartEnable = false;
                mPauseToStartEnable = false;
            }
            if (mPlayerView.playBtnVisible()) {
                mPlayerView.changePlayBtnState(false);
            }

            switchPlayState();

            if (thumbImageStateVisible() && currentTime >= 1) {
                changeThumbImageState(false);
            }
            double playableTime = Double.parseDouble(mData.getVideoModel().getPlayableTime());
            if ((totalTime == 0 ? 0 : currentTime * 1.0 / totalTime) >= playableTime) {
                if (!mEffectStaticEnable) {
                    mEffectStaticEnable = true;
                    startStatistics(StringManager.API_SHORT_VIDEO_VIEW_VALIDATE);
                }
            } else {
                mStaticEnable = false;
            }
        });

    }

    private void switchPlayState() {
        switch (mInnerPlayState) {
            case INNER_PLAY_STATE_PAUSE:
                pauseVideo();
                break;
            case INNER_PLAY_STATE_STOP:
                releaseVideo();
                break;
            default:
                break;
        }
    }

    /**
     * 开始播放入口
     */
    public void prepareAsync() {
//        isRequesting = false;
        mInnerPlayState = INNER_PLAY_STATE_START;
        mNeedChangePauseToStartEnable = true;
        if (TextUtils.isEmpty(mVideoUrl)) {
            releaseVideo();
            return;
        }
        mPlayerView.setUp(mVideoUrl, false, "");
        mPlayerView.startPlayLogic();
        if (!mStaticEnable) {
            mStaticEnable = true;
            startStatistics(StringManager.API_SHORT_VIDEO_ACCESS);
        }
    }

    public void resumeVideo() {
        if (mPauseToStartEnable) {
            prepareAsync();
            return;
        }
        mInnerPlayState = INNER_PLAY_STATE_PLAYING;
        mPlayerView.onVideoResume();
        mPlayerView.changePlayBtnState(false);
    }

    /**
     * 暂停
     */
    public void pauseVideo() {
        mInnerPlayState = INNER_PLAY_STATE_PAUSE;
        mPlayerView.onVideoPause();
        mPlayerView.changePlayBtnState(true);
    }

    @SuppressLint("DefaultLocale")
    private void statisticsVideoView() {
        try {
            String tag = getTag(STAT_TAG) != null ? (String) getTag(STAT_TAG) : getClass().getSimpleName();
            if (duration == 0) {
                duration = mPlayerView.getDuration();
            }
            float total = (playNum * duration + mPlayerView.getCurrentPositionWhenPlaying()) / 1000f;
            float temp = duration <= 0 ? 0 : mPlayerView.getCurrentPositionWhenPlaying() / duration + playNum;
            String n1 = temp <= 0 ? "0" : String.format("%.2f", temp);
            StatisticsManager.saveData(StatModel.createVideoViewModel(getContext().getClass().getSimpleName(), tag, String.valueOf(position + 1),
                    n1, String.format("%.2f", total), mData.getStatJson()));
        } catch (Exception e) {

        }
    }

    /**
     * 重置数据
     */
    public void releaseVideo() {
        if (INNER_PLAY_STATE_STOP != mInnerPlayState && mInnerPlayState != 0) {
            statisticsVideoView();
        }
        playNum = 0;
        duration = 0;
        mEffectStaticEnable = false;
        mStaticEnable = false;
        mInnerPlayState = INNER_PLAY_STATE_STOP;
        mPlayerView.release();
        mPlayerView.changePlayBtnState(false);
        changeThumbImageState(true);
    }

    public int getPlayState() {
        return mPlayerView.getCurrentState();
    }

    public boolean isPlaying() {
        return mPlayerView.getCurrentState() == GSYVideoPlayer.CURRENT_STATE_PLAYING;
    }

    /**
     * 设置数据
     *
     * @param module
     */
    public void setData(ShortVideoDetailModule module, int position) {
        mData = module;
        this.position = position;
        if (mData == null)
            return;
        this.position = position;
        playNum = 0;
        duration = 0;
        mUserName.setText(mData.getCustomerModel().getNickName());
        mIsSelf = TextUtils.equals(LoginManager.userInfo.get("code"), mData.getCustomerModel().getUserCode());
        if (mIsSelf) {
            mAttentionImage.setVisibility(View.GONE);
            mLikeImg.setVisibility(View.GONE);
            mMoreImg.setVisibility(View.VISIBLE);
        } else {
            mAttentionImage.setVisibility(mData.getCustomerModel().isFollow() ? View.GONE : View.VISIBLE);
            mMoreImg.setVisibility(View.GONE);
            mLikeImg.setSelected(mData.isFav());
            mLikeImg.setVisibility(View.VISIBLE);
        }
        //安卓手机视频尺寸适配初步策略
        //• 当视频高宽比小于等于 667/375 时，等宽，自适应高
        //• 当视频高宽比大于 667/375 时，按高铺满，自适应宽
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mThumbImg.getLayoutParams();
        int vW = Tools.parseIntOfThrow(mData.getVideoModel().getVideoW(), 0);
        int vH = Tools.parseIntOfThrow(mData.getVideoModel().getVideoH(), 0);
        int heightImg = 0;
        int widthImg = 0;
        if (vW == 0 || vH == 0) {
            widthImg = LayoutParams.MATCH_PARENT;
            heightImg = LayoutParams.MATCH_PARENT;
        } else {
            int vHW = vH / vW;
            if (vHW > mFixedHW) {
                heightImg = mScreenH;
                widthImg = vW * mScreenH / vH;
            } else {
                widthImg = mScreenW;
                heightImg = vH * mScreenW / vW;
            }
        }
        lp.width = widthImg;
        lp.height = heightImg;
        mThumbImg.setLayoutParams(lp);
        loadUserHeader(mData.getCustomerModel().getHeaderImg());
        String videoImg = mData.getVideoModel().getVideoImg();
        mThumbImg.setTag(TAG_ID, videoImg);
        mThumbImg.setImageResource(R.color.transparent);
        BitmapRequestBuilder builder = LoadImage.with(getContext()).load(videoImg).setPlaceholderId(R.color.transparent).setSaveType(FileManager.save_cache).build();
        if (builder != null) {
            builder.into(mThumbImg);
        }
        changeThumbImageState(mInnerPlayState == 0 || mInnerPlayState == INNER_PLAY_STATE_STOP || mInnerPlayState == INNER_PLAY_STATE_PAUSE);
        mVideoUrl = mData.getVideoModel().getVideoUrlMap().get("defaultUrl");

        mCommentImg.setImageResource(R.drawable.short_video_detail_comment);
        mCommentNumText.setText(mData.getCommentNum());
        mGoodImg.setImageResource(R.drawable.bg_select_good);
        mGoodImg.setSelected(mData.isLike());
        mGoodText.setText(mData.getLikeNum());
        mShareImg.setImageResource(R.drawable.short_video_detail_share);
        mShareNum.setText(mData.getShareNum());
        mTitleText.setText("");
        String title = mData.getName();
        if (!TextUtils.isEmpty(title)) {
            mTitleText.setVisibility(View.VISIBLE);
            if (mData.isEssence()) {
                IconTextSpan.Builder ib = new IconTextSpan.Builder();
                ib.setBgColorInt(getResources().getColor(R.color.color_fa273b));
                ib.setTextColorInt(getResources().getColor(R.color.c_white_text));
                ib.setText("精选");
                ib.setRadius(2f);
                ib.setRightMargin(3);
                ib.setBgHeight(18f);
                ib.setTextSize(12f);
                StringBuffer sb = new StringBuffer(" ");
                sb.append(title);
                SpannableStringBuilder ssb = new SpannableStringBuilder(sb.toString());
                ssb.setSpan(ib.build(getContext()), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mTitleText.setText(ssb);
            } else {
                mTitleText.setText(title);
            }
        } else {
            mTitleText.setVisibility(View.GONE);
        }
        mTopicClickUrl = mData.getTopicModel().getGotoUrl();
        String topicTitle = mData.getTopicModel().getTitle();
        if (!TextUtils.isEmpty(topicTitle)) {
            mTopicText.setText(topicTitle);
            mLayoutTopic.setVisibility(View.VISIBLE);
        } else {
            mLayoutTopic.setVisibility(View.GONE);
        }
        setActivityIcon();
        mAddressClickUrl = mData.getAddressModel().getGotoUrl();
        String address = mData.getAddressModel().getAddress();
        if (!TextUtils.isEmpty(address)) {
            mAddressText.setText(address);
            mLayoutAddress.setVisibility(View.VISIBLE);
        } else {
            mLayoutAddress.setVisibility(View.GONE);
        }

        String dateStr = (String) FileManager.loadShared(getContext(),FileManager.xmlFile_appInfo,"videoCommentGuide");
        String todayStr = Tools.getAssignTime("yyyyMMdd",0);
        boolean todayOnce = !TextUtils.equals(dateStr,todayStr);
        mGuideView.setVisibility(todayOnce?VISIBLE:GONE);
        FileManager.saveShared(getContext(),FileManager.xmlFile_appInfo,"videoCommentGuide",todayStr);
    }

    private void setActivityIcon() {
        String activityType = mData.getTopicModel().getActivityType();
        mHavePrizeText.setVisibility("1".equals(activityType) || "2".equals(activityType) ? VISIBLE : GONE);
    }

    public void setPos(int pos) {
        mPos = pos;
    }

    public int getPos() {
        return mPos;
    }

    public String getVideoUrl() {
        return mVideoUrl;
    }

    private void doFavorite() {
        if (!ToolsDevice.isNetworkAvailable(getContext())) {
            Toast.makeText(getContext(), "网络异常，请检查网络", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkLoginAndHandle()) {
            return;
        }
        if (mFavLoading.get())
            return;
        mFavLoading.set(true);
        FavoriteHelper.instance().setFavoriteStatus(context.getApplicationContext(), mData.getCode(), mData.getName(),
                FavoriteTypeEnum.TYPE_VIDEO, new FavoriteHelper.FavoriteStatusCallback() {
                    @Override
                    public void onSuccess(boolean isFav) {
                        mFavLoading.set(false);
                        mLikeImg.setSelected(isFav);
                        mData.setFav(isFav);
                        GlobalFavoriteModule module = new GlobalFavoriteModule();
                        module.setFavCode(mData.getCode());
                        module.setFav(isFav);
                        module.setFavNum(mData.getFavNum());
                        module.setFavType(FavoriteTypeEnum.TYPE_VIDEO);
                        GlobalVariableConfig.handleFavoriteModule(module);
                    }

                    @Override
                    public void onFailed() {
                        mFavLoading.set(false);
                    }
                });
    }

    OnClickListenerStat mOnClickListenerStat = new OnClickListenerStat(this) {
        @Override
        public void onClicked(View v) {
            switch (v.getId()) {
                case R.id.layout_address:
                    if (!TextUtils.isEmpty(mAddressClickUrl)) {
                        AppCommon.openUrl(mAddressClickUrl, true);
                        XHClick.mapStat(getContext(), ShortVideoDetailActivity.STA_ID, "位置点击量", "");
                    }
                    break;
                case R.id.layout_topic:
                    if (!TextUtils.isEmpty(mTopicClickUrl)) {
                        AppCommon.openUrl(mTopicClickUrl, true);
                        XHClick.mapStat(getContext(), ShortVideoDetailActivity.STA_ID, "话题点击量", "");
                    }
                    break;
                case R.id.image_back:
                    closeActivity();
                    XHClick.mapStat(getContext(), ShortVideoDetailActivity.STA_ID, "返回", "");
                    break;
                case R.id.image_user_header:
                case R.id.text_user_name:
                    gotoUser();
                    break;
                case R.id.img_attention:
                    attention();
                    break;
                case R.id.image_like:
                    doFavorite();
                    break;
                case R.id.image_more:
                    showBottomDialog();
                    break;
                case R.id.view_empty:
                    handlePlay();
                    break;
                case R.id.layout_bottom_share:
                    doShare();
                    XHClick.mapStat(getContext(), ShortVideoDetailActivity.STA_ID, "分享", "");
                    break;
                case R.id.layout_bottom_good:
                    doGood();
                    XHClick.mapStat(getContext(), ShortVideoDetailActivity.STA_ID, "点赞", "");
                    break;
                case R.id.layout_bottom_comment:
                    showComments();
                    mGuideView.setVisibility(GONE);
                    XHClick.mapStat(getContext(), ShortVideoDetailActivity.STA_ID, "评论", "评论按钮点击量");
                    break;
                case R.id.layout_bottom_info:
                    showCommentEdit();
                    mGuideView.setVisibility(GONE);
                    XHClick.mapStat(getContext(), ShortVideoDetailActivity.STA_ID, "评论", "说点什么点击量");
                    break;
            }
        }
    };

    private void showCommentEdit() {
        KeyboardDialog keyboardDialog = new KeyboardDialog(getContext());
        keyboardDialog.setTextLength(50);
        keyboardDialog.setOnSendClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboardDialog.cancel();
                String text = keyboardDialog.getText();
                if (TextUtils.isEmpty(text))
                    return;
                sendComment(text);
            }
        });
        keyboardDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mSendText = keyboardDialog.getText();
                isCompleteCallback = true;
            }
        });
        keyboardDialog.setContentStr(mSendText);
        keyboardDialog.show();
        isCompleteCallback = false;
    }

    private void showComments() {
        if (mData == null)
            return;
        Map<String, String> commentMap = new HashMap<>();
        commentMap.put("from", "2");
        commentMap.put("type", "2");
        commentMap.put("code", mData.getCode());
        commentMap.put("commentNum", mData.getCommentNum());
        if (mCommentDialog == null) {
            mCommentDialog = new CommentDialog(XHActivityManager.getInstance().getCurrentActivity(), commentMap);
            mCommentDialog.setCommentOptionSuccCallback(new CommentDialog.CommentOptionSuccCallback() {
                @Override
                public void onSendSucc() {
                    if (mData == null)
                        return;
                    int commentNum = Tools.parseIntOfThrow(mData.getCommentNum());
                    if (commentNum >= 0) {
                        commentNum++;
                        innerUpdateCommentNum(commentNum);
                    }
                }

                @Override
                public void onDelSucc() {
                    if (mData == null)
                        return;
                    int commentNum = Tools.parseIntOfThrow(mData.getCommentNum());
                    if (commentNum >= 0) {
                        commentNum--;
                        innerUpdateCommentNum(commentNum);
                    }
                }
            });
            mCommentDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mCommentDialog = null;
                    isCompleteCallback = true;
                }
            });
            mCommentDialog.setOnCommentTextUpdateListener(new CommentDialog.OnCommentTextUpdateListener() {
                @Override
                public void onCommentTextUpdate(String newText) {
                    mSendText = newText;
                }
            });
        }
        if (mCommentDialog.isShowing())
            return;
        mCommentDialog.setCommentText(mSendText);
        mCommentDialog.show();
        isCompleteCallback = false;
    }

    private void closeActivity() {
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    private void doGood() {
        if (checkLoginAndHandle()) {
            return;
        }
        if (mGoodLoading.get() || mData.isLike())
            return;
        mGoodLoading.set(true);
        String params = "code=" + mData.getCode();
        ReqEncyptInternet.in().doEncypt(StringManager.api_likeVideo, params, new InternetCallback() {
            @Override
            public void loaded(int flag, String s, Object o) {
                mGoodLoading.set(false);
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    mGoodImg.setSelected(true);
                    mData.setLike(true);
                    GlobalGoodModule goodModule = new GlobalGoodModule();
                    goodModule.setGoodCode(mData.getCode());
                    goodModule.setGood(true);
                    try {
                        int goodNum = Integer.parseInt(mData.getLikeNum()) + 1;
                        mData.setLikeNum(String.valueOf(goodNum));
                        mGoodText.setText(mData.getLikeNum());
                        goodModule.setGoodNum(String.valueOf(goodNum));
                    } catch (Exception e) {
                        e.printStackTrace();
                        goodModule.setGoodNum(mData.getLikeNum());
                    }
                    GlobalVariableConfig.handleGoodModule(goodModule);
                    if (mGoodResultCallback != null) {
                        mGoodResultCallback.onResult(true);
                    }
                }
            }
        });
    }

    private void doShare() {
        Intent intent = new Intent(context, ShareActivityDialog.class);
        intent.putExtra("tongjiId", ShortVideoDetailActivity.STA_ID);
        intent.putExtra("shareTwoContent", "分享框");
        intent.putExtra("isHasReport", !mIsSelf);
        intent.putExtra("nickName", mData.getCustomerModel().getNickName());
        intent.putExtra("code", mData.getCustomerModel().getUserCode());
        intent.putExtra("shareFrom", "shortVideo");
        intent.putExtra("imgUrl", mData.getShareModule().getImg());
        intent.putExtra("title", mData.getShareModule().getTitle());
        intent.putExtra("content", mData.getShareModule().getContent());
        intent.putExtra("extraParams", mData.getCode());
        intent.putExtra("new_report", true);
        intent.putExtra("new_type", "2");
        intent.putExtra("new_reportContent", mData.getName());
        intent.putExtra("new_reportType", "1");
        String clickUrl = mData.getShareModule().getUrl();
        intent.putExtra("clickUrl", clickUrl);
        context.startActivity(intent);
    }

    private void handlePlay() {
        switch (mPlayerView.getCurrentState()) {
            case GSYVideoPlayer.CURRENT_STATE_PLAYING:
                pauseVideo();
                break;
            case GSYVideoPlayer.CURRENT_STATE_ERROR:
                Toast.makeText(getContext(), "视频播放错误", Toast.LENGTH_SHORT).show();
                break;
            case GSYVideoPlayer.CURRENT_STATE_AUTO_COMPLETE:
                prepareAsync();
                break;
            case GSYVideoPlayer.CURRENT_STATE_PAUSE:
                resumeVideo();
                break;
            case GSYVideoPlayer.CURRENT_STATE_PLAYING_BUFFERING_START:
                Toast.makeText(getContext(), "正在缓冲", Toast.LENGTH_SHORT).show();
                break;
            case GSYVideoPlayer.CURRENT_STATE_PREPAREING:
                Toast.makeText(getContext(), "正在加载中", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void changeThumbImageState(boolean visible) {
        mThumbContainer.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    private boolean thumbImageStateVisible() {
        return mThumbContainer.getVisibility() == View.VISIBLE;
    }

    private void showBottomDialog() {
        BottomDialog dialog = new BottomDialog(getContext());
        dialog.addButton("删除", v -> {
            dialog.dismiss();
            openDeleteDialog();
        });
        dialog.show();
    }

    private void openDeleteDialog() {
        final DialogManager dialogManager = new DialogManager(getContext());
        dialogManager.createDialog(new ViewManager(dialogManager)
                .setView(new TitleMessageView(getContext()).setText("确定删除这个视频吗？"))
                .setView(new HButtonView(getContext()).setNegativeTextColor(Color.parseColor("#333333"))
                        .setNegativeText("取消", v -> dialogManager.cancel())
                        .setPositiveTextColor(Color.parseColor("#333333"))
                        .setPositiveText("确定", v -> {
                            dialogManager.cancel();
                            delete(mData.getCode());
                        }))).show();
    }

    private void delete(String code) {
        if (mDelLoading.get())
            return;
        mDelLoading.set(true);
        ReqEncyptInternet.in().doEncypt(StringManager.api_videoDel, "code=" + code,
                new InternetCallback() {
                    @Override
                    public void loaded(int flag, String url, Object obj) {
                        mDelLoading.set(false);
                        if (flag >= ReqEncyptInternet.REQ_OK_STRING) {
                            if (FriendHome.isAlive) {
                                Intent broadIntent = new Intent();
                                broadIntent.setAction(UploadStateChangeBroadcasterReceiver.ACTION);
                                broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.DATA_TYPE, "1");
                                broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.ACTION_DEL, "2");
                                Main.allMain.sendBroadcast(broadIntent);
                            }
                            if (mOnDeleteCallback != null) {
                                mOnDeleteCallback.onDelete(mData, position);
                            }
                        }
                    }
                });
    }

    private void attention() {
        if (checkLoginAndHandle()) {
            return;
        }
        if (mAttentionLoading.get())
            return;
        mAttentionLoading.set(true);
        AppCommon.onAttentionClick(mData.getCustomerModel().getUserCode(), "follow", () -> {
            mAttentionLoading.set(false);
            mAttentionImage.setVisibility(View.GONE);

            GlobalAttentionModule module = new GlobalAttentionModule();
            module.setAttentionUserCode(mData.getCustomerModel().getUserCode());
            module.setAttention(true);
            GlobalVariableConfig.handleAttentionModule(module);
            if (mAttentionResultCallback != null) {
                mAttentionResultCallback.onResult(true);
            }
        });
    }

    public void gotoUser() {
        AppCommon.openUrl(mData.getCustomerModel().getGotoUrl(), true);
    }

    private void loadUserHeader(String url) {
        mHeaderImg.setTag(TAG_ID, url);
        BitmapRequestBuilder<GlideUrl, Bitmap> requestBuilder = LoadImage.with(context).load(url)
                .setImageRound(getResources().getDimensionPixelSize(R.dimen.dp_30))
                .setPlaceholderId(R.drawable.bg_round_user_icon)
                .setErrorId(R.drawable.bg_round_user_icon)
                .setSaveType(FileManager.save_cache)
                .build();
        if (requestBuilder != null)
            requestBuilder.into(new SubBitmapTarget() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                    mHeaderImg.post(() -> {
                        if (!mHeaderImg.getTag(TAG_ID).equals(url))
                            return;
                        mHeaderImg.setImageBitmap(bitmap);
                    });
                }
            });
    }

    private boolean checkLoginAndHandle() {
        if (LoginManager.isLogin())
            return false;
        Intent intent = new Intent(context, LoginByAccout.class);
        context.startActivity(intent);
        return true;
    }

    public void setOnPlayPauseListener(OnPlayPauseClickListener onPlayPauseListener) {
        mOnPlayPauseListener = onPlayPauseListener;
    }

    public void setOnSeekBarTrackingTouchListener(OnSeekBarTrackingTouchListener onSeekBarTrackingTouchListener) {
        mOnSeekBarTrackingTouchListener = onSeekBarTrackingTouchListener;
    }

    /**
     * 发评论
     */
    private void sendComment(String content) {
        if (!LoginManager.isLogin()) {
            getContext().startActivity(new Intent(getContext(), LoginByAccout.class));
            return;
        }
        if (!StringManager.isHasChar(content)) {
            return;
        }

        if (content.length() > FIXED_TEXT_COUNT) {
            Tools.showToast(getContext(), String.format("最多%1d字", FIXED_TEXT_COUNT));
            return;
        }

        ArrayList<Map<String, String>> contentArray = new ArrayList<>();
        Map<String, String> contentMap = new HashMap<>();
        contentMap.put("text", content);
        contentMap.put("imgs", "[]");
        contentArray.add(contentMap);
        String contentParams = Tools.list2JsonArray(contentArray).toString();

        StringBuilder sbuild = new StringBuilder();
        sbuild.append("type=").append("2").append("&")
                .append("code=").append(mData.getCode()).append("&")
                .append("content=").append(Uri.encode(contentParams));
        ReqEncyptInternet.in().doEncypt(StringManager.api_addForum, sbuild.toString(),
                new InternetCallback() {
                    @Override
                    public void loaded(int flag, String url, Object obj) {
                        if (flag >= ReqEncyptInternet.REQ_OK_STRING) {
                            mSendText = "";
                            int commentNum = Tools.parseIntOfThrow(mData.getCommentNum());
                            if (commentNum >= 0) {
                                commentNum++;
                                innerUpdateCommentNum(commentNum);
                            }
                        } else {
                            Tools.showToast(getContext(), "评论失败，请重试");
                        }
                    }
                });
    }

    private void innerUpdateCommentNum(int commentNum) {
        mData.setCommentNum(String.valueOf(commentNum));
        if (mCommentNumText != null) {
            mCommentNumText.setText(mData.getCommentNum());
        }
        innerUpdateCommentNumData();
    }

    private void innerUpdateCommentNumData() {
        GlobalCommentModule module = new GlobalCommentModule();
        module.setFlagCode(mData.getCode());
        module.setCommentNum(mData.getCommentNum());
        GlobalVariableConfig.handleCommentModule(module);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (mOnSeekBarTrackingTouchListener != null)
            mOnSeekBarTrackingTouchListener.onStartTrackingTouch(seekBar.getProgress());
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mOnSeekBarTrackingTouchListener != null)
            mOnSeekBarTrackingTouchListener.onStopTrackingTouch(seekBar.getProgress());
    }

    public interface OnPlayPauseClickListener {
        void onClick(boolean isPlay);
    }

    public interface OnSeekBarTrackingTouchListener {
        void onStartTrackingTouch(int position);

        void onStopTrackingTouch(int position);
    }

    public final String getString(@StringRes int resId) {
        return getResources().getString(resId);
    }

    private void startStatistics(String url) {
        ReqEncyptInternet.in().doGetEncypt(url, "statJson=" + mData.getStatJson(), new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                super.loaded(i, s, o);
            }
        });
    }

    public void updateShareNum() {
        mShareNum.setText(mData.getShareNum());
    }

    public void updateAttentionState() {
        if (!mIsSelf) {
            mAttentionImage.setVisibility(mData.getCustomerModel().isFollow() ? View.GONE : View.VISIBLE);
        }
    }

    public void updateLikeState() {
        mGoodImg.setSelected(mData.isLike());
    }

    public void updateLikeNum() {
        mGoodText.setText(mData.getLikeNum());
    }

    public void updateFavoriteState() {
        mLikeImg.setSelected(mData.isFav());
    }

    public void updateCommentNum() {
        mCommentNumText.setText(mData.getCommentNum());
    }

    public void updateActivityType() {
        setActivityIcon();
    }

    public interface AttentionResultCallback {
        void onResult(boolean success);
    }

    private AttentionResultCallback mAttentionResultCallback;

    public void setAttentionResultCallback(AttentionResultCallback attentionResultCallback) {
        mAttentionResultCallback = attentionResultCallback;
    }

    public interface GoodResultCallback {
        void onResult(boolean success);
    }

    private GoodResultCallback mGoodResultCallback;

    public void setGoodResultCallback(GoodResultCallback goodResultCallback) {
        mGoodResultCallback = goodResultCallback;
    }

    public interface OnDeleteCallback {
        void onDelete(ShortVideoDetailModule module, int position);
    }

    private OnDeleteCallback mOnDeleteCallback;

    public void setOnDeleteCallback(OnDeleteCallback deleteCallback) {
        mOnDeleteCallback = deleteCallback;
    }

    public RvVericalVideoItemAdapter.PlayCompleteCallBack playCompleteCallBack;

    public void setPlayCompleteCallBack(RvVericalVideoItemAdapter.PlayCompleteCallBack completeCallBack) {
        this.playCompleteCallBack = completeCallBack;
    }

    boolean isRequesting = false;
    private void todayWatchVideo(){
        if(isRequesting || !LoginManager.isLogin()){
            return;
        }
        isRequesting = true;
        final String key = "watchVideo_" + LoginManager.userInfo.get("code");
        String dateStr = (String) FileManager.loadShared(getContext(),FileManager.xmlFile_task,key);
        String todayStr = Tools.getAssignTime("yyyyMMdd",0);
        boolean todayOnce = !TextUtils.equals(dateStr,todayStr);
        if(todayOnce){
            ReqEncyptInternet.in().doEncypt(StringManager.api_addTask,"channel=taskSeeShortVideo&type_id="+mData.getCode(),
                    new DefaultInternetCallback(){
                        @Override
                        public void loaded(int i, String s, Object o) {
                            super.loaded(i, s, o);
                            if(i>=ReqEncyptInternet.REQ_OK_STRING){
                                FileManager.saveShared(getContext(),FileManager.xmlFile_task,key,todayStr);
                            }
                        }
                    });
        }
    }
}
//1062