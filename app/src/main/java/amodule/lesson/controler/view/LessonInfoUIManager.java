package amodule.lesson.controler.view;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.rvlistview.RvListView;
import amodule._common.delegate.StatisticCallback;
import amodule._common.utility.WidgetUtility;
import amodule.lesson.activity.LessonInfo;
import amodule.lesson.view.info.LessonInfoHeader;
import amodule.lesson.view.info.LessonModuleView;
import amodule.vip.VIPButton;

/**
 * Description :
 * PackageName : amodule.vip.controller.view
 * Created by tanze on 2018/3/29 17:10.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonInfoUIManager {

    private final LessonInfo mActivity;

    private RelativeLayout mTopbar;
    private TextView mTitle;
    private RvListView mRvListView;
    private LessonInfoHeader mInfoHeader;
    private LessonModuleView mHaFriendCommentView;
    private LessonModuleView mConentView;
    private LessonModuleView mGuessYouLikeView;
    private VIPButton mVIPButton;

    private RecyclerView.OnScrollListener mOnScrollListener;

    private int mTopBarHeight = 0;
    private boolean hasNextItem = true;

    public LessonInfoUIManager(LessonInfo activity) {
        this.mActivity = activity;
        initializeData();
        initializeUI();

    }

    private void initializeData() {
        mTopBarHeight = Tools.getStatusBarHeight(mActivity);//Tools.getDimen(mActivity, R.dimen.dp_49) +
    }

    private void initializeUI() {
        initTitle();
        mRvListView = (RvListView) mActivity.findViewById(R.id.rvListview);

        mInfoHeader = new LessonInfoHeader(mActivity);
        mHaFriendCommentView = new LessonModuleView(mActivity);
        mHaFriendCommentView.setUseDefaultBottomPadding(true);
        mConentView = new LessonModuleView(mActivity);
        mGuessYouLikeView = new LessonModuleView(mActivity);
        mVIPButton = (VIPButton) mActivity.findViewById(R.id.vip_button);

        mRvListView.addHeaderView(mInfoHeader);
        mRvListView.addFooterView(mHaFriendCommentView);
        mRvListView.addFooterView(mConentView);
        mRvListView.addFooterView(mGuessYouLikeView);

        setStatisticCallback();
        setClickListener();
        setScrollListener();
    }

    private void setStatisticCallback() {
        StatisticCallback statisticCallback = (id, twoLevel, threeLevel, position) -> XHClick.mapStat(mActivity, id, twoLevel, threeLevel);
        mConentView.setStatisticCallback(statisticCallback);
        mGuessYouLikeView.setStatisticCallback(statisticCallback);
    }

    protected void initTitle() {
        mTopbar = (RelativeLayout) mActivity.findViewById(R.id.top_bar);
        mTitle = (TextView) mActivity.findViewById(R.id.title);
        resetTopbar();
        String colorValue = mActivity.getResources().getString(R.color.common_top_bg);
        Tools.setStatusBarColor(mActivity, Color.parseColor(colorValue));
    }

    private void resetTopbar() {
        Log.i("tzy", "resetTopbar: ");
        currentColorRes = R.color.transparent;
        mTopbar.setBackgroundResource(R.color.transparent);
    }

    private void setClickListener() {
        mActivity.findViewById(R.id.back).setOnClickListener(v -> onBackPressed());
    }

    public void setHeaderData(Map<String, String> headerData) {
        mInfoHeader.setData(headerData);
        String titleValue = headerData.get("name");
        if(!TextUtils.isEmpty(titleValue) && titleValue.length() >= 12){
            titleValue = titleValue.substring(0,11) + "...";
        }
        WidgetUtility.setTextToView(mTitle, titleValue);
        setRvListViewVisibility(View.VISIBLE);
//        showNextItem();
    }

    public void setHaFriendCommentData(Map<String, String> data) {
        mHaFriendCommentView.setData(data);
    }

    public void setLessonContentData(Map<String, String> data) {
        mConentView.setData(data);
    }

    public void setGuessYouLikeData(Map<String, String> data) {
        mGuessYouLikeView.setData(data);
        mGuessYouLikeView.setPadding(0, 0, 0, Tools.getDimen(mActivity, R.dimen.dp_20));
    }

    public void setVipButton(Map<String, String> data) {
        if (data == null || data.isEmpty() || mVIPButton == null) {
            return;
        }
        boolean isShow = "2".equals(data.get("vipButtonIsShow"));
        if (!isShow) {
            mVIPButton.setVisibility(View.GONE);
            return;
        }
        Map<String, String> vipButtonConfig = StringManager.getFirstMap(data.get("vipButton"));
        String titleValue = vipButtonConfig.get("title");
        String colorValue = vipButtonConfig.get("color");
        String bgColorValue = vipButtonConfig.get("bgColor");
        String url = vipButtonConfig.get("url");
        if (TextUtils.isEmpty(titleValue)
                || TextUtils.isEmpty(colorValue)
                || TextUtils.isEmpty(bgColorValue)) {
            return;
        }
        WidgetUtility.setTextToView(mVIPButton, titleValue);
        mVIPButton.setTextColor(Color.parseColor(colorValue));
        mVIPButton.setBackgroundColor(Color.parseColor(bgColorValue));
        mVIPButton.setOnClickListener(v -> {
            XHClick.mapStat(mActivity, LessonInfo.STATISTICS_ID_NONVIP, "底部浮动按钮点击", "");
            AppCommon.openUrl(mActivity, url, true);
        });
    }

    /** 设置滑动监听 */
    private void setScrollListener() {
        if (mOnScrollListener == null) {
            mOnScrollListener = new RecyclerView.OnScrollListener() {
//                boolean alowLoad = true;
//                int currentState;

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
//                    currentState = newState;
//                    RecyclerView.Adapter adapter = mRvListView.getAdapter();
//                    final LinearLayoutManager layoutManager = (LinearLayoutManager) mRvListView.getLayoutManager();
//                    if (layoutManager != null && adapter != null) {
//                        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
//                        if (newState == RecyclerView.SCROLL_STATE_IDLE
//                                && lastVisibleItemPosition + 1 >= adapter.getItemCount() - 4) {
//                            if (alowLoad) {
//                                alowLoad = false;
//                                if (currentState == SCROLL_STATE_IDLE
//                                        && hasNextItem) {
////                                    hasNextItem = showNextItem();
//                                }
//                                new Handler().postDelayed(() -> alowLoad = true, 500);
//                            }
//                        }
//                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    //更新topbar颜色
                    totalDy += dy;
                    updateTopbarBg(totalDy);
                }
            };
            mRvListView.addOnScrollListener(mOnScrollListener);
        }
    }

    private boolean showNextItem() {
        hasNextItem = mHaFriendCommentView.showNextItem()
                || mConentView.showNextItem()
                || mGuessYouLikeView.showNextItem();
        return hasNextItem;
    }

    private int currentColorRes = R.color.transparent;

    private void updateTopbarBg(int dy) {
        int totalHeight = mInfoHeader.getImageHeight() / 2 - mTopBarHeight;
        if (dy <= totalHeight) {
            String colorSuffix = Integer.toHexString((int) (255.0f * dy / totalHeight));
            if (colorSuffix.length() < 2) {
                colorSuffix = "0" + colorSuffix;
            }
            mTopbar.setBackgroundColor(Color.parseColor("#" + colorSuffix + "1B1B1F"));
            mTitle.setTextColor(Color.parseColor("#"+colorSuffix+"FFFFFF"));
        }else{
            mTitle.setTextColor(Color.parseColor("#FFFFFF"));
            mTopbar.setBackgroundResource(R.color.common_top_bg);
        }
    }

    private void onBackPressed() {
        if (mActivity != null) {
            mActivity.onBackPressed();
        }
    }

    public RvListView getRvListView() {
        return mRvListView;
    }

    public void setRvListViewVisibility(int visibility) {
        if (mRvListView != null) {
            mRvListView.setVisibility(visibility);
        }
    }

    public void refresh() {
        resetTopbar();
        returnListTop();
        setHaFriendCommentData(new HashMap<>());
    }

    private int totalDy = 0;

    //回到第一个位置
    private void returnListTop() {
        totalDy = 0;
        if (mRvListView != null) {
            mRvListView.scrollToPosition(0);
        }
    }
}
