package com.smartiotdevices.iotbox.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.JSchException;
import com.smartiotdevices.iotbox.preference.PreferenceCommand;
import com.smartiotdevices.iotbox.preference.PreferenceConnetion;
import com.smartiotdevices.iotbox.preference.PreferenceHostKeys;

public class DBHelper extends SQLiteOpenHelper
{

    private static String DATABASE_NAME = "technodynamite.shc";
    private static String tablehost="HostKeys";
    public static String tableconnection="Connection";
    public static String userCommand="Commander";
    private SQLiteDatabase dbh;

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+userCommand+"(sshCmdName VARCHAR,sshExec VARCHAR)");
        db.execSQL("CREATE TABLE IF NOT EXISTS "+tableconnection+"(connectionName VARCHAR,hostName VARCHAR,username VARCHAR,password VARCHAR,portNumber VARCHAR)");
        db.execSQL("CREATE TABLE IF NOT EXISTS "+tablehost+"(fingerprint VARCHAR,key VARCHAR,type VARCHAR,hostName VARCHAR)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }

    public void addConnection(PreferenceConnetion p)
    {
        dbh = getWritableDatabase();
        dbh.execSQL("INSERT INTO "+tableconnection+"(connectionName,hostName,username,password,portNumber) VALUES ('"+p.getName()+"','"+p.getHostName()+"','"+p.getUsername()+"','"+p.getPassword()+"','"+p.getPort()+"')");
    }

    public void deleteConnection(PreferenceConnetion p)
    {
        dbh = getWritableDatabase();
        String name=p.getName();
        String hname=p.getHostName();
        dbh.execSQL("DELETE FROM "+tableconnection+" where connectionName = '"+name+"' AND hostName = '"+hname+"'");
    }

    public void addCmd(PreferenceCommand p)
    {
        dbh = getWritableDatabase();
        dbh.execSQL("INSERT INTO "+userCommand+"(sshCmdName,sshExec) VALUES ('"+p.getCmd()+"', '"+p.getExec()+"')");
    }

    public void delCmd(PreferenceCommand p)
    {
        dbh = getWritableDatabase();
        String cmd=p.getCmd();
        String exec=p.getExec();
        dbh.execSQL("DELETE FROM "+userCommand+" where sshCmdName = '"+cmd+"' AND sshExec = '"+exec+"'");
    }

    public void addHostkey(PreferenceHostKeys h)
    {
        dbh = getWritableDatabase();
        dbh.execSQL("INSERT INTO "+tablehost+"(fingerprint,key,type,hostName) VALUES ('"+h.getFingerprint()+"','"+h.getKey()+"','"+h.getType()+"','"+h.getHostName()+"')");
    }

    public int fingercheck(String name, byte[] key)
    {
        dbh = getWritableDatabase();
        try
        {
            HostKey JcHost=new HostKey(name,key);
            PreferenceHostKeys hostKeys;
            Cursor cursor=dbh.rawQuery("select * from "+tablehost+" where hostName = '"+name+"'",null);
            while (cursor.moveToNext())
            {
                hostKeys=new PreferenceHostKeys(cursor.getString(3),cursor.getString(0),cursor.getString(1),cursor.getString(2));
                boolean res=JcHost.getKey().equals(hostKeys.getKey());
                if (res)
                    return 2;
                else return 0;
            }
        }
        catch (JSchException e)
        {
            e.printStackTrace();
        }
        return 1;
    }

    public void clearHostKeysTable()
    {
        dbh = getWritableDatabase();
        dbh.execSQL("DELETE FROM"+tablehost);
    }

    public void clearConnectionsTable()
    {
        dbh = getWritableDatabase();
        dbh.execSQL("DELETE FROM"+tableconnection);
    }

    public PreferenceHostKeys getHostKey(String name)
    {
        dbh = getWritableDatabase();
        PreferenceHostKeys hostKeys=null;
        Cursor cursor= dbh.rawQuery("select * from "+tablehost+" where hostName = '"+name+"'",null);
        while (cursor.moveToNext())
        {
            hostKeys=new PreferenceHostKeys(cursor.getString(3),cursor.getString(0),cursor.getString(1),cursor.getString(2));
        }
        return hostKeys;
    }
}