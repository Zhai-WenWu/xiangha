package amodule.dish.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.adapter.AdapterSimple;
import acore.override.helper.XHActivityManager;
import acore.override.view.ItemBaseView;
import amodule.dish.activity.DetailDish;
import third.mall.widget.ListViewForScrollView;

/**
 * 主辅料
 */
public class DishIngreDataShow extends ItemBaseView implements View.OnClickListener {
    private LinearLayout recommendAd_linear;
    private ArrayList<Map<String, String>> listAll= new ArrayList<>();
    private ArrayList<Map<String, String>> lists = new ArrayList<>();
    private ArrayList<Map<String, String>> listNoAll = new ArrayList<>();
    private AdapterSimple adapter;
    private String isSpread = "";//1--未展开状态，2--展开状态
    private boolean isSupport = false;
    private TextView ingre_all_tv;
    private ImageView ingre_all_img;

    public DishIngreDataShow(Context context) {
        super(context, R.layout.view_dish_data_show);
    }

    public DishIngreDataShow(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.view_dish_data_show);
    }

    public DishIngreDataShow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.view_dish_data_show);
    }

    @Override
    public void init() {
        super.init();
        recommendAd_linear = (LinearLayout) findViewById(R.id.recommendAd_linear);
        findViewById(R.id.ingre_all).setOnClickListener(this);
        ingre_all_tv= (TextView) findViewById(R.id.ingre_all_tv);
        ingre_all_img= (ImageView) findViewById(R.id.ingre_all_img);
        isSupport = false;
    }

    /**
     * 设置数据
     */
    public void setData(final ArrayList<Map<String, String>> listmap) {
        listAll.clear();lists.clear();listNoAll.clear();
        if (listmap == null || listmap.size() <= 0) return;
        this.listAll.addAll(listmap);
        for (Map<String, String> map : listAll) {
            map.put("goIngre", map.get("url").length() > 0 ? "ignore" : "hide");
        }
        if (listAll.size() > 5) {
            isSupport = true;
            for (int i = 0; i < 5; i++) {
                listNoAll.add(listAll.get(i));
            }
        }
        lists.addAll( isSupport ? listNoAll : listAll);
        isSpread = isSupport ? "1" : "2";
        findViewById(R.id.ingre_all).setVisibility(isSupport?View.VISIBLE:View.GONE);
        ListViewForScrollView listview_scroll = (ListViewForScrollView) findViewById(R.id.listview_scroll);
        if (lists.size() > 0) {
            listview_scroll.setVisibility(View.VISIBLE);
            adapter = new AdapterSimple(listview_scroll, lists,
                    R.layout.table_cell_burden,
                    new String[]{"name", "goIngre", "content"},
                    new int[]{R.id.itemText1, R.id.itemImg1, R.id.itemText2});
            listview_scroll.setAdapter(adapter);
            listview_scroll.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Map<String, String> ingre = lists.get(position);
                    if (ingre.get("url").length() > 0) {
                        XHClick.mapStat(context, DetailDish.tongjiId_detail, "用料", "食材信息按钮点击量");
                        AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), ingre.get("url"), false);
                    }
                }
            });
            adapter.notifyDataSetChanged();
        } else listview_scroll.setVisibility(View.GONE);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ingre_all:
                lists.clear();
                setIngreState();
                break;
        }
    }
    private void setIngreState(){
        if("1".equals(isSpread)){
            lists.addAll(listAll);
            isSpread="2";
            ingre_all_tv.setText("收起");
            ingre_all_img.setBackgroundResource(R.drawable.dish_ingre_pack);
            adapter.notifyDataSetChanged();
            XHClick.mapStat(context, DetailDish.tongjiId_detail, "用料", "折叠用料点击量");
        }else if("2".equals(isSpread)){
            lists.addAll(listNoAll);
            isSpread="1";
            ingre_all_tv.setText("展开全部");
            ingre_all_img.setBackgroundResource(R.drawable.dish_ingre_show);
            adapter.notifyDataSetChanged();
            XHClick.mapStat(context, DetailDish.tongjiId_detail, "用料", "展开用料点击量");
        }
    }
}
