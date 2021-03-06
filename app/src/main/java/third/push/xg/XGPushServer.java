package third.push.xg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.service.cache.CacheManager;

import java.util.HashMap;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.ChannelManager;
import acore.tools.FileManager;
import acore.tools.LogManager;

/**
 * 
 * @author zeyu_t
 * @date 2014年11月7日 下午3:45:23
 */
@SuppressLint("SimpleDateFormat")
public class XGPushServer {
	public static final String TAG = "XINGE";
	public Context mContext;
	public static String returnFlag = "";

	public XGPushServer(Context mContext) {
		this.mContext = mContext;
	}

	// 初始化推送,且注销用户与设备绑定
	public String initPush() {
		return initPush("*");
	}

	/**
	 *
	 * @param userID : 用户code，传code表示当前设备跟用户绑定，不传或者传*: 表示跟之前的用户解绑
	 * @return
     */
	public String initPush(String userID) {
		if(TextUtils.isEmpty(userID)){
			userID = "*";
		}
		XGPushConfig.enableDebug(mContext, true);
		XGPushConfig.setInstallChannel(mContext,ChannelManager.getInstance().getChannel(mContext));
		XGPushManager.registerPush(mContext.getApplicationContext(), new XGIOperateCallback() {

			@Override
			public void onSuccess(Object obj, int flag) {
				Log.i(TAG, "onSuccess: token=" + obj);
				XHClick.onEvent(mContext,"xg_register","成功");
				saveXGToken(obj);
			}

			@Override
			public void onFail(Object obj, int errCode, String msg) {
				XHClick.onEvent(mContext,"xg_register","失败");
				registerFail(obj, errCode, msg);
			}
		});
		XGPushConfig.enableOtherPush(mContext, true);
		XGPushConfig.setHuaweiDebug(true);
		XGPushConfig.setMiPushAppId(mContext, "2882303761517138495");
		XGPushConfig.setMiPushAppKey(mContext, "5711713867495");
		return returnFlag;
	}

	/**
	 * 注册失败
	 * @param obj
	 * @param errCode
	 * @param msg
	 */
	private void registerFail(Object obj, int errCode, String msg) {
		LogManager.reportError("信鸽注册_errCode_" + errCode, null);
		returnFlag = errCode + "";
	}
	
	/**
	 * 注册成功，存储Token
	 * @param obj
	 */
	private void saveXGToken(Object obj) {
		if (FileManager.ifFileModifyByCompletePath(FileManager.getDataDir() + FileManager.file_XGToken, -1)==null) {
			// 存储Token
			FileManager.saveFileToCompletePath(FileManager.getDataDir() + FileManager.file_XGToken, obj.toString(), false);
			CacheManager.getRegisterInfo(mContext);
		}
		if (FileManager.loadShared(mContext, FileManager.xmlFile_appInfo, FileManager.xmlKey_XGToken).toString().length() < 40) {
			// 存储Token
			Map<String, String> map = new HashMap<String, String>();
			map.put(FileManager.xmlKey_XGToken, obj.toString());
			FileManager.saveShared(mContext, FileManager.xmlFile_appInfo, map);
			CacheManager.getRegisterInfo(mContext);
		}
		returnFlag = "";
	}
	
	/**
	 * 获取设备token
	 * @return token
	 */
	public static String getXGToken(Context context) {
		try{
			if (context != null) {
				if (XGPushConfig.getToken(context).length() >= 40) {
					return XGPushConfig.getToken(context);
				}
				String token = FileManager.loadShared(context, FileManager.xmlFile_appInfo, FileManager.xmlKey_XGToken).toString().trim();
				if (token.length() >= 40) {
					return token;
				}
			} else {
				String token = FileManager.readFile(FileManager.getDataDir() + FileManager.file_XGToken).trim();
				if (token.length() >= 40)
					return token;
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return "000000000000000";
	}


}
