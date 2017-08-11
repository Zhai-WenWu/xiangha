package third.mall.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.Map;

import acore.override.view.ItemBaseView;
import acore.tools.Tools;
import acore.widget.ProperRatingBar;
import third.mall.activity.PublishEvalutionSingleActivity;

/**
 * PackageName : third.mall.view
 * Created by MrTrying on 2017/8/8 19:58.
 * E_mail : ztanzeyu@gmail.com
 */

public class CommodEvalutionItem extends ItemBaseView {
    public CommodEvalutionItem(Context context) {
        super(context,R.layout.item_commod_layout);
        initialize();
    }

    public CommodEvalutionItem(Context context, AttributeSet attrs) {
        super(context, attrs,R.layout.item_commod_layout);
        initialize();
    }

    public CommodEvalutionItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr,R.layout.item_commod_layout);
        initialize();
    }

    private ImageView image;
    private TextView title,starDesc,evalutionButton;
    private ProperRatingBar ratingBar;

    private String[] evalutionStarDescArray;
    private OnRatePickedCallback onRatePickedCallback;

    private void initialize(){
        initData();
        image = (ImageView) findViewById(R.id.image);
        title = (TextView) findViewById(R.id.title_text);
        starDesc = (TextView) findViewById(R.id.evalution_button);
        evalutionButton = (TextView) findViewById(R.id.evalution_status);
        ratingBar = (ProperRatingBar) findViewById(R.id.rating_bar);

    }

    private void initData() {
        evalutionStarDescArray = getResources().getStringArray(R.array.evalution_star_descriptions);
    }

    /**
     * 设置数据
     * @param data
     */
    public void setData(final Map<String, String> data) {
        setViewText(title,data,"product_name");
        setViewImage(image,"product_img");

        //初始化ratingbar
        int rating = evalutionStarDescArray.length - 1;
        if(data.containsKey("score") && !TextUtils.isEmpty(data.get("score"))){
            rating = Integer.parseInt(data.get("score"));
        }
        final int score = rating;
        ratingBar.setRating(rating);
        ratingBar.setListener(new ProperRatingBar.RatingListener() {
            @Override
            public void onRatePicked(ProperRatingBar ratingBar) {
                int rating = ratingBar.getRating() - 1;
                starDesc.setText(evalutionStarDescArray[rating]);
                if(onRatePickedCallback != null){
                    onRatePickedCallback.onRatePicked(ratingBar.getRating());
                }
            }
        });

        //初始化评价button
        if("2".equals(data.get("status"))){
            evalutionButton.setText("已评价");
            evalutionButton.setTextColor(getResources().getColor(R.color.common_super_tint_text));
            evalutionButton.setBackgroundResource(R.drawable.bg_evalution_status_select);

            evalutionButton.setClickable(false);
        }else{
            evalutionButton.setText("评价晒单");
            evalutionButton.setTextColor(getResources().getColor(R.color.comment_color));
            evalutionButton.setBackgroundResource(R.drawable.bg_evalution_status);

            evalutionButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String code = data.get("product_code");
                    if(TextUtils.isEmpty(code)){
                        Tools.showToast(getContext(),"数据错误");
                        return;
                    }
                    Intent intent = new Intent(getContext(), PublishEvalutionSingleActivity.class);
                    intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_CODE,code);
                    intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_IMAGE,data.get("product_img"));
                    intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_SCORE,score);
                    getContext().startActivity(intent);
                }
            });
        }
    }

    public OnRatePickedCallback getOnRatePickedCallback() {
        return onRatePickedCallback;
    }

    public void setOnRatePickedCallback(OnRatePickedCallback onRatePickedCallback) {
        this.onRatePickedCallback = onRatePickedCallback;
    }

    public interface OnRatePickedCallback{
        public void onRatePicked(int rating);
    }
}