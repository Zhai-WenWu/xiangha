package amodule.lesson.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import acore.logic.stat.intefaces.OnClickListenerStat;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.ColorUtil;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.rvlistview.RvListView;
import amodule.lesson.adapter.CourseIntroductionAdapter;
import amodule.lesson.controler.data.CourseDataController;
import amodule.lesson.view.ChefIntroductionView;
import amodule.lesson.view.introduction.CourseHorizontalView;
import amodule.lesson.view.introduction.CourseIntroduceHeader;
import amodule.lesson.view.introduction.CourseIntroductionBottomView;
import amodule.lesson.view.introduction.CourseVerticalView;
import amodule.main.Main;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import third.share.BarShare;

/**
 * Description :
 * PackageName : amodule.lesson.activity
 * Created by mrtrying on 2018/12/3 15:26.
 * e_mail : ztanzeyu@gmail.com
 */
public class CourseIntroduction extends BaseAppCompatActivity {

    public static final String EXTRA_CEODE = "code";

    private RelativeLayout mTopBarWhite,mTopBarBlack;
    private ImageView mShareIconWhite,mShareIconBlack;
    private CourseIntroduceHeader mCourseIntroduceHeader;
    private ChefIntroductionView mChefIntroductionView;
    private CourseHorizontalView mCourseHorizontalView;
    private CourseVerticalView mCourseVerticalView;
    private CourseIntroductionBottomView mBottomView;
    private RvListView mRvListView;
    private CourseIntroductionAdapter mAdatper;

    private List<Map<String, String>> mData = new ArrayList<>();
    private Map<String, String> shareMap = new HashMap<>();
    private String mCode;
    private boolean canStudy = false;
    private int topbarHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化数据
        initExtraData();
        initActivity("", 2, 0, 0, R.layout.a_course_introduce);
        //初始化UI
        initUI();
        //设置加载
        setLoad();
    }

    private void initExtraData() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        mCode = intent.getStringExtra(EXTRA_CEODE);
        mCode = "123";
    }

    int offsetHeight = 0;
    private void initUI() {
        initTitle();
        mBottomView = findViewById(R.id.course_bottom_layout);
        mRvListView = findViewById(R.id.rv_list_view);
        mAdatper = new CourseIntroductionAdapter(this, mData);
        mRvListView.setAdapter(mAdatper);

        mRvListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                offsetHeight += dy;
                Log.i("tzy", "onScrolled: " + offsetHeight);
                Log.i("tzy", "onScrolled:getVideoHeight " + mCourseIntroduceHeader.getVideoHeight());
                float offset = mCourseIntroduceHeader.getVideoHeight() - topbarHeight;
                float alpha = offsetHeight > offset ? 1 : offsetHeight/offset;
                if(alpha == 0){
                    mTopBarWhite.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                |View.SYSTEM_UI_FLAG_VISIBLE);
                        getWindow().setStatusBarColor(Color.TRANSPARENT);
                    }
                }else{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                |View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        String colorStr = "#"+Integer.toHexString((int) (alpha*255)).toUpperCase() + "FFFFFF";
                        getWindow().setStatusBarColor(ColorUtil.parseColor(colorStr));
                    }
                }
                mTopBarWhite.setAlpha(alpha);
            }
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        mCourseIntroduceHeader = new CourseIntroduceHeader(this);
        mCourseIntroduceHeader.setLayoutParams(params);
        mRvListView.addHeaderView(mCourseIntroduceHeader);

        mCourseHorizontalView = new CourseHorizontalView(this);
        mCourseHorizontalView.setLayoutParams(params);
        mRvListView.addHeaderView(mCourseHorizontalView);

        mChefIntroductionView = new ChefIntroductionView(this);
        mChefIntroductionView.setLayoutParams(params);
        mRvListView.addHeaderView(mChefIntroductionView);

        mCourseVerticalView = new CourseVerticalView(this);
        mCourseVerticalView.setLayoutParams(params);
        mRvListView.addFooterView(mCourseVerticalView);

        View holdView = new View(this);
        holdView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,Tools.getDimen(this,R.dimen.dp_38)));
        mRvListView.addFooterView(holdView);

        setListener();
    }

    private void setListener() {
        View.OnClickListener startCourseListListener = v -> startActivity(new Intent(CourseIntroduction.this, CourseList.class));
        mCourseVerticalView.setFooterOnClickListener(startCourseListListener);
        mCourseHorizontalView.setSubTitleOnClickListener(startCourseListListener);

        mBottomView.setFavClickListener(v -> {
            //收藏请求
            Toast.makeText(CourseIntroduction.this,"收藏",Toast.LENGTH_SHORT).show();
        });
        mBottomView.setVIPClickListener(v -> {
            mBottomView.showVipButton(false);
            mBottomView.showUploadButton(true);
            scrollToTop();
            mCourseHorizontalView.setVisibility(View.VISIBLE);
            mCourseVerticalView.setVisibility(View.GONE);
        });
        mBottomView.setUploadClickListener(v -> {
            mBottomView.showVipButton(true);
            mBottomView.showUploadButton(false);
            scrollToTop();
            mCourseHorizontalView.setVisibility(View.GONE);
            mCourseVerticalView.setVisibility(View.VISIBLE);
        });
    }

    private void scrollToTop() {
        LinearLayoutManager mLayoutManager = (LinearLayoutManager) mRvListView.getLayoutManager();
        mLayoutManager.scrollToPositionWithOffset(0, 0);
        offsetHeight=0;
    }

    private void initTitle() {
        setStatusBarFullTransparent();
        setFitSystemWindow(false);
        mTopBarBlack = findViewById(R.id.top_bar_black);
        mTopBarWhite = findViewById(R.id.top_bar_white);
        RelativeLayout topBar = findViewById(R.id.top_bar);
        int statusBarHeight = Tools.getStatusBarHeight(this);
        topbarHeight = statusBarHeight + Tools.getDimen(this,R.dimen.topbar_height);
        topBar.getLayoutParams().height = topbarHeight;
        topBar.setPadding(0,statusBarHeight,0,0);

        findViewById(R.id.back_black).setOnClickListener(getBackBtnAction());
        findViewById(R.id.back_white).setOnClickListener(getBackBtnAction());
        mShareIconWhite = findViewById(R.id.share_icon_white);
        mShareIconBlack = findViewById(R.id.share_icon_black);
        OnClickListenerStat shareClick = new OnClickListenerStat() {
            @Override
            public void onClicked(View v) {
                doShare();
            }
        };
        mShareIconWhite.setOnClickListener(shareClick);
        mShareIconBlack.setOnClickListener(shareClick);
    }

    private void doShare() {
        barShare = new BarShare(this, "", "");
        barShare.setShare(BarShare.IMG_TYPE_WEB, shareMap.get("title"), shareMap.get("content"),
                shareMap.get("img"), shareMap.get("url"));
        barShare.openShare();
    }

    private void setLoad() {
        loadManager.setLoading(v -> loadCourseTopData());
    }

    private void loadCourseTopData() {
        if (TextUtils.isEmpty(mCode)) {
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        CourseDataController.loadCourseTopData(mCode, new InternetCallback() {
            @Override
            public void loaded(int flag, String s, Object o) {
                if (flag >= ReqEncyptInternet.REQ_OK_STRING) {
                    Map<String, String> resultMap = StringManager.getFirstMap(o);
                    mCourseIntroduceHeader.setData(resultMap);
                    shareMap = StringManager.getFirstMap(resultMap.get("shareData"));
                    setShareVisibility();
                    loadCourseDescData();
                    loadCourseListData();
                }
                loadManager.loadOver(flag);
            }
        });
    }

    private void setShareVisibility() {
        mShareIconWhite.setVisibility(shareMap.isEmpty() ? View.GONE : View.VISIBLE);
        mShareIconBlack.setVisibility(shareMap.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void loadCourseDescData() {
        CourseDataController.loadCourseDescData(mCode, new InternetCallback() {
            @Override
            public void loaded(int flag, String s, Object o) {
                if (flag >= ReqEncyptInternet.REQ_OK_STRING) {
                    Map<String, String> resultMap = StringManager.getFirstMap(o);
                    mChefIntroductionView.setData(StringManager.getFirstMap(resultMap.remove("chefDesc")));
                    List<Map<String,String>> recomInfo = StringManager.getListMapByJson(resultMap.get("recomInfo"));
                    mData.addAll(recomInfo);
                    mAdatper.notifyDataSetChanged();
                }
            }
        });

    }

    private void loadCourseListData() {
        CourseDataController.loadCourseListData(mCode, "1", new InternetCallback() {
            @Override
            public void loaded(int flag, String s, Object o) {
                if (flag >= ReqEncyptInternet.REQ_OK_STRING) {
                    Map<String, String> resultMap = StringManager.getFirstMap(o);
                    mCourseVerticalView.setData(resultMap);
                    mCourseHorizontalView.setData(resultMap);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCourseIntroduceHeader != null) {
            mCourseIntroduceHeader.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCourseIntroduceHeader != null) {
            mCourseIntroduceHeader.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCourseIntroduceHeader != null) {
            mCourseIntroduceHeader.onDestroy();
        }
    }

    @Override
    public void onBackPressed() {
        if (mCourseIntroduceHeader != null && mCourseIntroduceHeader.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    /**
     * 全透状态栏
     */
    protected void setStatusBarFullTransparent() {
        if (Build.VERSION.SDK_INT >= 21) {//21表示5.0
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= 19) {//19表示4.4
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //虚拟键盘也透明
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    /**
     * 如果需要内容紧贴着StatusBar
     * 应该在对应的xml布局文件中，设置根布局fitsSystemWindows=true。
     */
    private View contentViewGroup;

    protected void setFitSystemWindow(boolean fitSystemWindow) {
        if (contentViewGroup == null) {
            contentViewGroup = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        }
        contentViewGroup.setFitsSystemWindows(fitSystemWindow);
    }
}