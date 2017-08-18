package third.mall.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Map;

import acore.logic.AppCommon;
import acore.tools.FileManager;
import acore.tools.StringManager;
import amodule.dish.tools.DishMouldControl;
import aplug.basic.ReqInternet;
import aplug.web.tools.JSAction;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

import static amodule.main.Main.timer;
import static aplug.web.tools.WebviewManager.ERROR_HTML_URL;

/**
 *菜谱详情页webview
 */
public class MallWebView extends XHWebView {

    private Activity mAct;
    public static final String TAG = "zyj";

    private String mMouldVersion;

    public static final String OPEN_NEW = "OPEN_NEW";
    public static final String OPEN_SELF = "OPEN_SELF";

    private String mOpenFlag = OPEN_NEW;
    private String code="";
    private String urlHtml="";

    public MallWebView(Context context) {
        super(context);
        init(context);
    }
    public MallWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public MallWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    protected void init(Context context){
        mAct = (Activity) context;
        WebviewManager.initWebSetting(this);


        setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(final WebView view, String url, Bitmap favicon) {
                if (!ERROR_HTML_URL.equals(url)) {
                    MallWebView.this.setUrl(url);
                }
                Log.i("zyj","onPageStarted");
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i("zyj","onPageFinished");
                super.onPageFinished(view, url);
                if (JSAction.loadAction.length() > 0) {
                    view.loadUrl("javascript:" + JSAction.loadAction + ";");
                    JSAction.loadAction = "";
                }
                if (url.indexOf(StringManager.api_exchangeList) != 0 && url.indexOf(StringManager.api_scoreList) != 0) {
                    view.loadUrl("javascript:window.appCommon.setTitle(document.title);");
                }
                if (timer != null) {
                    timer.cancel();
                    timer.purge();
                }
                // 读取cookie的sessionId
                CookieManager cookieManager = CookieManager.getInstance();
                Map<String, String> map = UtilString.getMapByString(cookieManager.getCookie(url), ";", "=");
                String sessionId = UtilInternet.cookieMap.get("USERID");
                if (map.get("USERID") != null && !map.get("USERID").equals(sessionId == null ? "" : sessionId)) {
                    UtilInternet.cookieMap.put("USERID", map.get("USERID"));
                }
                view.setFocusable(false);
            }

            // 当前页打开
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                if (OPEN_SELF.equals(mOpenFlag)) {
                    view.loadUrl(url);
                    return false;
                }
                AppCommon.openUrl(mAct, url, true);
                return true;
            }
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler sslhandler, SslError error) {
                sslhandler.proceed();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                MallWebView.this.loadUrl(ERROR_HTML_URL);
            }
        });
        WebviewManager.setWebChromeClient(this,null);
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        JsAppCommon jsObj = new JsAppCommon((Activity) this.getContext(),this,null,null);
        addJavascriptInterface(jsObj, jsObj.TAG);
        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    }
    public void loadDishData(String code,String urlHtml){
        if(TextUtils.isEmpty(code)){
            return;
        }
        this.code = code;
        this.urlHtml=urlHtml;
        loadMould(code);
    }
    /**
     * 根据code，加载模板
     * @param code
     */
    private void loadMould(final String code){
        DishMouldControl.getDishMould(new DishMouldControl.OnDishMouldListener() {
            @Override
            public void loaded(boolean isSucess, String data,String mouldVersion) {
                mMouldVersion = mouldVersion;
                if(isSucess){
                    data = data.replace("<{code}>",code);
                    final String html = data;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            loadDataWithBaseURL(null,html,"text/html","utf-8", null);
                        }
                    });
                }
            }
        });
    }

}