package amodule.search.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

import com.xiangha.R;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.SetDataView;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.search.data.SearchConstant;
import amodule.search.data.SearchDataImp;
import aplug.basic.InternetCallback;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;
import cn.srain.cube.views.ptr.PtrDefaultHandler;
import cn.srain.cube.views.ptr.PtrFrameLayout;
import third.ad.BannerAd;
import third.ad.scrollerAd.XHAllAdControl;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilString;

import static third.ad.tools.AdPlayIdConfig.SEARCH_DEFAULT;

/**
 * Created by ：airfly on 2016/10/14 11:31.
 */
public class DefaultSearchView extends LinearLayout implements View.OnClickListener {

    private BaseActivity mActivity;
    private DefaultViewCallback callback;
    private MultiTagView hotTable;
    private TableLayout hisTable;
    private ObservableHorizontalScrollView hor_hotwords;
    private LinearLayout ll_hottag;
    private ArrayList<Map<String, String>> listSearchHistory = new ArrayList<Map<String, String>>();// 历史记录的内容;search
    private ArrayList<Map<String, String>> hotWords = new ArrayList<Map<String, String>>();
    private int searchType;
    private String searchKey;
    private LinearLayout rl_no_history;
    private LinearLayout ll_has_his;
    private LinearLayout his_search_title;
    private int limitSearchType;
    private PtrClassicFrameLayout search_header_list_view_frame;
    private boolean ishorizonScroll;


    public DefaultSearchView(Context context) {
        this(context, null);
    }

    public DefaultSearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefaultSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.c_view_bar_default_search_bottom, this, true);
    }


    public void init(BaseActivity activity, DefaultViewCallback callback, int searchType) {
        mActivity = activity;
        this.callback = callback;
        limitSearchType = searchType;
        initView();
        setListener();

    }


    private void initView() {

        search_header_list_view_frame = (PtrClassicFrameLayout) findViewById(R.id.search_header_list_view_frame);
        search_header_list_view_frame.setKeepHeaderWhenRefresh(false);
        if (search_header_list_view_frame.getHeader() != null
                && search_header_list_view_frame.getHeader().findViewById(R.id.framelayout_refresh) != null) {
            search_header_list_view_frame.getHeader().findViewById(R.id.framelayout_refresh).setVisibility(INVISIBLE);
        }
        rl_no_history = (LinearLayout) findViewById(R.id.rl_no_history);
        ll_has_his = (LinearLayout) findViewById(R.id.ll_has_his);
        his_search_title = (LinearLayout) findViewById(R.id.his_search_title);
        hotTable = (MultiTagView) findViewById(R.id.search_hot_table);
        hisTable = (TableLayout) findViewById(R.id.search_his_table);
        hor_hotwords = (ObservableHorizontalScrollView) findViewById(R.id.hor_hotwords);
        ll_hottag = (LinearLayout) findViewById(R.id.ll_hottag);
    }

    public void refresh() {
        setHistoryVisiable(true);
        setHotTable();
    }


    private void setListener() {

        findViewById(R.id.search_his_clean_img).setOnClickListener(this);
        hor_hotwords.setScrollViewListener(new ObservableHorizontalScrollView.ScrollViewListener() {//滑动监听,获取图片
            @Override
            public void onScrollChanged(ObservableHorizontalScrollView scrollView, int x, int y, int oldx, int oldy) {
                if (x - oldx > 50) {
                    XHClick.mapStat(mActivity, "a_search_default", "热搜词", "左滑动");
                } else if (oldx - x > 50) {
                    XHClick.mapStat(mActivity, "a_search_default", "热搜词", "右滑动");
                }
            }

            @Override
            public void onScrollStateChange(boolean isScroll) {
                ishorizonScroll = isScroll;
            }
        });

        search_header_list_view_frame.disableWhenHorizontalMove(true);
        search_header_list_view_frame.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                search_header_list_view_frame.refreshComplete();
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return !ishorizonScroll;
            }
        });
    }


    public void setHistoryVisiable(boolean isShow) {
        if (isShow) {
            listSearchHistory.clear();
            listSearchHistory = new SearchDataImp().getHistoryWords();
            showHisTable();
        }
        showHotTable();
    }


    private void setHotTable() {
        new SearchDataImp().getHotWords(mActivity, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    List<Map<String, String>> list = UtilString.getListMapByJson(returnObj);
                    if (list.size() > 0) {
                        hotWords.clear();
                        String hotWord;
                        Map<String, String> tempMap;
                        for (Map<String, String> originalMap : list) {

                            hotWord = originalMap.get("");
                            tempMap = new HashMap<String, String>();
                            tempMap.put("hot", hotWord.trim());
                            hotWords.add(tempMap);
                        }
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showHotTable();
                            }
                        });
                    }
                    list.clear();
                }
                mActivity.loadManager.hideProgressBar();
            }
        });
    }

    private void showHotTable() {

        if (SearchConstant.SEARCH_CAIPU != limitSearchType) {
            his_search_title.setVisibility(View.GONE);
            rl_no_history.setVisibility(View.GONE);
            return;
        }

        if (listSearchHistory.size() == 0) {
            ll_has_his.setVisibility(View.GONE);
            rl_no_history.setVisibility(View.VISIBLE);
            hotTable.clearTags();
            hotTable.addTags(hotWords, new MultiTagView.MutilTagViewCallBack() {
                @Override
                public void onClick(int index) {
                    callback.disableEditFocus(true);
                    if (hotWords == null || hotWords.size() < 1)
                        return;

                    searchType = SearchConstant.SEARCH_CAIPU;
                    searchKey = hotWords.get(index).get("hot").trim();
                    XHClick.mapStat(mActivity, "a_search_default", "热搜词", searchKey);
                    callback.onSearchMsgChanged(searchKey, searchType);
                    callback.toSearch(searchKey, searchType);
                }
            });
            initAd();
        } else {
            ll_has_his.setVisibility(View.VISIBLE);
            rl_no_history.setVisibility(View.GONE);
            hotTable.removeAllTagView();
            ll_hottag.removeAllViews();
            AdapterSimple adapter = new AdapterSimple(hor_hotwords, hotWords, R.layout.a_hot_tag_item,
                    new String[]{"hot"}, new int[]{R.id.tag_type_tv});
            SetDataView.horizontalView(hor_hotwords, adapter, null, new SetDataView.ClickFunc[]{new SetDataView.ClickFunc() {
                @Override
                public void click(int index, View v) {

                    callback.disableEditFocus(true);
                    Assert.assertEquals(true, hotWords.size() > 0);
                    if (hotWords == null || hotWords.size() < 1)
                        return;
                    searchType = SearchConstant.SEARCH_CAIPU;
                    searchKey = hotWords.get(index).get("hot");
                    XHClick.mapStat(mActivity, "a_search_default", "热搜词", searchKey);
                    callback.onSearchMsgChanged(searchKey, searchType);
                    callback.toSearch(searchKey, searchType);
                }
            }});
            initAd();
            View view = LayoutInflater.from(mActivity).inflate(R.layout.a_hot_tag_start_item, null);
            ll_hottag.addView(view, 0);
        }
    }


    private void showHisTable() {
        hisTable.removeAllViews();
        ll_has_his.setVisibility(View.VISIBLE);
        AdapterSimple adapterHistory = new AdapterSimple(hisTable, listSearchHistory,
                R.layout.c_view_bar_search_item_history_global,
                new String[]{"search"},
                new int[]{R.id.item_search_history_global});
        SetDataView.view(hisTable, 1, adapterHistory, null, new SetDataView.ClickFunc[]{new SetDataView.ClickFunc() {
            @Override
            public void click(int index, View view) {
                if (index >= 0) {
                    callback.disableEditFocus(true);
                    Assert.assertEquals(true, listSearchHistory.size() > 0);
                    if (listSearchHistory == null || listSearchHistory.size() < 1)
                        return;
                    searchType = SearchConstant.SEARCH_CAIPU;
                    searchKey = listSearchHistory.get(index).get("search");
                    XHClick.mapStat(mActivity, "a_search_default", "搜索历史", searchKey);
                    callback.onSearchMsgChanged(searchKey, searchType);
                    callback.toSearch(searchKey, searchType);
                }
            }
        }}, android.view.ViewGroup.LayoutParams.MATCH_PARENT, ToolsDevice.dp2px(mActivity, 40));
        // 没有搜索历史时不显示
        int visibility = listSearchHistory.size() == 0 ? View.GONE : View.VISIBLE;
        findViewById(R.id.ll_has_his).setVisibility(visibility);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            // 清除搜索历史
            case R.id.search_his_clean_img:
                FileManager.delete(UtilFile.getDataDir() + FileManager.file_searchHis);
                XHClick.mapStat(mActivity, "a_search_default", "搜索历史", "清除历史");
                Log.i("tzy", "清除历史");
                listSearchHistory.clear();
                refresh();
                callback.disableEditFocus(false);
                break;
            default:
                break;
        }

    }


    public interface DefaultViewCallback {

        void disableEditFocus(boolean isClearFocus);

        void onSearchMsgChanged(String searchKey, int searchType);

        void toSearch(String searchKey, int searchType);

    }

    XHAllAdControl xhAllAdControl;
    boolean isLoad = false;

    private void initAd() {
        if(isLoad){
            return;
        }
        isLoad = true;
        RelativeLayout adBannerHasHis = (RelativeLayout) findViewById(R.id.rl_search_ad_gdt_has_history);
        RelativeLayout adBannerNoHis = (RelativeLayout) findViewById(R.id.rl_search_ad_gdt_no_history);
        ImageView imageViewHasHis = (ImageView) adBannerHasHis.findViewById(R.id.ad_banner_item_iv_single);
        ImageView imageViewNoHis = (ImageView) adBannerNoHis.findViewById(R.id.ad_banner_item_iv_single);
        ArrayList<String> list = new ArrayList<>();
        list.add(SEARCH_DEFAULT);
        xhAllAdControl = new XHAllAdControl(list, mActivity, "search_default");
        xhAllAdControl.start((isRefresh, map) -> {
            if (xhAllAdControl == null) {
                return;
            }
            BannerAd bannerAdBurden = new BannerAd(mActivity, xhAllAdControl, imageViewHasHis);
            bannerAdBurden.marginLeft = ((MarginLayoutParams)adBannerHasHis.getLayoutParams()).leftMargin;
            bannerAdBurden.marginRight = ((MarginLayoutParams)adBannerHasHis.getLayoutParams()).rightMargin;
            map = StringManager.getFirstMap(map.get(SEARCH_DEFAULT));
            bannerAdBurden.onShowAd(map);
            bannerAdBurden = new BannerAd(mActivity, xhAllAdControl, imageViewNoHis);
            bannerAdBurden.marginLeft = ((MarginLayoutParams)adBannerNoHis.getLayoutParams()).leftMargin;
            bannerAdBurden.marginRight = ((MarginLayoutParams)adBannerNoHis.getLayoutParams()).rightMargin;
            bannerAdBurden.onShowAd(map);
            xhAllAdControl.onAdBind(0, imageViewHasHis, "");
        });
        xhAllAdControl.registerRefreshCallback();
    }
}
