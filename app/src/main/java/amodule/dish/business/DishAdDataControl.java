package amodule.dish.business;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import acore.tools.StringManager;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.scrollerAd.XHScrollerAdParent;

import static third.ad.tools.AdPlayIdConfig.COMMEND_THREE_MEALS;

/**
 * Created by ：fei_teng on 2017/3/9 16:55.
 * 三餐推荐的广告获取
 */
public class DishAdDataControl {

    public XHAllAdControl xhAllAdControl;
    private ArrayList<Map<String, String>> adDataList = new ArrayList<>();
    private ArrayList<Map<String, String>> finalDataLists = new ArrayList<>();

    public void getDishAdData(Context context, @NonNull final DishAdDataControlCallback callback) {
        finalDataLists.clear();
        ArrayList<String> adPosList = new ArrayList<>();
        for (String posStr : COMMEND_THREE_MEALS) {
            adPosList.add(posStr);
        }
        xhAllAdControl = new XHAllAdControl(adPosList, new XHAllAdControl.XHBackIdsDataCallBack() {
            @Override
            public void callBack(boolean isRefresh,Map<String, String> map) {
                if (map != null && map.size() > 0) {
                    adDataList.clear();
                    for (String adKey : COMMEND_THREE_MEALS) {
                        String adStr = map.get(adKey);
                        if (!TextUtils.isEmpty(adStr)) {
                            ArrayList<Map<String, String>> adList = StringManager.getListMapByJson(adStr);
                            if (adList != null && adList.size() > 0) {
                                assembleData(adList.get(0));
                            }
                        }
                    }
                    callback.onGetDataComplete(isRefresh);
                }
            }
        }, (Activity) context, "other_threeMeals_list");
        xhAllAdControl.registerRefreshCallback();
    }


    private void assembleData(Map<String, String> map) {

        if (map != null && map.size() > 0) {
            HashMap<String, String> dataMap = new HashMap<String, String>();

            if (XHScrollerAdParent.ADKEY_BANNER.equals(map.get("type"))) {
                if (!TextUtils.isEmpty(map.get("imgUrl2")))
                    map.put("imgUrl", map.get("imgUrl2"));
            }

            dataMap.put("type", map.get("type"));
            dataMap.put("isPromotion", "2");
            dataMap.put("indexOnData", map.get("index"));
            dataMap.put("indexOnShow", (adDataList.size() + 1) + "");
            dataMap.put("name", map.get("title"));
            dataMap.put("nickName", map.get("title"));
            dataMap.put("img", map.get("imgUrl"));
            dataMap.put("isYou", "hide");
            dataMap.put("isJin", "hide");
            dataMap.put("isToday", "hide");
            dataMap.put("hasVideo", "1");
            dataMap.put("isToday", "hide");
            dataMap.put("isYou", "hide");
            String iconUrl = TextUtils.isEmpty(map.get("iconUrl")) ? map.get("imgUrl") : map.get("iconUrl");
            dataMap.put("userImg", iconUrl);
            Random random = new Random();
            int v = random.nextInt(4000) + 6000;
            dataMap.put("allClick", v + "浏览");
            dataMap.put("isAd","ico" + R.drawable.i_ad_hint);
            adDataList.add(dataMap);
        }
        return;

    }


    public void addAdDataToList(boolean isRefresh,@NonNull ArrayList<Map<String, String>> arrayList) {
        for(int i = 0;i<arrayList.size();i++){
            Map<String, String> map = arrayList.get(i);
            if("往期推荐".equals(map.get("isToday")) && !adDataList.isEmpty()){
                Map<String,String> adMap = adDataList.get(0);
                adMap.put("isAdItem","2");
                finalDataLists.add(adMap);
            }else if(isRefresh && "2".equals(map.get("isAdItem"))){
                map = adDataList.get(0);
                map.put("isAdItem","2");
            }
            finalDataLists.add(map);
        }
        arrayList.clear();
        arrayList.addAll(finalDataLists);
    }

    public interface DishAdDataControlCallback {
        void onGetDataComplete(boolean isRefresh);
    }
}

