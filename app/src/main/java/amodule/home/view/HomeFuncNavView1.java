package amodule.home.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.stat.intefaces.OnItemClickListenerRvStat;
import acore.override.helper.XHActivityManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.rvlistview.RvHorizatolListView;
import acore.widget.rvlistview.RvListView;
import amodule._common.utility.WidgetUtility;
import amodule.home.adapter.HorizontalAdapterFuncNav1;

import static acore.logic.stat.StatConf.STAT_TAG;
import static acore.logic.stat.StatisticsManager.STAT_DATA;

/**
 * Description :
 * PackageName : amodule.home.view
 * Created by MrTrying on 2017/11/14 11:14.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeFuncNavView1 extends LinearLayout {
    private Context context;
    private int mFirstIntervalSpacing, mLastIntervalSpacing, mCenterIntervalSpacing;
    protected int mScreenWidth;
    public HomeFuncNavView1(Context context) {
        this(context, null);
    }

    public HomeFuncNavView1(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeFuncNavView1(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        mScreenWidth = Tools.getPhoneWidth();
        initialize();
    }

    public ArrayList<Map<String,String>> mapArrayList  = new ArrayList<>();
    public HorizontalAdapterFuncNav1 adapterFuncNav1;
    public RvHorizatolListView listView;
    private void initialize() {
        //填充UI
        LayoutInflater.from(getContext()).inflate(R.layout.widget_func_nav_1_layout_new, this, true);
        adapterFuncNav1 = new HorizontalAdapterFuncNav1(context,mapArrayList);
        listView= (RvHorizatolListView) findViewById(R.id.recycler_view);
        listView.setAdapter(adapterFuncNav1);
        listView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int pos = parent.getChildAdapterPosition(view);
                int size = mapArrayList.size();
                switch (size) {
                    case 0:
                        break;
                    case 1:
                        outRect.left = mFirstIntervalSpacing;
                        outRect.right = mLastIntervalSpacing;
                        break;
                    default:
                        if (pos == 0) {
                            outRect.left = mFirstIntervalSpacing;
                            outRect.right = mCenterIntervalSpacing / 2;
                        } else if (pos == size - 1) {
                            outRect.left = mCenterIntervalSpacing / 2;
                            outRect.right = mLastIntervalSpacing;
                        } else {
                            outRect.left = mCenterIntervalSpacing / 2;
                            outRect.right = outRect.left;
                        }
                        break;
                }
            }
        });
        initData();
    }

    protected void initData() {
        int[] iconArray = {R.drawable.home_fanc_nav_1, R.drawable.home_fanc_nav_2, R.drawable.home_fanc_nav_3, R.drawable.home_fanc_nav_4};
        String[] textArray = {"菜谱分类", "三餐推荐", "本周佳作", "香哈商城"};
        String[] urls = {
                "xiangha://welcome?fenlei.app",
                "xiangha://welcome?HomeSecond.app?type=day",
                "xiangha://welcome?WeekDish.app",
                "xiangha://welcome?xhds.home.app",
        };
        listView.setTag(STAT_TAG,"导航1");
        for (int index = 0; index < iconArray.length; index++) {
            Map<String,String> map = new HashMap<>();
            map.put("text1",textArray[index]);
            map.put("drawableId",String.valueOf(iconArray[index]));
            map.put("url",urls[index]);
            mapArrayList.add(map);
        }
        computeItemSpacing();
        adapterFuncNav1.setCache(true);
        adapterFuncNav1.notifyDataSetChanged();
        setVisibility(VISIBLE);

        listView.setOnItemClickListener(new OnItemClickListenerRvStat() {
            @Override
            protected String getStatData(int position) {
                return null;
            }

            @Override
            public void onItemClicked(View view, RecyclerView.ViewHolder holder, int position) {
                if(position<mapArrayList.size()) {
                    AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), mapArrayList.get(position).get("url"), true);
                }
            }
        });

    }

    protected void computeItemSpacing() {
        int centerSpacing = 0;
        int firstLastSpacing = getResources().getDimensionPixelSize(R.dimen.dp_20);
        int itemWidth = getResources().getDimensionPixelSize(R.dimen.dp_55);
        int itemSize = mapArrayList.size();
        if (itemSize > 5) {
            centerSpacing = (mScreenWidth - 2 * firstLastSpacing - itemWidth * 5) / 5;
        } else if (itemSize > 1){
            centerSpacing = (mScreenWidth - 2 * firstLastSpacing - itemWidth * itemSize) / (itemSize - 1);
        } else if (itemSize == 1){
            firstLastSpacing = (mScreenWidth - itemWidth) / 2;
        }
        mFirstIntervalSpacing = firstLastSpacing;
        mCenterIntervalSpacing = centerSpacing;
        mLastIntervalSpacing = firstLastSpacing;
    }
}
