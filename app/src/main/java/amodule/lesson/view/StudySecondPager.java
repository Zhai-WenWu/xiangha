package amodule.lesson.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.tools.StringManager;
import amodule.lesson.adapter.SecondPagerAdapter;

public class StudySecondPager extends RelativeLayout {

    private Context mContext;
    private ViewPager mViewPager;
    private SecondPagerAdapter secondPagerAdapter;
    private List<String> mDataList = new ArrayList<>();
    private CourseCommentView mCourseCommentView;

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public StudySecondPager(Context context) {
        this(context, null);
    }

    public StudySecondPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StudySecondPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.view_second_pager, this, true);
        mViewPager = findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(2);
        secondPagerAdapter = new SecondPagerAdapter(mContext);
        mViewPager.setAdapter(secondPagerAdapter);
    }

    public void initData(Map<String, Map<String, String>> mData, int commentIndex, String mCode, String mChapterCode) {
        mCourseCommentView = new CourseCommentView(mContext);
        mCourseCommentView.setCode(mCode, mChapterCode);
        mCourseCommentView.setOnLoadFinish(new CourseCommentView.OnLoadFinish() {
            @Override
            public void loadFinish() {
                secondPagerAdapter.initCommentView(mCourseCommentView);
                mDataList.clear();
                List<Map<String, String>> labelDataList = StringManager.getListMapByJson(mData.get("lessonInfo").get("labelData"));
                for (Map<String, String> label : labelDataList) {
                    mDataList.add(label.get("url"));
                }
                secondPagerAdapter.setData(mDataList, commentIndex);
                mViewPager.setAdapter(secondPagerAdapter);
            }
        });
    }

    public void setSelect(int currentItem) {
        mViewPager.setCurrentItem(currentItem);
    }

    public SecondPagerAdapter getSecondPagerAdapter() {
        return secondPagerAdapter;
    }
}
