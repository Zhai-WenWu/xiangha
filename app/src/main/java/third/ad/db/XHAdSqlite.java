package third.ad.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Map;

import acore.override.XHApplication;
import acore.tools.StringManager;
import third.ad.db.bean.AdBean;

import static third.ad.db.bean.AdBean.AdEntry;
import static third.ad.tools.AdPlayIdConfig.FULL_SRCEEN_ACTIVITY;

/**
 * Description :
 * PackageName : third.ad.db
 * Created by mrtrying on 2018/2/6 13:36:20.
 * e_mail : ztanzeyu@gmail.com
 */
public class XHAdSqlite extends SQLiteOpenHelper {
    public static final int VERSION = 2;
    public static final String NAME = "ad.db";

    public static final String TABLE_ADCONFIG = "tb_ad_config2";
    public static final String TABLE_ADCONFIG_OLD = "tb_ad_config";

    private volatile static XHAdSqlite mInstance = null;

    public static XHAdSqlite newInstance(Context context){
        if(null == mInstance){
            synchronized (XHAdSqlite.class){
                if(null == mInstance){
                    mInstance = new XHAdSqlite(context);
                }
            }
        }
        return mInstance;
    }

    private XHAdSqlite(Context context) {
        super(context==null ? XHApplication.in() : context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createAdConfigTable(db);
    }

    private void createAdConfigTable(SQLiteDatabase db) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("create table if not exists ").append(TABLE_ADCONFIG)
                .append(" (")
                .append(AdEntry._ID).append(" integer primary key autoincrement,")
                .append(AdEntry.COLUMN_ADINDEX_INLIST).append(" integer,")
                .append(AdEntry.COLUMN_ADID).append(" text,")
                .append(AdEntry.COLUMN_ADCONFIG).append(" text,")
                .append(AdEntry.COLUMN_UPDATETIME).append(" long")
                .append(")");
        db.execSQL(buffer.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                db.beginTransaction();
                createAdConfigTable(db);
                db.endTransaction();
                break;
        }
    }

    public void updateConfig(String jsonValue){
        synchronized (XHAdSqlite.class){
            SQLiteDatabase database = null;
            try{
                database = getWritableDatabase();
                ContentValues values = null;
                ArrayList<Map<String, String>> arr = StringManager.getListMapByJson(jsonValue);
                for (Map<String, String> map : arr) {
                    String data = map.get("");
                    Map<String, String> dataMap = StringManager.getFirstMap(data);
                    String adPos = dataMap.get("adPosition");
                    if(!FULL_SRCEEN_ACTIVITY.equals(adPos)){
                        values = new ContentValues();
                        values.put(AdEntry.COLUMN_ADID,adPos);
                        values.put(AdEntry.COLUMN_ADINDEX_INLIST,dataMap.get("index"));
                        values.put(AdEntry.COLUMN_ADCONFIG,dataMap.get
                                ("adConfig"));
                        values.put(AdEntry.COLUMN_UPDATETIME,System.currentTimeMillis());
                        update(database, TABLE_ADCONFIG,values);
                    }else{

                    }

                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                closeDatabase(database);
            }
        }
    }

    private void update(SQLiteDatabase database, String tableName,ContentValues values) throws Exception{
        if(database.isOpen() && !database.isReadOnly()
                && !TextUtils.isEmpty(tableName)
                && values != null && values.size() > 0){
            if(TextUtils.isEmpty(values.getAsString(AdEntry.COLUMN_ADID))){
                return;
            }
            final String whereClause = AdEntry.COLUMN_ADID  + "=?";
            final String[] whereArgs = new String[]{values.getAsString(AdEntry.COLUMN_ADID)};
            Cursor cursor = database.rawQuery("select * from " + tableName + " where " + whereClause,whereArgs);
            if (cursor.moveToFirst()){
                database.update(tableName,values,whereClause,whereArgs);
            }else{
                database.insert(tableName,null,values);
            }
            closeCursor(cursor);
        }
    }

    public AdBean getAdConfig(String adid){
        return getCompatAdConfig(adid);
    }

    private AdBean getCompatAdConfig(String adid){
        synchronized (XHAdSqlite.class){
            AdBean adBean = getAdByADId(TABLE_ADCONFIG,adid);
            if(adBean == null){
                adBean = getOldAdConfig(adid);
            }
            return adBean;
        }
    }

    private AdBean getOldAdConfig(String adid) {
        synchronized (XHAdSqlite.class){
            return getAdByADId(TABLE_ADCONFIG_OLD, adid);
        }
    }

    @Nullable
    private AdBean getAdByADId(String tableName,String adid){
        if(TextUtils.isEmpty(tableName) || TextUtils.isEmpty(adid)){
            return null;
        }
        SQLiteDatabase database = null;
        AdBean adBean = null;
        Cursor cursor = null;
        try{
            database = getReadableDatabase();
            cursor = database.rawQuery("select * from " + tableName + " where " + AdEntry.COLUMN_ADID + "=?",new String[]{adid});
            if(cursor.moveToFirst()){
                adBean = cursorToBean(cursor);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeCursor(cursor);
            closeDatabase(database);
        }
        return adBean;
    }

    private AdBean cursorToBean(Cursor cursor) {
        AdBean adBean = new AdBean();
        adBean._id = cursor.getInt(cursor.getColumnIndexOrThrow(AdEntry._ID));
        adBean.index = cursor.getInt(cursor.getColumnIndexOrThrow(AdEntry.COLUMN_ADINDEX_INLIST));
        adBean.updateTime = cursor.getInt(cursor.getColumnIndexOrThrow(AdEntry.COLUMN_UPDATETIME));
        adBean.adId = cursor.getString(cursor.getColumnIndexOrThrow(AdEntry.COLUMN_ADID));
        adBean.adConfig = cursor.getString(cursor.getColumnIndexOrThrow(AdEntry.COLUMN_ADCONFIG));
        return adBean;
    }

    public void deleteOverdueConfig(){
        synchronized (XHAdSqlite.class){
            SQLiteDatabase database = null;
            try{
                database = getWritableDatabase();
                final long OverdueTime = System.currentTimeMillis() - (24*60*60*1000L);
                database.execSQL("delete from tb_ad_config where " + AdEntry.COLUMN_UPDATETIME +
                        "<="+OverdueTime+";");
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                closeDatabase(database);
            }
        }
    }

    private void closeDatabase(SQLiteDatabase database) {
        try{
            if(database != null){
                database.close();
            }
        }catch (Exception ignored){

        }
    }

    private void closeCursor(Cursor cursor) {
        try{
            if(cursor != null){
                cursor.close();
            }
        }catch (Exception ignored){

        }
    }

}