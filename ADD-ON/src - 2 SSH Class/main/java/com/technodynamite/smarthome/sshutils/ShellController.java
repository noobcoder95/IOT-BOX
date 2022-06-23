package com.smartiotdevices.iotbox.sshutils;

import android.os.Handler;
import android.util.Log;
import android.widget.EditText;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellController
{
    private static final String TAG = "ShellController";
    private BufferedReader mBufferedReader;
    private DataOutputStream mDataOutputStream;
    private Channel mChannel;
    private String mSshText = null;
    ShellController()
    {

    }

    public DataOutputStream getDataOutputStream()
    {
        return mDataOutputStream;
    }

    public synchronized void disconnect() throws IOException
    {
        Log.v(TAG, "close shell channel");
        if (mChannel != null)
            mChannel.disconnect();

        Log.v(TAG, "close streams");
        mDataOutputStream.flush();
        mDataOutputStream.close();
        mBufferedReader.close();

    }

    void writeToOutput(String command)
    {
        if (mDataOutputStream != null)
        {
            try
            {
                mDataOutputStream.writeBytes(command + "\r\n");
                mDataOutputStream.flush();
            }

            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static String fetchPrompt(String returnedString)
    {
        return "";
    }

    public static String removePrompt(String command)
    {
        if(command != null && command.trim().split("\\$").length > 1)
        {
            String[] split = command.trim().split("\\$");
            StringBuilder s = new StringBuilder();
            for(int i = 1; i< split.length; i++)
            {
                s.append(split[i]);
            }
            return s.toString();
        }
        return command;
    }

    void startShell(Session session, Handler handler, EditText editText) throws JSchException, IOException
    {
        if (session == null) throw new NullPointerException("Session cannot be null!");
        if (!session.isConnected()) throw new IllegalStateException("Session must be connected.");
        final Handler myHandler = handler;
        final EditText myEditText = editText;
        mChannel = session.openChannel("shell");
        mChannel.connect();
        mBufferedReader = new BufferedReader(new InputStreamReader(mChannel.getInputStream()));
        mDataOutputStream = new DataOutputStream(mChannel.getOutputStream());
        new Thread(() ->
        {
            try
            {
                String line;
                while (true)
                {
                    while ((line = mBufferedReader.readLine()) != null)
                    {
                        final String result = line;
                        if (mSshText == null) mSshText = result;
                        myHandler.post(() ->
                        {
                            myEditText.setText(myEditText.getText().toString() + "\r\n" + result);
                            Log.d(TAG, "LINE : " + result);
                        });
                    }
                }
            }

            catch (Exception e)
            {
                Log.e(TAG, " Exception " + e.getMessage() + "." + e.getCause() + "," + e.getClass().toString());
            }
        }).start();
    }
}