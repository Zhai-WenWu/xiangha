package acore.logic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.annimon.stream.Stream;
import com.popdialog.util.PushManager;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;
import com.xiangha.Welcome;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.stat.StatConf;
import acore.logic.stat.StatModel;
import acore.logic.stat.StatisticsManager;
import acore.notification.controller.NotificationSettingController;
import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.main.Main;
import amodule.main.activity.MainHomePage;
import amodule.other.listener.HomeKeyListener;
import amodule.other.listener.HomeKeyListener.OnHomePressedListener;
import aplug.basic.ReqEncyptInternet;
import third.ad.XHAdAutoRefresh;
import third.ad.tools.AdConfigTools;
import third.ad.tools.WelcomeAdTools;
import third.mall.aplug.MallCommon;

import static acore.logic.ConfigMannager.KEY_RANDPROMOTION;
import static amodule.main.Main.colse_level;

public class ActivityMethodManager {
    private Activity mAct;
    //监听home键的
    private HomeKeyListener mHomeWatcher;
    private ArrayList<IAutoRefresh> mAdControls = new ArrayList<>();
    private long lastOnResumeTime,intervalOnResumeTime;
    private long onResumeTime = 0;
    private String mStatF;

    public static boolean isAppShow=false;

    public ActivityMethodManager(Activity mAct) {
        this.mAct = mAct;
        /*随机广告*/
        randPromotion();
        initStatData();
    }

    private void initStatData() {
        if (mAct != null && mAct.getIntent() != null) {
            mStatF = mAct.getIntent().getStringExtra(StatConf.STAT_F);
        }
    }

    public void onRestart(){
    }

    public void onResume(int level) {
        if(Main.allMain != null){
            Main.allMain.initRunTime();
        }
        onResumeTime = System.currentTimeMillis();
        if(lastOnResumeTime == 0){
            lastOnResumeTime = System.currentTimeMillis();
        }else{
            intervalOnResumeTime = System.currentTimeMillis() - lastOnResumeTime;
        }
        //广告刷新定时器开始
        XHAdAutoRefresh.getInstance().startTimer(this,intervalOnResumeTime);
        //Log.i("FRJ", "level:" + level);
        //Log.i("FRJ", "colse_level:" + colse_level);
        MobclickAgent.onResume(mAct);
        StatService.onResume(mAct);//mta腾讯统计
        // 应用到后台时如果数据被清理，需要重新自动登录
        if (LoginManager.userInfo.size() == 0) {
            Map<String, String> userInfoMap = (Map<String, String>) FileManager.loadShared(mAct, FileManager.xmlFile_userInfo, "");
            if (userInfoMap.get("userCode") != null && userInfoMap.get("userCode").length() > 1) {
                LoginManager.loginByAuto(mAct);
                MallCommon.getSaveMall(mAct);//处理电商
            }
        }

        if (WelcomeAdTools.getInstance().checkTwiceSplashEnable()) {
            WelcomeAdTools.getInstance().resetTime();
            AdConfigTools.getInstance().getAdConfigInfo();
            mAct.startActivity(new Intent(mAct, Welcome.class));
        }

        registerHomeListener();
        // 控制页面关闭
        if (colse_level <= level) {
            if (level == 1 && colse_level != 0) {
                if (Main.allMain != null) {
                    Main.allMain.setCurrentTabByClass(MainHomePage.class);
                }
                colse_level = 1000;
            } else {
                mAct.finish();
            }
        }
        //电商是3，登录界面是4，其他页面是5，加次判断是为了解决从首页发视频菜谱，跳到上传列表，通过colseLevel关闭发视频菜谱页面，饼跳到菜谱列表页面
        else if (colse_level != 6 || level < 4) {
            colse_level = 1000;
        }
        //通知开启后统计
        if(!TextUtils.isEmpty((CharSequence) FileManager.loadShared(XHApplication.in(),FileManager.app_notification,FileManager.push_setting_state))
                &&"2".equals(FileManager.loadShared(XHApplication.in(),FileManager.app_notification,FileManager.push_setting_state))){
            FileManager.saveShared(XHApplication.in(),FileManager.app_notification,FileManager.push_setting_state,"");
            if(PushManager.isNotificationEnabled(XHActivityManager.getInstance().getCurrentActivity())) {
                String key = (String) FileManager.loadShared(XHApplication.in(),FileManager.app_notification,FileManager.push_setting_message);
                XHClick.mapStat(XHApplication.in(), "a_push_guidelayer",key,"开启成功");
                if(mAct != null){
                    NotificationSettingController.statOpenSuccess(mAct.getClass().getSimpleName());
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    public void onPause() {
        long stayTime = System.currentTimeMillis() - onResumeTime;
        StatisticsManager.saveData(StatModel.createPageStayModel(mAct.getClass().getSimpleName(),String.format("%.2f",stayTime/1000f), TextUtils.isEmpty(mStatF) ? StatModel.EMPTY : mStatF));
        //广告刷新定时器停止
        XHAdAutoRefresh.getInstance().stopTimer();
        MobclickAgent.onPause(mAct);
        StatService.onPause(mAct);//mta腾讯统计
        XHClick.sendBrowseCodes(mAct);
        if (mHomeWatcher != null)
            mHomeWatcher.stopWatch();
    }

    public void onStop() {
    }

    public void onDestroy() {
        //清除还没有请求的接口
        ReqEncyptInternet.in().clearListIntenert();
        unregisterAllAdController();
    }

    public void onUserLeaveHint(){
        Log.i("tzy", "onUserLeaveHint: ");
        if(Main.allMain != null){
            Main.allMain.stopTimer();
        }
    }

    /**
     * 注册Home键的监听
     */
    private void registerHomeListener() {
        if (mHomeWatcher == null) {
            mHomeWatcher = new HomeKeyListener(mAct);
            mHomeWatcher.setOnHomePressedListener(new OnHomePressedListener() {
                @Override
                public void onHomePressed() {
                    //刷新广告配置数据
                    AdConfigTools.getInstance().getAdConfigInfo();
                    // 进行点击Home键的处理
                    Log.i("zhangyujian", "HomeKeyListener111");
                    WelcomeAdTools.getInstance().recordTime();
                    WelcomeAdTools.getInstance().handlerAdData(true);
                    if(ActivityMethodManager.isAppShow) {
                        ActivityMethodManager.isAppShow = false;
                        if(Main.allMain!=null&&Main.allMain.allTab.get(MainHomePage.KEY)!=null){
                            ((MainHomePage)Main.allMain.allTab.get(MainHomePage.KEY)).handleNoGif();
                        }
                    }
                }

                // 进行长按Home键的处理
                @Override
                public void onHomeLongPressed() {
                }
            });
        }
        mHomeWatcher.startWatch();
    }

    /** 随机广告推广的问题 */
    private  void randPromotion() {
        //获取 Acitivty 完整路径名
        String classKey = mAct.getComponentName().getClassName();
        //获取 config 中的 randPromotion 数据
        Map<String,String> randProConfigMap = StringManager.getFirstMap(ConfigMannager.getConfigByLocal(KEY_RANDPROMOTION));
        if(randProConfigMap.containsKey(classKey)
                && "2".equals(randProConfigMap.get(classKey))){
            //数据
            String text = AppCommon.loadRandPromotionData().trim();
            if(!TextUtils.isEmpty(text)){
                //写如剪切板
                Tools.inputToClipboard(mAct,text);
//                Log.i("tzy","inputToClipboard :: text = " + text);
            }
        }
    }

    public void registerADController(IAutoRefresh control){
        if(control != null
                && mAdControls != null
                && !mAdControls.contains(control)){
            mAdControls.add(control);
        }
    }

    public void unregisterADController(IAutoRefresh control){
        if(control != null
                && mAdControls != null){
            mAdControls.remove(control);
        }
    }

    public void unregisterAllAdController(){
        if(mAdControls != null){
            mAdControls.clear();
        }
    }

    public void autoRefreshSelfAD(){
        Stream.of(mAdControls)
                .filter(value -> null != value)
                .forEach(IAutoRefresh::autoRefreshSelfAD);
    }

    public interface IAutoRefresh{
        void autoRefreshSelfAD();
    }

    public interface IAutoRefreshCallback{
        void refreshSelfAD();
    }

}
