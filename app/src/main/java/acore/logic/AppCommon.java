/**
 * @author Jerry
 * 2013-1-22 下午3:00:33
 * Copyright: Copyright (c) xiangha.com 2011
 */
package acore.logic;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.download.container.DownloadCallBack;
import com.download.down.DownLoad;
import com.download.tools.FileUtils;
import com.xiangha.R;

import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import acore.logic.load.LoadManager;
import acore.override.XHApplication;
import acore.override.activity.base.WebActivity;
import acore.override.activity.mian.MainBaseActivity;
import acore.tools.FileManager;
import acore.tools.LogManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.db.DataOperate;
import amodule.dish.db.ShowBuySqlite;
import amodule.health.activity.HealthTest;
import amodule.health.activity.MyPhysique;
import amodule.main.Main;
import amodule.main.view.CommonBottomView;
import amodule.quan.db.CircleData;
import amodule.quan.db.CircleSqlite;
import amodule.user.activity.ChangeUrl;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.basic.XHConf;
import aplug.web.FullScreenWeb;
import aplug.web.ShowWeb;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;
import third.ad.scrollerAd.XHAllAdControl;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilString;
import xh.windowview.BottomDialog;

import static xh.basic.tool.UtilString.getListMapByJson;

public class AppCommon {
    public static int quanMessage = 0; // 美食圈新消息条数
    public static int feekbackMessage = 0; // 系统新消息条数
    public static int buyBurdenNum = 0; // 离线清单条数
    public static int follwersNum = -1; // 关注人数

    public static int nextDownDish = -1;
    public static int maxDownDish = 10000;

    private static int fiallNum = 0;

    /**
     * 获取公用数据消息
     */
    public static void getCommonData(final InternetCallback callback) {
        ReqInternet.in().doGet(StringManager.api_commonData + "?m=commonData", new InternetCallback(XHApplication.in()) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    fiallNum = 0;
                    String[] alertArr = ((String) returnObj).split("-");
                    if (alertArr != null && alertArr.length > 2) {
                        quanMessage = Integer.parseInt(alertArr[1]);
                        feekbackMessage = Integer.parseInt(alertArr[2]);
                        try {
                            // 所有消息数
                            Main.setNewMsgNum(3, quanMessage + feekbackMessage);
                            // tok值
                            long tok = Integer.parseInt(alertArr[0]);
                            int c = (new Random()).nextInt(9) + 1;
                            LoadManager.tok = c + "" + (tok + 54321) * c;
                        } catch (Exception e) {
                            LogManager.reportError("获取心跳消息", e);
                        }
                    }
                    if (callback != null) callback.loaded(flag, url, "加载成功");
                } else if (fiallNum < 3) {
                    fiallNum++;
                    getCommonData(callback);
                } else if (callback != null) {
                    callback.loaded(flag, url, "加载失败");
                }
            }
        });
    }

    /**
     * 获取应用初始信息
     *
     * @param act
     * @param callback ：回调
     */
    // 获取应用初始信息
    public static void getIndexData(final Context act, final InternetCallback callback) {
        final String indexJson = FileManager.readFile(FileManager.getDataDir() + FileManager.file_indexData);

        // 是否在n分钟内修改过，且一定包含hotUser项，才可加载局部
        final boolean isPart = FileManager.ifFileModifyByCompletePath(FileManager.getDataDir() + FileManager.file_indexData, 15) != null;
        boolean isNetOk = ToolsDevice.getNetActiveState(act);
        LogManager.print("d", "isPart is:" + isPart);
        if (!isPart && isNetOk) {
            // 通过文件时间判断加载哪部分数据
            // String url = StringManager.api_indexData + "?type=newData";
            String url = StringManager.api_indexDataNew;
            // 请求网络信息
            ReqInternet.in().doGet(url, new InternetCallback(XHApplication.in()) {
                @Override
                public void loaded(int flag, String url, final Object returnObj) {
                    if (flag >= ReqInternet.REQ_OK_STRING) {
                        ArrayList<Map<String, String>> list = getListMapByJson(returnObj);
                        final Map<String, String> map = list.get(0);
                        if (url.indexOf("&type=part") > -1) {
                            callback.loaded(flag, "part", map);
                        } else {
                            callback.loaded(flag, "newData", map);
                            FileManager.saveFileToCompletePath(FileManager.getDataDir() + FileManager.file_indexData, (String) returnObj, false);
                        }
                    } else {
                        if (indexJson.length() > 10) {
                            ArrayList<Map<String, String>> list = getListMapByJson(indexJson);
                            if (list.size() == 1) {
                                Map<String, String> map = getListMapByJson(indexJson).get(0);
                                if (map.size() > 2) {
                                    // 加载文件中数据
                                    callback.loaded(ReqInternet.REQ_OK_STRING, "file", map);
                                }
                            }
                        }
                        callback.loaded(flag, "file", returnObj);
                    }
                }
            });
        } else {
            if (indexJson.length() > 10) {
                ArrayList<Map<String, String>> list = getListMapByJson(indexJson);
                if (list.size() == 1) {
                    Map<String, String> map = getListMapByJson(indexJson).get(0);
                    if (map.size() > 2) {
                        // 加载文件中数据
                        callback.loaded(ReqInternet.REQ_OK_STRING, "file", map);
                    } else {
                        getInterExstoreData(act, callback);
                    }
                } else {
                    getInterExstoreData(act, callback);
                }
            } else {
                getInterExstoreData(act, callback);
            }
        }
    }

    private static void getInterExstoreData(Context act, InternetCallback callback) {
        String fromAssets = UtilFile.getFromAssets(act, FileManager.file_indexData);
        Map<String, String> mapByJson = getListMapByJson(fromAssets).get(0);
        callback.loaded(ReqInternet.REQ_OK_STRING, "file", mapByJson);
    }

    public static void deleteIndexData() {
        FileManager.delDirectoryOrFile(FileManager.getDataDir() + FileManager.file_indexData);
    }

    /**
     * 打开url 如果url能打开原生页面就开原生
     * 如果不能就开webview，如果openThis为true,或已打开的webview太多，则直接使用WebView打开Url
     * @param act
     * @param url
     * @param openThis
     */
    public static void openUrl(final Activity act, String url, Boolean openThis) {
        //url为null直接不处理
        if (TextUtils.isEmpty(url)) return;
        if ( !url.startsWith("xiangha://welcome?") && !url.startsWith("http")
                && (!url.contains(".app") && !url.contains("circleHome"))
                ) return;
        // 如果识别到外部开启链接，则解析
        try {
            if (url.indexOf("xiangha://welcome?") == 0) {
                //按#分割，urls【1】是表示外部吊起的平台例如360
                String[] urls = url.replace("xiangha://welcome?", "").split("#");
                if (urls.length > 0) {
                    url = StringManager.wwwUrl;
                    try {
                        url = URLDecoder.decode(urls[0], HTTP.UTF_8);
                        if (url.contains("url=")) {
                            url = url.substring(url.indexOf("url=") + 4);
                        }
                    } catch (UnsupportedEncodingException e) {
                        LogManager.reportError("URLDecoder异常", e);
                    }
                }
                if (!TextUtils.isEmpty(url) && urls.length > 1) {
                    //不会有.app了，变成包名加类名啦
                    int indexs = url.indexOf(".app");
                    String data = url.substring(0, indexs + 4); //+4是为了加上.app4个字符
                    XHClick.mapStat(XHApplication.in(), "a_from_other", urls[1], data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bundle bundle = new Bundle();
        Intent intent = null;
        try {
            // 开启url，同时识别是否是原生的
            bundle.putString("url", url);
        }catch (Exception e){
            e.printStackTrace();
        }
        //下载apk---直接中断
        if (url.indexOf("download.app") > -1) {
            String temp = url.substring(url.indexOf("?") + 1, url.length());
            LinkedHashMap<String, String> map_link = UtilString.getMapByString(temp, "&", "=");
            String downUrl = map_link.get("url");
            try {
                final DownLoad downLoad = new DownLoad(XHApplication.in());
                downLoad.setNotifaction("开始下载", map_link.get("appname") + ".apk", "正在下载", R.drawable.ic_launcher, false);
                downLoad.starDownLoad(downUrl, FileManager.getCameraDir(), map_link.get("appname"), true, new DownloadCallBack() {
                    @Override
                    public void starDown() {
                        super.starDown();
                        Tools.showToast(XHApplication.in(), "开始下载");
                    }
                    @Override
                    public void downOk(Uri uri) {
                        super.downOk(uri);
                        FileUtils.install(XHApplication.in(), uri);
                        downLoad.cacelNotification();
                    }

                    @Override
                    public void downError(String s) {
                        Tools.showToast(XHApplication.in(), "下载失败：" + s);
                        downLoad.cacelNotification();
                    }
                });
            } catch (Exception e) {
                Tools.showToast(XHApplication.in(), "下载异常");
                e.printStackTrace();
            }
            return;
        }else if(url.indexOf("fullScreen=2") > -1){
            Intent it = new Intent(act, FullScreenWeb.class);
            it.putExtra("url",StringManager.replaceUrl(url));
            act.startActivity(it);
            return;
        }
        else if (url.indexOf("ChangeUrl.app") > -1) {
            Intent it = new Intent(act, ChangeUrl.class);
            act.startActivity(it);
            return;
        } else if (url.indexOf("MyRebate.app") > -1) { //我的返现页面
            return;
        } else if (url.indexOf("GoodsList.app") > -1) { //返现商品列表
            return;
        } else if(url.indexOf("link.app")==0){//外链
            String temp = url.substring(url.indexOf("?") + 1, url.length());
            LinkedHashMap<String, String> map_link = UtilString.getMapByString(temp, "&", "=");
            String openUrl = map_link.get("url");
            Intent intentLink = new Intent();
            intentLink.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(openUrl);
            intentLink.setData(content_url);
            act.startActivity(intentLink);
            return;

        }
        intent = parseURL(XHApplication.in(), bundle, url);
        LogManager.print(XHConf.log_tag_net, "d", "------------------解析网页url------------------\n" + url);
        if (intent == null) {
            bundle.putString("url", StringManager.replaceUrl(url));
            if (Main.colse_level <= 2) {
                if (!(act instanceof MainBaseActivity))
                    act.finish();
                return;
            }
            if (act instanceof WebActivity) {
                final WebActivity allAct = (WebActivity) act;
                boolean isSelfLoad = allAct.selfLoadUrl(url, openThis);
                if (!isSelfLoad && !url.contains(".app")) {
                    intent = new Intent(act, ShowWeb.class);
                    intent.putExtras(bundle);
                }
            } else if (!url.contains(".app") || url.indexOf("aboutus") == 0) {
                intent = new Intent(act, ShowWeb.class);
                intent.putExtras(bundle);
            }
        }else if(url.indexOf("nousInfo") > -1){
            String code = intent.getStringExtra("code");
            AppCommon.openUrl(act, StringManager.api_nouseInfo + code, true);
            intent = null;
        }
        //必须这样再判断，避免给的url是原生，但parseURL方法中没解析
        if (intent != null) {
            act.startActivity(intent);
        }
    }

	/**
	 * 解析url是否为原生页面
	 * @param act
	 * @param bundle
	 * @param url
	 * @return
	 */
	public static Intent parseURL(Context act, Bundle bundle, String url) {
		if (url.indexOf("stat=1") > -1) {
			//服务端做统计用的
			ReqInternet.in().doGet(StringManager.api_setAppUrl + "?url=" + url, new InternetCallback(XHApplication.in()) {
				@Override
				public void loaded(int flag, String url, Object returnObj) {
					LogManager.print("d", "res=" + flag + "----data=" + returnObj.toString());
				}
			});
		}
		Intent intent = null;
		LogManager.print("d", "parseURL:" + url);
		//特殊处理体质
		if (url.indexOf("tizhitest.app") > -1) {
			String result = isHealthTest();
			if (result.equals("")) {
				intent = new Intent(act, HealthTest.class);
			} else {
				intent = new Intent(act, MyPhysique.class);
				bundle.putString("params", result);
				intent.putExtras(bundle);
			}
			return intent;
		}
		//开浏览器
		if (url.indexOf("internet.app") > -1) {
			String[] urls = url.split("=");
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urls[1]));
			return intent;
		}
		if (url.indexOf("ingreInfo.app?type=tizhi") > -1 || url.indexOf("ingreInfo.app?type=jieqi") > -1) {
			url = url.replace("ingreInfo.app", "jiankang.app");
		}
		//常规解析
		String newUrl = old2new(act, url);
		try {
			String[] urls = newUrl.split("\\?");
			if (urls.length > 0) {
				final Class<?> c = Class.forName(urls[0]);
				if (urls[0].contains("amodule.main.activity.") || urls[0].contains("HomeNous")||urls[0].contains("third.mall.MainMall")) {
					Main.colse_level = 2;
					if (Main.allMain != null) {
						Main.allMain.setCurrentTabByClass(c);
					}
					return intent;
				}
				if (urls.length > 1) {
					bundle = new Bundle();
					//web要的不是参数，是url链接，故这样处理，但此版要兼容老版，故会在老版处理，此新版不用单独处理
					if (urls.length == 3) {
						urls[1] = urls[1] + "?" + urls[2];
						String key = urls[1].substring(0, urls[1].indexOf("="));
						String value = urls[1].substring(urls[1].indexOf("=") + 1, urls[1].length());
						bundle.putString(key, value);
					} else {
						String[] parameter = urls[1].split("&");
						for (String p : parameter) {
							String[] value = p.split("=");
							if (value.length == 2) {
								bundle.putString(URLDecoder.decode(value[0], HTTP.UTF_8), URLDecoder.decode(value[1], HTTP.UTF_8));
							}
						}
					}
				}
				if(url.indexOf("MyDishNew.app")  > -1 || url.indexOf("MySubject.app") > -1){
					if(LoginManager.isLogin()){
						bundle = new Bundle();
						bundle.putString("code",LoginManager.userInfo.get("code"));
					}else{
						Intent it = new Intent(act, LoginByAccout.class);
						return it;
					}
					if(url.indexOf("MyDishNew.app")  > -1){
						bundle.putInt("index",1);
					}else{
						bundle.putInt("index",0);
					}
				}
				intent = new Intent(act, c);
				if (bundle != null)
					intent.putExtras(bundle);
				return intent;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return intent;
	}

    private static String old2new(Context context, String oldUrl) {
        String newUrl = "";
        String[] urls = oldUrl.split("\\?");
        Map<String, String> urlRule = geturlRule(context);
        String urlKey = urls[0];
        if (urls[0].lastIndexOf("/") >= 0) {
            urlKey = urls[0].substring(urls[0].lastIndexOf("/") + 1);
        }
        if (urlRule == null || urlRule.get(urlKey) == null) {
//			if(!oldUrl.contains(".app")){
//				return "aplug.web.ShowWeb?url=" + oldUrl;
//			}
            return oldUrl;
        }
        newUrl = oldUrl.replace(urls[0], urlRule.get(urlKey));
        return newUrl;
    }

    /**
     * 存储App数据
     * 菜谱分类、
     */
    public synchronized static void saveAppData() {
        final String appDataPath = FileManager.getDataDir() + FileManager.file_appData;
//        if (FileManager.ifFileModifyByCompletePath(appDataPath, 6 * 60) == null) {
            ReqInternet.in().doGet(StringManager.api_appData + "?type=newData", new InternetCallback(XHApplication.in()) {
                @Override
                public void loaded(int flag, String url, final Object returnObj) {
                    if (flag >= ReqInternet.REQ_OK_STRING) {
                        saveAppDataToFile(returnObj.toString());
                    } else {
                        String json = FileManager.getFromAssets(context, FileManager.file_appData);
                        saveAppDataToFile(json);
                    }
                }

                private void saveAppDataToFile(final String json) {
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            FileManager.saveFileToCompletePath(appDataPath, json, false);
                        }
                    }.start();
                }
            });
//        }
    }

    /**
     * 获取appData
     *
     * @param context
     * @param key     为null则返回整个数据，否则返回key所对应的数据
     *
     * @return
     */
    public static String getAppData(Context context, String key) {
        String jsonStr = "";
        final String appDataPath = FileManager.getDataDir() + FileManager.file_appData;
        String appDataStr = FileManager.readFile(appDataPath);
        List<Map<String, String>> dataArray = getListMapByJson(appDataStr);
        if (dataArray == null || dataArray.size() == 0) {
            appDataStr = FileManager.getFromAssets(context, FileManager.file_appData);
            dataArray = getListMapByJson(appDataStr);
        }
        if (TextUtils.isEmpty(key)) {
            return appDataStr;
        }
        if (dataArray.size() > 0 && dataArray.get(0).containsKey(key)) {
            jsonStr = dataArray.get(0).get(key);
        }
        return jsonStr;
    }

    /**
     * 关注请求
     *
     * @param code
     */
    public static void onAttentionClick(String code, final String type) {
        onAttentionClick(code, type, null);
    }

    /**
     * 关注请求
     *
     * @param code
     */
    public static void onAttentionClick(String code, final String type, final Runnable succRun) {
        if (code != null) {
            ReqInternet.in().doPost(StringManager.api_setUserData, "type=" + type + "&p1=" + code.toString(),
                    new InternetCallback(XHApplication.in()) {
                        @Override
                        public void loaded(int flag, String url, Object returnObj) {
                            if (flag >= ReqInternet.REQ_OK_STRING) {
                                LoginManager.modifyUserInfo(XHApplication.in(), "followNum", returnObj.toString());
                                AppCommon.follwersNum = Integer.valueOf(returnObj.toString());
                                if (succRun != null)
                                    succRun.run();
                            }
                        }
                    });
        }
    }

    /**
     * @param code 菜谱code
     *
     * @return 收藏状态
     */
    public static void onFavoriteClick(final Context context, String type, final String code,
                                       final InternetCallback callback) {
        if (code != null) {
            ReqInternet.in().doPost(StringManager.api_setDishInfo, "type=" + type + "&code=" + code, new InternetCallback(context) {
                @Override
                public void loaded(int flag, String url, Object returnObj) {
                    if (flag >= ReqInternet.REQ_OK_STRING) {
                        Map<String, String> map = getListMapByJson(returnObj).get(0);
                        boolean nowFav = map.get("type").equals("2");
                        String dishJson = DataOperate.buyBurden(context, code);
                        if (dishJson.length() > 10 && dishJson.contains("\"makes\":")) {
                            // 修改splite数据
                            dishJson = dishJson.replace(nowFav ? "\"isFav\":1" : "\"isFav\":2", nowFav ? "\"isFav\":2" : "\"isFav\":1");
                            ShowBuySqlite sqlite = new ShowBuySqlite(context);
                            sqlite.updateIsFav(code, dishJson);
                        }
                        Tools.showToast(context, nowFav ? "收藏成功" : "取消收藏");
                        callback.loaded(ReqInternet.REQ_OK_STRING, url, returnObj);
                    } else {
                        callback.loaded(0, url, returnObj);
                        Tools.showToast(context, returnObj.toString());
                    }
                }
            });
        } else
            callback.loaded(-1, null, "");// 暂时这么写
    }

    /**
     * @param lv        等级
     * @param imageView 等级图片
     */
    public static boolean setLvImage(int lv, ImageView imageView) {
        int[] lv_img_id = {R.drawable.z_z_ico_level_01, R.drawable.z_z_ico_level_02, R.drawable.z_z_ico_level_03,
                R.drawable.z_z_ico_level_04, R.drawable.z_z_ico_level_05, R.drawable.z_z_ico_level_06,
                R.drawable.z_z_ico_level_07, R.drawable.z_z_ico_level_08, R.drawable.z_z_ico_level_09,
                R.drawable.z_z_ico_level_10, R.drawable.z_z_ico_level_11, R.drawable.z_z_ico_level_12,
                R.drawable.z_z_ico_level_13, R.drawable.z_z_ico_level_14, R.drawable.z_z_ico_level_15,
                R.drawable.z_z_ico_level_16, R.drawable.z_z_ico_level_17, R.drawable.z_z_ico_level_18};
        if (lv == 0) {
            imageView.setVisibility(View.GONE);
            return false;
        }
        else {
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageResource(lv_img_id[lv - 1]);
            return true;
        }
    }

    /**
     * @param imageView
     */
    public static boolean setUserTypeImage(int isGourmet, ImageView imageView) {
        int[] lv_img_id = {R.drawable.z_user_gourmet_ico};
        if (isGourmet == 2) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setBackgroundResource(lv_img_id[0]);
            return true;
        } else {
            imageView.setVisibility(View.GONE);
            return false;
        }
    }

    // 适配ListView的滑动
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void scorllToIndex(ListView listView, int index) {
        int version = android.os.Build.VERSION.SDK_INT;
        if (version >= 11) {
            int lastVisible = listView.getLastVisiblePosition();
            if (index > lastVisible) {
                listView.setSelection(index);
            } else
                listView.smoothScrollToPositionFromTop(index, 0);
        } else if (version < 11 && version >= 8) {
            int firstVisible = listView.getFirstVisiblePosition();
            int lastVisible = listView.getLastVisiblePosition();
            if (index < firstVisible)
                listView.smoothScrollToPosition(index);
            else
                listView.smoothScrollToPosition(index + lastVisible - firstVisible - 2);
        }
    }

    /**
     * 获取惊喜页面的红点是否显示
     */
    public static void getActivityState(final Context context) {
        ReqInternet.in().doGet(StringManager.api_getChangeTime, new InternetCallback(XHApplication.in()) {

            private String noGoTime = "";
            private String goTime = "";

            @Override
            public void loaded(int flag, String url, Object returnObj) {
                View tab4 = null;
                if (Main.allMain != null) {
                    tab4 = Main.allMain.getTabView(4);
                }
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> listMapByJson = getListMapByJson(returnObj);
                    if (listMapByJson.size() > 0) {
                        String string = listMapByJson.get(0).get("subject");
                        ArrayList<Map<String, String>> listMapByJson2 = getListMapByJson(string);
                        if (listMapByJson2.size() > 0) {
                            Map<String, String> map2 = listMapByJson2.get(0);
                            noGoTime = map2.get("noGoTime");
                            goTime = map2.get("goTime");
//							Map<String, String> msgMap = new HashMap<String, String>();
                            if (tab4 != null) {
                                if (FileManager.loadShared(context, "Activity_state", "goTime") == "") {
                                    tab4.findViewById(R.id.activity_tabhost_redhot).setVisibility(View.VISIBLE);
                                    CommonBottomView.BottomViewBuilder.getInstance().setIconShow(CommonBottomView.BOTTOM_FIVE, -1, true);
                                } else {
                                    String oldNoGoTime = (String) FileManager.loadShared(context, "Activity_state", "noGoTime");
                                    String oldGoTime = (String) FileManager.loadShared(context, "Activity_state", "goTime");
                                    if (!noGoTime.equals(oldNoGoTime) || !goTime.equals(oldGoTime)) {
                                        tab4.findViewById(R.id.activity_tabhost_redhot).setVisibility(View.VISIBLE);
                                        CommonBottomView.BottomViewBuilder.getInstance().setIconShow(CommonBottomView.BOTTOM_FIVE, -1, true);
                                    } else {
                                        tab4.findViewById(R.id.activity_tabhost_redhot).setVisibility(View.GONE);
                                        CommonBottomView.BottomViewBuilder.getInstance().setIconShow(CommonBottomView.BOTTOM_FIVE, -1, false);
                                    }
                                }
                            }
                        }
                    }
                } else if (tab4 != null) {
                    tab4.findViewById(R.id.activity_tabhost_redhot).setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * 清理数据缓存
     */
    public static void clearCache() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 清除部分缓存
                FileManager.delDirectoryOrFile(FileManager.getSDDir() + FileManager.save_cache, 150);
            }
        }).start();
    }

    public static boolean getTodayTastHintIsShow(Context con) {
        boolean isShowTaskInfo = false;
        if (UtilFile.loadShared(con, "score_store", "user_task") == "") {
            isShowTaskInfo = true;
        } else {
            int year = Tools.getDate("year");
            int month = Tools.getDate("month");
            int date = Tools.getDate("date");
            String user_taks = (String) UtilFile.loadShared(con, "score_store", "user_task");
            String[] user_taks_data = user_taks.split("_");
            if (user_taks_data.length == 3) {
                if (Integer.parseInt(user_taks_data[0]) < year) {
                    isShowTaskInfo = true;
                } else if (Integer.parseInt(user_taks_data[1]) < month) {
                    isShowTaskInfo = true;
                } else if (Integer.parseInt(user_taks_data[2]) < date) {
                    isShowTaskInfo = true;
                }
            }
        }
        return isShowTaskInfo;
    }

    /**
     * 判断是否有体质测试结果
     */
    public static String isHealthTest() {
        if (LoginManager.isLogin()) {// 是否有用户
            if (LoginManager.userInfo.containsKey("crowd") && LoginManager.userInfo.get("crowd") != null)
                return LoginManager.userInfo.get("crowd");
        } else {
            if (UtilFile.ifFileModifyByCompletePath(UtilFile.getDataDir() + FileManager.file_healthResult, -1) != null) {// 本地是否有测试结果
                return UtilFile.readFile(UtilFile.getDataDir() + FileManager.file_healthResult).trim();
            }
        }
        return "";
    }

    public synchronized static void saveUrlRuleFile(Context context) {
        final String urlRulePath = FileManager.getDataDir() + FileManager.file_urlRule;
		//方便测试
//		if(FileManager.ifFileModifyByCompletePath(urlRulePath, -1) == null){
//			String urlRuleData = FileManager.getFromAssets(context, FileManager.file_urlRule);
//			FileManager.saveFileToCompletePath(urlRulePath, urlRuleData, false);
//		}
//		if(FileManager.ifFileModifyByCompletePath(urlRulePath, 6 * 60) == null){
        String uptime = "";
        String urlRuleJson = FileManager.readFile(urlRulePath);
        if (!TextUtils.isEmpty(urlRuleJson)) {
            List<Map<String, String>> data = getListMapByJson(urlRuleJson);
            if (data.size() > 0) {
                uptime = data.get(0).get("uptime");
            }
        }
        String url = StringManager.api_getWebRule;
        String params = TextUtils.isEmpty(uptime) ? "" : "?uptime=" + uptime;
        ReqInternet.in().doGet(url + params, new InternetCallback(context) {

            @Override
            public void loaded(int flag, String url, final Object msg) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    if (msg != null && !TextUtils.isEmpty(msg.toString())) {
                        List<Map<String, String>> returnData = getListMapByJson(msg);
                        if (returnData.size() > 0) {
                            new Thread() {
                                @Override
                                public void run() {
                                    super.run();
                                    FileManager.delDirectoryOrFile(urlRulePath);
                                    FileManager.saveFileToCompletePath(urlRulePath, msg.toString(), false);
                                }
                            }.start();
                        }
                    }
                }
            }
        });
//		}
    }

    private static Map<String, String> urlRuleMap = null;

    private static Map<String, String> geturlRule(Context context) {
        if (urlRuleMap == null) {
            final String urlRulePath = FileManager.getDataDir() + FileManager.file_urlRule;
            String urlRuleJson = FileManager.readFile(urlRulePath);
            if (TextUtils.isEmpty(urlRuleJson)) {
                urlRuleJson = FileManager.getFromAssets(context, FileManager.file_urlRule);
            }
            List<Map<String, String>> data = getListMapByJson(urlRuleJson);
            if (data.size() > 0) {
                String urlRule = data.get(0).get("data");
                data = getListMapByJson(urlRule);
                if (data.size() > 0) {
                    urlRuleMap = data.get(0);
                }
            }
        }
        return urlRuleMap;
    }

    /**
     * 保存所有圈子的静态数据
     * 每次开启应用都会请求
     */
    public static void saveCircleStaticData(final Context context) {
        final String allCircleJsonPath = FileManager.getDataDir() + FileManager.file_allCircle;
        final String allCircleJson = FileManager.readFile(allCircleJsonPath);
        if (TextUtils.isEmpty(allCircleJson)) {
            CircleSqlite circleSqlite = new CircleSqlite(context);
            String allCircleJsonByAssets = FileManager.getFromAssets(context, FileManager.file_allCircle);
            saveCircleData(allCircleJsonPath, allCircleJsonByAssets, circleSqlite);
        }
        ReqInternet.in().doGet(StringManager.api_circleStaticData, new InternetCallback(context) {
            @Override
            public void loaded(int flag, String url, Object msg) {
                //成功
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    //与本地数据对比，相同则return
                    if (allCircleJson.equals(msg)) {
                        return;
                    }
                    //删除数据
                    CircleSqlite circleSqlite = new CircleSqlite(context);
                    circleSqlite.deleteAll();
                    //保存所有数据
                    saveCircleData(allCircleJsonPath, msg, circleSqlite);
                    //失败，判断本地保存数据是否为空，若为空则获取assets文件保存到本地
                } else {

                }
            }
        });
    }

    private static void saveCircleData(final String allCircleJsonPath, Object msg, CircleSqlite circleSqlite) {
        //保存数据
        FileManager.saveFileToCompletePath(allCircleJsonPath, msg.toString(), false);
        List<Map<String, String>> array = getListMapByJson(msg);
        Map<String, String> map = null;
        if (array.size() > 0) {
            map = array.get(0);
            array = getListMapByJson(map.get("allQuan"));
            //数据库存储
            for (Map<String, String> mapCircle : array) {
                CircleData circleData = new CircleData();
                circleData.setCid(mapCircle.get("cid"));
                circleData.setName(mapCircle.get("name"));
                circleData.setRule(mapCircle.get("rule"));
                circleData.setSkip(mapCircle.get("skip"));
                circleData.setInfo(mapCircle.get("info"));
                circleData.setImg(mapCircle.get("img"));
                circleData.setCustomerNum(mapCircle.get("customerNum"));
                circleData.setDayHotNum(mapCircle.get("dayHotNum"));
                circleSqlite.insert(circleData);
            }
            //移除多余数据
            map.remove("allQuan");
            //转换成json存储
            String jsonStr = Tools.map2Json(map);
            String filePath = FileManager.getDataDir() + FileManager.file_indexModuleAndRecCircle;
            FileManager.saveFileToCompletePath(filePath, jsonStr, false);
        }
    }

    /** 保存config */
    public static void saveConfigData(Context context) {
        ReqInternet.in().doGet(StringManager.api_getConf, new InternetCallback(context) {
            @Override
            public void loaded(int flag, String url, final Object msg) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            FileManager.saveFileToCompletePath(FileManager.getDataDir() + FileManager.file_config, msg.toString(), false);


                        }
                    }).start();
                }
            }
        });
    }
    /**
     * 获取config数据
     * @param key 若key为空，返回所有config数据
     * @return
     */
    public static String getConfigByLocal(String key) {
        String data = "";
        String configData = FileManager.readFile(FileManager.getDataDir() + FileManager.file_config);
        if (TextUtils.isEmpty(key)) {
            return configData;
        }
        Map<String, String> map = StringManager.getFirstMap(configData);
        if (map != null && map.containsKey(key)) {
            data = map.get(key);
        }
        return data;
    }

	public static Map<String,Integer> createCount = new HashMap<>();

	/**
	 * @param context
	 * @param loadManager
	 * @param rl
     * @param url
     */
	public static void createWeb(Activity context , LoadManager loadManager, RelativeLayout rl, String url,
								 @NonNull String type, int maxCount){
		try{
			//添加限制
			if(createCount == null){
				createCount = new HashMap<>();
			}
			int currentCount = 0;
			String key = type + url;
			if(createCount.containsKey(key)){
				currentCount = createCount.get(key);
			}
			if(maxCount == -1 || currentCount >= maxCount){
				return;
			}
			currentCount++;
			createCount.put(key,currentCount);
			//请求web
			String cookieKey = "";
			String newUrl = url.replace("http://","");
			String host = newUrl.substring(newUrl.indexOf("."),newUrl.indexOf("/") > -1 ? newUrl.indexOf("/") : newUrl.length());
			String[] strArray = host.split(":");
			if(strArray.length > 1){
				host = strArray[0];
			}
			WebviewManager webviewManager = new WebviewManager(context,loadManager,false);
			XHWebView webView = webviewManager.createWebView(0);
			Map<String,String> header=ReqInternet.in().getHeader(context);
			String cookieStr=header.containsKey("Cookie")?header.get("Cookie"):"";
			String[] cookie = cookieStr.split(";");
			CookieManager cookieManager = CookieManager.getInstance();
			for (int i = 0; i < cookie.length; i++) {
				cookieManager.setCookie(url, cookie[i]);
			}
			cookieManager.setCookie(url, "xhWebStat=1");
			CookieSyncManager.getInstance().sync();
			rl.addView(webView,0,0);
			webView.loadUrl(url);
		}catch (Exception e){

		}
	}

    public static boolean setVip(final Activity act, ImageView vipView, String data){
        return setVip(act,vipView,data,"","");
    }

    public static boolean setVip(final Activity act, ImageView vipView, String data, View.OnClickListener listener){
        return setVip(act,vipView,data,"","","",listener);
    }

    public static boolean setVip(final Activity act, ImageView vipView, String data, final String eventId, final String twoLevel){
        return setVip(act,vipView,data,eventId,twoLevel,"",null);
    }

    public static boolean setVip(final Activity act, ImageView vipView, String data, final String eventId, final String twoLevel, final String threadLevel, final View.OnClickListener listener){
        boolean isVip = isVip(data);
        if(isVip){
            vipView.setVisibility(View.VISIBLE);
            vipView.setImageResource(R.drawable.i_user_home_vip);
        }else{
            vipView.setVisibility(View.GONE);
        }
        vipView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(eventId)) XHClick.mapStat(act,eventId,twoLevel,TextUtils.isEmpty(threadLevel) ? "会员皇冠" : threadLevel);
                if(listener != null) listener.onClick(v);
                AppCommon.openUrl(act, StringManager.api_vip, true);
            }
        });
        return isVip;
    }

    public static boolean isVip(String data){
        boolean isVip = false;

        if(TextUtils.isEmpty(data))
            return false;
        if("2".equals(data)){
            isVip = true;
        }else {
            ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(data);
            if (arrayList.size() > 0) {
                Map<String, String> map = arrayList.get(0);
                if ("2".equals(map.get("isVip"))) {
                    isVip = true;
                }
            }
        }
        return isVip;
    }

    public static void setAdHintClick(final Activity act, View adHintView, final XHAllAdControl xhAllAdControl, final int index, final String listIndex){
        setAdHintClick(act,adHintView,xhAllAdControl,index,listIndex,"","");
    }

    public static void onAdHintClick(final Activity act, final XHAllAdControl xhAllAdControl, final int index, final String listIndex){
       onAdHintClick(act,xhAllAdControl,index,listIndex,"","");
    }

    public static void setAdHintClick(final Activity act, View adHintView, final XHAllAdControl xhAllAdControl, final int index, final String listIndex,
                                      final String eventID, final String twoLevel){
        adHintView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAdHintClick(act,xhAllAdControl,index,listIndex,eventID,twoLevel);
            }
        });
    }

    public static void onAdHintClick(final Activity act, final XHAllAdControl xhAllAdControl, final int index, final String listIndex, final String eventID, final String twoLevel){
        final BottomDialog bottomDialog = new BottomDialog(act);
        bottomDialog.setTopButton("赞助商提供的广告信息", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(!TextUtils.isEmpty(eventID)) XHClick.mapStat(act,eventID,twoLevel,"点击赞助商提供广告");
                if (xhAllAdControl != null) xhAllAdControl.onAdClick(index, listIndex);
                bottomDialog.cancel();
            }
        }).setBottomButton("会员全站去广告", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(eventID)) XHClick.mapStat(act,eventID,twoLevel,"点击【会员全站去广告】按钮");
                if(LoginManager.isLogin()) AppCommon.openUrl(act, StringManager.api_openVip,true);
                else AppCommon.openUrl(act, StringManager.api_vip,true);
                bottomDialog.cancel();
            }
        }).setBottomButtonColor("#59bdff").show();
    }


}
