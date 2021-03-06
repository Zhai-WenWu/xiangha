package amodule.nous.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import org.json.JSONObject;

import java.util.Map;

import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.main.Main;
import amodule.user.db.BrowseHistorySqlite;
import amodule.user.db.HistoryData;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.web.ApiShowWeb;
import third.share.BarShare;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

public class NousInfo extends ApiShowWeb {

    private String code,titleText;
    private boolean isFav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XHClick.track(this, "浏览知识");
        code = getIntent().getStringExtra("code");
        if (TextUtils.isEmpty(code)) {
            Tools.showToast(this, "数据错误");
            finish();
        }
//        registerObserver();
    }

//    private IObserver mIObserver;
//    private void registerObserver(){
//        mIObserver = new IObserver() {
//            @Override
//            public void notify(String name, Object sender, Object data) {
//                requestFavoriteState();
//            }
//        };
//        ObserverManager.getInstance().registerObserver(mIObserver,ObserverManager.NOTIFY_LOGIN);
//    }
//
//    @Override
//    protected void onDestroy() {
//        ObserverManager.getInstance().unRegisterObserver(mIObserver);
//        super.onDestroy();
//    }

    @Override
    protected void setTitle() {
        title.setText("香哈头条");
        rightBtn.setVisibility(View.GONE);
        shareLayout.setVisibility(View.GONE);
        favLayout.setVisibility(View.GONE);
        homeLayout.setVisibility(View.GONE);
        shareLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (barShare != null) {
                    barShare.openShare();
                }
            }
        });
        homeLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Main.colse_level = 1;
                finish();
            }
        });
//        favLayout.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //7.29新添加统计
//                XHClick.mapStat(NousInfo.this, "a_collection", "香哈头条", "");
//                if (LoginManager.isLogin()) {
//                    FavoriteHelper.instance().setFavoriteStatus(NousInfo.this, code, titleText, FavoriteHelper.TYPE_NOUS,
//                            new FavoriteHelper.FavoriteStatusCallback() {
//                                @Override
//                                public void onSuccess(boolean state) {
//                                    isFav = state;
//                                    favoriteNousImageView.setImageResource(isFav ? R.drawable.z_caipu_xiangqing_topbar_ico_fav_active
//                                            : R.drawable.z_caipu_xiangqing_topbar_ico_fav);
//                                    favoriteNousTextView.setText(isFav ? "已收藏" : "  收藏  ");
//                                }
//
//                                @Override
//                                public void onFailed() {
//                                }
//                            });
//                } else {
//                    Intent intent = new Intent(NousInfo.this, LoginByAccout.class);
//                    startActivity(intent);
//                }
//            }
//        });
    }

    @Override
    public void loadData() {
//        requestFavoriteState();

        String apiUrl = StringManager.api_nousInfo + "?code=" + code;
        ReqInternet.in().doGet(apiUrl, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    Map<String, String> am = UtilString.getListMapByJson(returnObj).get(0);
                    saveHistoryToDB(am);
                    titleText = am.get("title");
                    String content = am.get("content");
                    nousCodeString = am.get("code");

                    if (content.length() > 28) {
                        content = content.substring(0, 28) + "..." + "(香哈菜谱)";
                    } else {
                        content = content + "(香哈菜谱)";
                    }
                    htmlData = am.get("html");
                    //http://www.xiangha.com/zhishi/236824.html   http://nativeapp.xiangha.com/
                    webview.loadDataWithBaseURL("https://www.xiangha.com/zhishi/" + NousInfo.this.getIntent().getStringExtra("code") + ".html", htmlData, "text/html", "utf-8", null);
                    XHClick.mapStat(XHApplication.in(), "a_share400", "香哈头条", "");
                    //这里的小知识文案不能动，影响统计加积分--FangRuijiao
                    barShare = new BarShare(NousInfo.this, "香哈头条", "");
                    String zhishiurl = StringManager.wwwUrl + "zhishi/" + am.get("code") + ".html";
                    barShare.setShare(BarShare.IMG_TYPE_WEB, am.get("title"), content, am.get("img"), zhishiurl);
                    rightBtn.setVisibility(View.VISIBLE);
                    shareLayout.setVisibility(View.VISIBLE);
                    homeLayout.setVisibility(View.GONE);
                } else
                    rightBtn.setVisibility(View.GONE);
            }
        });
    }

//    private void requestFavoriteState() {
//        FavoriteHelper.instance().getFavoriteStatus(this, code, FavoriteHelper.TYPE_NOUS,
//                new FavoriteHelper.FavoriteStatusCallback() {
//                    @Override
//                    public void onSuccess(boolean state) {
//                        //设置收藏按钮图片
//                        isFav = state;
//                        favoriteNousImageView.setImageResource(isFav ? R.drawable.z_caipu_xiangqing_topbar_ico_fav_active
//                                : R.drawable.z_caipu_xiangqing_topbar_ico_fav);
//                        favoriteNousTextView.setText(isFav ? "已收藏" : "  收藏  ");
//                        favLayout.setVisibility(View.VISIBLE);
//                    }
//
//                    @Override
//                    public void onFailed() {
//                    }
//                });
//    }

    private boolean saveHistoryOver = false;

    /** 保存历史 */
    private void saveHistoryToDB(final Map<String, String> map) {
        if (!saveHistoryOver) {
            saveHistoryOver = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = handlerJSONData(map);
                    HistoryData data = new HistoryData();
                    data.setCode(map.get("code"));
                    data.setBrowseTime(System.currentTimeMillis());
                    data.setDataJson(jsonObject.toString());
                    BrowseHistorySqlite sqlite = new BrowseHistorySqlite(NousInfo.this);
                    sqlite.insertSubject(BrowseHistorySqlite.TB_NOUS_NAME, data);
                }
            }).start();
        }
    }

    private JSONObject handlerJSONData(Map<String, String> map) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("allClick", map.get("allClick") + "");
            jsonObject.put("code", map.get("code") + "");
            jsonObject.put("content", map.get("content") + "");
            jsonObject.put("title", map.get("title") + "");
            jsonObject.put("img", map.get("img") + "");
        } catch (Exception ignored) {

        }
        return jsonObject;
    }

}
