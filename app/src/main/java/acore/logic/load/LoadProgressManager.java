package acore.logic.load;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import acore.tools.Tools;
import acore.tools.ToolsDevice;

/**
 * progress的管理类
 * @author Eva
 *
 */
public class LoadProgressManager {
	private LinearLayout mLoadFailLayout;
	private RelativeLayout mProgressBar;
	private View mProgressShadow;
	private ObjectAnimator loadingAnim;
	private int topHeight;
	
	public LoadProgressManager(Context context , RelativeLayout layout){
		topHeight = Tools.getStatusBarHeight(context) + Tools.getDimen(context,R.dimen.topbar_height);
		initProgress(context , layout);
		initLoadFailLayout(context, layout);
	}
	
	/**
	 * 初始化progress
	 * @param context 上下文
	 * @param layout
	 */
	@SuppressLint({"InflateParams", "WrongConstant"})
	private void initProgress(Context context , RelativeLayout layout){
		LayoutInflater inflater = LayoutInflater.from(context);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mProgressShadow = new View(context);
		mProgressShadow.setBackgroundColor(Color.parseColor("#33000000"));
		mProgressShadow.setLayoutParams(lp);
		mProgressShadow.setVisibility(View.GONE);
		layout.addView(mProgressShadow);
		//初始化progressBar
		mProgressBar = (RelativeLayout) inflater.inflate(R.layout.xh_main_loading, null,true);
		lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		mProgressBar.setLayoutParams(lp);
		//设置加载中动画
		ImageView loadingView = mProgressBar.findViewById(R.id.loadingIv);
		loadingAnim = ObjectAnimator.ofFloat(loadingView,"rotation",0f,359f);
		loadingAnim.setRepeatCount(ValueAnimator.INFINITE);
		loadingAnim.setRepeatMode(ValueAnimator.INFINITE);
		loadingAnim.setInterpolator(new LinearInterpolator());
		loadingAnim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationCancel(Animator animation) {
				super.onAnimationCancel(animation);
				loadingAnim.setFloatValues(0f);
			}
		});

		hideProgressBar();
		layout.addView(mProgressBar);
	}
	
	/**
	 * 初始化loadFailLayout
	 * @param context 上下文
	 * @param layout
	 */
	private void initLoadFailLayout(Context context , RelativeLayout layout){
		mLoadFailLayout = new LinearLayout(context);
		mLoadFailLayout.setOrientation(LinearLayout.VERTICAL);
		ImageView loadFaildImg = new ImageView(context);
		TextView loadFailTv = new TextView(context);
		TextView loadFailBtn = new TextView(context);
		mLoadFailLayout.addView(loadFaildImg);
		mLoadFailLayout.addView(loadFailTv);
		mLoadFailLayout.addView(loadFailBtn);
		
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		lp.setMargins(0,topHeight,0,0);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		mLoadFailLayout.setLayoutParams(lp);
		mLoadFailLayout.setGravity(Gravity.CENTER);
		
		int dp = ToolsDevice.dp2px(context, 52); 
		LinearLayout.LayoutParams loadFaildParams = new LinearLayout.LayoutParams(dp, dp);
		loadFaildParams.setMargins(0, 0, 0, ToolsDevice.dp2px(context, 32));
		loadFaildParams.gravity = Gravity.CENTER_HORIZONTAL;
		loadFaildImg.setLayoutParams(loadFaildParams);
		loadFaildParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		loadFaildParams.setMargins(0, 0, 0, ToolsDevice.dp2px(context, 14));
		loadFailTv.setLayoutParams(loadFaildParams);
		loadFaildParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		loadFailBtn.setLayoutParams(loadFaildParams);
		
		loadFaildImg.setScaleType(ScaleType.CENTER_INSIDE);
		loadFaildImg.setBackgroundResource(R.drawable.z_loading_failed);
		loadFaildImg.setClickable(true);
		loadFailTv.setText("加载失败，请重试");
		loadFailTv.setTextSize(Tools.getDimenSp(context, R.dimen.sp_16));
		loadFailTv.setTextColor(Color.parseColor("#949494"));
		GradientDrawable gradientDrawable = new GradientDrawable();
		gradientDrawable.setCornerRadius(Tools.getDimen(context,R.dimen.dp_4));
		String color = Tools.getColorStr(context,R.color.comment_color);
		gradientDrawable.setStroke(Tools.getDimen(context,R.dimen.dp_0_5), Color.parseColor(color));
		loadFailBtn.setBackgroundDrawable(gradientDrawable);
		loadFailBtn.setClickable(false);
		loadFailBtn.setGravity(Gravity.CENTER);
		loadFailBtn.setText("重新加载");
		loadFailBtn.setTextColor(Color.parseColor(color));
		loadFailBtn.setTextSize(Tools.getDimenSp(context, R.dimen.sp_14));
		int lrDp = Tools.getDimen(context,R.dimen.dp_20);
		int tbDp = Tools.getDimen(context,R.dimen.dp_7);
		loadFailBtn.setPadding(lrDp, tbDp, lrDp, tbDp);
		hideLoadFailBar();
		layout.addView(mLoadFailLayout);
	}
	
	/**
	 * 设置FailLayout的OnClickListener
	 * @param listener
	 */
	public void setFailClickListener(OnClickListener listener){
		if(listener != null && mLoadFailLayout != null){
			mLoadFailLayout.setOnClickListener(listener);
		}
	}
	
	public boolean isShowingProgressBar(){
		if(mProgressBar == null){
			return false;
		}
		return mProgressBar.getVisibility() == View.VISIBLE;
	}
	
	public void showProgressBar(){
		if(mProgressBar != null){
			loadingAnim.start();
			mProgressBar.setVisibility(View.VISIBLE);
		}
	}
	
	public void hideProgressBar(){
		if(mProgressBar != null){
			mProgressBar.setVisibility(View.GONE);
			loadingAnim.cancel();
		}
	}
	
	public boolean isShowingLoadFailBar(){
		if(mLoadFailLayout == null){
			return false;
		}
		return mLoadFailLayout.getVisibility() == View.VISIBLE;
	}
	
	public void showLoadFailBar(){
		if(mLoadFailLayout != null){
			mLoadFailLayout.setVisibility(View.VISIBLE);
		}
	}
	
	 public void hideLoadFailBar(){
		 if(mLoadFailLayout != null){
			 mLoadFailLayout.setVisibility(View.GONE);
		 }
	 }
	 
	 public void showProgressShadow(){
		 if(mProgressShadow != null){
			 mProgressShadow.setVisibility(View.VISIBLE);
		 }
		 mProgressBar.setOnClickListener(new OnClickListener() {
			@Override	public void onClick(View v) {}
		});
	 }
}
