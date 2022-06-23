package com.smartiotdevices.iotbox.mousepadcomponent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.smartiotdevices.iotbox.ActivityMousepad;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class MyConnectionHandler
{
    private JSch jsch=new JSch();
    private Session session =null;
    private Channel channel=null;
    private PipedOutputStream pin =null;
    private Activity myActivity;

    public MyConnectionHandler(Activity a)
    {
        myActivity = a;
    }

    public void xMouseTryConnect()
    {
        if(session==null || !session.isConnected())
        {
            if(ActivityMousepad.setting_user.isEmpty() || ActivityMousepad.setting_host.isEmpty() || String.valueOf(ActivityMousepad.setting_port).isEmpty())
            {
                Toast.makeText(myActivity, "A connection setting is blank", Toast.LENGTH_LONG).show();
                return;
            }

            @SuppressLint("StaticFieldLeak")
            SshConnectTask  t = new SshConnectTask(	myActivity, ActivityMousepad.setting_user, ActivityMousepad.setting_host, ActivityMousepad.setting_pass, ActivityMousepad.setting_port, "")
            {
                protected void onPostExecute(String result)
                {
                    if (dialog.isShowing())
                    {
                        dialog.dismiss();
                    }
                    if(result.isEmpty())
                    {
                        Toast.makeText(myActivity, "Connection failed, check settings and try again", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        if(result.equals("true"))
                        {
                            Toast.makeText(myActivity, "Connection established", Toast.LENGTH_LONG).show();
                            executeShellCommand(ActivityMousepad.setting_xdotool_initial);
                        }
                        else
                        {
                            Toast.makeText(myActivity, "Error: "+result, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            };
            t.execute("");
        }
        else
        {
            xMouseDisconnect();
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class SshConnectTask extends AsyncTask<String, String, String>
    {
        private String mUser;
        private String mHost;
        private int mPort;
        private String mPass;
        private String mShell;

        SshConnectTask(Activity a, String user, String host, String pass, int port, String shell)
        {
            this.mUser=user;
            this.mHost=host;
            this.mPass=pass;
            this.mPort=port;
            this.mShell = shell;
            dialog = new ProgressDialog(a);
        }

        ProgressDialog dialog;
        protected String doInBackground(String... params)
        {
            try
            {
                String TAG = "MyConnectionHandler";
                Log.d(TAG,"Connecting to... "+mUser+"@"+mHost+":"+mPort);
                session= jsch.getSession(mUser, mHost, mPort);
                session.setConfig("PreferredAuthentications", "password,keyboard-interactive");
                session.setPassword(mPass);
                Log.d("SshConnectTask", "attempt password auth");
                String xhost = "127.0.0.1";
                session.setX11Host(xhost);
                int xport = 0;
                session.setX11Port(xport + 6000);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();

                if (TextUtils.isEmpty(mShell))
                {
                    channel = session.openChannel("shell");
                }
                else
                {
                    channel = session.openChannel("exec");
                    ((ChannelExec) channel).setCommand(mShell);
                }
                PipedInputStream in = new PipedInputStream();
                pin = new PipedOutputStream(in);
                channel.setInputStream(in);
                channel.connect(3000);
            }

            catch(Exception e)
            {
                e.printStackTrace();
                return e.getMessage();
            }
            if(session.isConnected())
            {
                return String.valueOf(session.isConnected());
            }
            else
            {
                return "Not connected, no exception thrown.";
            }
        }
        @Override
        protected void onProgressUpdate(String... text)
        {

        }
        @Override
        protected void onPreExecute()
        {
            this.dialog.setMessage("Connecting to "+mUser+"@"+mHost+":"+mPort);
            this.dialog.show();
        }
    }
    public void xMouseDisconnect()
    {
        if(session!=null)
        {
            if(session.isConnected())
            {
                channel.disconnect();
                session.disconnect();
                session=null;
                channel = null;
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public boolean executeShellCommand(String cmd)
    {
        ActivityMousepad.recentCmdTextView.setText(cmd);
        if(session==null)
        {
            return false;
        }
        if(session.isConnected() && channel.isConnected())
        {
            try
            {
                cmd=cmd+"\r\n";
                pin.write(cmd.getBytes());
                pin.flush();
                return true;
            }
            catch (IOException e)
            {
                ActivityMousepad.recentCmdTextView.setText(e.getMessage()+"\t"+cmd);
                e.printStackTrace();
            }
        }
        return false;
    }
}
