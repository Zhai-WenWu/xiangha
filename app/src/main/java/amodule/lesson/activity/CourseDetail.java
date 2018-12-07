package amodule.lesson.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.lesson.controler.data.CourseDataController;
import amodule.lesson.view.StudyAskView;
import amodule.lesson.view.StudySyllabusView;
import amodule.lesson.view.StudylIntroductionView;
import amodule.lesson.view.StudyTitleView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

/**
 * Description :
 * PackageName : amodule.lesson.activity
 * Created by mrtrying on 2018/12/3 15:27.
 * e_mail : ztanzeyu@gmail.com
 */
public class CourseDetail extends BaseAppCompatActivity implements View.OnClickListener {
    public static final String EXTRA_CODE = "code";
    public static final String EXTRA_TYPE = "type";
    public static final String EXTRA_GROUP = "group";
    public static final String EXTRA_CHILD = "child";
    //    private RvListView mCourseList;
    private Map<String, String> mTopInfoMap;
    private ArrayList<Map<String, String>> videoList;
    private String mCode = "0";
    private String mChapterCode = "0";
    private String mType = "1";
    private StudyAskView studyAskView;
    private StudyTitleView studyTitleView;
    private StudySyllabusView studySyllabusView;
    private StudylIntroductionView studylIntroductionView;
    private final int SELECT_COURSE = 1;
    private Intent intent;
    private Map<String, String> desc;
    private int mGroupSelectIndex = 0;
    private int mChildSelectIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 2, 0, R.layout.c_view_bar_title, R.layout.a_course_detail);
        initView();
        loadInfo();
    }

    private void initView() {
        loadManager = new LoadManager(this, rl);
        rl = (RelativeLayout) findViewById(R.id.activityLayout);
        studySyllabusView = findViewById(R.id.view_syllabus);
        Button mainPointsBt = findViewById(R.id.bt_main_points);
        Button askBt = findViewById(R.id.bt_ask);
        Button introductBt = findViewById(R.id.bt_introduct);
        mainPointsBt.setOnClickListener(this);
        askBt.setOnClickListener(this);
        introductBt.setOnClickListener(this);

        videoList = new ArrayList<>();
    }

    private void loadInfo() {
//        mCode = getIntent().getStringExtra(EXTRA_CODE);
//        mType = getIntent().getStringExtra(EXTRA_TYPE);
        CourseDataController.loadChapterInfoData(mCode, mChapterCode, new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqInternet.REQ_OK_STRING) {
                    mTopInfoMap = StringManager.getFirstMap(o);
                }
            }

        });

        CourseDataController.loadCourseListData(mCode, mType, new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqInternet.REQ_OK_STRING) {
                    Map<String, String> mCourseListMap = StringManager.getFirstMap(o);
                    initCourseListData(mCourseListMap);
                }
            }
        });

        CourseDataController.loadChapterPointData(mCode, new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqInternet.REQ_OK_STRING) {
                    Map<String, String> mDescoMap = StringManager.getFirstMap(o);
                    initDescData(mDescoMap);
                }
            }

        });
    }


    private void initCourseListData(Map<String, String> courseListMap) {

        //课程横划
        ArrayList<Map<String, String>> info = StringManager.getListMapByJson(courseListMap.get("chapterList"));
        Map<String, String> lessonListMap = info.get(mGroupSelectIndex);//第几章
        studySyllabusView.setData(lessonListMap, mChildSelectIndex);
        //课程横划点击回调
        studySyllabusView.setOnSyllabusSelect(new StudySyllabusView.OnSyllabusSelect() {
            @Override
            public void onSelect(int position) {
                Intent intent = new Intent(CourseDetail.this, CourseDetail.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });
        TextView mClassNumTv = studySyllabusView.findViewById(R.id.tv_class_num);
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


    private void initDescData(Map<String, String> descoMap) {
        //简介
        desc = StringManager.getFirstMap(descoMap.get("desc"));

        //视频内容
        Map<String, String> videoDetail = StringManager.getFirstMap(descoMap.get("videoDetail"));
        if (videoDetail != null) {
            String videoTitle = videoDetail.get("title");
            studylIntroductionView.setVideoTitle(videoTitle);
            ArrayList<Map<String, String>> infoList = StringManager.getListMapByJson(videoDetail.get("info"));
            for (Map<String, String> info : infoList) {
                ArrayList<Map<String, String>> vidList = StringManager.getListMapByJson(info.get("info"));
                boolean isFirstItem = true;
                for (Map<String, String> vidInfo : vidList) {
                    Map<String, String> stringMap = new ArrayMap<>();
                    if (isFirstItem) {
                        stringMap.put("subTitle", info.get("subTitle"));
                        isFirstItem = false;
                    }
                    stringMap.put("content", vidInfo.get("content"));
                    stringMap.put("img", vidInfo.get("img"));
                    videoList.add(stringMap);
                }
            }
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_main_points:
                intent = new Intent(this, StudyPoint.class);
                intent.putExtra(StudyIntroduct.EXTRA_DESC, Tools.list2Json(videoList));
                startActivity(intent);
                break;
            case R.id.bt_ask:
                intent = new Intent(this, StudyAsk.class);
//                intent.putExtra(StudyIntroduct.EXTRA_DESC, Tools.list2Json(videoList));
                startActivity(intent);
                break;
            case R.id.bt_introduct:
                intent = new Intent(this, StudyIntroduct.class);
                intent.putExtra(StudyIntroduct.EXTRA_DESC, Tools.map2Json(desc));
                startActivity(intent);
                break;
        }
    }
}