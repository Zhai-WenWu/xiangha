package amodule.dish.activity;

import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import acore.broadcast.ConnectionChangeReceiver;
import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.FileManager;
import acore.observer.IObserver;
import acore.observer.ObserverManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule._common.conf.FavoriteTypeEnum;
import amodule._common.conf.GlobalAttentionModule;
import amodule._common.conf.GlobalCommentModule;
import amodule._common.conf.GlobalFavoriteModule;
import amodule._common.conf.GlobalGoodModule;
import amodule._common.conf.GlobalVariableConfig;
import amodule.dish.adapter.RvVericalVideoItemAdapter;
import amodule.dish.helper.ParticularPositionEnableSnapHelper;
import amodule.dish.video.module.ShareModule;
import amodule.dish.video.module.ShortVideoDetailADModule;
import amodule.dish.video.module.ShortVideoDetailModule;
import amodule.topic.model.AddressModel;
import amodule.topic.model.CustomerModel;
import amodule.topic.model.ImageModel;
import amodule.topic.model.TopicModel;
import amodule.topic.model.VideoModel;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.tools.AdPlayIdConfig;

import static acore.observer.ObserverManager.NOTIFY_LOGIN;
import static acore.observer.ObserverManager.NOTIFY_SHARE;

public class ShortVideoDetailActivity extends BaseAppCompatActivity implements IObserver {

    public static final String STA_ID = "a_video_details";
    public static final String[] AD_IDS = new String[]{
            AdPlayIdConfig.VIDEO_LIST_1,
            AdPlayIdConfig.VIDEO_LIST_2
    };

    private final int UP_SCROLL = 1;
    private final int DOWN_SCROLL = 2;

    private ConstraintLayout mGuidanceLayout;

    private RecyclerView recyclerView;
    private RvVericalVideoItemAdapter mAdapter;

    private DataController mDataController;
    private boolean mFirstPlayStarted;
    private boolean mResumeFromPause;
    private XHAllAdControl mXHAllAdControl;
    private List<ShortVideoDetailADModule> mAdData = new ArrayList<>();

    private String mUserCode;
    private String mSourcePage;
    private String topicCode;
    private ArrayList<ShortVideoDetailModule> mDatas = new ArrayList<>();

    private ConnectionChangeReceiver mReceiver;
    private DialogManager mNetStateTipDialog;
    private ShortVideoDetailModule mExtraModule;

    private int mMoveXRang;
    private float mDownX = -1;
    private boolean mCanStatic = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFormat(PixelFormat.TRANSPARENT);
        initAd();
        setContentView(R.layout.layout_shortvideo_detail_activity);
        level = 2;
        if ("null".equals(ToolsDevice.getNetWorkSimpleType(XHApplication.in()))) {
            Tools.showToast(this, "网络异常，请检查网络");
            finish();
            return;
        }
        init();
        addListener();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String code = bundle.getString("code");
            if (TextUtils.isEmpty(code)) {
                finish();
                return;
            }
            String json = bundle.getString("extraJson");
            if (!TextUtils.isEmpty(json)) {
                json = Uri.decode(json);
                Map<String, String> extraData = StringManager.getFirstMap(json);
                mExtraModule = mDataController.getModuleByMap(extraData);
                if (mExtraModule != null) {
                    mDatas.add(mExtraModule);
                    mAdapter.notifyItemRangeInserted(0, mDatas.size());
                }
            }
            mUserCode = bundle.getString("userCode");
            mSourcePage = bundle.getString("sourcePage");
            topicCode = bundle.getString("topicCode");
            mDataController.start(code);
        }
        ObserverManager.getInstance().registerObserver(this, NOTIFY_SHARE, NOTIFY_LOGIN);
        registerConnectionReceiver();
    }


    private void addListener() {
        mGuidanceLayout.setOnClickListener(v -> mGuidanceLayout.setVisibility(View.GONE));
        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                int pos = recyclerView.getChildAdapterPosition(view);
                if (pos == 0 && !mFirstPlayStarted) {
                    mFirstPlayStarted = true;
                    RvVericalVideoItemAdapter.ItemViewHolder viewHolder = (RvVericalVideoItemAdapter.ItemViewHolder) recyclerView.getChildViewHolder(view);
                    mAdapter.setCurrentViewHolder(viewHolder);
                    String netState = ToolsDevice.getNetWorkSimpleType(XHApplication.in());
                    switch (netState) {
                        case "wifi":
                            viewHolder.startVideo();
                            break;
                        case "null":
                            Tools.showToast(ShortVideoDetailActivity.this, "加载失败，请重试");
                            break;
                        default:
                            if (canShowTipDialog()) {
                                showNetworkTip();
                            } else {
                                viewHolder.startVideo();
                            }
                            break;
                    }
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {

            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int pos = llm.findLastCompletelyVisibleItemPosition();
                    RvVericalVideoItemAdapter.ItemViewHolder currentHolder = (RvVericalVideoItemAdapter.ItemViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
                    RvVericalVideoItemAdapter.ItemViewHolder adapterLastHolder = mAdapter.getCurrentViewHolder();
                    if (currentHolder == adapterLastHolder || adapterLastHolder == null || currentHolder == null)
                        return;
                    mAdapter.setCurrentViewHolder(currentHolder);
                    currentHolder.startVideo();
                    int orientationScroll = 0;
                    if (currentHolder.getAdapterPosition() > adapterLastHolder.getAdapterPosition()) {
                        orientationScroll = DOWN_SCROLL;
                    } else if (currentHolder.getAdapterPosition() < adapterLastHolder.getAdapterPosition()) {
                        orientationScroll = UP_SCROLL;
                    }
                    //处理请求下一页
                    if (currentHolder.getAdapterPosition() >= mDatas.size() - 1) {
                        mDataController.executeNextOption();
                    }
                    if (orientationScroll != 0)
                        XHClick.mapStat(ShortVideoDetailActivity.this, STA_ID, "滑动", orientationScroll == DOWN_SCROLL ? "上滑" : "下滑");
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        mAdapter.setAttentionResultCallback(success -> {
            if (success) {
                Iterator<ShortVideoDetailModule> dataIterator = mDatas.iterator();
                while (dataIterator.hasNext()) {
                    ShortVideoDetailModule dataModule = dataIterator.next();
                    GlobalAttentionModule attentionModule = GlobalVariableConfig.containsAttentionModule(dataModule.getCustomerModel().getUserCode());
                    if (attentionModule != null && (attentionModule.isAttention() != dataModule.getCustomerModel().isFollow())) {
                        dataModule.getCustomerModel().setFollow(attentionModule.isAttention());
                    }
                }
            }
        });
        mAdapter.setGoodResultCallback(success -> {
            if (success) {
                Iterator<ShortVideoDetailModule> dataIterator = mDatas.iterator();
                while (dataIterator.hasNext()) {
                    ShortVideoDetailModule dataModule = dataIterator.next();
                    GlobalGoodModule goodModule = GlobalVariableConfig.containsGoodModule(dataModule.getLikeNum());
                    if (goodModule != null && (goodModule.isGood() != dataModule.isLike())) {
                        dataModule.setLike(goodModule.isGood());
                        dataModule.setLikeNum(goodModule.getGoodNum());
                    }
                }
            }
        });
        mAdapter.setOnDeleteCallback((module, position) -> {
            if (mDatas != null) {
                mAdapter.getCurrentViewHolder().stopVideo();
                ShortVideoDetailActivity.this.finish();
            }
        });
        mAdapter.setPlayCompleteCallBack(new RvVericalVideoItemAdapter.PlayCompleteCallBack() {
            @Override
            public void videoComplete(int position,boolean hasDialogShow) {
                if (position >= 0 && position + 1 < mDatas.size()) {
                    String playMode = mDatas.get(position).getPlayMode();
                    if ("2".equals(playMode) && "wifi".equals(ToolsDevice.getNetWorkType(ShortVideoDetailActivity.this))
                            && hasDialogShow) {
                        recyclerView.smoothScrollToPosition(position + 1);
                    }else if("1".equals(playMode)||"2".equals(playMode) && !"wifi".equals(ToolsDevice.getNetWorkType(ShortVideoDetailActivity.this))){
                        RvVericalVideoItemAdapter.ItemViewHolder currentHolder = (RvVericalVideoItemAdapter.ItemViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                        if(currentHolder!=null){
                            currentHolder.startVideo();
                        }
                    }
                }
            }
        });
        mAdapter.setOnADShowCallback((index, view, listIndex) -> mXHAllAdControl.onAdBind(index, view, listIndex));
        mAdapter.setOnADClickCallback((view, index, listIndex) -> mXHAllAdControl.onAdClick(view, index, listIndex));
        mAdapter.setOnAdHintClickListener((indexInData, promotionIndex) -> AppCommon.onAdHintClick(ShortVideoDetailActivity.this, mXHAllAdControl, indexInData, promotionIndex, STA_ID, "第" + promotionIndex + "位广告按钮"));
    }

    private void init() {
        recyclerView = findViewById(R.id.recyclerView);
        ParticularPositionEnableSnapHelper pagerSnapHelper = new ParticularPositionEnableSnapHelper();
        pagerSnapHelper.attachToRecyclerView(recyclerView);
        mGuidanceLayout = findViewById(R.id.guidance_layout);
        mAdapter = new RvVericalVideoItemAdapter(this, mDatas);
        mAdapter.setADData(mAdData);
        recyclerView.setItemViewCacheSize(4);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
        mDataController = new DataController();

        mMoveXRang = Tools.getPhoneWidth() / 5;
    }

    private void registerConnectionReceiver() {
        mReceiver = new ConnectionChangeReceiver(new ConnectionChangeReceiver.ConnectionChangeListener() {
            @Override
            public void disconnect() {
                Tools.showToast(ShortVideoDetailActivity.this, "加载失败，请重试");
            }

            @Override
            public void wifi() {
                if (mNetStateTipDialog != null && mNetStateTipDialog.isShowing()) {
                    mNetStateTipDialog.cancel();
                    playCurrent();
                }
            }

            @Override
            public void mobile() {
                if (canShowTipDialog()) {
                    showNetworkTip();
                }
            }
        });
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
    }

    public void gotoUser() {
        if (mAdapter != null) {
            mAdapter.notifyGotoUser();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkShowGuidance();
        mAdapter.onResume();
        if (mResumeFromPause) {
            mResumeFromPause = false;
            Iterator<ShortVideoDetailModule> dataIterator = mDatas.iterator();
            while (dataIterator.hasNext()) {
                ShortVideoDetailModule dataModule = dataIterator.next();
                GlobalFavoriteModule favModule = GlobalVariableConfig.containsFavoriteModule(dataModule.getCode(), FavoriteTypeEnum.TYPE_VIDEO);
                boolean favChanged = false;
                boolean attentionChanged = false;
                boolean likeChanged = false;
                boolean commentChanged = false;
                if (favModule != null && (favModule.isFav() != dataModule.isFav())) {
                    favChanged = true;
                    dataModule.setFav(favModule.isFav());
                }
                GlobalAttentionModule attentionModule = GlobalVariableConfig.containsAttentionModule(dataModule.getCustomerModel().getUserCode());
                if (attentionModule != null && (attentionModule.isAttention() != dataModule.getCustomerModel().isFollow())) {
                    attentionChanged = true;
                    dataModule.getCustomerModel().setFollow(attentionModule.isAttention());
                }
                GlobalGoodModule goodModule = GlobalVariableConfig.containsGoodModule(dataModule.getCode());
                if (goodModule != null && (goodModule.isGood() != dataModule.isLike() || !TextUtils.equals(goodModule.getGoodNum(), dataModule.getLikeNum()))) {
                    likeChanged = true;
                    dataModule.setLike(goodModule.isGood());
                    dataModule.setLikeNum(goodModule.getGoodNum());
                }
                GlobalCommentModule commentModule = GlobalVariableConfig.containsCommentModule(dataModule.getCode());
                if (commentModule != null && !TextUtils.equals(dataModule.getCommentNum(), commentModule.getCommentNum())) {
                    commentChanged = true;
                    dataModule.setCommentNum(commentModule.getCommentNum());
                }
                if (mAdapter != null && mAdapter.getCurrentViewHolder() != null && mAdapter.getCurrentViewHolder().data != null && TextUtils.equals(mAdapter.getCurrentViewHolder().data.getCode(), dataModule.getCode())) {
                    RvVericalVideoItemAdapter.ItemViewHolder currentHolder = mAdapter.getCurrentViewHolder();
                    if (attentionChanged) {
                        currentHolder.updateAttentionState();
                    }
                    if (favChanged) {
                        currentHolder.updateFavoriteState();
                    }
                    if (likeChanged) {
                        currentHolder.updateLikeState();
                        currentHolder.updateLikeNum();
                    }
                    if (commentChanged) {
                        currentHolder.updateCommentNum();
                    }
                }
            }
        }
    }

    private void checkShowGuidance() {
        String show = (String) FileManager.loadShared(this, FileManager.xhmKey_shortVideoGuidanceShow, "show");
        if (TextUtils.equals(show, "2"))
            return;
        mGuidanceLayout.setVisibility(View.VISIBLE);
        FileManager.saveShared(this, FileManager.xhmKey_shortVideoGuidanceShow, "show", "2");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mResumeFromPause = true;
        mAdapter.onPause();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mDownX == -1) {
                    mDownX = ev.getX();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = ev.getX();
                float rawX = moveX - mDownX;
                int orientation = -1;// 1.向左滑，2.向右滑
                if (rawX >= mMoveXRang) {
                    orientation = 2;
                } else if (rawX <= -mMoveXRang) {
                    orientation = 1;
                }

                if (orientation != -1 && mCanStatic) {
                    mCanStatic = false;
                    XHClick.mapStat(this, STA_ID, "滑动", orientation == 1 ? "左滑" : "右滑");
                }
                break;
            case MotionEvent.ACTION_UP:
                mCanStatic = true;
                mDownX = -1;
                break;
        }


        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mResumeFromPause = false;
        if (mAdapter != null) {
            mAdapter.onDestroy();
        }
        ObserverManager.getInstance().unRegisterObserver(this);
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void notify(String name, Object sender, Object data) {
        if (name == null)
            return;
        switch (name) {
            case NOTIFY_SHARE:
                if (data != null) {
                    Map<String, String> dataMap = (Map<String, String>) data;
                    String videoCode = dataMap.get("callbackParams");
                    if (!TextUtils.isEmpty(videoCode)) {
                        for (int i = 0; i < mDatas.size(); i++) {
                            ShortVideoDetailModule module = mDatas.get(i);
                            if (module != null && TextUtils.equals(module.getCode(), videoCode)) {
                                try {
                                    module.setShareNum(String.valueOf(Integer.parseInt(module.getShareNum()) + 1));
                                    RvVericalVideoItemAdapter.ItemViewHolder currentHolder = mAdapter.getCurrentViewHolder();
                                    if (currentHolder != null) {
                                        ShortVideoDetailModule currentModule = mAdapter.getItem(currentHolder.getAdapterPosition());
                                        if (currentModule != null && TextUtils.equals(currentModule.getCode(), videoCode)) {
                                            currentHolder.updateShareNum();
                                            break;
                                        }
                                    }
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                }
                break;
            case NOTIFY_LOGIN:
                if (!LoginManager.isShowAd()) {
                    cleanAdData();
                }
                break;
        }
    }

    public void cleanAdData() {
        mAdData.clear();
        for (int i = 0; i < mDatas.size(); i++) {
            if (mDatas.get(i) instanceof ShortVideoDetailADModule) {
                mDatas.remove(i--);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private void initAd() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String sourcePage = bundle.getString("sourcePage");
            if (!TextUtils.equals("1", sourcePage)) {
                return;
            }
        }
        final ArrayList<String> adIdList = new ArrayList<>();
        Collections.addAll(adIdList, AD_IDS);
        int[] adPositionArr = {3, 14};
        Map<String, Integer> adPositionMap = new HashMap<>();
        for (int i = 0, length = Math.min(adPositionArr.length, adIdList.size()); i < length; i++) {
            adPositionMap.put(adIdList.get(i), adPositionArr[i]);
        }

        mXHAllAdControl = new XHAllAdControl(adIdList, (isRefresh, map) -> {
            if (map != null && !map.isEmpty()) {
                if (mAdData != null && !mAdData.isEmpty()) {
                    cleanAdData();
                }
                for (int i = 0; i < adIdList.size(); i++) {
                    String adId = adIdList.get(i);
                    if (map.containsKey(adId) && !TextUtils.isEmpty(map.get(adId)) && adPositionMap.get(adId) != null) {
                        Map<String, String> adMap = StringManager.getFirstMap(map.get(adId));
                        Log.i("tzy", "initAd: " + adMap.toString());
                        ShortVideoDetailADModule adModule = new ShortVideoDetailADModule();
                        adModule.adId = adId;
                        adModule.adPositionInData = adPositionMap.get(adId);
                        adModule.adType = adMap.get("type");
                        adModule.adRealPosition = Tools.parseIntOfThrow(adMap.get("index"));
                        //数据
                        adModule.setPlayMode("1");
                        adModule.setName(adMap.get("desc"));
                        adModule.setLikeNum(String.valueOf(Tools.getRandom(500, 1001)));
                        adModule.setShareNum(String.valueOf(Tools.getRandom(50, 201)));
                        adModule.setCommentNum(String.valueOf(Tools.getRandom(50, 151)));
                        ImageModel imageModel = new ImageModel();
                        imageModel.setImageUrl(adMap.get("imgUrl"));
                        adModule.setImageModel(imageModel);
                        CustomerModel customerModel = new CustomerModel();
                        String title = adMap.get("title");
                        if (title.length() > 8) {
                            title = title.substring(0, 8) + "...";
                        }
                        customerModel.setNickName(title);
                        customerModel.setHeaderImg(adMap.get("iconUrl"));
                        adModule.setCustomerModel(customerModel);
                        mAdData.add(adModule);
                    }
                }
                if (mAdapter != null) {
                    mAdapter.setADData(mAdData);
                }
            }
        }, this, "search_list", false);
    }

    private class DataController {
        private static final int COUNT_EACH_PAGE = 10;
        private int mNextPageStartPosition;
        private ArrayList<String> mCodes;

        private AtomicBoolean mCodesLoading;
        private AtomicBoolean mLoadCodesEnable;
        private AtomicBoolean mDetailLoading;

        public DataController() {
            mCodes = new ArrayList<>();
            mCodesLoading = new AtomicBoolean(false);
            mLoadCodesEnable = new AtomicBoolean(true);
            mDetailLoading = new AtomicBoolean(false);
        }

        public void executeNextOption() {
            ArrayList<String> nextPageCodes = getNextPageCodes();
            if (!nextPageCodes.isEmpty()) {
                loadVideoDetail(nextPageCodes, true, false);
                if (nextPageCodes.size() < COUNT_EACH_PAGE) {
                    loadVideoCodes(nextPageCodes.get(nextPageCodes.size() - 1), null, null, false);
                }
            } else {
                loadVideoCodes(mCodes.get(mNextPageStartPosition - 1), null, null, true);
            }
        }

        private ArrayList<String> getNextPageCodes() {
            int lastPosition = mCodes.size() - 1;
            ArrayList<String> ret = new ArrayList<>();
            for (int i = 0; i < COUNT_EACH_PAGE; i++) {
                int getPos = mNextPageStartPosition + i;
                if (getPos > lastPosition)
                    break;
                ret.add(mCodes.get(getPos));
            }
            return ret;
        }

        public void start(String code) {
            mCodes.add(code);
            loadVideoDetail(mCodes, false, true);
            loadVideoCodes(code, null, null, true);
        }

        /**
         * 加载详情
         *
         * @param codes
         * @param loadMore
         */
        private void loadVideoDetail(ArrayList<String> codes, boolean loadMore, boolean firstLoad) {
            if (mDetailLoading.get() || codes == null || codes.isEmpty())
                return;
            mDetailLoading.set(true);
            mNextPageStartPosition += codes.size();
            StringBuffer buffer = new StringBuffer("codes=");
            for (int i = 0; i < codes.size(); i++) {
                buffer.append(codes.get(i));
                if (i != codes.size() - 1) {
                    buffer.append(",");
                }
            }
            String params = buffer.toString();
            ReqEncyptInternet.in().doGetEncypt(StringManager.api_getVideoInfo, params, new InternetCallback() {
                @Override
                public void loaded(int flag, String s, Object o) {
                    mDetailLoading.set(false);
                    if (flag >= ReqInternet.REQ_OK_STRING) {
                        if (firstLoad && mExtraModule != null) {
                            Map<String, String> firstMap = StringManager.getFirstMap(o);
                            ShortVideoDetailModule firstLoadModule = getModuleByMap(firstMap);
                            boolean updateFavorite = false;
                            if (mExtraModule.isFav() != firstLoadModule.isFav()) {
                                updateFavorite = true;
                                mExtraModule.setFav(firstLoadModule.isFav());
                            }
                            boolean updateAttention = false;
                            if (mExtraModule.getCustomerModel().isFollow() != firstLoadModule.getCustomerModel().isFollow()) {
                                updateAttention = true;
                                mExtraModule.getCustomerModel().setFollow(firstLoadModule.getCustomerModel().isFollow());
                            }
                            boolean updateLike = false;
                            if (mExtraModule.isLike() != firstLoadModule.isLike()) {
                                updateLike = true;
                                mExtraModule.setLike(firstLoadModule.isLike());
                                mExtraModule.setLikeNum(firstLoadModule.getLikeNum());
                                GlobalGoodModule goodModule = GlobalVariableConfig.containsGoodModule(mExtraModule.getCode());
                                if (goodModule != null) {
                                    goodModule.setGoodNum(firstLoadModule.getLikeNum());
                                }
                            }
                            boolean updateComment = false;
                            if (!TextUtils.equals(mExtraModule.getCommentNum(), firstLoadModule.getCommentNum())) {
                                updateComment = true;
                                mExtraModule.setCommentNum(firstLoadModule.getCommentNum());
                            }
                            boolean updateActivityType = false;
                            if (!TextUtils.equals(mExtraModule.getTopicModel().getActivityType(), firstLoadModule.getTopicModel().getActivityType())) {
                                updateActivityType = true;
                                mExtraModule.getTopicModel().setActivityType(firstLoadModule.getTopicModel().getActivityType());
                            }
                            mExtraModule.setShareModule(firstLoadModule.getShareModule());
                            if (mAdapter != null && TextUtils.equals(mAdapter.getCurrentViewHolder().data.getCode(), mExtraModule.getCode())) {
                                RvVericalVideoItemAdapter.ItemViewHolder currentHolder = mAdapter.getCurrentViewHolder();
                                if (updateFavorite) {
                                    currentHolder.updateFavoriteState();
                                }
                                if (updateAttention) {
                                    currentHolder.updateAttentionState();
                                }
                                if (updateLike) {
                                    currentHolder.updateLikeState();
                                    currentHolder.updateLikeNum();
                                }
                                if (updateComment) {
                                    currentHolder.updateCommentNum();
                                }
                                if (updateActivityType) {
                                    currentHolder.updateActivityType();
                                }
                            }
                        } else {
                            int insertPosStart = mDatas.size();
                            ArrayList<Map<String, String>> datas = StringManager.getListMapByJson(o);
                            for (int i = 0; i < datas.size() && datas.size() > 0; i++) {
                                Map<String, String> itemMap = datas.get(i);
                                ShortVideoDetailModule module = getModuleByMap(itemMap);
                                if (module != null) {
                                    mDatas.add(module);
                                }
                            }
                            insertADData(insertPosStart);
                            if (mDatas.size() != insertPosStart) {
                                mAdapter.notifyItemRangeInserted(insertPosStart, datas.size());
                            }
                        }
                    } else {
                        if (!loadMore) {
                            // TODO: 2018/4/17 第一次的网络请求失败
                        } else {
                            // TODO: 2018/4/19 加载更多时加载失败
                        }
                    }
                }
            });
        }

        /**
         * 加载ids
         *
         * @param lastCode
         * @param successRun
         * @param failedRun
         * @param needLoadDetail
         */
        private void loadVideoCodes(String lastCode, Runnable successRun, Runnable failedRun, boolean needLoadDetail) {
            if (!mLoadCodesEnable.get() || mCodesLoading.get() || lastCode == null || lastCode.isEmpty()) {
                // TODO: 2018/8/9 正在加载中 或者 数据错误
                return;
            }
            mCodesLoading.set(true);
            StringBuffer sb = new StringBuffer();
            sb.append("code=").append(lastCode).append("&");
            sb.append("sourcePage=").append(mSourcePage).append("&");
            sb.append("userCode=").append(TextUtils.isEmpty(mUserCode) ? "" : mUserCode);
            if (!TextUtils.isEmpty(topicCode)) {
                sb.append("&").append("topicCode=").append(topicCode);
            }
            ReqEncyptInternet.in().doGetEncypt(StringManager.API_SHORT_VIDEOCODES, sb.toString(), new InternetCallback() {
                @Override
                public void loaded(int flag, String s, Object o) {
                    mCodesLoading.set(false);
                    if (flag >= ReqInternet.REQ_OK_STRING) {
                        ArrayList<Map<String, String>> maps = StringManager.getListMapByJson(StringManager.getFirstMap(o).get("list"));
                        for (int i = 0; i < maps.size(); i++) {
                            String code = maps.get(i).get("");
                            mCodes.add(code);
                        }
                        if (maps.size() <= 0) {
                            mLoadCodesEnable.set(false);
                        }
                        if (successRun != null)
                            successRun.run();

                        if (needLoadDetail) {
                            loadVideoDetail(getNextPageCodes(), true, false);
                        }
                    } else {
                        mLoadCodesEnable.set(false);
                        if (failedRun != null)
                            failedRun.run();
                    }
                }
            });
        }

        public ShortVideoDetailModule getModuleByMap(Map<String, String> itemMap) {
            if (itemMap == null || itemMap.isEmpty()) {
                return null;
            }
            ShortVideoDetailModule module = new ShortVideoDetailModule();
            module.setCode(itemMap.get("code"));
            module.setName(itemMap.get("name"));
            module.setPlayMode(itemMap.get("playMode"));
            module.setEssence("2".equals(itemMap.get("isEssence")));
            module.setFav("2".equals(itemMap.get("isFav")));
            module.setLike("2".equals(itemMap.get("isLike")));
            module.setFavNum(itemMap.get("favNum"));
            module.setCommentNum(itemMap.get("commentNum"));
            module.setLikeNum(itemMap.get("likeNum"));
            module.setShareNum(itemMap.get("shareNum"));
            module.setClickNum(itemMap.get("clickNum"));
            Map<String, String> videoMap = StringManager.getFirstMap(itemMap.get("video"));
            VideoModel videoModel = new VideoModel();
            videoModel.setAutoPlay("2".equals(videoMap.get("isAuto")));
            videoModel.setVideoTime(videoMap.get("time"));
            videoModel.setPlayableTime(videoMap.get("playableTime"));
            videoModel.setVideoW(videoMap.get("width"));
            videoModel.setVideoH(videoMap.get("height"));
            videoModel.setVideoUrlMap(StringManager.getFirstMap(videoMap.get("videoUrl")));
            videoModel.setVideoImg(videoMap.get("videoImg"));
            videoModel.setVideoGif(videoMap.get("videoGif"));
            module.setVideoModel(videoModel);
            Map<String, String> imageMap = StringManager.getFirstMap(itemMap.get("image"));
            ImageModel imageModel = new ImageModel();
            imageModel.setImageW(imageMap.get("width"));
            imageModel.setImageH(imageMap.get("height"));
            imageModel.setImageUrl(imageMap.get("url"));
            module.setImageModel(imageModel);
            Map<String, String> customerMap = StringManager.getFirstMap(itemMap.get("customer"));
            CustomerModel customerModel = new CustomerModel();
            customerModel.setUserCode(customerMap.get("code"));
            customerModel.setNickName(customerMap.get("nickName"));
            customerModel.setHeaderImg(customerMap.get("img"));
            customerModel.setFollow("2".equals(customerMap.get("isFollow")));
            customerModel.setGotoUrl(customerMap.get("url"));
            module.setCustomerModel(customerModel);
            Map<String, String> topicMap = StringManager.getFirstMap(itemMap.get("topic"));
            TopicModel topicModel = new TopicModel();
            topicModel.setCode(topicMap.get("code"));
            topicModel.setTitle(topicMap.get("title"));
            topicModel.setActivityType(topicMap.get("activityType"));
            topicModel.setColor(topicMap.get("color"));
            topicModel.setBgColor(topicMap.get("bgColor"));
            topicModel.setGotoUrl(topicMap.get("url"));
            module.setTopicModel(topicModel);
            Map<String, String> addressMap = StringManager.getFirstMap(itemMap.get("address"));
            AddressModel addressModel = new AddressModel();
            addressModel.setCode(addressMap.get("code"));
            addressModel.setAddress(addressMap.get("title"));
            addressModel.setColor(addressMap.get("color"));
            addressModel.setBgColor(addressMap.get("bgColor"));
            addressModel.setGotoUrl(addressMap.get("url"));
            module.setAddressModel(addressModel);
            Map<String, String> shareMap = StringManager.getFirstMap(itemMap.get("share"));
            ShareModule shareModule = new ShareModule();
            shareModule.setUrl(shareMap.get("url"));
            shareModule.setContent(shareMap.get("content"));
            shareModule.setTitle(shareMap.get("title"));
            shareModule.setImg(shareMap.get("img"));
            module.setShareModule(shareModule);
            module.setStatJson(itemMap.get("statJson"));
            return module;
        }
    }

    private void insertADData(int insertPosStart) {
        if (mAdData.isEmpty()) {
            return;
        }
        for (ShortVideoDetailModule module : mAdData) {
            ShortVideoDetailADModule adModule = (ShortVideoDetailADModule) module;
            if (adModule.adPositionInData >= 0 && adModule.adPositionInData < mDatas.size()
                    && adModule.adPositionInData >= insertPosStart) {
                if (!(mDatas.get(adModule.adPositionInData) instanceof ShortVideoDetailADModule)) {
                    mDatas.add(adModule.adPositionInData, module);
                }
            }
        }
    }

    private void showNetworkTip() {
        if (mNetStateTipDialog != null && mNetStateTipDialog.isShowing()) {
            return;
        }
        if (mAdapter != null) {
            RvVericalVideoItemAdapter.ItemViewHolder currentHolder = mAdapter.getCurrentViewHolder();
            if (currentHolder != null) {
                int playState = currentHolder.getPlayState();
                switch (playState) {
                    case GSYVideoPlayer.CURRENT_STATE_PLAYING:
                        currentHolder.pauseVideo();
                        break;
                    case GSYVideoPlayer.CURRENT_STATE_PLAYING_BUFFERING_START:
                    case GSYVideoPlayer.CURRENT_STATE_PREPAREING:
                    case GSYVideoPlayer.CURRENT_STATE_AUTO_COMPLETE:
                    case GSYVideoPlayer.CURRENT_STATE_ERROR:
                        currentHolder.stopVideo();
                        break;
                    default:
                        currentHolder.stopVideo();
                        break;
                }
            }
        }
        if (mNetStateTipDialog == null) {
            mNetStateTipDialog = new DialogManager(this);
            ViewManager viewManager = new ViewManager(mNetStateTipDialog);
            viewManager.setView(new TitleMessageView(this).setText("非wifi环境，是否使用流量继续观看视频？"))
                    .setView(new HButtonView(this).setPositiveText("继续播放", v -> {
                        mNetStateTipDialog.cancel();
                        playCurrent();
                        GlobalVariableConfig.shortVideoDetail_netStateTip_dialogEnable = false;
                    }).setNegativeText("退出播放", v -> {
                        mNetStateTipDialog.cancel();
                        ShortVideoDetailActivity.this.finish();
                    }));
            mNetStateTipDialog.setCancelable(false);
            mNetStateTipDialog.createDialog(viewManager);
        }
        mNetStateTipDialog.show();
    }

    private void playCurrent() {
        if (mAdapter != null) {
            RvVericalVideoItemAdapter.ItemViewHolder currentHolder = mAdapter.getCurrentViewHolder();
            if (currentHolder != null) {
                int playState = currentHolder.getPlayState();
                switch (playState) {
                    case GSYVideoPlayer.CURRENT_STATE_PAUSE:
                        currentHolder.resumeVideo();
                        break;
                    default:
                        currentHolder.startVideo();
                        break;
                }
            }
        }
    }

    private boolean canShowTipDialog() {
        return GlobalVariableConfig.shortVideoDetail_netStateTip_dialogEnable;
    }
}
