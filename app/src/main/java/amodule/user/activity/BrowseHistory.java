package amodule.user.activity;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;

import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import acore.widget.NoScrollViewPager;
import acore.widget.PagerSlidingTabStrip;
import amodule.user.view.HistoryDishView;
import amodule.user.view.HistoryNousView;
import amodule.user.view.HistorySubjectView;
import amodule.user.view.HistoryView;

/**
 * 浏览历史
 */
public class BrowseHistory extends BaseActivity implements View.OnClickListener {
	private PagerSlidingTabStrip mTab;
	private NoScrollViewPager mViewPager;
	private RelativeLayout barTitle;
	private TextView rightText;
	private int defaultIndex = 0;
	private List<HistoryView> viewList = new ArrayList<>();
	private String[] titles = new String[]{"菜谱", "美食贴", "头条"};

	private boolean isChoose = false;//进此页面是否是为了选择菜谱

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("", 2, 0, 0, R.layout.a_user_browse_history);
		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			String position = bundle.getString("tab");
			if(!TextUtils.isEmpty(position)){
				 defaultIndex = Integer.parseInt(position);
			}
			isChoose = bundle.getBoolean("isChoose",false);
		}
		Log.i("zyj","isChoose::"+isChoose);
		initView();
//		initTitle();
	}

	private void initView() {
		TextView title = (TextView) findViewById(R.id.title);
		title.setText("浏览历史");

		findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		barTitle = (RelativeLayout) findViewById(R.id.bar_title);
		mTab = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mViewPager = (NoScrollViewPager) findViewById(R.id.viewpager);
		viewList.add(new HistoryDishView(this,isChoose).onCreateView());
		viewList.add(new HistorySubjectView(this).onCreateView());
		viewList.add(new HistoryNousView(this).onCreateView());
		BrowsePagerAdapter adapter = new BrowsePagerAdapter(viewList, titles);
		mViewPager.setAdapter(adapter);

		mTab.setViewPager(mViewPager);
		mTab.setListener();
		if(!isChoose) {
			rightText = (TextView) findViewById(R.id.rightText);
			rightText.setText("清空");
			rightText.setVisibility(View.VISIBLE);
			mTab.setmDelegatePageListener(new ViewPager.OnPageChangeListener() {
				@Override
				public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				}

				@Override
				public void onPageSelected(int position) {
					rightText.setVisibility(hasData() ? View.VISIBLE : View.GONE);
				}

				@Override
				public void onPageScrollStateChanged(int state) {
				}
			});
			rightText.setOnClickListener(this);
		}

		mViewPager.setCurrentItem(defaultIndex);
		if(isChoose){
			mTab.setVisibility(View.GONE);
			mViewPager.setNoScrollView(isChoose);
		}
	}

	private void initTitle() {
		if (Tools.isShowTitle()) {
			int topbarHeight = Tools.getDimen(this, R.dimen.topbar_height);
			int height = topbarHeight + Tools.getStatusBarHeight(this);

			RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
			barTitle.setLayoutParams(layout);
			barTitle.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(!isChoose) rightText.setVisibility(hasData() ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.rightText:
				final DialogManager dialogManager = new DialogManager(this);
				dialogManager.createDialog(new ViewManager(dialogManager)
						.setView(new TitleMessageView(this).setText("确定清空所有" + titles[mViewPager.getCurrentItem()] + "浏览记录？"))
						.setView(new HButtonView(this)
								.setNegativeText("取消", new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										dialogManager.cancel();
									}
								})
								.setPositiveText("确定", new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										dialogManager.cancel();
										clearCurrentHistory();
									}
								}))).show();
				break;
			default:
				break;
		}
	}

	private void clearCurrentHistory() {
		final int currentIndex = mViewPager.getCurrentItem();
		HistoryView view = viewList.get(currentIndex);
		if (view != null) {
			view.cleanData();
		}
	}

	private boolean hasData() {
		final int currentIndex = mViewPager.getCurrentItem();
		HistoryView view = viewList.get(currentIndex);
		if (view != null) {
			return view.hasData();
		}
		return false;
	}


	class BrowsePagerAdapter extends PagerAdapter {
		private List<HistoryView> views;
		private String[] titles;

		public BrowsePagerAdapter(List<HistoryView> views, String[] titles) {
			this.views = views;
			this.titles = titles;
		}

		@Override
		public int getCount() {
			return views.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return titles[position];
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(views.get(position).getRootView());
		}

		@Override
		public Object instantiateItem(View container, int position) {
			((ViewPager) container).addView(views.get(position).getRootView());
			return views.get(position).getRootView();
		}
	}

}
