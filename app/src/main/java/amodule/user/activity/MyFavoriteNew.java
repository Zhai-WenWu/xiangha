package amodule.user.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.rvlistview.RvListView;
import amodule.main.Main;
import amodule.user.adapter.AdapterMyFavorite;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;

/**
 * 我的收藏页面改版
 */
public class MyFavoriteNew extends BaseActivity implements View.OnClickListener{
    private ArrayList<Map<String, String>> mapList= new ArrayList<>();
    private TextView title,rightText;
    private RvListView rvListview;
    private EditText ed_search_word;
    private AdapterMyFavorite myFavorite;
    private String name="";//当前搜索的名称
    private int currentpage=0;//页面号码
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 2, 0, 0, R.layout.a_my_favorite);
        initUi();
        initData();
    }
    private void initUi() {
        title= (TextView) findViewById(R.id.title);
        rightText= (TextView) findViewById(R.id.rightText);
        title.setText("我的收藏");
        rightText.setText("浏览历史");
        rightText.setTextColor(Color.parseColor("#fffffe"));
        rvListview= (RvListView) findViewById(R.id.rvListview);
        findViewById(R.id.seek_but_rela).setOnClickListener(this);
        findViewById(R.id.btn_search_global).setOnClickListener(this);
        ed_search_word= (EditText) findViewById(R.id.ed_search_word);
        ed_search_word.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        isShowEditView(false);
    }
    private void initData(){
        myFavorite= new AdapterMyFavorite(this,mapList);
        rvListview.setAdapter(myFavorite);
//        loadManager.setLoading(rvListview, myFavorite, true, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
                requestData();
//            }
//        });
    }
    /**
     * 请求数据
     */
    private void requestData(){
        String url= StringManager.API_COLLECTIONLIST;
        ++currentpage;
        String params= "page="+currentpage;
        if(!TextUtils.isEmpty(name))params+="&name="+name;
//        loadManager.showProgressBar();
        ReqEncyptInternet.in().doEncypt(url, params, new InternetCallback(this) {
            @Override
            public void loaded(int flag, String url, Object msg) {
                if(flag>= ReqInternet.REQ_OK_STRING){
                    Log.i(Main.TAG,"msg::"+msg);
                    Map<String,String> maps= StringManager.getFirstMap(msg);
                    if(maps.containsKey("list")&&!TextUtils.isEmpty(maps.get("list"))){
                        ArrayList<Map<String,String>> listMaps= StringManager.getListMapByJson(maps.get("list"));
                        mapList.addAll(listMaps);
                        myFavorite.notifyDataSetChanged();
                    }
                }
//                loadManager.hideProgressBar();
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.seek_but_rela:
                isShowEditView(true);
                break;
            case R.id.btn_search_global:
                handlerSearch();
                break;
                default:
                    break;
        }
    }

    /**
     * 处理搜索
     */
    private void handlerSearch(){
        name= ed_search_word.getText().toString();
        if(TextUtils.isEmpty(name)){
            Tools.showToast(this,"傻逼没有数据");
            return;
        }
        currentpage=0;
        requestData();
    }
    /**
     * 判断是否显示
     * @param isShow
     */
    private void isShowEditView(boolean isShow){
        findViewById(R.id.seek_edit_rela).setVisibility(isShow ? View.VISIBLE : View.GONE);
        findViewById(R.id.seek_but_rela).setVisibility(isShow ? View.GONE : View.VISIBLE);
    }
}
