package third.mall.upload;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import amodule.upload.callback.UploadListNetCallBack;
import aplug.basic.BreakPointControl;
import aplug.basic.BreakPointUploadManager;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import third.mall.bean.EvalutionBean;

/**
 * PackageName : third.mall.upload
 * Created by MrTrying on 2017/8/8 20:39.
 * E_mail : ztanzeyu@gmail.com
 */

public class EvalutionUploadControl {
    private Context context;
    private OnPublishCallback onPublishCallback;
    private EvalutionBean bean;

    boolean isCanceled = false;
    boolean isPublishing = false;
    protected boolean instantlyUpload = false;

    public EvalutionUploadControl(Context context) {
        this.context = context;
        bean = new EvalutionBean();
    }

    public void uploadImage(final String filePath) {
        uploadImage(filePath, true, new SimpleUploadListNetCallBack() {
        });
    }

    private void uploadAgin(final String filePath) {
        uploadImage(filePath, false, new SimpleUploadListNetCallBack() {
        });
    }

    /**
     * 上传
     *
     * @param filePath
     */
    public void uploadImage(final String filePath, boolean isFirst, @NonNull final UploadListNetCallBack callBack) {
        if (TextUtils.isEmpty(filePath))
            return;
        if (isFirst) {
            bean.addImage(filePath);
        }
        if (instantlyUpload || !isFirst) {
            BreakPointControl uploadControl = new BreakPointControl(context, filePath, filePath, "img");
            BreakPointUploadManager.getInstance().addBreakPointContorl(filePath, uploadControl);
            uploadControl.start(new UploadListNetCallBack() {
                @Override
                public void onProgress(double progress, String uniqueId) {
                    if (callBack != null)
                        callBack.onProgress(progress, uniqueId);
                }

                @Override
                public void onSuccess(String url, String uniqueId, JSONObject jsonObject) {
                    //成功更新url
                    bean.replaceImage(filePath, url);
                    //回调
                    if (callBack != null)
                        callBack.onSuccess(url, uniqueId, jsonObject);
                    //如果处于发布中，再次调用发布方法
                    if (isPublishing && !isCanceled) {
                        Log.i("tzy","upload.onSuccess :: isPublishing = " + isPublishing);
                        publishEvalution();
                    }
                }

                @Override
                public void onFaild(String faild, String uniqueId) {
                    if (callBack != null)
                        callBack.onFaild(faild, uniqueId);

                    //如果发布中图片上传失败，发布直接失败
                    if (isPublishing && onPublishCallback != null) {
                        isPublishing = false;
                        onPublishCallback.onFailed();
                    }
                }

                @Override
                public void onLastUploadOver(boolean flag, String responseStr) {
                    if (callBack != null)
                        callBack.onLastUploadOver(flag, responseStr);
                }

                @Override
                public void onProgressSpeed(String uniqueId, long speed) {
                    if (callBack != null)
                        callBack.onProgressSpeed(uniqueId, speed);
                }
            });
        }
    }

    /**
     * @param filePath
     */
    public void delUploadImage(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            bean.removeImage(filePath);
            BreakPointUploadManager.getInstance().delBreakPointUpload(filePath);
        }
    }

    /** 发布 */
    public void publishEvalution() {
        isPublishing = true;
        if (onPublishCallback != null)
            onPublishCallback.onStratPublish();
        if (isAllUploadOver()) {
            //
            MallReqInternet.in().doPost(MallStringManager.mall_addComment,
                    combineParameter(),
                    new MallInternetCallback(context) {
                        @Override
                        public void loadstat(int flag, String url, Object msg, Object... stat) {
                            isPublishing = false;
                            if (flag >= MallReqInternet.REQ_OK_STRING) {

                                if (onPublishCallback != null)
                                    onPublishCallback.onSuccess(msg);
                            } else {

                                if (onPublishCallback != null)
                                    onPublishCallback.onFailed();
                            }
                        }
                    });
        } else {
            //上传未上传完成的的图片
            for (String imageUrl : bean.getImages()) {
                if (!imageUrl.startsWith("http"))
                    uploadAgin(imageUrl);
            }
        }
    }

    /**
     * 获取参数
     *
     * @return
     */
    public LinkedHashMap<String, String> combineParameter() {
        LinkedHashMap<String, String> uploadTextData = new LinkedHashMap<>();
        uploadTextData.put("type", "6");
        uploadTextData.put("product_code", bean.getProductId());
        uploadTextData.put("score", String.valueOf(bean.getScore()));
        uploadTextData.put("order_id", bean.getOrderId());
        uploadTextData.put("is_quan", bean.isCanShare() ? "2" : "1");
        uploadTextData.put("content[0][text]", bean.getContent());
        ArrayList<String> images = bean.getImages();
        for (int i = 0; i < images.size(); i++) {
            uploadTextData.put("content[0][imgs][" + i + "]", images.get(i));
        }
        Log.i("tzy", "combineParameter :: " + uploadTextData.toString());
        return uploadTextData;
    }

    /**
     * 获取content字段内容
     *
     * @return
     */
//    public ArrayList<Map<String, String>> getCotnent() {
//        ArrayList<Map<String, String>> contentArray = new ArrayList<>();
//        Map<String, String> content = new HashMap<>();
//        content.put("text", bean.getContent());
//        content.put("imgs", bean.getImages().toString());
//        contentArray.add(content);
//        return contentArray;
//    }

    /**
     * 是否全部上传完成
     *
     * @return
     */
    private boolean isAllUploadOver() {
        ArrayList<String> images = bean.getImages();
        for (String imageUrl : images) {
            if (!imageUrl.startsWith("http")) {
                return false;
            }
        }
        return true;
    }

    /**取消发布*/
    public void cancelUpload() {
        isPublishing = false;
        Log.i("tzy","cancelUpload :: isPublishing = " + isPublishing);
//        isCanceled = true;
        MallReqInternet.in().cancelRequset(
                new StringBuffer(MallStringManager.mall_addComment)
                        .append(combineParameter().toString()
                        ).toString());
    }

    public EvalutionUploadControl setContent(String content) {
        bean.setContent(content);
        return this;
    }

    public EvalutionUploadControl setOrderId(String orderId) {
        bean.setOrderId(orderId);
        return this;
    }

    public EvalutionUploadControl setProductId(String code) {
        bean.setProductId(code);
        return this;
    }

    public EvalutionUploadControl setScore(int score) {
        bean.setScore(score);
        return this;
    }

    public EvalutionUploadControl setCanShare(boolean canShare) {
        bean.setCanShare(canShare);
        return this;
    }

    public OnPublishCallback getOnPublishCallback() {
        return onPublishCallback;
    }

    public void setOnPublishCallback(OnPublishCallback onPublishCallback) {
        this.onPublishCallback = onPublishCallback;
    }

    /**发布状态回调*/
    public interface OnPublishCallback {
        /**开始发布*/
        void onStratPublish();
        /**发布成功*/
        void onSuccess(Object msg);
        /**发布失败*/
        void onFailed();
    }

    /**默认上传进度回调*/
    public static abstract class SimpleUploadListNetCallBack implements UploadListNetCallBack {
        @Override
        public void onProgress(double progress, String uniqueId) {
        }

        @Override
        public void onLastUploadOver(boolean flag, String responseStr) {
        }

        @Override
        public void onProgressSpeed(String uniqueId, long speed) {
        }

        @Override
        public void onSuccess(String url, String uniqueId, JSONObject jsonObject) {
        }

        @Override
        public void onFaild(String faild, String uniqueId) {
        }
    }
}
