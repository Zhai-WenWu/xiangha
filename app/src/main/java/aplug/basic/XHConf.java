package aplug.basic;

import android.content.Context;
import android.util.DisplayMetrics;

import com.umeng.onlineconfig.OnlineConfigAgent;
import com.xiangha.BuildConfig;
import com.xiangha.R;

import org.apache.http.protocol.HTTP;

import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import third.mall.aplug.MallStringManager;
import xh.basic.BasicConf;
import xh.basic.tool.UtilFile;

/**
 * 基础包配置类
 * @author Jerry
 */
public class XHConf extends BasicConf {
	/**
	 * 统计相关的filter
	 */
	public static String log_tag_stat="xh_stat";
	/**
	 * 上传相关统计的filter
	 */
	public static String log_tag_upload="xh_upload";
	
	/**
	 * 程序初始化时调用，修改框架配置
	 */
	public static void init(Context context){
		if (context == null)
			return;
		//文件配置
		file_sdCardDir="/xiangha";
		//此路径必须用应用包名做文件夹
		file_dataDir= "/" + context.getPackageName() +  "/file";
		file_encoding=HTTP.UTF_8;
				
		//日志配置
		log_isDebug = BuildConfig.LOG_DEBUG;
		log_save2file=false;
		log_tag_all="xh_all";
		log_tag_default="xh_default";
		log_tag_img="xh_img";
		log_tag_net="xh_network";
		
		//网络配置
		net_timeout=20;
		net_imgRefererUrl="www.xiangha.com";
		net_imgUploadJpg=true;
		net_encode=HTTP.UTF_8;
		net_imgUploadHeight=net_imgUploadWidth=900;
		net_imgUploadKb=300;
		if(!log_isDebug) net_domain2ipJson="{'api.xiangha.com':[{'ip':'101.201.172.223','weight':100}],'api.huher.com':[{'ip':'182.92.245.125','weight':100}]}";
	
		//设置url和域名配置
		String domain= UtilFile.loadShared(context, FileManager.xmlFile_appInfo,FileManager.xmlKey_domain).toString();
		String protocol= UtilFile.loadShared(context, FileManager.xmlFile_appInfo,FileManager.xmlKey_protocol).toString();
		StringManager.changeUrl(protocol,domain);
		//设置电商url切换
		String mall_domain= UtilFile.loadShared(context, FileManager.xmlFile_appInfo,FileManager.xmlKey_mall_domain).toString();
		MallStringManager.changeUrl(mall_domain);
		//调试模式下不处理domain2ip
		if(!log_isDebug){
			String jsonStr = OnlineConfigAgent.getInstance().getConfigParams(context, "domain2ip");
			if(jsonStr.length()>1) net_domain2ipJson=jsonStr;
		}

		
		//设置图片配置
		DisplayMetrics dm=ToolsDevice.getWindowPx(context);
		img_showHeight=img_showWidth=dm.widthPixels;
		img_showKb=Integer.parseInt(ToolsDevice.getTotalMemory())/8;
		if(img_showKb<150) img_showKb=150;
		if(img_showKb>500) img_showKb=500;
		//图片占位图
		img_errorID = R.drawable.i_nopic;
		img_placeholderID = R.drawable.i_nopic;
		img_defaultPath = "file:///android_asset/i_nopic.png";
	}
}
