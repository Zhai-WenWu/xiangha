package amodule.user.activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.popdialog.util.FullScreenManager;
import com.popdialog.util.PushManager;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.MessageTipController;
import acore.logic.VersionOp;
import acore.logic.XHClick;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.main.Main;
import amodule.other.activity.ClientDebug;
import amodule.other.activity.Comment;
import amodule.user.activity.login.AccoutActivity;
import amodule.user.activity.login.UserSetting;
import amodule.user.view.LeftAndRightTextView;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqInternet;
import aplug.web.ApiShowWeb;
import third.push.xg.XGPushServer;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

import static acore.tools.FileManager.SIZETYPE_MB;

/**
 * Created by ：fei_teng on 2017/2/21 20:32.
 */

public class Setting extends BaseLoginActivity implements View.OnClickListener {

    private RelativeLayout mQASetting;
    private RelativeLayout back;
    private ScrollView my_setting_scrollview;
    private RelativeLayout rl_user_info;
    private ImageView iv_user_icon;
    private TextView tv_nickname;
    private LinearLayout ll_accout;
    private LinearLayout ll_common_setting;
    private LeftAndRightTextView view_notify;
    private LeftAndRightTextView view_vip;
    private LeftAndRightTextView view_advise;
    private LeftAndRightTextView view_about;
    private LeftAndRightTextView view_change_sever;
    private LeftAndRightTextView view_platform;
    private LeftAndRightTextView view_qa_arbitration;
    private LeftAndRightTextView view_activity;
    private LeftAndRightTextView view_client_debug;
    private LeftAndRightTextView view_clear_cace;
    private LeftAndRightTextView view_check_update;
    private LinearLayout ll_internal_used;
    private LinearLayout ll_sign_out;
    private TextView tv_version;
    private Handler handler;
    public static final int ONREFRESH = 1;

    private long cacheSize;
    private String tel;
    private String phoneZone;
    private Button btn_bind_phone;
    private ImageView iv_bind_phone;

    private final String tongjiId = "a_setting520";
    private String nickName;
    private String img;
    private String userCode;

    private boolean isChecked = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 3, 0, 0, R.layout.user_info_setting);
        initData();
        initView();
        initTitle();
        initViewData();
    }

    private void initView() {
        TextView title = (TextView) findViewById(R.id.title);
        title.setText("设置");

        mQASetting = (RelativeLayout) findViewById(R.id.qa_setting);
        back = (RelativeLayout) findViewById(R.id.back);
        my_setting_scrollview = (ScrollView) findViewById(R.id.my_setting_scrollview);
        iv_user_icon = (ImageView) findViewById(R.id.iv_user_icon);
        tv_nickname = (TextView) findViewById(R.id.tv_nickname);

        rl_user_info = (RelativeLayout) findViewById(R.id.rl_user_info);
        ll_accout = (LinearLayout) findViewById(R.id.ll_accout);
        btn_bind_phone = (Button) findViewById(R.id.btn_bind_phone);
        iv_bind_phone = (ImageView) findViewById(R.id.iv_bind_phone);
        ll_common_setting = (LinearLayout) findViewById(R.id.ll_common_setting);
        ll_internal_used = (LinearLayout) findViewById(R.id.ll_internal_used);
        ll_sign_out = (LinearLayout) findViewById(R.id.ll_sign_out);

        view_notify = (LeftAndRightTextView) findViewById(R.id.view_notify);
        view_vip = (LeftAndRightTextView) findViewById(R.id.view_vip);
        if (AppCommon.isVip(LoginManager.userInfo.get("vip"))) {
            view_vip.setVisibility(View.VISIBLE);
        }
        view_clear_cace = (LeftAndRightTextView) findViewById(R.id.view_clear_cache);
        view_check_update = (LeftAndRightTextView) findViewById(R.id.view_check_update);
        view_advise = (LeftAndRightTextView) findViewById(R.id.view_advise);
        view_about = (LeftAndRightTextView) findViewById(R.id.view_about);
        view_change_sever = (LeftAndRightTextView) findViewById(R.id.view_change_sever);
        view_platform = (LeftAndRightTextView) findViewById(R.id.view_platform);
        view_qa_arbitration = (LeftAndRightTextView) findViewById(R.id.view_qa_arbitration);
        view_activity = (LeftAndRightTextView) findViewById(R.id.view_activity);
        view_client_debug = (LeftAndRightTextView) findViewById(R.id.view_client_debug);

        tv_version = (TextView) findViewById(R.id.tv_version);
        tv_version.setText("版本号:" + ToolsDevice.getVerName(this));

        mQASetting.setOnClickListener(this);
        back.setOnClickListener(this);
        rl_user_info.setOnClickListener(this);
        ll_sign_out.setOnClickListener(this);
        ll_accout.setOnClickListener(this);

        boolean isLogin = LoginManager.isLogin();
        mQASetting.setVisibility(isLogin ? View.VISIBLE : View.GONE);
        view_qa_arbitration.setVisibility(isLogin ? View.VISIBLE : View.GONE);
        showItemGrop();
        initSettingItem();
    }

    private void initViewData() {
        if (AppCommon.isVip(LoginManager.userInfo.get("vip"))) {
            getIsAuto();
        }
        setCacheSize();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUserInfo();
        if (LoginManager.userInfo.size() > 0) {
            getUserData();
        }
    }

    protected void initTitle() {
//        if (Tools.isShowTitle()) {
//            int topbarHeight = Tools.getDimen(this, R.dimen.topbar_height);
//            int height = topbarHeight + Tools.getStatusBarHeight(this);
//            RelativeLayout bar_title = (RelativeLayout) findViewById(R.id.title_rela_all);
//            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
//            bar_title.setLayoutParams(layout);
//            bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
//        }
        String colors = Tools.getColorStr(this, R.color.common_top_bg);
        Tools.setStatusBarColor(this, Color.parseColor(colors));
    }


    private void initData() {
        Map<String, String> userInfo = (Map<String, String>) FileManager.loadShared(mAct, FileManager.xmlFile_userInfo, "");
        nickName = userInfo.get("nickName");
        img = userInfo.get("img");
        tel = userInfo.get("tel");
        userCode = userInfo.get("userCode");

    }


    //获取用户数据
    private void getUserData() {
        String param = "type=getData&devCode=" + XGPushServer.getXGToken(this);
        ReqInternet.in().doPost(StringManager.api_getUserInfo, param, new InternetCallback() {

            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> listReturn = UtilString.getListMapByJson(returnObj);
                    if (listReturn.size() > 0) {
                        Map<String, String> user_info = listReturn.get(0);
                        img = user_info.get("img");
                        nickName = user_info.get("nickName");
                        tel = user_info.get("tel");
                        phoneZone = user_info.get("phoneZone");
                        setUserInfo();
                    }
                } else {
                    loadManager.setFailClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendRefreshMsg();
                        }
                    });
                }
                my_setting_scrollview.setVisibility(View.VISIBLE);
            }
        });
    }


    private void sendRefreshMsg() {
        if (handler != null)
            handler.sendEmptyMessage(ONREFRESH);
    }

    private void setUserInfo() {
        setUserImage(iv_user_icon, img);
        tv_nickname.setText(nickName);
        if (TextUtils.isEmpty(tel)) {
            btn_bind_phone.setVisibility(View.VISIBLE);
            iv_bind_phone.setVisibility(View.INVISIBLE);
        } else {
            btn_bind_phone.setVisibility(View.INVISIBLE);
            iv_bind_phone.setVisibility(View.VISIBLE);
        }
        showItemGrop();
    }


    private void showItemGrop() {

        //如果 是debug 或者 是管理员，就显示‘后台’和端口切换
        if (Tools.isDebug(this) || LoginManager.isManager() || isShowClientDebug()) {
            ll_internal_used.setVisibility(View.VISIBLE);
            view_client_debug.setVisibility(isShowClientDebug() ? View.VISIBLE : View.GONE);
        } else {
            ll_internal_used.setVisibility(View.GONE);
        }

        ll_common_setting.setVisibility(View.VISIBLE);
        if (LoginManager.userInfo.size() == 0) {
            rl_user_info.setVisibility(View.GONE);
            ll_accout.setVisibility(View.GONE);
            ll_sign_out.setVisibility(View.GONE);
            loadManager.hideProgressBar();
        } else {
            rl_user_info.setVisibility(View.VISIBLE);
            ll_accout.setVisibility(View.VISIBLE);
            ll_sign_out.setVisibility(View.VISIBLE);
        }
    }

    private void initSettingItem() {
        view_notify.init("消息通知", "", true, true,
                new LeftAndRightTextView.LeftAndRightTextViewCallback() {
                    @Override
                    public void onClick() {
                        XHClick.mapStat(Setting.this, tongjiId, "消息通知", "");
                        setMsgNotify();
                    }
                });
        final DialogManager dialogManager = new DialogManager(Setting.this);
        dialogManager.createDialog(new ViewManager(dialogManager)
                .setView(new TitleMessageView(Setting.this).setText("若关闭自动续费，会员到期后将不再自动扣除下月费用"))
                .setView(new HButtonView(Setting.this)
                        .setNegativeText("不关闭", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogManager.cancel();
                            }
                        })
                        .setPositiveTextColor(Color.parseColor("#007aff"))
                        .setPositiveText("确认关闭", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogManager.cancel();
                                setIsAuto(false);
                            }
                        })));

        view_vip.init("香哈会员自动续费", "", true, false, null);
        view_vip.setSwitch(true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isChecked) //要关闭
                    dialogManager.show();
                else {
                    setIsAuto(true);
                }
            }
        });

        view_clear_cace.init("清理缓存", "", false, new LeftAndRightTextView.LeftAndRightTextViewCallback() {
            @Override
            public void onClick() {
                XHClick.mapStat(Setting.this, tongjiId, "清理缓存", "");
                clearCache();
            }});
        view_check_update.init("检查新版本", ToolsDevice.getVerName(this), false, new LeftAndRightTextView.LeftAndRightTextViewCallback() {
            @Override
            public void onClick() {
                XHClick.mapStat(Setting.this, tongjiId, "检查新版本", "");
                VersionOp.getInstance().toUpdate(new VersionOp.OnCheckUpdataCallback() {
                    @Override
                    public void onPreUpdate() {
                        loadManager.startProgress("正在获取最新版本信息");
                    }

                    @Override
                    public void onNeedUpdata() {
                        loadManager.dismissProgress();
                    }

                    @Override
                    public void onNotNeed() {
                        loadManager.dismissProgress();
                    }

                    @Override
                    public void onFail() {
                        loadManager.dismissProgress();
                    }
                },true);
            }

        });
        view_advise.init("给香哈评价", "", true, true, new LeftAndRightTextView.LeftAndRightTextViewCallback() {
            @Override
            public void onClick() {
                XHClick.mapStat(Setting.this, tongjiId, "给香哈评价", "");
                giveAdvise();
            }

        });
        view_about.init("关于我们", "", false, true, new LeftAndRightTextView.LeftAndRightTextViewCallback() {
            @Override
            public void onClick() {
                XHClick.mapStat(Setting.this, tongjiId, "关于我们", "");
                seeAboutInfo();
            }

        });
        view_change_sever.init("服务器切换", "", true, true, new LeftAndRightTextView.LeftAndRightTextViewCallback() {
            @Override
            public void onClick() {

                changeServer();
            }

        });
        view_platform.init("后台", "", true, true, new LeftAndRightTextView.LeftAndRightTextViewCallback() {
            @Override
            public void onClick() {

                toPlatform();
            }

        });
        view_qa_arbitration.setNewHintVisibility(MessageTipController.newInstance().hasArbitration ? View.VISIBLE : View.GONE);
        view_qa_arbitration.init("问答仲裁", "", !MessageTipController.newInstance().hasArbitration, new LeftAndRightTextView.LeftAndRightTextViewCallback() {
            @Override
            public void onClick() {
                view_qa_arbitration.setNewHintVisibility(View.GONE);
                view_qa_arbitration.setArrowRightVisibility(View.VISIBLE);
                MessageTipController.newInstance().hasArbitration = false;
                openQAArbitration();
            }
        });
        view_client_debug.init("Debug", "", true, true, new LeftAndRightTextView.LeftAndRightTextViewCallback() {
            @Override
            public void onClick() {
                startActivity(new Intent(mAct,ClientDebug.class));
            }
        });
        view_activity.init("活动", "", false, true, new LeftAndRightTextView.LeftAndRightTextViewCallback() {
            @Override
            public void onClick() {
                toActivityPage();
            }

        });

    }

    /**
     * @return
     */
    private boolean isShowClientDebug(){
        if(!LoginManager.isLogin()){
            return false;
        }
        String currentUserCode = LoginManager.userInfo.get("code");
        if(TextUtils.isEmpty(currentUserCode)){
            return false;
        }
        String[] canDebugCodeArray = {"547050520741", "100602334991", "72228625021", "42737350684",
                "48977470607", "6393152531", "1949369181", "809959148911", "49282607107",
                "813991925811", "72228625021", "10191"};
        for(String code:canDebugCodeArray){
            if(TextUtils.equals(code,currentUserCode)){
                return true;
            }
        }
        return false;
    }

    private void setCacheSize() {
        new Thread(() -> {
            long fileSize = FileManager.getFileOrFolderSize(FileManager.getDataDir() + FileManager.file_appData);
            fileSize += FileManager.getFileOrFolderSize(FileManager.getSDDir() + LoadImage.SAVE_CACHE);
            cacheSize = fileSize;
            Setting.this.runOnUiThread(() -> view_clear_cace.setRightText(FileManager.formatFileSize(cacheSize,SIZETYPE_MB)));
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_sign_out:
                XHClick.mapStat(Setting.this, tongjiId, "退出账号", "");
                loadManager.showProgressBar();
                Main.colse_level = 4;
                LoginManager.logout(this);
                break;
            case R.id.rl_user_info:
                XHClick.mapStat(this, tongjiId, "点击头像和昵称", "");
                gotoUserSetting();
                break;
            case R.id.ll_accout:
                XHClick.mapStat(this, tongjiId, "账号与安全", "");
                gotoAcccoutSetting();
                break;
            case R.id.back:
                finish();
                break;
            case R.id.qa_setting:
                AppCommon.openUrl(Setting.this, StringManager.API_QA_QASETTING + "?notify=" + (PushManager.isNotificationEnabled(Setting.this) ? "2" : "1"), false);
                break;
            default:
                break;
        }

    }

    private void gotoAcccoutSetting() {
        Intent intent = new Intent(this, AccoutActivity.class);
        intent.putExtra(PHONE_NUM, tel);
        intent.putExtra(ZONE_CODE, phoneZone);
        startActivity(intent);
    }

    private void gotoUserSetting() {
        Intent intent = new Intent(this, UserSetting.class);
        startActivity(intent);
    }


    private void setMsgNotify() {
        Intent intent = new Intent(this, MyMsgInformSetting.class);
        startActivity(intent);
    }

    private void clearCache() {
        if (cacheSize == 0)
            return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                FileManager.delete(FileManager.getDataDir() + FileManager.file_appData);
                FileManager.delete(FileManager.getSDDir() + LoadImage.SAVE_CACHE);
                FullScreenManager.saveWelcomeInfo(Setting.this,null,null);
                Setting.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Tools.showToast(Setting.this, "缓存已清除");
                        view_clear_cace.setRightText("0.00MB");
                        cacheSize = 0;
                    }
                });

            }
        }).start();
    }

    private void giveAdvise() {
        Intent comment = new Intent(this, Comment.class);
        startActivity(comment);
    }

    private void seeAboutInfo() {
        Intent aboutUs = new Intent(this, ApiShowWeb.class);
        aboutUs.putExtra("url", StringManager.api_aboutus);
        aboutUs.putExtra("name", "关于我们");
        startActivity(aboutUs);
    }

    private void changeServer() {
        Intent it = new Intent(this, ChangeUrl.class);
        startActivity(it);
    }

    private void toPlatform() {
        AppCommon.openUrl(this, StringManager.mmUrl, true);
    }

    private void openQAArbitration() {
        AppCommon.openUrl(this, StringManager.API_QA_QAARBITRATION, true);
    }

    private void toActivityPage() {
        AppCommon.openUrl(this, "activityList.app", true);
    }

    public void setUserImage(final ImageView v, String value) {
        v.setVisibility(View.VISIBLE);
        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(this)
                .load(value)
                .setPlaceholderId(R.drawable.a_login_setting_head)
                .setErrorId(R.drawable.a_login_setting_head)
                .setImageRound(ToolsDevice.dp2px(Setting.this, 500))
                .build();
        if (bitmapRequest != null)
            bitmapRequest.into(v);

    }

    private void setIsAuto(final boolean isAuto) {
        XHClick.mapStat(Setting.this, tongjiId, "香哈会员自动续费", "");
        ReqInternet.in().doPost(StringManager.api_setIsAuto, "userCode=" + LoginManager.userInfo.get("code") + "&isAuto=" + (isAuto ? "2" : "1"), new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                isChecked = i >= ReqInternet.REQ_OK_STRING ? isAuto : !isAuto;
                view_vip.switchState(isChecked);
            }
        });
    }

    private void getIsAuto() {
        ReqInternet.in().doGet(StringManager.api_isAuto + "?userCode=" + LoginManager.userInfo.get("code"), new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(o);
                    if (arrayList.size() >= 0) {
                        String isAuto = arrayList.get(0).get("is_auto");
                        isChecked = "2".equals(isAuto);
                        view_vip.switchState(isChecked);
                    }
                }
            }
        });
    }
}
