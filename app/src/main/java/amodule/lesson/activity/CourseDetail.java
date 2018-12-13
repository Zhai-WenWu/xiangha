package amodule.lesson.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.logic.stat.intefaces.OnClickListenerStat;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.lesson.adapter.VerticalAdapter;
import amodule.lesson.controler.data.CourseDataController;
import amodule.lesson.view.StudyFirstPager;
import amodule.lesson.view.StudySecondPager;
import amodule.lesson.view.StudySyllabusView;
import amodule.lesson.view.VerticalViewPager;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.web.view.XHWebView;
import third.share.BarShare;
import third.video.VideoPlayerController;

/**
 * Description :
 * PackageName : amodule.lesson.activity
 * Created by mrtrying on 2018/12/3 15:27.
 * e_mail : ztanzeyu@gmail.com
 */
public class CourseDetail extends BaseAppCompatActivity {
    public static final String EXTRA_CHAPTER_CODE = "chapterCode";
    public static final String EXTRA_CODE = "code";
    public static final String EXTRA_GROUP = "group";
    public static final String EXTRA_CHILD = "child";
    //    private RvListView mCourseList;
//    private StudySyllabusView mStudySyllabusView;
//    private LinearLayout mBottomLableLayout;
    private Map<String, String> mTopInfoMap;
    private VideoPlayerController mVideoPlayerController;
    private String mCode = "0";
    private String mChapterCode = "0";
    private final int SELECT_COURSE = 1;
    private int mGroupSelectIndex = 0;
    private int mChildSelectIndex = -1;
    private Map<String, String> shareMap;
    private VerticalViewPager viewPager;
    private Map<String, Map<String, String>> mData = new ArrayMap<>();
    private VerticalAdapter mVerticalAdapter;
    private StudyFirstPager studyFirstPager;
    private StudySecondPager studySecondPager;
    private LinearLayout mFirstPageBottomBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initExtraData();
        initActivity("", 2, 0, 0, R.layout.a_course_detail);
        initView();
    }

    private void initExtraData() {
        mChapterCode = getIntent().getStringExtra(EXTRA_CHAPTER_CODE);
//        mCode = getIntent().getStringExtra(EXTRA_CODE);
        mGroupSelectIndex = getIntent().getIntExtra(EXTRA_GROUP, 0);
        mChildSelectIndex = getIntent().getIntExtra(EXTRA_CHILD, -1);
    }

    private void initView() {
        viewPager = findViewById(R.id.viewpager);
        studyFirstPager = new StudyFirstPager(CourseDetail.this);
        studySecondPager = new StudySecondPager(CourseDetail.this);
        mFirstPageBottomBtn = studyFirstPager.getmBtnLayout();
        //底部按钮点击回调
        studyFirstPager.setOnClickBottomView(new StudyFirstPager.OnClickBottomView() {
            @Override
            public void clickView(int i) {
                viewPager.setCurrentItem(1, true);
                studySecondPager.setSelect(i);
            }
        });

        //滑动冲突解决
        boolean[] canScroll = new boolean[1];
        canScroll[0] = true;
        studySecondPager.getSecondPagerWebAdapter().setmScrollInterface(new XHWebView.ScrollInterface() {
            @Override
            public void onSChanged(WebView webView, int l, int t, int oldl, int oldt) {
                if (webView.getScrollY() == 0) {
                    canScroll[0] = true;
                } else {
                    canScroll[0] = false;
                }
            }
        });
        viewPager.setWebScrollTop(new VerticalViewPager.OnWebScrollTop() {
            @Override
            public boolean canScroll() {
                return canScroll[0];
            }
        });

        //底部按钮悬浮展示
        int bottomBtnHeight = Tools.getDimen(this, R.dimen.dp_49);
        viewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int viewpagerHeight = viewPager.getHeight();
                double i = ((double) bottomBtnHeight) / viewpagerHeight;
                viewPager.setScale(i);
            }
        });

        mVerticalAdapter = new VerticalAdapter(this);
        loadManager.setLoading(v -> loadInfo());
    }


    public void initTitle() {
        RelativeLayout topBarWhite = findViewById(R.id.top_bar_white);
        ImageView shareBtn = findViewById(R.id.share_icon_white);
        shareBtn.setVisibility(View.VISIBLE);
        TextView titleTv = (TextView) findViewById(R.id.title);
        TextView titleBottomTv = (TextView) findViewById(R.id.title_bottom);
        titleTv.setMaxWidth(ToolsDevice.getWindowPx(this).widthPixels - ToolsDevice.dp2px(this, 45 + 40));
        titleTv.setText(mTopInfoMap.get("name"));
        titleBottomTv.setMaxWidth(ToolsDevice.getWindowPx(this).widthPixels - ToolsDevice.dp2px(this, 45 + 40));
        titleBottomTv.setText(mTopInfoMap.get("name"));
        shareMap = StringManager.getFirstMap(mTopInfoMap.get("shareData"));
        OnClickListenerStat shareClick = new OnClickListenerStat() {
            @Override
            public void onClicked(View v) {
                doShare();
            }
        };
        shareBtn.setOnClickListener(shareClick);
        findViewById(R.id.back_white).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPager.getShowPosition() > 0) {
                    viewPager.setCurrentItem(0, true);
                } else {
                    finish();
                }
            }
        });

        //title渐变
        viewPager.setScrollDistance(new VerticalViewPager.OnScrollDistance() {
            @Override
            public void scrollDistance(float distance) {
                if (distance > 0 && distance < 1) {
                    topBarWhite.setAlpha(distance);
                    mFirstPageBottomBtn.setAlpha(distance);
                }
            }

            @Override
            public void scrollEnd(int state) {
                if (state == 0) {
                    int showPosition = viewPager.getShowPosition();
                    if (showPosition == 0) {
                        topBarWhite.setAlpha(0);
                        mFirstPageBottomBtn.setAlpha(0);
                    } else if (showPosition == 1) {
                        topBarWhite.setAlpha(1);
                        mFirstPageBottomBtn.setAlpha(1);
                    }
                }
            }
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (viewPager.getShowPosition() > 0) {
                viewPager.setCurrentItem(0, true);
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void doShare() {
        barShare = new BarShare(this, "", "");
        barShare.setShare(BarShare.IMG_TYPE_WEB, shareMap.get("title"), shareMap.get("content"),
                shareMap.get("img"), shareMap.get("url"));
        barShare.openShare();
    }

    private void loadInfo() {
        CourseDataController.loadLessonInfoData(mChapterCode, mCode, new InternetCallback() {
            @Override
            public void loaded(int flag, String s, Object o) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    mTopInfoMap = StringManager.getFirstMap(o);
                    mData.put("lessonInfo", mTopInfoMap);
                    initTitle();
                    loadCourseListData();
                }
            }
        });
    }

    private void loadCourseListData() {
        CourseDataController.loadCourseListData(mCode, "2", new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqInternet.REQ_OK_STRING) {
                    Map<String, String> resultMap = StringManager.getFirstMap(o);
                    mData.put("syllabusInfo", resultMap);
                    viewPager.setAdapter(mVerticalAdapter);
                    mVerticalAdapter.setData(mData);

                    studyFirstPager.initData(mData, mGroupSelectIndex, mChildSelectIndex);
                    studySecondPager.initData(mData);

                    mVerticalAdapter.setView(studyFirstPager, studySecondPager);
                    mVerticalAdapter.notifyDataSetChanged();
                    loadManager.loadOver(i);
                    initCourseListData(studyFirstPager);
                }
            }
        });
    }


    private void initCourseListData(StudyFirstPager studyFirstPager) {
        //课程横划
        StudySyllabusView mStudySyllabusView = studyFirstPager.findViewById(R.id.view_syllabus);
        //课程横划点击回调
        mStudySyllabusView.setOnSyllabusSelect(new StudySyllabusView.OnSyllabusSelect() {
            @Override
            public void onSelect(int position) {
//                    mCode = data.getStringExtra("code");
                mChildSelectIndex = position;
                loadInfo();
            }
        });
        TextView mClassNumTv = mStudySyllabusView.findViewById(R.id.tv_class_num);
        mClassNumTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //课程页
                Intent intent = new Intent(CourseDetail.this, CourseList.class);
                intent.putExtra(CourseDetail.EXTRA_GROUP, mGroupSelectIndex);
                intent.putExtra(CourseDetail.EXTRA_CHILD, mChildSelectIndex);
                intent.putExtra(CourseList.EXTRA_FROM_STUDY, true);
                startActivityForResult(intent, SELECT_COURSE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case SELECT_COURSE:
//                    mCode = data.getStringExtra("code");
                    mGroupSelectIndex = data.getIntExtra(EXTRA_GROUP, 0);
                    mChildSelectIndex = data.getIntExtra(EXTRA_CHILD, -1);
                    loadInfo();
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoPlayerController != null) {
            mVideoPlayerController.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoPlayerController != null) {
            mVideoPlayerController.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoPlayerController != null) {
            mVideoPlayerController.onDestroy();
        }
    }

    @Override
    public void onBackPressed() {
        if (mVideoPlayerController != null && mVideoPlayerController.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}
