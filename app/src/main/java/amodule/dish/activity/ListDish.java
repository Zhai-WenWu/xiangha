package amodule.dish.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.adapter.AdapterListDish;
import amodule.user.activity.FriendHome;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.tools.AdPlayIdConfig;
import third.share.BarShare;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * Title:ListDish.java Copyright: Copyright (c) 2014~2017
 *
 * @author zeyu_t
 * @date 2014年10月14日
 */
@SuppressLint("InflateParams")
public class ListDish extends BaseActivity {

	private TextView authorName, dishInfo,dishName;
	
	private AdapterListDish adapter = null;
	private ArrayList<Map<String, String>> arrayList = null;

	private int currentPage = 0,everyPage = 0, loadPage = 0 ;
	private String name = "", g1 = "", type = "";
	private String shareImg = "";
	public boolean moreFlag = true, offLineOver = false, infoVoer = false,isToday=false;
	private String shareName = "";
	private String data_type = "";//推荐列表过来的数据
	private String module_type = "";//推荐列表过来的数据
	private Long startTime;//统计使用的时间
	private ArrayList<String> adIds;
	private XHAllAdControl xhAllAdControl;
	private static final Integer[] AD_INSTERT_INDEX = new Integer[]{3,9,16,24,32,40,48,56,64,72};//插入广告的位置。
	private ArrayList<Map<String,String>> adData;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = this.getIntent().getExtras();
		startTime = System.currentTimeMillis();
		if(bundle != null){
			type = bundle.getString("type");
			g1 = bundle.getString("g1");
			name = bundle.getString("name");
			data_type= bundle.getString("data_type");
			module_type= bundle.getString("module_type");
			shareName = name;
		}
		if ("recommend".equals(type) || "typeRecommend".equals(type))
			initActivity(name, 2, 0, R.layout.c_view_bar_title_time, R.layout.a_dish_caidan_list);
		else
			initActivity("", 2, 0, R.layout.c_view_bar_title, R.layout.a_dish_caidan_list);
		initAdData();
		initMenu();
		initBarView();

	}

	/**
	 * 初始化广告数据
	 */
	private void initAdData() {
		String[] ids= AdPlayIdConfig.MAIN_HOME_WEEK_GOOD_LIST;
		adIds= new ArrayList<>();
		adData= new ArrayList<>();//广告数据集合
		for(String id:ids)adIds.add(id);

		String statisticKey = "jz_list";
		Log.i("zhangyujian","adids:::"+adIds.size());
		xhAllAdControl= new XHAllAdControl(adIds, new XHAllAdControl.XHBackIdsDataCallBack() {
			@Override
			public void callBack(Map<String, String> map) {
				//处理广告数据
				int size= adIds.size();
				Log.i("zhangyujian","size11::"+size);
				for(int i=0;i<size;i++){
					if(map.containsKey(adIds.get(i))&&!TextUtils.isEmpty(map.get(adIds.get(i)))){
						String object = map.get(adIds.get(i));
						Map<String,String> tempMap = StringManager.getFirstMap(object);
						//进行数据拼装
						Log.i("zhangyujian","测试数据：：："+object);
						tempMap.put("adStyle","1");
						tempMap.put("info",map.get("desc"));
						tempMap.put("name",map.get("title"));
						tempMap.put("img",map.get("imgUrlao"));
						tempMap.put("nickName","zhangyujian");
						tempMap.put("allClick","zhangyujian");
						tempMap.put("favorites","favorites");
						adData.add(tempMap);

					}else{
						adData.add(new HashMap<String, String>());
					}
				}

			}
		},statisticKey);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		arrayList.clear();
		System.gc();
		long nowTime=System.currentTimeMillis();
		if(startTime>0&&(nowTime-startTime)>0&&!TextUtils.isEmpty(data_type)&&!TextUtils.isEmpty(module_type)){
			XHClick.saveStatictisFile("ListDish",module_type,data_type,g1,"","stop",String.valueOf((nowTime-startTime)/1000),"","","","");
		}
	}

	//初始化
	private void initMenu() {
		ListView listView = (ListView) findViewById(R.id.dish_menu_listview);
		if (type.equals("recommend") || type.equals("typeRecommend")) {
			TextView title_time = (TextView) findViewById(R.id.title_time);
			title_time.setText("" + Tools.getAssignTime("yyyy-MM-dd",0));
		} else {
			View view = LayoutInflater.from(ListDish.this).inflate(R.layout.a_dish_head_caidan_view, null);
			dishName = (TextView) view.findViewById(R.id.dish_menu_name);
			authorName = (TextView) view.findViewById(R.id.dish_menu_author_name);
			dishInfo = (TextView) view.findViewById(R.id.dish_menu_info);
			dishInfo.setClickable(true);
			listView.addHeaderView(view, null, false);
		}
		arrayList = new ArrayList<Map<String, String>>();
		// 绑定列表数据
		adapter = new AdapterListDish(this, listView, arrayList, 
				R.layout.a_dish_item_menu_new,
				new String[] {"info", "nickName", "allClick", "favorites"},
				new int[] {R.id.title, R.id.user_name, R.id.num1,
						R.id.num2},
				type);
		adapter.imgWidth = ToolsDevice.getWindowPx(this).widthPixels - Tools.getDimen(this.getApplicationContext(), R.dimen.dp_20);//20=10*2
		adapter.scaleType = ScaleType.CENTER_CROP;
		adapter.isAnimate = true;
		
		
		loadManager.setLoading(listView, adapter, true, new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				loadData();
			}
		});
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Intent intent = new Intent(ListDish.this, DetailDish.class);
				if (!type.equals("recommend") && !type.equals("typeRecommend")) 
					position--;
				if(position > -1 && position < arrayList.size()){
					intent.putExtra("code", arrayList.get(position).get("code"));
					intent.putExtra("name", arrayList.get(position).get("name"));
					startActivity(intent);
				}
			}
		});
	}

	private void initBarView() {
		// titleBar初始化
		ImageView img_share = (ImageView) findViewById(R.id.rightImgBtn2);
		img_share.setImageResource(R.drawable.z_z_topbar_ico_share);
		img_share.setVisibility(View.VISIBLE);
		img_share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doShare();
			}
		});
	}

	@SuppressLint("NewApi")
	public void loadData() {
		currentPage++;
		loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, -1, -1, currentPage,arrayList.size() == 0);
		String url = null;
		if (type.equals("recommend")) 
			url = StringManager.api_getDishList + "?type=" + type + "&page=" + currentPage;
		 else  if(type.equals("typeRecommend"))
			url = StringManager.api_getDishList + "?type=" + type + "&g1="+g1+"&page=" + currentPage;
		else
			url = StringManager.api_getDishList + "?type=" + type + "&g1=" + g1 + "&page=" + currentPage;
		ReqInternet.in().doGet(url, new InternetCallback(this) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if (flag >= UtilInternet.REQ_OK_STRING) {
					ArrayList<Map<String, String>> returnList = UtilString.getListMapByJson(returnObj);
					if (!type.equals("recommend") && !type.equals("typeRecommend")) {
						String classifyName = returnList.get(0).get("name");
						String customer = returnList.get(0).get("customer");
						final ArrayList<Map<String, String>> customers = StringManager.getListMapByJson(customer);
						String info = returnList.get(0).get("info");
						String authorName = "";
						if (customers != null && customers.size() > 0) {
							authorName = customers.get(0).get("nickName");
						}
						if (!TextUtils.isEmpty(classifyName)) {
							ListDish.this.dishName.setText(classifyName);
						} else {
							ListDish.this.dishName.setVisibility(View.GONE);
						}
						TextView title = (TextView)findViewById(R.id.title);
						if (!TextUtils.isEmpty(authorName)) {
							if (title != null) {
								String str = "";
								if (authorName.length() > 11)
									str = authorName.substring(0, 11) + "...";
								else
									str = authorName;
								title.setText(str);
							}
							ListDish.this.authorName.setText(authorName);
							findViewById(R.id.from_container).setVisibility(View.VISIBLE);
							ListDish.this.authorName.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									Intent intent = new Intent(ListDish.this, FriendHome.class);
									Bundle bundle = new Bundle();
									bundle.putString("code", customers.get(0).get("code"));
									intent.putExtras(bundle);
									ListDish.this.startActivity(intent);
								}
							});
						} else {
							if (title != null)
								title.setText("菜单");
							ListDish.this.authorName.setVisibility(View.GONE);
						}
						if (!TextUtils.isEmpty(info)) {
							ListDish.this.dishInfo.setText(info);
						} else {
							ListDish.this.dishInfo.setVisibility(View.GONE);
						}
						shareName = classifyName;
						returnList = UtilString.getListMapByJson(returnList.get(0).get("dishs"));

					}
					loadPage = returnList.size();
					if(!type.equals("recommend") && !type.equals("typeRecommend")) {
						Log.i("zhangyujian", "当前数据大小zz：：" + returnList.size());
						//插入广告。
						returnList = handlerAdData(returnList);
						Log.i("zhangyujian", "当前数据大小zz：：" + returnList.size());
					}
					for (int i = 0; i < returnList.size(); i++) {
						Map<String, String> map = returnList.get(i);
						if (i == 0) shareImg = returnList.get(i).get("img");
						map.put("allClick", map.get("allClick") + "浏览");
						map.put("favorites", map.get("favorites") + "收藏");
						if(type.equals("typeRecommend") && map.get("isToday").equals("1") && !isToday){
							map.put("isToday", "往期推荐");
							isToday=true;
						}else
							map.put("isToday", "hide");
						map.put("isDel", "hide");
						if(!map.containsKey("hasVideo")){
							map.put("hasVideo", "1");
						}
						String nickName = map.get("nickName");
						if(nickName == null || nickName.equals("") || nickName.equals("null"))
							map.put("nickName", "hide");
						arrayList.add(map);
					}
					Log.i("zhangyujian","展示数据集合：："+arrayList.size());

					adapter.notifyDataSetChanged();
				} else {
					toastFaildRes(flag,true,returnObj);
				}
				if (everyPage == 0) everyPage = loadPage;
				currentPage = loadManager.changeMoreBtn(flag, everyPage, loadPage, currentPage,arrayList.size() == 0);
				// 如果总数据为空,显示没有消息
				if (flag >= UtilInternet.REQ_OK_STRING && arrayList.size() == 0) {
					findViewById(R.id.dish_menu_noData).setVisibility(View.VISIBLE);
				}
				// 否则显示结果
				else
					findViewById(R.id.dish_menu_listview).setVisibility(View.VISIBLE);
			}
		});
	}

	protected void doShare() {
		XHClick.mapStat(this, "a_share400", "菜谱", "菜单详情页");
		if (TextUtils.isEmpty(shareName)) {
			shareName = "精选菜单";
		}
		String imgType = BarShare.IMG_TYPE_WEB;
		String title = "";
		String clickUrl = "";
		String content = "";
		if (type.equals("recommend")) {
			clickUrl = StringManager.wwwUrl + "caipu/recommend/";
		} else  {
			clickUrl = StringManager.wwwUrl + "caipu/caidan/" + g1;
		}
		// 是推荐菜单
		barShare = new BarShare(ListDish.this,"菜单详情", "菜谱");
		if (type.equals("caidan")) {
			// 是推荐菜单
			title = shareName + "，果断收藏！";
			content = shareName + "，各种精选菜谱，非常有用，推荐一下。（香哈菜谱）";
		} else {
			title = "今日推荐菜谱-" + Tools.getAssignTime("MM月dd日",0);
			clickUrl = StringManager.third_downLoadUrl;
			content = "今日推荐菜谱很不错，每天可以尝试不同的菜，吃货必备呀 ";
		}
		barShare.setShare(imgType, title, content, shareImg, clickUrl);
		barShare.openShare();
	}

	/**
	 * 拼装广告数据
	 * @param listData
	 * @return
	 */
	private ArrayList<Map<String,String>> handlerAdData(ArrayList<Map<String,String>> listData){
		Log.i("zhangyujian","adData:::"+adData.size());
		if(adData==null||adData.size()<=0){
			return listData;
		}
		for(int i= 0;i<listData.size();i++){
			int lenght= AD_INSTERT_INDEX.length;
			for(int j=0;j<lenght;j++){
				if(i==AD_INSTERT_INDEX[j]){//是要插广告的位置
					Log.i("zhangyujian","i::"+i);
					//数据无不是广告直接插入广告
					if(!listData.get(i).containsKey("adStyle")||TextUtils.isEmpty(listData.get(i).get("adStyle"))){
						//插入广告
						if(adData.get(j)!=null&&adData.get(j).size()>0){//数据
							Log.i("zhangyujian","数据：："+adData.get(j));
							listData.add(adData.get(j));
						}
					}//不进行如何操作。
				}
			}
		}
		return listData;
	}
}
