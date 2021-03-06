package amodule.dish.db;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.tools.FileManager;
import acore.tools.ImgManager;
import acore.tools.Tools;
import aplug.basic.LoadImage;
import xh.basic.tool.UtilString;

public class DataOperate {
	public final static String NWELINE = "\r\n";
	//没登录时默认离线菜谱个数
	public static final int MAX_DOWN_DISH=10;

	public static void saveBuyBurden(final Context context, final String json, final String mouldVersion) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				//统计
				XHClick.onEventValue(context, "dishDownload315", "dishDownload", "下载" , 1);
				//加入到数据库中
				DishOffSqlite sqlite = new DishOffSqlite(context);
				DishOffData buyData = new DishOffData();
				ArrayList<Map<String,String>> arrayList = UtilString.getListMapByJson(json);
				if(arrayList.size() > 0){
					Map<String,String> dishInfoMap = arrayList.get(0);
					buyData.setCode(dishInfoMap.get("code"));
					buyData.setName(dishInfoMap.get("name"));
					buyData.setAddTime(Tools.getAssignTime("yyyy-MM-dd HH:mm:ss",0));
					buyData.setMoudleVersion(mouldVersion);
					ImgManager.saveImg(dishInfoMap.get("img"),LoadImage.SAVE_LONG);
					buyData.setJson(json);
					int id = sqlite.insert(buyData);
					if(id != -1)AppCommon.buyBurdenNum++;
					sqlite.close();
				}
			}
		}).start();
	}
	/**
	 *  保存购物单
	 * @param context
	 * @param json 要保存的json码
	 */
	public static void saveBuyBurden2(final Context context,final String json) {
		new Thread(new Runnable(){ 
			@Override
			public void run() {
				//统计
				XHClick.onEventValue(context, "dishDownload315", "dishDownload", "下载" , 1);
				//加入到数据库中
				DishOffSqlite sqlite = new DishOffSqlite(context);
				DishOffData buyData = new DishOffData();
				ArrayList<Map<String,String>> arrayList = UtilString.getListMapByJson(json);
				for(int i=0;i<arrayList.size();i++){			
					buyData.setCode(arrayList.get(i).get("code"));
					buyData.setName(arrayList.get(i).get("name"));
					buyData.setAddTime(Tools.getAssignTime("yyyy-MM-dd HH:mm:ss",0));
					buyData.setJson(json);
					int id = sqlite.insert(buyData);
					if(id != -1)AppCommon.buyBurdenNum++;
				}
				if(arrayList.size() > 0){
					Map<String, String> theDish = arrayList.get(0);
					//缓存大图并存储到本地
					ImgManager.saveImg(theDish.get("img"),LoadImage.SAVE_LONG);
					//缓存步骤图并存储到本地
					ArrayList<Map<String, String>> dishMake=UtilString.getListMapByJson(theDish.get("makes"));
					for(int i=0;i<dishMake.size();i++){
						ImgManager.saveImg(dishMake.get(i).get("img"),LoadImage.SAVE_LONG);
					}
				}
				sqlite.close();
			}
		}).start();
	}


	
	/**
	 *  删除购物单
	 * @param context
	 * @param code :code的值为空则删除所有数据,否则删除指定code的记录
	 */
	public static void deleteBuyBurden(final Context context, final String code) {
		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					DishOffSqlite sqlite = new DishOffSqlite(context);
					//删除缓存图片
					sqlite.deleteImg(code);	
					if(code.length()==0){
						sqlite.deleteByCode(code);
						AppCommon.buyBurdenNum = 0;
					}else{
						sqlite.deleteByCode(code);
						AppCommon.buyBurdenNum --;
					}
					sqlite.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}).start();
	}

	/**
	 * 保存搜索关键字
	 * @param searchWord
	 */
	public static void saveSearchWord(String searchWord) {
		if (searchWord != null && searchWord.trim().length() > 0){
			String his = FileManager.readFile(FileManager.getDataDir() + FileManager.file_searchHis);
			if (his.length() == 0) {
				his = NWELINE;
			}
			// 兼容老文件
			else if (his.indexOf(NWELINE) != 0) {
				his = NWELINE + his + NWELINE;
			}
			// 已存在放到首位
			if (his.indexOf(NWELINE + searchWord + NWELINE) >= 0) {
				his = NWELINE + searchWord + his.replace(NWELINE + searchWord + NWELINE, NWELINE);
			} else {
				String[] hiss = his.split(NWELINE);
				if (hiss.length < 51) {
					his = NWELINE + searchWord + his;
				} else {
					his = NWELINE + searchWord;
					for (int i = 1; i < hiss.length - 1; i++) {
						his += NWELINE + hiss[i];
					}
					his += NWELINE;
				}
			}
			FileManager.saveFileToCompletePath(FileManager.getDataDir() + FileManager.file_searchHis, his, false);
		}
	}
	
	/**
	 * 保存历史记录Code 
	 */
	public static void saveHistoryCode(String searchWord) {
		if (searchWord != null && searchWord.trim().length() > 0){
			String his = FileManager.readFile(FileManager.getDataDir() + FileManager.file_historyCode);
			if (his.length() == 0) {
				his = NWELINE;
			}
			// 兼容老文件
			else if (his.indexOf(NWELINE) != 0) {
				his = NWELINE + his + NWELINE;
			}
			// 已存在放到首位
			if (his.indexOf(NWELINE + searchWord + NWELINE) >= 0) {
				his = NWELINE + searchWord + his.replace(NWELINE + searchWord + NWELINE, NWELINE);
			} else {
				String[] hiss = his.split(NWELINE);
				if (hiss.length < 101) {
					his = NWELINE + searchWord + his;
				} else {
					his = NWELINE + searchWord;
					for (int i = 1; i < hiss.length - 1; i++) {
						his += NWELINE + hiss[i];
					}
					his += NWELINE;
				}
			}
			FileManager.saveFileToCompletePath(FileManager.getDataDir() + FileManager.file_historyCode, his, false);
		}
	}
	
	/**
	 * 获取buyBurden
	 * @param context 
	 * @param code 为空则返回buyBurden的所有内容，为x则返回有多少个菜谱，其他则读取这个菜谱的json数据，
	 * @return
	 */
	public static String buyBurden(Context context, String code) {
		DishOffSqlite sqlite = new DishOffSqlite(context);
		try{
			if (TextUtils.isEmpty(code)) return sqlite.getAllDataFromDB();
			else if (code.equals("x")) return sqlite.selectCount() + "";
			else return sqlite.selectByCode(code);
		}catch(Exception e){
			e.printStackTrace();
//			if(code.equals("x")) return "0";
//			else return "";
			return "0";
		}
	}
	/**
	 * 分页获取
	 * @param context
	 * @param page
	 * @return
	 */
	public static String loadPageBuyBurden(Context context, int page){
		DishOffSqlite sqlite = new DishOffSqlite(context);
		try{
			return sqlite.LoadPage(page);
		}catch(Exception e){
			return "";
		}
	}
	
	/**
	 * 获取离线菜谱上限
	 * @param context
	 * @return
	 */
	public static int getDownDishLimit(Context context){
		if(!LoginManager.isLogin())
			return MAX_DOWN_DISH;
		String limit=FileManager.loadShared(context, FileManager.xmlFile_appInfo, FileManager.xmlKey_downDishLimit).toString();
		if(limit.length()==0) return MAX_DOWN_DISH;
		else return Integer.parseInt(limit);
	}
	/**
	 * 设置离线菜谱上限，必须大于本地时才会设置
	 * @param context
	 * @param limit
	 */
	public static boolean setDownDishLimit(Context context,int limit){
		if(limit>getDownDishLimit(context)){
			Map<String, String> map=new HashMap<String, String>();
			map.put(FileManager.xmlKey_downDishLimit, limit+"");
			FileManager.saveShared(context, FileManager.xmlFile_appInfo, map);
			return true;
		}
		else return false;
	}
}
