package amodule.topic.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.xiangha.R;

import acore.tools.FileManager;
import amodule.topic.style.CustomClickableSpan;
import aplug.basic.BlurBitmapTransformation;
import aplug.basic.LoadImage;
import aplug.basic.SubAnimTarget;
import xh.basic.tool.UtilImage;

public class TopicHeaderView extends ConstraintLayout {

    private CustomClickableSpan mCustomClickableSpan;

    private ImageView mUserRearImg;
    private ImageView mUserFrontImg;
    private TextView mTopicUser;
    private ImageView mTopicAttention;
    private TextView mTopicInfo;
    private TextView mTopicNum;
    public TopicHeaderView(Context context) {
        super(context);
        initView(context);
    }

    public TopicHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public TopicHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.topic_header_layout, this, true);
        mUserRearImg = findViewById(R.id.user_rear_img);
        mUserFrontImg = findViewById(R.id.user_front_img);
        mTopicUser = findViewById(R.id.topic_user);
        mTopicAttention = findViewById(R.id.topic_attention);
        mTopicInfo = findViewById(R.id.topic_info);
        mTopicNum = findViewById(R.id.topic_num);
    }

    public void showUserImage(String url, OnClickListener listener) {
        if (TextUtils.isEmpty(url)) {
            hideTopicImage();
            return;
        }
        mUserFrontImg.setOnClickListener(listener);
        mUserFrontImg.setTag(R.string.tag, url);
        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(getContext())
                .load(url)
                .setSaveType(FileManager.save_cache)
                .build();
        bitmapRequest.into(new SubAnimTarget(mUserFrontImg) {
            @Override
            protected void setResource(Bitmap bitmap) {
                if (bitmap != null && mUserFrontImg.getTag(R.string.tag) != null && mUserFrontImg.getTag(R.string.tag).equals(url)) {
                    mUserFrontImg.setVisibility(View.VISIBLE);
                    mUserFrontImg.setImageBitmap(bitmap);
                    Bitmap bitmap1 = UtilImage.BoxBlurFilter(bitmap, 3, 3, 3);
                    mUserRearImg.setImageBitmap(bitmap1);
                } else {
                    hideTopicImage();
                }
            }

            @Override
            public void onLoadFailed(Exception e, Drawable drawable) {
                super.onLoadFailed(e, drawable);
                hideTopicImage();
            }
        });
    }

    public void showTopicUser(String userName, OnClickListener listener) {
        if (TextUtils.isEmpty(userName)) {
            hideTopicUser();
            return;
        }
        mTopicUser.setVisibility(View.VISIBLE);
        SpannableStringBuilder ssb = new SpannableStringBuilder(userName);
        if (mCustomClickableSpan == null) {
            mCustomClickableSpan = new CustomClickableSpan();
            mCustomClickableSpan.setTextColor(Color.parseColor("#ffd914"));
            mCustomClickableSpan.setHasUnderline(false);
        }
        mCustomClickableSpan.setOnClickListener(listener);
        ssb.setSpan(mCustomClickableSpan, 0, userName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.append(" 创建的话题");
        mTopicUser.setText(ssb);
    }

    public void showTopicAttention(boolean attentioned, OnClickListener listener){
        mTopicAttention.setVisibility(View.VISIBLE);
        mTopicAttention.setOnClickListener(listener);
        mTopicAttention.setEnabled(!attentioned);
    }

    public void setAttentionEnable(boolean enable) {
        mTopicAttention.setEnabled(enable);
    }

    public void showTopicInfo(String info) {
        if (TextUtils.isEmpty(info)) {
            hideTopicInfo();
            return;
        }
        mTopicInfo.setVisibility(View.VISIBLE);
        mTopicInfo.setText(info);
    }

    public void showTopicNum(String numStr) {
        if (TextUtils.isEmpty(numStr)) {
            hideTopicNum();
            return;
        }
        mTopicNum.setVisibility(View.VISIBLE);
        mTopicNum.setText(String.format("-%s人参与-", numStr));
    }

    public void hideTopicImage() {
        mUserFrontImg.setVisibility(View.GONE);
    }

    public void hideTopicUser() {
        mTopicUser.setVisibility(View.GONE);
    }

    public void hideTopicAttention() {
        mTopicAttention.setVisibility(View.GONE);
    }

    public void hideTopicInfo() {
        mTopicInfo.setVisibility(View.GONE);
    }

    public void hideTopicNum() {
        mTopicNum.setVisibility(View.GONE);
    }

}