package third.ad.option;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import acore.tools.StringManager;
import third.ad.scrollerAd.XHScrollerAdParent;

/**
 * Created by Fang Ruijiao on 2017/4/25.
 */
public abstract class  AdOptionList extends AdOptionParent {

    public AdOptionList(String[] adPlayIds, Integer[] adIndexs) {
        super(adPlayIds, adIndexs);
    }

    @Override
    protected ArrayList<Map<String, String>> getBdData(ArrayList<Map<String, String>> old_list,boolean isBack) {
        Log("getBdData adArray.size():" + adArray.size() + "    adIdList.size():" + adIdList.size());
        ArrayList<Map<String,String>> tempList = new ArrayList<>();
        Log.i("zhangyujian","getLimitNum::"+getLimitNum());
//        int size = old_list.size();
        if(!isBack&&getLimitNum()>0){//listDatas，向下翻页
            int limitNum= getLimitNum();
            for(int index=0;index<limitNum;index++){
                tempList.add(old_list.get(index));
            }
            for(int index=0;index<limitNum;index++){
                old_list.remove(0);
            }
            Log.i("zhangyujian","节点数据为::"+old_list.get(0).get("name"));
        }
        if (adArray.size() > 0) {
            Map<String,String> adMap;
            if(!isBack){ //向上加载时添加广告数据
                cunrrentIndex = 0;
            }
            for (int idIndex = 0,size = adArray.size(),adIdListSize = adIdList.size(); cunrrentIndex < size && idIndex < adIdListSize; cunrrentIndex++,idIndex++) {
                int index = adIdList.get(idIndex);
                if (index > 0 && index < old_list.size()) {
                    Map<String, String> dataMap = old_list.get(index);
                    String adstyle = isBack ? old_list.get(index - 1).get("adstyle") : dataMap.get("adstyle");
                    //判断此广告位是否添加广告，如果此广告位已添加广告，则不添加
                    if(!"ad".equals(adstyle)){
                        adMap = adArray.get(cunrrentIndex);
                        boolean dataIsOk = getDataIsOk(adMap);
                        if(dataIsOk) {
                            JSONArray styleData = new JSONArray();
                            //腾讯api广告不用根据上一个item样式变;101:表示返回的是一张小图、202:一个大图、301:3张小图
                            try {
                                if (XHScrollerAdParent.ADKEY_API.equals(adMap.get("adClass"))) {
                                    Log.i("FRJ","stype:" + adMap.get("stype"));
                                    if ("101".equals(adMap.get("stype"))) {
                                        adMap.put("style", "2");
                                        JSONObject styleObject = new JSONObject();
                                        styleObject.put("url", adMap.get("img"));
                                        styleObject.put("type", "1");
                                        styleData.put(styleObject);
                                    } else if ("202".equals(adMap.get("stype"))) {
                                        adMap.put("style", "1");
                                        JSONObject styleObject = new JSONObject();
                                        styleObject.put("url", adMap.get("img"));
                                        styleObject.put("type", "1");
                                        styleData.put(styleObject);
                                    } else if ("301".equals(adMap.get("stype"))) {
                                        adMap.put("style", "3");
                                        String imgsUrl = adMap.get("imgs");
                                        ArrayList<Map<String, String>> imgsMap = StringManager.getListMapByJson(imgsUrl);
                                        if (imgsMap != null && imgsMap.size() > 0) {
                                            for (Map<String, String> imgMap : imgsMap) {
                                                if (imgMap != null && imgMap.get("") != null) {
                                                    String imgUrl = imgMap.get("");
                                                    JSONObject styleObject = new JSONObject();
                                                    styleObject.put("url", imgUrl);
                                                    styleObject.put("type", "1");
                                                    styleData.put(styleObject);
                                                }
                                            }
                                        }
                                    } else {
                                        adMap.put("style", "4");
                                    }
                                } else {
                                    int aboveIndex = index - 1; //广告要跟上一个样式保持一致
                                    if(aboveIndex < 0) aboveIndex = index;
                                    Map<String, String> aboveMap = old_list.get(aboveIndex);
                                    ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(aboveMap.get("imgs"));
                                    String type = aboveMap.get("style");
                                    if (TextUtils.isEmpty(type)) {//如果上一个样式的字段不存在则默认右图样式
                                        JSONObject styleObject = new JSONObject();
                                        styleObject.put("url", adMap.get("img"));
                                        styleObject.put("type", "1");
                                        styleData.put(styleObject);
                                    } else {
                                        String adImg = adMap.get("img");
                                        ArrayList<Map<String, String>> imgsMap = StringManager.getListMapByJson(adMap.get("imgs"));
                                        if (imgsMap != null && imgsMap.size() > 0) {
                                            for (Map<String, String> imgMap : imgsMap) {
                                                if (imgMap != null && imgMap.get("") != null) {
                                                    JSONObject styleObject = new JSONObject();
                                                    styleObject.put("url", imgMap.get(""));
                                                    styleObject.put("type", "1");
                                                    styleData.put(styleObject);
                                                }
                                            }
                                        } else {
                                            JSONObject styleObject = new JSONObject();
                                            styleObject.put("url", adImg);
                                            styleObject.put("type", "1");
                                            styleData.put(styleObject);
                                        }
                                        switch (type) {
                                            case "1"://大图
                                            case "5"://蒙版
                                                adMap.put("style", TextUtils.isEmpty(adImg) && (imgsMap == null || imgsMap.isEmpty()) ? "4" : "1");
                                                break;
//                                            case "6"://任意图
//                                            case "2"://右图
//                                            case "3"://三图
//                                            case "4"://无图
                                            default://除大图样式外，其余默认右图，如果没有图片则无图。
                                                adMap.put("style", TextUtils.isEmpty(adImg) && (imgsMap == null || imgsMap.isEmpty()) ? "4" : "2");
                                                break;
                                        }
                                    }
                                }
                            } catch (JSONException e) {

                            }

                            adMap.put("styleData", styleData.toString());
                            Log("ad controlTag:" + adMap.get("controlTag") + "    ad name:" + adMap.get("name") + "   style:" + adMap.get("style"));
                            if(!TextUtils.isEmpty(adMap.get("style")))
                                old_list.add(index, adMap);
                        }
                    }
                }else{
                    break;
                }
            }
        }
        if(!isBack&&tempList!=null&&tempList.size()>0){
            old_list.addAll(0,tempList);
        }
        return old_list;
    }
}