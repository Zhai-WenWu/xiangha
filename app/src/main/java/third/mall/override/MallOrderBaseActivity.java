package third.mall.override;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.xianghatest.R;

import acore.logic.ActivityMethodManager;
import acore.logic.load.LoadManager;
import acore.override.XHApplication;
import acore.tools.PageStatisticsUtils;
import amodule.main.Main;
import third.mall.aplug.MallCommon;

public class MallOrderBaseActivity extends FragmentActivity{
	protected int level = 3;
	public RelativeLayout rl;
	public LoadManager loadManager;
	private ActivityMethodManager mActMagager;

	private long resumeTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActMagager = new ActivityMethodManager(this);
		String from = getIntent().getStringExtra("from");
		if(!TextUtils.isEmpty(from)){
			PageStatisticsUtils.onPageChange(from,this);
		}
	}
	
	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		rl = (RelativeLayout) findViewById(R.id.activityLayout);
		if (rl != null) {
			loadManager = new LoadManager(this,rl);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return mActMagager.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		mActMagager.onMenuItemSelected(featureId, item);
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onBackPressed() {
		// 程序如果未初始化但却有定时器执行，则停止它。主要用于外部吊起应用时
		if (Main.allMain == null && Main.timer != null) {
			Main.stopTimer();
		}
		super.onBackPressed();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		resumeTime = System.currentTimeMillis();
		if(XHApplication.in()==null){
			Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());    
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    
			startActivity(i); 
			return;
		}
		mActMagager.onResume(level);
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		PageStatisticsUtils.onPausePage(this,resumeTime,System.currentTimeMillis());
		mActMagager.onPause();
		//用完即回收
		if(MallCommon.interfaceMall!=null)
			MallCommon.interfaceMall=null;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		// 设置切换动画，从右边进入，左边退出
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
		// 设置切换动画，从右边进入，左边退出
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mActMagager.onDestroy();
	}
}