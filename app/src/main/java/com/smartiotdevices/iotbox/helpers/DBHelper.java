package com.smartiotdevices.iotbox.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.smartiotdevices.iotbox.preference.PreferenceCommand;
import com.smartiotdevices.iotbox.preference.PreferenceConnetion;

public class DBHelper extends SQLiteOpenHelper
{

    private static String DATABASE_NAME = "smartiot.db";
    public static String CONNECTION_TABLE="Connection";
    public static String USER_COMMAND="Commander";
    private static DBHelper db_instance;
    private SQLiteDatabase sq_lite_database;
//    private static String tablehost="HostKeys";

    public static synchronized DBHelper getInstance(Context context)
    {
        if (db_instance == null)
        {
            db_instance = new DBHelper(context.getApplicationContext());
        }
        return db_instance;
    }

    private DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+USER_COMMAND+"(sshCmdName VARCHAR,sshExec VARCHAR)");
        db.execSQL("CREATE TABLE IF NOT EXISTS "+CONNECTION_TABLE+"(username VARCHAR, hostName VARCHAR,portNumber VARCHAR, connectionName VARCHAR)");
//        db.execSQL("CREATE TABLE IF NOT EXISTS "+tablehost+"(fingerprint VARCHAR,key VARCHAR,type VARCHAR,hostName VARCHAR)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }

    public void addConnection(PreferenceConnetion p)
    {
        sq_lite_database = getWritableDatabase();
        sq_lite_database.execSQL("INSERT INTO "+CONNECTION_TABLE+"(username,hostName,portNumber,connectionName) VALUES ('"+p.getUsername()+"','"+p.getHostName()+"','"+p.getPort()+"','"+p.getName()+"')");
    }

    public void deleteConnection(PreferenceConnetion p)
    {
        sq_lite_database = getWritableDatabase();
        String name=p.getName();
        String hname=p.getHostName();
        sq_lite_database.execSQL("DELETE FROM "+CONNECTION_TABLE+" where connectionName = '"+name+"' AND hostName = '"+hname+"'");
    }

    public void addCmd(PreferenceCommand p)
    {
        sq_lite_database = getWritableDatabase();
        sq_lite_database.execSQL("INSERT INTO "+USER_COMMAND+"(sshCmdName,sshExec) VALUES ('"+p.getCmd()+"', '"+p.getExec()+"')");
    }
}