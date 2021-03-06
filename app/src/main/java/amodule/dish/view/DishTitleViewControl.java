package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangha.R;

import java.util.HashMap;
import java.util.Map;

import acore.logic.FavoriteHelper;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.logic.load.LoadManager;
import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.PopWindowDialog;
import amodule._common.conf.FavoriteTypeEnum;
import amodule._common.conf.GlobalFavoriteModule;
import amodule._common.conf.GlobalVariableConfig;
import amodule.dish.activity.DetailDish;
import amodule.dish.activity.upload.UploadDishActivity;
import amodule.dish.tools.OffDishToFavoriteControl;
import amodule.main.Main;
import amodule.user.activity.login.LoginByAccout;
import third.share.BarShare;
import third.share.activity.ShareActivityDialog;
import third.share.tools.ShareTools;
import third.video.VideoPlayerController;
import com.xh.windowview.BottomDialog;

/**
 * 顶部view控制
 */
public class DishTitleViewControl implements View.OnClickListener {
    private Context context;
    private RelativeLayout mShareWechat;
    private RelativeLayout mShareComments;
    private ImageView favImg, integralTip;
    private Activity detailDish;
    private String state, dishState;
    private Map<String, String> dishInfoMap;//dishInfo数据
    private VideoPlayerController mVideoPlayerController;
    private boolean loading = true;//收藏标示
    private boolean isAuthor;
    private boolean editable;

    private String code;
    private String userCode;
    private boolean isHasVideo;
    private boolean nowFav;
    private boolean showIntegralTip;
    private PopWindowDialog mFavePopWindowDialog;
    private LoadManager loadManager;
    private String nickName = "";

    private String mShareStr;

    public DishTitleViewControl(Context context) {
        this.context = context;
    }

    public void initView(Activity detailDish) {
        this.detailDish = detailDish;
        //处理标题
        detailDish.findViewById(R.id.back).setOnClickListener(this);
        detailDish.findViewById(R.id.fav_layout).setOnClickListener(this);
        detailDish.findViewById(R.id.more_layout).setOnClickListener(this);
        detailDish.findViewById(R.id.fav_layout).setVisibility(View.VISIBLE);
        detailDish.findViewById(R.id.leftClose).setVisibility(View.GONE);
        integralTip = (ImageView) detailDish.findViewById(R.id.comments_tip);
        favImg = (ImageView) detailDish.findViewById(R.id.img_fav);
        favImg.setVisibility(View.VISIBLE);
        favImg.setImageResource(R.drawable.z_caipu_xiangqing_topbar_ico_fav);

        mShareWechat = (RelativeLayout) detailDish.findViewById(R.id.share_wechat);
        mShareComments = (RelativeLayout) detailDish.findViewById(R.id.share_wechatcomments);
        mShareWechat.setOnClickListener(this);
        mShareComments.setOnClickListener(this);
    }

    /**
     * 设置数据
     *
     * @param dishInfoMaps-----数据集合
     * @param code------code菜谱
     * @param isHasVideo----是否是视频贴
     */
    public void setData(Map<String, String> dishInfoMaps, String code, boolean isHasVideo, String dishState, LoadManager loadManager) {
        this.dishInfoMap = dishInfoMaps;
        this.code = code;
        this.isHasVideo = isHasVideo;
        this.dishState = dishState;
        this.loadManager = loadManager;
        if (dishInfoMaps == null)
            return;
        Map<String, String> customerMap = StringManager.getFirstMap(dishInfoMap.get("customer"));
        userCode = customerMap.get("customerCode");
        //登录并是自己的菜谱贴
        if (LoginManager.isLogin() && !TextUtils.isEmpty(userCode) && userCode.equals(LoginManager.userInfo.get("code"))) {
            isAuthor = true;
        }
        requestFavoriteState();
    }

    public void setShareData(String shareData) {
        mShareStr = shareData;
    }

    public PopWindowDialog getPopWindowDialog() {
        return mFavePopWindowDialog;
    }

    /**
     * 设置当前状态
     *
     * @param states
     */
    public void setstate(String states) {
        this.state = states;
    }

    /**
     * 初始化当前状态
     */
    public void setViewState() {
        detailDish.findViewById(R.id.fav_layout).setVisibility(View.VISIBLE);//state != null ? View.GONE :
        //编辑
        if (state != null) {
            if (isHasVideo && ("6".equals(dishState) || TextUtils.isEmpty(dishState))) { //视频菜谱，并且审核通过了，则不允许编辑
                editable = false;
            } else {
                editable = true;
            }
        } else {
            editable = false;
        }

    }

    public void setFavStatus(boolean isFav) {
        this.nowFav = isFav;
        favImg.setImageResource(isFav ? R.drawable.z_caipu_xiangqing_topbar_ico_fav_active : R.drawable.z_caipu_xiangqing_topbar_ico_fav);
    }

    public void setIntegralTipStatus(boolean show) {
        showIntegralTip = show;
        integralTip.setVisibility(showIntegralTip ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share_wechat:
                openShareSingle(ShareTools.WEI_XIN);
                XHClick.mapStat(detailDish, DetailDish.tongjiId_detail, "顶部导航栏", "微信分享点击");
                break;
            case R.id.share_wechatcomments:
                openShareSingle(ShareTools.WEI_QUAN);
                XHClick.mapStat(detailDish, DetailDish.tongjiId_detail, "顶部导航栏", "朋友圈分享点击");
                break;
            case R.id.back:
                XHClick.mapStat(detailDish, DetailDish.tongjiId_detail, "顶部导航栏", "返回点击量");
                detailDish.finish();
                break;
            case R.id.leftClose:
                XHClick.mapStat(detailDish, DetailDish.tongjiId_detail, "顶部导航栏", "关闭点击量");
                Main.colse_level = 1;
                detailDish.finish();
                break;
            case R.id.fav_layout://收藏
                if (detailDish != null)
                    XHClick.track(detailDish, "收藏菜谱");
                XHClick.mapStat(detailDish, DetailDish.tongjiId_detail, "顶部导航栏", "收藏点击量");
                doFavorite();
                break;
            case R.id.more_layout: //查看更多按钮
                if (editable) {
                    BottomDialog bottomDialog = new BottomDialog(context);
                    bottomDialog.setTopButton("分享", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bottomDialog.cancel();
                            XHClick.mapStat(detailDish, DetailDish.tongjiId_detail, "顶部导航栏", "分享点击量");
                            openShare();
                        }
                    }).setBottomButton("编辑", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bottomDialog.cancel();
                            XHClick.mapStat(detailDish, DetailDish.tongjiId_detail, "顶部导航栏", "二次编辑点击量");
                            if (isHasVideo) {
                                Tools.showToast(context, "请用香哈（视频版）编辑");
                            } else doModify();
                        }
                    }).show();
                } else {
                    openShare();
                }
                break;
        }
    }

    /**
     * 修改菜谱
     */
    private void doModify() {
        if (state != null) {
            XHClick.onEventValue(detailDish.getApplicationContext(), "dishOperate", "dishOperate", "修改已发布", 1);
            Intent intent = new Intent(detailDish, UploadDishActivity.class);
            intent.putExtra("code", code);
            String dishTypeValue = UploadDishActivity.DISH_TYPE_NORMAL;
            if (isHasVideo) dishTypeValue = UploadDishActivity.DISH_TYPE_VIDEO;
            intent.putExtra(UploadDishActivity.DISH_TYPE_KEY, dishTypeValue);
            intent.putExtra("state", UploadDishActivity.UPLOAD_DISH_EDIT);
            intent.putExtra("titleName", "修改菜谱");
            detailDish.startActivity(intent);
            detailDish.finish();
        }
    }

    private void openShareSingle(String platform) {
        Map<String, String> shareMap = getShareData(isAuthor, true);
        if (shareMap == null)
            return;
        shareMap.put("platform", platform);
        shareMap.put("from", "菜谱详情页");
        if (!TextUtils.isEmpty(mShareStr))
            shareMap.put("shareParams", mShareStr);
        ShareTools tools = ShareTools.getBarShare(context);
        tools.showSharePlatform(shareMap);
    }

    private void openShare() {
        if (detailDish != null)
            XHClick.track(detailDish, "分享菜谱");
        XHClick.mapStat(detailDish, "a_share400", "菜谱", "菜谱详情页");
        XHClick.mapStat(detailDish, DetailDish.tongjiId_detail, "顶部导航栏", "分享点击量");

        if (dishInfoMap == null)
            return;
        Map<String, String> mapData = getShareData(isAuthor, false);
        if (mapData == null)
            return;

        Intent intent = new Intent(detailDish, ShareActivityDialog.class);
        intent.putExtra("tongjiId", DetailDish.tongjiId_detail);
        intent.putExtra("isHasReport", !isAuthor);
        intent.putExtra("nickName", nickName);
        intent.putExtra("code", userCode);
        intent.putExtra("type", mapData.get("mType"));
        intent.putExtra("shareFrom", "菜谱详情页");
        intent.putExtra("reportUrl", "Feedback.app?feekUrl=https://www.xiangha.com/caipu/" + code + ".html");
        intent.putExtra("imgUrl", mapData.get("mImgUrl"));
        intent.putExtra("clickUrl", mapData.get("mClickUrl"));
        intent.putExtra("title", mapData.get("mTitle"));
        intent.putExtra("content", mapData.get("mContent"));
        intent.putExtra("showIntegralTip", showIntegralTip);
        if (!TextUtils.isEmpty(mShareStr)) {
            intent.putExtra("shareParams", mShareStr);
        }
        detailDish.startActivity(intent);
    }

    public Map<String, String> getShareData(boolean isAuthor, boolean shareSingle) {
        if (dishInfoMap == null)
            return null;
        //点击显示数据
        String mType, mTitle, mClickUrl, mContent, mImgUrl, isVideo;
        //登录并是自己的菜谱贴
        if (isAuthor) {
            mTitle = "【香哈菜谱】我上传了" + dishInfoMap.get("name") + "的做法";
            mClickUrl = StringManager.wwwUrl + "caipu/" + code + ".html";
            mContent = "我在香哈做出了史上最好吃的" + dishInfoMap.get("name") + "，进来请你免费享用！";
            mImgUrl = dishInfoMap.get("img");
            mType = BarShare.IMG_TYPE_WEB;
            isVideo = "1";
            //不是自己的菜谱贴
        } else if (isHasVideo) {
            mTitle = "【香哈菜谱】看了" + dishInfoMap.get("name") + "的教学视频，我已经学会了，味道超赞！";
            mClickUrl = StringManager.wwwUrl + "caipu/" + code + ".html";
            mContent = "顶级大厨的做菜视频，讲的真是太详细啦！想吃就赶快进来免费学习吧~ ";
            mImgUrl = dishInfoMap.get("img");
            mType = BarShare.IMG_TYPE_WEB;
            isVideo = "2";
        } else {
            mTitle = "【香哈菜谱】" + dishInfoMap.get("name") + "的做法";
            mClickUrl = StringManager.wwwUrl + "caipu/" + code + ".html";
            mContent = "我又学会了一道" + dishInfoMap.get("name") + "，太棒了，强烈推荐你也用香哈学做菜！";
            mImgUrl = dishInfoMap.get("img");
            mType = BarShare.IMG_TYPE_WEB;
            isVideo = "1";
        }
        Map<String, String> map = new HashMap<>();
        map.put(shareSingle ? "type" : "mType", mType);
        map.put(shareSingle ? "title" : "mTitle", mTitle);
        map.put(shareSingle ? "url" : "mClickUrl", mClickUrl);
        map.put(shareSingle ? "content" : "mContent", mContent);
        map.put(shareSingle ? "img" : "mImgUrl", mImgUrl);
        map.put("isVideo", isVideo);
        return map;
    }

    public void setNickName(String name) {
        this.nickName = name;
    }

    /**
     * 收藏
     */
    private void doFavorite() {
        if (context != null && !ToolsDevice.isNetworkAvailable(context)) {
            Toast.makeText(context, "网络异常，请检查网络", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(code) || dishInfoMap == null || TextUtils.isEmpty(dishInfoMap.get("name")))
            return;
        if (LoginManager.userInfo.size() > 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (loading && context != null) loadManager.startProgress("仍在进行");
                }
            }, 1000);
            FavoriteTypeEnum type = isHasVideo ? FavoriteTypeEnum.TYPE_DISH_VIDEO : FavoriteTypeEnum.TYPE_DISH_ImageNText;
            FavoriteHelper.instance().setFavoriteStatus(detailDish.getApplicationContext(), code, dishInfoMap.get("name"), type, new FavoriteHelper.FavoriteStatusCallback() {
                        @Override
                        public void onSuccess(boolean state) {
                            loading = false;
                            loadManager.dismissProgress();
                            nowFav = state;
                            favImg.setImageResource(nowFav ? R.drawable.z_caipu_xiangqing_topbar_ico_fav_active : R.drawable.z_caipu_xiangqing_topbar_ico_fav);

                            //统计
                            XHClick.onEvent(detailDish.getApplicationContext(), "dishFav", nowFav ? "收藏" : "取消");
                            XHClick.mapStat(detailDish, DetailDish.tongjiId_detail, "顶部导航栏", "收藏点击量");
                            dishInfoMap.put("favNum", nowFav ? "2" : "1");
                            if (nowFav) {
                                boolean isShow = PopWindowDialog.isShowPop(FileManager.xmlKey_shareShowPopDataFavDish, FileManager.xmlKey_shareShowPopNumFavDish);
                                if (isShow) {
                                    boolean isAutoOff = OffDishToFavoriteControl.getIsAutoOffDish(detailDish.getApplicationContext());
                                    mFavePopWindowDialog = new PopWindowDialog(XHActivityManager.getInstance().getCurrentActivity(), "收藏成功", "这道菜已经被多人分享过，分享给好友？",
                                            isAutoOff ? "已离线到本地,可在设置-收藏菜谱关闭。" : null, true);

                                    Map<String, String> shareMap = getShareData(isAuthor, true);
                                    if (shareMap == null)
                                        return;
                                    shareMap.put("from", "菜谱收藏成功后");
                                    shareMap.put("parent", "强化分享");
                                    if (!TextUtils.isEmpty(mShareStr))
                                        shareMap.put("shareParams", mShareStr);
                                    mFavePopWindowDialog.show(shareMap);
                                    XHClick.mapStat(XHApplication.in(), "a_share400", "强化分享", "菜谱收藏成功后");
                                }
                            }
                            GlobalFavoriteModule module = new GlobalFavoriteModule();
                            module.setFav(nowFav);
                            module.setFavCode(code);
                            module.setFavType(type);
                            GlobalVariableConfig.handleFavoriteModule(module);
                        }

                        @Override
                        public void onFailed() {
                            loading = false;
                            loadManager.dismissProgress();
                        }
                    });
        } else {
            Intent intent = new Intent(detailDish, LoginByAccout.class);
            detailDish.startActivity(intent);
        }
    }

    private void requestFavoriteState() {
        FavoriteHelper.instance().getFavoriteStatus(context, code,
                isHasVideo ? FavoriteTypeEnum.TYPE_DISH_VIDEO : FavoriteTypeEnum.TYPE_DISH_ImageNText,
                new FavoriteHelper.FavoriteStatusCallback() {
                    @Override
                    public void onSuccess(boolean state) {
                        //处理收藏状态
                        setFavStatus(state);
                    }

                    @Override
                    public void onFailed() {
                        setFavStatus(false);
                    }
                });
    }

    /**
     * 页面销毁时调用
     */
    public void onDestroy() {
        if (mVideoPlayerController != null) {
            mVideoPlayerController = null;
        }
        if (mFavePopWindowDialog != null) {
            mFavePopWindowDialog = null;
        }
        if (loadManager != null) {
            loadManager.dismissProgress();
        }
    }
}

