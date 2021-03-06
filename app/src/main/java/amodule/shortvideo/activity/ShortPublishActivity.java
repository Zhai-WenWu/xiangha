package amodule.shortvideo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.quze.videorecordlib.VideoRecorderCommon;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.observer.ObserverManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.article.db.UploadArticleData;
import amodule.article.db.UploadVideoSQLite;
import amodule.dish.db.UploadDishData;
import amodule.other.activity.PlayVideo;
import amodule.search.view.MultiTagView;
import amodule.shortvideo.tools.ShortVideoPublishBean;
import amodule.shortvideo.tools.ShortVideoPublishManager;
import amodule.topic.activity.SearchTopicActivity;
import amodule.user.activity.FriendHome;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import third.aliyun.work.AliyunCommon;
import third.location.LocationHelper;

import static acore.observer.ObserverManager.NOTIFY_SAVE_VIDEO_DRAF;

/**
 * 视频发布页面
 */
public class ShortPublishActivity extends BaseActivity implements View.OnClickListener {

    private EditText edit_text;
    private ImageView video_cover, location_img, topic_img;
    private RelativeLayout publish_layout;
    private TextView location_tv, topic_tv;
    private String videoPath, imgPath, otherData, topicCode, topicName;
    private String location_state = "1";//定位状态 1-正在定位，2-定位成功，3-定位失败
    private boolean isShowLocation = true;//是否显示定位信息
    private ArrayList<Map<String, String>> topicList = new ArrayList<>();
    private ShortVideoPublishBean shortVideoPublishBean = new ShortVideoPublishBean();
    private String extraDataJson;
    private String id;
    private final int SELECTED_TOPIC = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 2, 0, 0, R.layout.a_short_video_publish);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            videoPath = (String) bundle.get("videoPath");
            imgPath = (String) bundle.get("imgPath");
            otherData = (String) bundle.get("otherData");
            topicCode = (String) bundle.get("topicCode");
            topicName = (String) bundle.get("topicName");
            extraDataJson = (String) bundle.get("extraDataJson");
            id = (String) bundle.get("id");
        }
        handleExtraData();
        initView();
        initData();
    }

    private void handleExtraData() {
        if ((TextUtils.isEmpty(videoPath) || TextUtils.isEmpty(imgPath)) && TextUtils.isEmpty(extraDataJson)) {
            if (!TextUtils.isEmpty(id)) {
                UploadVideoSQLite uploadVideoSQLite = new UploadVideoSQLite(this);
                uploadVideoSQLite.deleteById(Integer.parseInt(id));
            }
            this.finish();
            return;
        }
        if (!TextUtils.isEmpty(videoPath)) {
            shortVideoPublishBean.setVideoPath(videoPath);
        }
        if (!TextUtils.isEmpty(imgPath)) {
            shortVideoPublishBean.setImagePath(imgPath);
        }
        if (!TextUtils.isEmpty(otherData)) {
            Map<String, String> mapTemp = StringManager.getFirstMap(otherData);
            shortVideoPublishBean.setImageSize(mapTemp.get("imageSize"));
            shortVideoPublishBean.setVideoSize(mapTemp.get("videoSize"));
            shortVideoPublishBean.setVideoTime(mapTemp.get("videoTime"));
        }
        if (!TextUtils.isEmpty(topicCode) && !TextUtils.isEmpty(topicName)) {
            shortVideoPublishBean.setTopicCode(topicCode);
            shortVideoPublishBean.setTopicName(topicName);
        }

        if (!TextUtils.isEmpty(extraDataJson)) {
            shortVideoPublishBean.jsonToBean(extraDataJson);
        }
        if (!TextUtils.isEmpty(id)) {
            shortVideoPublishBean.setId(id);
        }
    }

    private void initView() {
        ((TextView) findViewById(R.id.title)).setText("发布");
        findViewById(R.id.rightImgBtn2).setVisibility(View.GONE);
        findViewById(R.id.rightImgBtn4).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.rightText)).setText("存草稿");
        findViewById(R.id.rightText).setVisibility(View.VISIBLE);
        findViewById(R.id.rightText).setOnClickListener(this);
        edit_text = findViewById(R.id.edit_text);
        video_cover = findViewById(R.id.video_cover);
        location_tv = findViewById(R.id.location_tv);
        location_img = findViewById(R.id.location_img);
        topic_img = findViewById(R.id.topic_img);
        topic_tv = findViewById(R.id.topic_tv);
        publish_layout = findViewById(R.id.publish_layout);
        publish_layout.setOnClickListener(this);
        findViewById(R.id.topic_more_linear).setOnClickListener(this);
        findViewById(R.id.topic_back).setOnClickListener(this);
        findViewById(R.id.topic_delete).setOnClickListener(this);
        findViewById(R.id.video_duration_layout).setOnClickListener(this);
        findViewById(R.id.video_cover).setOnClickListener(this);
        findViewById(R.id.location_tv).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XHClick.onEvent(XHApplication.in(), "a_pre_release", "上一步");
                ShortPublishActivity.this.finish();
            }
        });
        findViewById(R.id.topic_tv).setOnClickListener(this);
        edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String textTemp = s.toString();
                if (textTemp.length() > 50) {
                    edit_text.setText(textTemp.substring(0, 50));
                    Tools.showToast(XHApplication.in(), "最多50字");
                } else {

                }
            }
        });
    }

    private void initData() {
        initUIData();
        handleLocation();
    }

    public void initUIData() {
        if (!TextUtils.isEmpty(shortVideoPublishBean.getName())) {
            edit_text.setText(shortVideoPublishBean.getName());
        }
        handleImgPath();
        handleTopicUi();
    }

    /**
     * 处理定位信息
     */
    private void handleLocation() {
        locationMap = new LinkedHashMap<String, String>();
        LocationHelper.getInstance().registerLocationListener(locationCallBack);
        LocationHelper.getInstance().startLocation();
        location_state = "1";
        handleLocationMsg("");
    }

    /**
     * 校验数据
     * isValid
     */
    private boolean checkData() {
//        if(TextUtils.isEmpty(edit_text.getText().toString())){
//            Tools.showToast(this,"请输入文字");
//            return true;
//        }
        saveUIData();
        return false;
    }

    /**
     * 保存ui数据
     */
    private void saveUIData() {
        String title = edit_text.getText().toString();
        shortVideoPublishBean.setName(title);

    }

    /**
     * 保存数据---存储草稿
     */
    private void saveData() {
        saveUIData();
        UploadVideoSQLite uploadVideoSQLite = new UploadVideoSQLite(this);
        if (uploadVideoSQLite.checkOver(UploadDishData.UPLOAD_DRAF)) {
            Tools.showToast(this, "您已经有10个草稿待发布了，清理一下草稿箱后再继续存储吧～");
            return;
        }
        if (!isShowLocation) {
            shortVideoPublishBean.setAddress("");
        }
        UploadArticleData uploadArticleData = new UploadArticleData();
        uploadArticleData.setTitle(shortVideoPublishBean.getName());
        uploadArticleData.setImg(shortVideoPublishBean.getImagePath());
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(shortVideoPublishBean.getVideoPath());
        uploadArticleData.setVideos(jsonArray.toString());
        uploadArticleData.setExtraDataJson(shortVideoPublishBean.toJsonString());
        uploadArticleData.setUploadType(UploadDishData.UPLOAD_DRAF);
        if (!TextUtils.isEmpty(shortVideoPublishBean.getId())) {
            int num = uploadVideoSQLite.update(Integer.parseInt(shortVideoPublishBean.getId()), uploadArticleData);
            if (num > 0) {
                Tools.showToast(this, "已经成功草稿");
            }
        } else {
            int id = uploadVideoSQLite.insert(uploadArticleData);
            if (id > 0) {
                Tools.showToast(this, "已经成功草稿");
                shortVideoPublishBean.setId(String.valueOf(id));
            }
        }
        this.finish();
        AliyunCommon.getInstance().deleteAllActivity();
        VideoRecorderCommon.instance().deleteAllActivity();


    }

    /**
     * 开始发布
     */
    public void startPublish() {
        if (checkData()) {
            return;
        }
        if (ShortVideoPublishManager.getInstance().isUpload()) {
            Tools.showToast(this, "当前有正在上传数据");
            return;
        }
        if (!isShowLocation) {
            shortVideoPublishBean.setAddress("");
        }
        ShortVideoPublishManager.getInstance().setShortVideoPublishBean(shortVideoPublishBean);
        ShortVideoPublishManager.getInstance().startUpload();
        if (TextUtils.isEmpty(id)) {
            Intent intent = new Intent(this, FriendHome.class);
            intent.putExtra("type", "video");
            intent.putExtra("code", LoginManager.userInfo.get("code"));
            startActivity(intent);
        }
        this.finish();
        AliyunCommon.getInstance().deleteAllActivity();
        VideoRecorderCommon.instance().deleteAllActivity();
    }

    /**
     * 删除选中的话题
     */
    private void deleteSelectTopic() {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.publish_layout://发布
                if ("wifi".equals(ToolsDevice.getNetWorkType(this))) {//WiFi状态下
                    startPublish();
                } else {//非wif状态下
                    showDialog();
                }

                XHClick.onEvent(XHApplication.in(), "a_pre_release", "发布");
                break;
            case R.id.rightText://草稿
                saveData();
                ObserverManager.getInstance().notify(NOTIFY_SAVE_VIDEO_DRAF, null, null);
                XHClick.onEvent(XHApplication.in(), "a_pre_release", "存草稿");
                break;
            case R.id.topic_more_linear://更多话题
                break;
            case R.id.topic_delete://删除话题
                uiTopic("");
                shortVideoPublishBean.setTopicName("");
                shortVideoPublishBean.setTopicCode("");
                break;
            case R.id.video_duration_layout://预览
            case R.id.video_cover:
                startPalyVideo();
                XHClick.onEvent(XHApplication.in(), "a_pre_release", "视频缩略图");
                break;
            case R.id.location_tv://定位
                XHClick.onEvent(XHApplication.in(), "a_pre_release", "地理位置");
                isShowLocation = !isShowLocation;
                handleLocationMsg(showText);
                break;
            case R.id.topic_tv:
                XHClick.onEvent(XHApplication.in(), "a_pre_release", "添加话题", "添加话题");
                Intent intent = new Intent(ShortPublishActivity.this, SearchTopicActivity.class);
                startActivityForResult(intent, SELECTED_TOPIC);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case SELECTED_TOPIC:

                    shortVideoPublishBean.setTopicCode(data.getStringExtra("code"));
                    shortVideoPublishBean.setTopicName(data.getStringExtra("name"));
                    uiTopic(data.getStringExtra("name"));
                    break;
            }
        }
    }

    private void startPalyVideo() {
        if (!TextUtils.isEmpty(shortVideoPublishBean.getImagePath()) && !TextUtils.isEmpty(shortVideoPublishBean.getVideoPath())) {
            Intent intent = new Intent(ShortPublishActivity.this, PlayVideo.class);
            intent.putExtra("url", shortVideoPublishBean.getVideoPath());
            intent.putExtra("img", shortVideoPublishBean.getImagePath());
            intent.putExtra("name", shortVideoPublishBean.getName());
            this.startActivity(intent);

        }
    }

    //***************************定位start*******************************************
    private LinkedHashMap<String, String> locationMap;
    private String showText;

    public void unregisterLocationListener() {
        LocationHelper.getInstance().unregisterLocationListener(locationCallBack);
    }

    private LocationHelper.LocationListener locationCallBack = new LocationHelper.LocationListener() {
        @Override
        public void onSuccess(AMapLocation value) {
            unregisterLocationListener();

            locationMap.put("latitude", "" + value.getLatitude());
            locationMap.put("longitude", "" + value.getLongitude());
            if (!TextUtils.isEmpty(value.getAdCode()) && !TextUtils.isEmpty(value.getAddress())) {
                locationMap.put("adCode", value.getAdCode());
                locationMap.put("address", value.getAddress());
                locationMap.put("addressDetail", value.getAddress());
                locationMap.put("country", value.getCountry());
                locationMap.put("province", value.getProvince());
                locationMap.put("city", value.getCity());
                locationMap.put("district", value.getDistrict());
                if (!TextUtils.isEmpty(value.getProvince()) || !TextUtils.isEmpty(value.getCity())
                        || !TextUtils.isEmpty(value.getDistrict())) {
                    if (value.getProvince().equals(value.getCity())) {
                        showText = value.getCity() + " " + value.getDistrict();
                    } else {
                        showText = value.getProvince() + " " + value.getCity();
                    }
                }
                String jsonTemp = Tools.map2Json(locationMap);
                shortVideoPublishBean.setAddress(jsonTemp);
                location_state = "2";
                handleLocationMsg(showText);
            } else {
                location_state = "3";
                handleLocationMsg("定位失败");
            }

        }

        @Override
        public void onFailed() {
            unregisterLocationListener();
            location_state = "3";
            handleLocationMsg("定位失败");
        }
    };

    /**
     * 显示请求字符串
     *
     * @param str
     */
    private void handleLocationMsg(String str) {
        try {
            if (!isShowLocation) {//当前使用
                location_img.setSelected(false);
                location_tv.setTextColor(Color.parseColor("#999999"));
                location_tv.setText("不显示我的定位");
                return;
            }
            if ("2".equals(location_state)) {//定位成功
                if (str == null || str.trim().equals("") || str.equals("null")) {
                    location_img.setSelected(false);
                    location_tv.setTextColor(Color.parseColor("#999999"));
                    location_tv.setText("定位失败");
                    location_state = "3";
                } else {
                    location_img.setSelected(true);
                    location_tv.setTextColor(Color.parseColor("#3e3e3e"));
                    location_tv.setText(str);
                }
            } else if ("3".equals(location_state)) {//定位失败
                location_img.setSelected(false);
                location_tv.setTextColor(Color.parseColor("#999999"));
                location_tv.setText("定位失败");
            } else if ("1".equals(location_state)) {
                location_img.setSelected(false);
                location_tv.setTextColor(Color.parseColor("#999999"));
                location_tv.setText("正在定位");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //***************************定位start*******************************************

    /**
     * 处理视频图片
     */
    private void handleImgPath() {
        if (!TextUtils.isEmpty(shortVideoPublishBean.getImagePath())) {
            LoadImage.with(this).load(shortVideoPublishBean.getImagePath()).build().into(video_cover);
        }
    }

    /**
     * 处理话题样式
     */
    public void handleTopicUi() {
        if (!TextUtils.isEmpty(shortVideoPublishBean.getTopicCode()) && !TextUtils.isEmpty(shortVideoPublishBean.getTopicName())) {
            checkTopic();
        }
    }

    public void uiTopic(String topic) {
        topic_img.setSelected(!TextUtils.isEmpty(topic));
        topic_tv.setText(TextUtils.isEmpty(topic) ? "添加话题" : topic);
        topic_tv.setTextColor(Color.parseColor(TextUtils.isEmpty(topic) ? "#999999" : "#3e3e3e"));
        findViewById(R.id.topic_delete).setVisibility(TextUtils.isEmpty(topic) ? View.GONE : View.VISIBLE);
    }

    /**
     * 校验
     */
    public void checkTopic() {
        if (!TextUtils.isEmpty(shortVideoPublishBean.getTopicCode())) {
            String url = StringManager.API_SHORTVIDEO_TOPICCHECK;
            String params = "code=" + shortVideoPublishBean.getTopicCode();
            ReqEncyptInternet.in().doGetEncypt(url, params, new InternetCallback() {
                @Override
                public void loaded(int flag, String url, Object msg) {
                    if (flag >= ReqInternet.REQ_OK_STRING) {
                        uiTopic(shortVideoPublishBean.getTopicName());
                    } else {
                        Tools.showToast(XHApplication.in(), "此话题不存在，请您重新选择~");
                        shortVideoPublishBean.setTopicCode("");
                        shortVideoPublishBean.setTopicName("");
                    }
                }
            });
        }

    }

    private void showDialog() {
        DialogManager dialogManager = new DialogManager(this);
        dialogManager.createDialog(new ViewManager(dialogManager)
                .setView(new TitleMessageView(this).setText("当前不是wifi环境,是否发布?"))
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
                                startPublish();
                            }
                        }))).show();
    }
}
