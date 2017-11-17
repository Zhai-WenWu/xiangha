package amodule.dish.view.manager;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import amodule.dish.activity.DetailDish;
import amodule.main.Main;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;

/**
 * 菜谱详情页面数据管理者
 */
public class DetailDishDataManager {
    public final static String DISH_DATA_TOP = "dish_top";//topInfo数据类型
    public final static String DISH_DATA_BASE = "dish_base";//菜谱基础数据类型接口
    public final static String DISH_DATA_INGRE = "dish_ingre";//用料
    public final static String DISH_DATA_BANNER = "dish_banner";//banner
    public final static String DISH_DATA_STEP = "dish_step";//步骤
    public final static String DISH_DATA_USER = "dish_user";//用户信息
    public final static String DISH_DATA_TIE = "dish_tie";//用户信息
    public final static String DISH_DATA_LIKE = "dish_like";//菜谱点赞处理
    private String dishCode;//菜谱code
    private Context mContext = XHActivityManager.getInstance().getCurrentActivity().getApplicationContext();
    private String customerCode;//用户code
    //权限
    private Map<String,String> permissionMap = new HashMap<>();
    private Map<String,String> detailPermissionMap = new HashMap<>();
    private String lastPermission = "";
    private boolean hasPermission = true;
    private boolean contiunRefresh = true;
    private boolean loadOver = false;

    public DetailDishDataManager(String code) {
        dishCode = code;
        reqTopInfo();
    }
    //重置权限数据
    private void resetData(){
        loadOver = false;
        hasPermission = true;
        contiunRefresh = true;
        lastPermission = "";
        detailPermissionMap.clear();
        permissionMap.clear();
    }
    /**
     * 第一次请求接口合集
     */
    public void reqOne() {
        reqDishBase();
        reqDishUser();
        reqIngre();
        reqBanner();
    }

    public void reqTwo() {
        reqStep();
        reqOtherTieData();
        reqOtherData();
    }

    /**
     * 请求topInfo数据---第一请求，有权限请求
     */
    private void reqTopInfo() {
        String params = "code=" + dishCode;
        ReqEncyptInternet.in().doEncypt(StringManager.api_getDishTopInfo, params, new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String url, Object object) {
                if(flag>=UtilInternet.REQ_OK_STRING) {
                    customerCode= StringManager.getFirstMap(object).get("customerCode");
                    handleDataSuccess(flag, DISH_DATA_TOP, object);
                    reqOne();
                    reqTwo();
                }
            }

            @Override
            public void getPower(int flag, String url, Object obj) {
                //权限检测
                if(permissionMap.isEmpty() && !TextUtils.isEmpty((String)obj) && !"[]".equals(obj)&& !"{}".equals(obj)){
                    if(TextUtils.isEmpty(lastPermission)){
                        lastPermission = (String) obj;
                    }else{
                        contiunRefresh = !lastPermission.equals(obj.toString());
                        if(contiunRefresh)
                            lastPermission = obj.toString();
                    }
                    permissionMap = StringManager.getFirstMap(obj);
                    if(permissionMap.containsKey("page")){
                        Map<String,String> pagePermission = StringManager.getFirstMap(permissionMap.get("page"));
//                        hasPermission = dishActivityViewControl.analyzePagePermissionData(pagePermission);
                        if(!hasPermission) return;
                    }
                    if(permissionMap.containsKey("detail"))
                        detailPermissionMap = StringManager.getFirstMap(permissionMap.get("detail"));
                }else if(loadOver && TextUtils.isEmpty(lastPermission)){
                    contiunRefresh = false;
                }
            }
        });
    }

    /**
     * 请求菜谱基本信息
     */
    private void reqDishBase() {
        String params = "code=" + dishCode;
        ReqEncyptInternet.in().doEncypt(StringManager.API_GETDISHBASICINFOBYCODE, params, new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String url, Object object) {
                handleDataSuccess(flag,DISH_DATA_BASE,object);
            }
        });
    }
    /**
     * 请求用户信息
     */
    private void reqDishUser() {
        String params = "code=" + customerCode;
        ReqEncyptInternet.in().doEncypt(StringManager.API_GETUSERINFOBYCODE, params, new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String url, Object object) {
                handleDataSuccess(flag,DISH_DATA_USER,object);
            }
        });
    }
    /**
     * 请求用料数据
     */
    private void reqIngre() {
        String params = "code=" + dishCode;
        ReqEncyptInternet.in().doEncypt(StringManager.API_GETDISHBURDENBYCODE, params, new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String url, Object object) {
                handleDataSuccess(flag,DISH_DATA_INGRE,object);
            }
        });
    }

    /**
     * 请求banner数据
     */
    private void reqBanner() {
        String params = "code=" + dishCode;
        ReqEncyptInternet.in().doEncypt(StringManager.API_GETBELOWBURDENBANNER, params, new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String url, Object object) {
                handleDataSuccess(flag,DISH_DATA_BANNER,object);
            }
        });
    }

    /**
     * 请求步骤数据
     */
    private void reqStep() {
        String params = "code=" + dishCode;
        ReqEncyptInternet.in().doEncypt(StringManager.API_getDishMakeByCode, params, new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String url, Object object) {
                handleDataSuccess(flag,DISH_DATA_STEP,object);
            }
        });
    }

    /**
     * 请求帖子数据
     */
    private void reqOtherTieData(){
        String params = "code=" + dishCode;
        //获取帖子数据
        ReqEncyptInternet.in().doEncypt(StringManager.api_getDishTieInfo,params, new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String s, Object object) {
                handleDataSuccess(flag,DISH_DATA_TIE,object);
            }
        });
    }
    /**
     * 请求其他接口数据
     */
    private void reqOtherData(){
        String params = "code=" + dishCode;
        //获取点赞数据
        ReqEncyptInternet.in().doEncypt(StringManager.api_getDishLikeNumStatus, params, new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String s, Object object) {
                handleDataSuccess(flag,DISH_DATA_LIKE,object);
            }
        });

        ReqEncyptInternet.in().doEncypt(StringManager.api_getDishstatusValue, params, new InternetCallback(mContext) {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqInternet.REQ_OK_STRING){
//                    saveApiData(o.toString());
                }
            }
        });
    }
    /**
     * 集中处理接口返回数据
     * @param flag
     * @param type
     * @param object
     */
    public void handleDataSuccess(int flag, String type,Object object){
        if (flag >= UtilInternet.REQ_OK_STRING && dishDataCallBack != null) {
            ArrayList<Map<String,String>> list=StringManager.getListMapByJson(object);
            if(list.size()>0)dishDataCallBack.handlerTypeData(type,list);
        }
    }

    public DishDataCallBack dishDataCallBack;
    /**
     * 接口请求数据回调
     */
    public interface DishDataCallBack {
        public void handlerTypeData(String type, ArrayList<Map<String,String>> list);
    }
    public void setDishDataCallBack(DishDataCallBack callBack) {
        dishDataCallBack = callBack;
    }
}
