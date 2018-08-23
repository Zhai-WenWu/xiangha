package amodule.main.view.item;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.constraint.Guideline;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.xiangha.R;

import java.util.Map;

import acore.logic.FavoriteHelper;
import acore.logic.LoginManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.IconTextSpan;
import amodule._common.conf.FavoriteTypeEnum;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.LoadImage;

public class HomeStaggeredGridItem extends HomeItem {

//    private ConstraintLayout mRootLayout;
    private Guideline mGuideline;
    private ConstraintLayout mContentLayout;
    private ImageView mImg;
    private TextView mTitle,num_tv;
    private ImageView auther_userImg,img_fav;
    private int mImgMinHeight, mImgMaxHeight,fixedWidth;

    public HomeStaggeredGridItem(Context context) {
        this(context, null);
    }

    public HomeStaggeredGridItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeStaggeredGridItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.home_staggered_grid_item);
    }
    @SuppressLint("WrongViewCast")
    @Override
    protected void initView() {
        super.initView();
//        mRootLayout = findViewById(R.id.staggered_root);
        mContentLayout = findViewById(R.id.staggered_container);
        mImg = findViewById(R.id.img);
        mTitle = findViewById(R.id.title);
        auther_userImg = findViewById(R.id.user_header_img);
        img_fav = findViewById(R.id.img_fav);
        num_tv= findViewById(R.id.num_tv);
        mGuideline= findViewById(R.id.guideline);

        mImgMinHeight = (Tools.getPhoneWidth() - getResources().getDimensionPixelSize(R.dimen.dp_51)) / 2 * 4 / 5;
        mImgMaxHeight = getResources().getDimensionPixelSize(R.dimen.dp_260);
        fixedWidth = (Tools.getPhoneWidth() - getResources().getDimensionPixelSize(R.dimen.dp_51)) / 2;
    }

    @Override
    public void setData(Map<String, String> dataMap, int position) {
        super.setData(dataMap, position);
        if(mDataMap == null)
            return;
        int imgWidth = 0, imgHeight = 0;
        String parseResourceDataWidth = mDataMap.get("parseResourceData_width");
        if (TextUtils.isEmpty(parseResourceDataWidth)) {
            Map<String, String> resourceData = StringManager.getFirstMap(mDataMap.get("resourceData"));
            if (resourceData != null && !resourceData.isEmpty()) {
                String widthStr = resourceData.get("width");
                imgWidth = Integer.parseInt(widthStr);
                String heightStr = resourceData.get("height");
                imgHeight = Integer.parseInt(heightStr);
                mDataMap.put("parseResourceData_width", widthStr);
                mDataMap.put("parseResourceData_height", heightStr);
                mDataMap.put("parseResourceData_gif", resourceData.get("gif"));
                mDataMap.put("parseResourceData_img", resourceData.get("img"));
            }
        } else {
            imgWidth = Integer.parseInt(mDataMap.get("parseResourceData_width"));
            imgHeight = Integer.parseInt(mDataMap.get("parseResourceData_height"));
        }

        int realImgHeight = fixedWidth * imgHeight / (imgWidth < 1 ? 1 : imgWidth);
        if (realImgHeight < mImgMinHeight) {
            realImgHeight = mImgMinHeight;
        } else if (realImgHeight > mImgMaxHeight) {
            realImgHeight = mImgMaxHeight;
        }

        ConstraintSet cs = new ConstraintSet();
        cs.constrainWidth(mImg.getId(), ConstraintSet.MATCH_CONSTRAINT);
        cs.constrainHeight(mImg.getId(), realImgHeight);
        cs.constrainMinHeight(mImg.getId(), mImgMinHeight);
        cs.connect(mImg.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        cs.connect(mImg.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        cs.connect(mImg.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        cs.connect(mImg.getId(), ConstraintSet.BOTTOM, R.id.guideline, ConstraintSet.TOP);
        cs.applyTo(mContentLayout);
        if (!TextUtils.isEmpty(mDataMap.get("parseResourceData_gif"))) {
            mImg.setTag(TAG_ID, mDataMap.get("parseResourceData_gif"));
            Glide.with(getContext()).load(mDataMap.get("parseResourceData_gif")).asGif().placeholder(R.drawable.i_nopic).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(mImg);
        } else {
            mImg.setTag(TAG_ID, mDataMap.get("parseResourceData_img"));
            LoadImage.with(getContext()).load(mDataMap.get("parseResourceData_img")).setSaveType(FileManager.save_cache).setPlaceholderId(R.drawable.i_nopic).setErrorId(R.drawable.i_nopic).build().into(mImg);
        }
        mTitle.setText("");
        String title = mDataMap.get("name");
        if (!TextUtils.isEmpty(title)) {
            mTitle.setVisibility(View.VISIBLE);
            if (TextUtils.equals(mDataMap.get("isEssence"), "2")) {
                IconTextSpan.Builder ib = new IconTextSpan.Builder(getContext());
                ib.setBgColorInt(getResources().getColor(R.color.icon_text_bg));
                ib.setTextColorInt(getResources().getColor(R.color.c_white_text));
                ib.setText("精选");
                ib.setRadius(2f);
                ib.setRightMargin(3);
                ib.setBgHeight(14f);
                ib.setTextSize(10f);
                StringBuffer sb = new StringBuffer(" ");
                sb.append(title);
                SpannableStringBuilder ssb = new SpannableStringBuilder(sb.toString());
                ssb.setSpan(ib.build(), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mTitle.setText(ssb);
            } else {
                mTitle.setText(title);
            }
                mTitle.setText(title);
        } else {
            mTitle.setVisibility(View.GONE);
        }
        setImgFav();
        if(mDataMap.containsKey("favorites")){
            num_tv.setText(mDataMap.get("favorites"));
        }
        if (TextUtils.isEmpty(mDataMap.get("parseResourceData_customer_img"))) {
            Map<String, String> map = StringManager.getFirstMap(mDataMap.get("customer"));
            mDataMap.put("parseResourceData_customer_img", map.get("img"));
        }
        LoadImage.with(getContext()).load(mDataMap.get("parseResourceData_customer_img")).setSaveType(FileManager.save_cache).setPlaceholderId(R.drawable.i_nopic).setErrorId(R.drawable.i_nopic).build().into(auther_userImg);
    }


    private void setImgFav(){
        img_fav.setImageResource("2".equals(mDataMap.get("isFavorites"))?R.drawable.icon_home_good_selected:R.drawable.icon_home_good_def);
    }

    public ConstraintLayout getContentLayout() {
        return mContentLayout;
    }
}
