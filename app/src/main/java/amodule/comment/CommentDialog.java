package amodule.comment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.logic.load.LoadManager;
import acore.override.XHApplication;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import acore.widget.KeyboardDialog;
import amodule.article.activity.ReportActivity;
import amodule.comment.view.ViewCommentItem;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;

import static xh.basic.tool.UtilString.getListMapByJson;

public class CommentDialog extends Dialog implements View.OnClickListener {

    private final int  KEYBOARD_OPTION_COMMENT = 1;
    private final int  KEYBOARD_OPTION_REPLAY = 2;

    private Context mContext;

    private LoadManager mLoadManager;
    private RelativeLayout mContentView;
    private DownRefreshList downRefreshList;
    private AdapterSimple adapterSimple;
    private ArrayList<Map<String, String>> listArray;
    private String type, code;
    private int currentPage = 0, everyPage = 0;
    private int dropPage = 1,upDropPage = 1,downDropPage = 1,slide = 1,from = 1,source = 1;

    private TextView commend_write_tv;
    private LinearLayout titleLayout;
    private TextView titleTv;
    private TextView comment_allNum;
    private ImageView close_img;

    private View mCommentHintView;

    private String gotoCommentId,gotoReplayId;

    private String mSendText;
    private String mReplayText;

    private String contentTongjiId,likeTongjiId = "a_like",reportTongjiId="a_report",deleteTongjiId = "a_delete";
    private String likeTwoLeven,reportTwoLeven,deleteTwoLeven;

    private StringBuffer commentIdStrBuffer;

    private String currentUrl = StringManager.api_addForum,currentParams,oldCommentId,mCurrentReplayId, mCurrentReplayName;
    private int replayIndex;
    private boolean isSend = false,isAddForm;

    private int mCommentsNum = -1;
    private String mCommentsNumStr;

    private CommentOptionSuccCallback mCommentOptionSuccCallback;

    private int mKeyboardDialogOptionFrom = KEYBOARD_OPTION_COMMENT;

    public CommentDialog(Context context, Map<String, String> data) {
        super(context, R.style.dialog_comment);
        mContext = context;
        View rootView = LayoutInflater.from(context).inflate(R.layout.a_comment_dialog, null);
        setContentView(rootView);
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.BottomInOutPopupAnim);
        initView(rootView);
        initData(data);
    }

    private void initView(View rootView) {
        mContentView = rootView.findViewById(R.id.activityLayout);
        mContentView.setPadding(0, ToolsDevice.getWindowPx(mContext).heightPixels / 3, 0, 0);
        titleLayout = rootView.findViewById(R.id.comment_title_layout);
        titleTv = rootView.findViewById(R.id.comment_title);
        listArray = new ArrayList<>();
        mCommentHintView = rootView.findViewById(R.id.commend_hind);
        comment_allNum = rootView.findViewById(R.id.comment_allNum);
        close_img = rootView.findViewById(R.id.close_img);
        close_img.setOnClickListener(this);
        commend_write_tv = rootView.findViewById(R.id.commend_write_tv);
        commend_write_tv.setOnClickListener(this);
        downRefreshList = rootView.findViewById(R.id.comment_listview);
        adapterSimple = new AdapterSimple(downRefreshList, listArray, R.layout.a_comment_item, new String[]{}, new int[]{}) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                final ViewCommentItem viewCommentItem = (ViewCommentItem) view.findViewById(R.id.comment_item);
                viewCommentItem.setCommentItemListener(getCommentItenListener(viewCommentItem,position));
                viewCommentItem.setUserInforListenr(getUserInforListener());
                viewCommentItem.setData(listArray.get(position));
                return view;
            }
        };
        mContentView.setOnClickListener(this);
    }

    private void initData(Map<String, String> data) {
        mLoadManager = new LoadManager(mContext, mContentView);
        commentIdStrBuffer = new StringBuffer();
        mCommentsNumStr = data.get("commentNum");
        try {
            mCommentsNum = Integer.parseInt(mCommentsNumStr);
        } catch (Exception e) {
            mCommentsNum = -1;
        }
        setCommentsNum();
        type = data.get("type");
        code = data.get("code");
        String newsId = data.get("newsId");
        //消息是否读过
        if (!TextUtils.isEmpty(newsId)) {
            String params = "type=news&p1=" + newsId;
            ReqInternet.in().doPost(StringManager.api_setUserData, params, new InternetCallback() {
                @Override
                public void loaded(int flag, String url, Object returnObj) {}
            });
        }
        //Log.i("FRJ","newsId:" + newsId);
        gotoCommentId = data.get("commentId");
        gotoReplayId = data.get("replayId");
        String fromType = data.get("from");
        if(!TextUtils.isEmpty(fromType)){
            from = Integer.parseInt(fromType);
        }
        String sourceType = data.get("source");
        if(!TextUtils.isEmpty(sourceType)){
            source = Integer.parseInt(sourceType);
        }

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(code)) {
            Tools.showToast(mContext, "缺少 类型 或 主题");
            dismiss();
        }

        if("1".equals(type)){
            contentTongjiId = "a_article_comment";
            likeTwoLeven = "文章评论点赞量";
            reportTwoLeven = "文章";
            deleteTwoLeven = "文章";
        }else if("2".equals(type)){
            contentTongjiId = "a_video_comment";
            likeTwoLeven = "视频评论点赞量";
            reportTwoLeven = "视频";
            deleteTwoLeven = "视频";
        }else{
            Tools.showToast(mContext, "类型不对");
            dismiss();
        }
        mLoadManager.showProgressBar();
        mLoadManager.setLoading(downRefreshList, adapterSimple, true, new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                getCommentData(false);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCommentData(true);
            }
        });
        View view = new View(mContext);
        view.setBackgroundResource(R.color.backgroup_color);
        view.setMinimumHeight(Tools.getDimen(mContext, R.dimen.dp_40));
        downRefreshList.addFooterView(view);
    }

    private void setCommentsNum() {
        comment_allNum.setText("全部评论(" + (mCommentsNum == -1 ? mCommentsNumStr : mCommentsNum) + ")");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.commend_write_tv:
                XHClick.mapStat(mContext,contentTongjiId,"点击评论框","");
                currentUrl = StringManager.api_addForum;
                mKeyboardDialogOptionFrom = KEYBOARD_OPTION_COMMENT;
                showCommentEdit();
                break;
            case R.id.close_img:
                cancel();
                break;
            case R.id.activityLayout:
                cancel();
                break;
        }
    }

    public void setCommentText(String commentText) {
        updateCommentText(commentText);
    }

    private void updateCommentText(String newText) {
        mSendText = newText;
        if (mOnCommentTextUpdateListener != null) {
            mOnCommentTextUpdateListener.onCommentTextUpdate(newText);
        }
    }

    private void showCommentEdit() {
        KeyboardDialog keyboardDialog = new KeyboardDialog(getContext());
        keyboardDialog.setTextLength(50);
        if (mKeyboardDialogOptionFrom == KEYBOARD_OPTION_REPLAY) {
            keyboardDialog.setContentStr(mReplayText);
            if (TextUtils.isEmpty(mReplayText)) {
                keyboardDialog.setHintStr("回复" + mCurrentReplayName);
            }
        } else if (mKeyboardDialogOptionFrom == KEYBOARD_OPTION_COMMENT) {
            keyboardDialog.setContentStr(mSendText);
        }
        keyboardDialog.setOnSendClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboardDialog.cancel();
                String sendText = keyboardDialog.getText();
                if(LoginManager.isLogin()) {
                    keyboardDialog.setContentStr(null);
                }
                sendData(sendText);
            }
        });
        keyboardDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                String finalStr = keyboardDialog.getText();
                if (mKeyboardDialogOptionFrom == KEYBOARD_OPTION_COMMENT) {
                    setCommentText(finalStr);
                } else if (mKeyboardDialogOptionFrom == KEYBOARD_OPTION_REPLAY) {
                    mReplayText = finalStr;
                }
            }
        });
        keyboardDialog.show();
    }

    private void changeDataChange(){
        Log.i("commentActivity","changeDataChange() size:" + listArray.size());
        if(listArray.size() == 0){
            downRefreshList.setVisibility(View.GONE);
            mCommentHintView.setVisibility(View.VISIBLE);
        }else{
            downRefreshList.setVisibility(View.VISIBLE);
            downRefreshList.onRefreshComplete();
            mCommentHintView.setVisibility(View.GONE);
        }
    }

    private ViewCommentItem.OnCommentItenListener getCommentItenListener(final ViewCommentItem viewCommentItem, final int position){
        return new ViewCommentItem.OnCommentItenListener() {
            @Override
            public void onShowAllReplayClick(String comment_id) {
                ReqEncyptInternet.in().doEncypt(StringManager.api_replayList, "type=" + type + "&code=" + code + "&commentId=" + comment_id , new InternetCallback() {
                    @Override
                    public void loaded(int flag, String s, Object o) {
                        if(flag >= ReqInternet.REQ_OK_STRING){
                            viewCommentItem.addReplayView(o.toString(),true);
                        }
                    }
                });
            }

            @Override
            public void onReportCommentClick(String comment_id, String comment_user_code, String comment_user_name, String reportContent,String reportType) {
                if(!LoginManager.isLogin()){
                    mContext.startActivity(new Intent(mContext, LoginByAccout.class));
                    return;
                }
                if(LoginManager.isLogin()
                        && !TextUtils.isEmpty(LoginManager.userInfo.get("code"))
                        && !TextUtils.isEmpty(comment_user_code)
                        && !comment_user_code.equals(LoginManager.userInfo.get("code"))){
                    XHClick.mapStat(mContext,reportTongjiId,reportTwoLeven,reportType);
                    Intent intent = new Intent(mContext,ReportActivity.class);
                    intent.putExtra("type",type);
                    intent.putExtra("code",code);
                    intent.putExtra("commentId",comment_id);
                    intent.putExtra("userCode",comment_user_code);
                    intent.putExtra("reportName",comment_user_name);
                    intent.putExtra("reportContent",reportContent);
                    mContext.startActivity(intent);
                }
            }

            @Override
            public void onReportReplayClick(String comment_id, String replay_id, String replay_user_code, String replay_user_name, String reportContent) {
                Log.i("commentReplay","onReportReplayClick()");
                if(!LoginManager.isLogin()){
                    mContext.startActivity(new Intent(mContext, LoginByAccout.class));
                    return;
                }
                if(!TextUtils.isEmpty(LoginManager.userInfo.get("code"))
                        && !TextUtils.isEmpty(replay_user_code)
                        && !replay_user_code.equals(LoginManager.userInfo.get("code"))){
                    XHClick.mapStat(mContext,reportTongjiId,reportTwoLeven,"点击楼中楼的举报");
                    Intent intent = new Intent(mContext,ReportActivity.class);
                    intent.putExtra("type",type);
                    intent.putExtra("code",code);
                    intent.putExtra("commentId",comment_id);
                    intent.putExtra("replayId",replay_id);
                    intent.putExtra("userCode",replay_user_code);
                    intent.putExtra("reportName",replay_user_name);
                    intent.putExtra("reportContent",reportContent);
                    mContext.startActivity(intent);
                }
            }

            @Override
            public void onDeleteReplayClick(final int replayIndex, String comment_id, String replay_id) {
                XHClick.mapStat(mContext,deleteTongjiId,deleteTwoLeven,"点击楼中楼的删除按钮");
                ReqEncyptInternet.in().doEncypt(StringManager.api_delReplay, "type=" + type + "&code=" + code + "&commentId="+comment_id + "&replayId=" + replay_id,
                        new InternetCallback() {
                            @Override
                            public void loaded(int flag, String s, Object o) {
                                Log.i("commentReplay","deleteReplay() flag:" + flag + "   obj:" + o);
//                        Log.i("commentReplay","deleteReplay() position:" + position + "  listArray.size():" + listArray.size() + "   replayIndex:" + replayIndex);
                                if(flag >= ReqInternet.REQ_OK_STRING){
                                    if(position < listArray.size()) {
                                        Map<String, String> map = listArray.get(position);
                                        String replay = map.get("replay");
                                        ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(replay);
//                                Log.i("commentReplay","deleteReplay() arrayList.size():" + arrayList.size());
                                        if (replayIndex < arrayList.size()) {
                                            arrayList.remove(replayIndex);
                                            map.put("replay", StringManager.getJsonByArrayList(arrayList).toString());
                                            viewCommentItem.addReplayView(arrayList, true);
                                        }
                                    }
                                }
                            }
                        });
            }

            @Override
            public void onDeleteCommentClick(final String comment_id,String deleteType) {
                XHClick.mapStat(mContext,deleteTongjiId,deleteTwoLeven,deleteType);
                final DialogManager dialogManager = new DialogManager(mContext);
                dialogManager.createDialog(new ViewManager(dialogManager)
                        .setView(new TitleMessageView(mContext).setText("确认删除我的评论？"))
                        .setView(new HButtonView(mContext)
                                .setNegativeText("取消", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogManager.cancel();
                                    }
                                })
                                .setPositiveText("确认", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String params = "type=" + type + "&code=" + code + "&commentId="+comment_id;
                                        ReqEncyptInternet.in().doEncypt(StringManager.api_delForum, params, new InternetCallback() {
                                            @Override
                                            public void loaded(int flag, String s, Object o) {
                                                if(flag >= ReqInternet.REQ_OK_STRING){
                                                    listArray.remove(position);
                                                    adapterSimple.notifyDataSetChanged();
                                                    try {
                                                        mCommentsNum = Integer.parseInt(mCommentsNumStr);
                                                    } catch (Exception e) {
                                                        mCommentsNum = -1;
                                                    }
                                                    if (mCommentsNum != -1) {
                                                        mCommentsNum--;
                                                        mCommentsNum = Math.max(mCommentsNum, 0);
                                                        mCommentsNumStr = String.valueOf(mCommentsNum);
                                                    }
                                                    setCommentsNum();
                                                    if(listArray.size() == 0){
                                                        upDropPage = 1;
                                                        gotoCommentId = null;
                                                        gotoReplayId = null;
                                                        getCommentData(true);
                                                    }else {
                                                        changeDataChange();
                                                    }
                                                    if (mCommentOptionSuccCallback != null)
                                                        mCommentOptionSuccCallback.onDelSucc();
                                                }
                                            }
                                        });
                                        dialogManager.cancel();
                                    }
                                }))).show();
            }

            @Override
            public void onPraiseClick(String comment_id) {
                if(!LoginManager.isLogin()){
                    Intent intent = new Intent(mContext, LoginByAccout.class);
                    mContext.startActivity(intent);
                    return;
                }
                XHClick.mapStat(mContext,contentTongjiId,"点赞","");
                XHClick.mapStat(mContext,likeTongjiId,likeTwoLeven,"");
                String params = "type=" + type + "&code=" + code + "&commentId=" + comment_id;
                ReqEncyptInternet.in().doEncypt(StringManager.api_likeForum, params, new InternetCallback() {
                    @Override
                    public void loaded(int i, String s, Object o) {
                        if(i >= ReqInternet.REQ_OK_STRING) {
                            ArrayList<Map<String,String>> arrayList = getListMapByJson(o);
                            if(arrayList.size() > 0) {
                                Map<String, String> map = listArray.get(position);
                                map.put("is_fabulous", "2");
                                map.put("fabulous_num", arrayList.get(0).get("num"));
                                adapterSimple.notifyDataSetChanged();
                            }
                        }
                    }
                });
            }

            @Override
            public void onContentReplayClick(String comment_id,String replay_id,String replay_code, String replay_name,String type,boolean isShowKeyBoard,boolean isMyselft) {
                if(isMyselft) return;
                if (!TextUtils.equals(mCurrentReplayId, replay_code)) {
                    mReplayText = null;
                }
                currentParams = "&commentId=" + comment_id + "&replayUcode=" + replay_code;
                replayIndex = position;
                currentUrl = StringManager.api_addReplay;
                oldCommentId = comment_id;
                mCurrentReplayId = replay_code;
                mCurrentReplayName = replay_name;
                mKeyboardDialogOptionFrom = KEYBOARD_OPTION_REPLAY;
                showCommentEdit();
                XHClick.mapStat(mContext,contentTongjiId,"回复",type);
            }
        };
    }

    private ViewCommentItem.OnUserInforListener getUserInforListener(){
        return new ViewCommentItem.OnUserInforListener() {
            @Override
            public void onReplayUserNameClick(boolean isAuther, String userName) {
                XHClick.mapStat(mContext,contentTongjiId,"用户信息",isAuther ? "点击楼层作者用户名" : "点击楼层其他用户名");
            }
            @Override
            public void onCommentUserNameClick(String userName) {
                XHClick.mapStat(mContext,contentTongjiId,"用户信息","点击评论用户名");
            }
            @Override
            public void onCommentUserIconClick() {
                XHClick.mapStat(mContext,contentTongjiId,"用户信息","点击用户头像");
            }
            @Override
            public void onCommentUserVipClick() {
                XHClick.mapStat(mContext,contentTongjiId,"用户信息","点击会员icon");
            }
        };
    }

    private void getCommentData(final boolean isForward) {
        if (isForward) {
            currentPage = 1;
            slide = 2;
            if(listArray.size() > 0){
                gotoCommentId = listArray.get(0).get("comment_id");
            }
            dropPage = upDropPage;
        } else {
            currentPage++;
            slide = 1;
            if(listArray.size() > 0){
                gotoCommentId = listArray.get(listArray.size() - 1).get("comment_id");
            }
            dropPage = downDropPage;
        }

        mLoadManager.loading(downRefreshList,listArray.size() == 0);
        String params = "type=" + type + "&code=" + code;
        if(!TextUtils.isEmpty(gotoCommentId))
            params +=  "&commentId=" + gotoCommentId;
        if(!TextUtils.isEmpty(gotoReplayId))
            params += "&replayId=" + gotoReplayId;;
        params += "&from=" + from + "&source=" + source + "&slide=" + slide + "&dropPage=" + dropPage;
        Log.i("commentReplay","getCommentData() params:" + params);
        ReqEncyptInternet.in().doEncypt(StringManager.api_forumList, params, new InternetCallback() {
            @Override
            public void loaded(int flag, String s, Object o) {
                int loadCount = 0;
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    from = 1;
                    source = 1;
                    ArrayList<Map<String,String>> arrayList = getListMapByJson(o);
                    if(arrayList.size() > 0) {
                        Map<String,String> dataMap = arrayList.get(0);
                        String status = dataMap.get("status");
                        if(!"2".equals(status)){
                            dismiss();
                            return;
                        }
                        String dataList = dataMap.get("list");
                        String dataPage = dataMap.get("page");
                        String titleInfo = dataMap.get("info");
                        arrayList = getListMapByJson(dataList);
                        if(isForward){
                            if(dropPage == 1){
                                listArray.clear();
                                Log.i("commentReplay","清除数据，重新加载");
                            }
                            listArray.addAll(0,arrayList);
                        }else{
                            listArray.addAll(arrayList);
                        }
                        if(!TextUtils.isEmpty(dataPage) && !"null".equals(dataPage)) {
                            if (isForward)
                                upDropPage = Integer.parseInt(dataPage);
                            else
                                downDropPage = Integer.parseInt(dataPage);
                        }
                        if(!TextUtils.isEmpty(titleInfo)){
                            ArrayList<Map<String,String>> titleArray = StringManager.getListMapByJson(titleInfo);
                            if(titleArray.size() > 0){
                                Map<String,String> map = titleArray.get(0);
                                String title = map.get("title");
                                final String clickUrl = map.get("url");
                                if(!TextUtils.isEmpty(title)){
                                    titleLayout.setVisibility(View.VISIBLE);
                                    titleTv.setText(title);
                                    if(!TextUtils.isEmpty(clickUrl)) {
                                        titleLayout.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                AppCommon.openUrl((Activity) mContext, clickUrl, true);
                                            }
                                        });
                                    }
                                }
                            }
                        }

                        adapterSimple.notifyDataSetChanged();
                        loadCount = arrayList.size();
                        ;
                        if (everyPage == 0)
                            everyPage = loadCount;
                    }
                    changeDataChange();
                }
                mLoadManager.loadOver(flag,downRefreshList, loadCount,"没有更多评论啦");
            }
        });
    }

    private synchronized void sendData(String sendText){
        if(isSend || TextUtils.isEmpty(sendText))return;
        if(!LoginManager.isLogin()){
            Intent intent = new Intent(mContext, LoginByAccout.class);
            mContext.startActivity(intent);
            return;
        }
        isSend = true;

        String newParams;
        isAddForm = false;
        if(StringManager.api_addForum.equals(currentUrl)){
            isAddForm = true;
            JSONArray jsonArray = new JSONArray();
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("text", sendText);
                jsonArray.put(jsonObject);
            }catch (JSONException e){
                e.printStackTrace();
            }
            newParams = "type=" + type + "&code=" + code + "&content=" + jsonArray.toString();
        }else{
            newParams = "type=" + type + "&code=" + code + currentParams + "&content=" + sendText;
        }
        newParams += "&commentIds=" + commentIdStrBuffer;
        Log.i("commentReplay","sendData() newParams:" + newParams);
        ReqEncyptInternet.in().doEncypt(currentUrl,newParams,new InternetCallback(){
            @Override
            public void loaded(int flag, String s, Object o) {
                Log.i("commentReplay","sendData() flag:" + flag + "   o:" + o);
                if(flag >= ReqInternet.REQ_OK_STRING) {
                    if(isAddForm){
                        ArrayList<Map<String,String>> arrayList = getListMapByJson(o);
                        Log.i("commentReplay","sendData() arrayList:" + arrayList.size());
                        if(arrayList.size() > 0) {
                            Map<String,String> map = arrayList.get(0);
                            if(commentIdStrBuffer.length() != 0) commentIdStrBuffer.append(",");
                            commentIdStrBuffer.append(map.get("comment_id"));
                            listArray.add(0, arrayList.get(0));
                            if(listArray.size() == 1)
                                changeDataChange();
                        }
                        adapterSimple.notifyDataSetChanged();
                        downRefreshList.setSelection(0);
                    }else{
                        Log.i("commentReplay","sendData() replayIndex:" + replayIndex);
                        Map<String,String> map = listArray.get(replayIndex);
                        String replay = map.get("replay");
                        Log.i("commentReplay","sendData() replay:" + replay);
                        if(!TextUtils.isEmpty(replay)) {
                            JSONArray jsonArray = new JSONArray();
                            JSONObject jsonObject;
                            Map<String, String> replayMap;
                            ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(replay);
                            ArrayList<Map<String, String>> newList = StringManager.getListMapByJson(o);
                            arrayList.addAll(newList);
                            try {
                                for (int i = 0; i < arrayList.size(); i++) {
                                    jsonObject = new JSONObject();
                                    replayMap = arrayList.get(i);
                                    for (String key : replayMap.keySet()) {
                                        jsonObject.put(key, replayMap.get(key));
                                    }
                                    jsonArray.put(jsonObject);
                                }
                            }catch (JSONException exception){
                                exception.printStackTrace();
                            }
                            map.put("replay",jsonArray.toString());
                            Log.i("commentReplay","sendData() jsonArray:" + jsonArray.toString());
                            adapterSimple.notifyDataSetChanged();
                        }
                    }

                    try {
                        mCommentsNum = Integer.parseInt(mCommentsNumStr);
                    } catch (Exception e) {
                        mCommentsNum = -1;
                    }
                    if (mCommentsNum != -1) {
                        mCommentsNum++;
                        mCommentsNumStr = String.valueOf(mCommentsNum);
                    }
                    setCommentsNum();
                    if (mCommentOptionSuccCallback != null)
                        mCommentOptionSuccCallback.onSendSucc();
                }else{
                    Tools.showToast(XHApplication.in(),String.valueOf(o));
                }
                isSend = false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void setCommentOptionSuccCallback(CommentOptionSuccCallback callback) {
        mCommentOptionSuccCallback = callback;
    }

    public interface CommentOptionSuccCallback {
        void onSendSucc();
        void onDelSucc();
    }

    public interface OnCommentTextUpdateListener {
        void onCommentTextUpdate(String newText);
    }

    private OnCommentTextUpdateListener mOnCommentTextUpdateListener;

    public void setOnCommentTextUpdateListener(OnCommentTextUpdateListener onCommentTextUpdateListener) {
        mOnCommentTextUpdateListener = onCommentTextUpdateListener;
    }
}
