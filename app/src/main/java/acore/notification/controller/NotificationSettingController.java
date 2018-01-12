package acore.notification.controller;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.xiangha.BuildConfig;
import com.xiangha.R;

import acore.logic.VersionOp;
import acore.notification.BuildProperties;
import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;

/**
 * Created by sll on 2018/1/8.
 */

public class NotificationSettingController {

    public void showNotificationPermissionSetView(){
        if(XHActivityManager.getInstance().getCurrentActivity()!=null){
            Activity activity = XHActivityManager.getInstance().getCurrentActivity();
//            if(!TextUtils.isEmpty((CharSequence) FileManager.loadShared(activity,FileManager.app_notification, VersionOp.getVerName(activity)))){
//                return;
//            }
            Log.i("xianghaTag","activity：：："+activity.getComponentName().getClassName());
            if(activity.findViewById(R.id.activityLayout)==null)return;
            showPermissionSetView(activity, (RelativeLayout) activity.findViewById(R.id.activityLayout));
        }
    }
    private void showPermissionSetView(Context context, RelativeLayout rl){
        if(context==null||rl==null)return;
        View view= LayoutInflater.from(context).inflate(R.layout.view_notification_set,null);
        RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, Tools.getDimen(context,R.dimen.dp_39));//两个参数分别是layout_width,layout_height
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        view.setLayoutParams(lp);
        view.findViewById(R.id.view_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {if(view!=null&&rl!=null)rl.removeView(view);}});
        rl.addView(view);
        FileManager.saveShared(context,FileManager.app_notification, VersionOp.getVerName(context),"2");
    }

    public static void openNotificationSettings() {
        BuildProperties properties = ToolsDevice.getBuildProperties();
        if (properties == null)
            return;
        try {
            openAndroidSystemNotificationSettings();
        } catch (Exception e) {
            e.printStackTrace();
            String rom_type = ToolsDevice.getRomType(properties);
            switch (rom_type) {
                case ToolsDevice.EMUI:
                    openEMUINotificationSettings();
                    break;
                case ToolsDevice.MIUI:
                    openMIUINotificationSettings(properties);
                    break;
                case ToolsDevice.FLYME:
                    openFlymeNotificationSettings();
                    break;
                case ToolsDevice.OPPO:
                    openOPPONotificationSettings();
                    break;
                case ToolsDevice.VIVO:
                    openVIVONotificationSettings();
                    break;
            }
        }
    }

    private static void openEMUINotificationSettings() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
        ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
        intent.setComponent(comp);
        XHActivityManager.getInstance().getCurrentActivity().startActivity(intent);
    }

    private static void openMIUINotificationSettings(BuildProperties properties) {
        String name = properties.getProperty(ToolsDevice.KEY_MIUI_VERSION_NAME);
        if (TextUtils.isEmpty(name))
            return;
        Context context = XHActivityManager.getInstance().getCurrentActivity();
        Intent intent = null;
        switch (name) {
            case "V5":
                intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, getPackageUri());
                break;
            case "V6":
            case "V7":
                intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                intent.putExtra("extra_pkgname", context.getPackageName());
                break;
            case "V8":
                intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                intent.putExtra("extra_pkgname", context.getPackageName());
                break;
            default:
                intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                intent.putExtra("extra_pkgname", context.getPackageName());
                break;
        }
        context.startActivity(intent);
    }

    private static void openFlymeNotificationSettings() {
        Context context = XHActivityManager.getInstance().getCurrentActivity();
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
        context.startActivity(intent);
    }

    private static void openOPPONotificationSettings() {
        Context context = XHActivityManager.getInstance().getCurrentActivity();
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
        context.startActivity(intent);
    }

    private static void openVIVONotificationSettings() {
        Context context = XHActivityManager.getInstance().getCurrentActivity();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.iqoo.secure");
        if(intent != null) {
            context.startActivity(intent);
        }
    }

    private static void openAndroidSystemNotificationSettings() {
        Context context = XHActivityManager.getInstance().getCurrentActivity();
        Intent intent = new Intent();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
        } else if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", context.getPackageName(), (String)null));
        }
        context.startActivity(intent);
    }

    private static Uri getPackageUri() {
        return Uri.parse("package:" + XHApplication.in().getPackageName());
    }
}
