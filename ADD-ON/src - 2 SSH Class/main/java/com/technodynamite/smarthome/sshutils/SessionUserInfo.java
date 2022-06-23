package com.smartiotdevices.iotbox.sshutils;

import android.app.Activity;
import android.util.Log;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import com.smartiotdevices.iotbox.R;
import com.smartiotdevices.iotbox.alertdialogs.AlertBox;
import com.smartiotdevices.iotbox.alertdialogs.AlertDialog;
import com.smartiotdevices.iotbox.alertdialogs.AlertInput;
import com.smartiotdevices.iotbox.alertdialogs.BlockingOnUIRunnable;

public class SessionUserInfo implements UserInfo, UIKeyboardInteractive
{

    private String mPassword;
    private String mRsa;
    private final String mUser;
    private final String mHost;
    private final int mPort;

    private Activity parent;

    private final String log = "SessionUserInfo";

    private SessionController conn;

    public SessionUserInfo(String user, String host, int port, Activity ac)
    {
        mHost = host;
        mUser = user;
        mPort = port;
        parent = ac;
        conn = null;
        mPassword = null;
    }

    public String getRsa()
    {
        return mRsa;
    }

    public boolean usingRSA()
    {
        return ((mPassword == null) && (mRsa != null));
    }

    public void setPassword(String pass)
    {
        mPassword = pass;
        mRsa = null;
    }

    int getPort()
    {
        return mPort;
    }

    public void setConnectionHandler(SessionController c)
    {
        conn = c;
    }

    @Override
    public String[] promptKeyboardInteractive(String destination, String name, String instruction,
                                              String[] prompt, boolean[] echo)
    {
        Log.d(log, "prompt keyboard");
        return null;
    }

    public String promptInput(String title, String promptMessage)
    {
        AlertInput alert = new AlertInput(parent, title, promptMessage);
        BlockingOnUIRunnable actionRunnable = new BlockingOnUIRunnable(parent, alert);
        actionRunnable.startOnUiAndWait();
        return alert._userResponse.responseString;
    }

    void handleException(JSchException paramJSchException)
    {
        String error = paramJSchException.getMessage();

        if (paramJSchException.getMessage().contains("reject HostKey"))
        {
            error = parent.getString(R.string.message_hostkeyreject);
        }
        
        else if (paramJSchException.getMessage().contains("UnknownHostKey"))
        {
            error = parent.getString(R.string.message_hkeyunknown);
        }
        
        else if (paramJSchException.getMessage().contains("HostKey has been changed"))
        {
            error = parent.getString(R.string.message_hkeychanged);
        }
        
        else if (paramJSchException.getMessage().contains("Auth fail"))
        {
            error = parent.getString(R.string.message_authfail);
        }
        
        else if (paramJSchException.getMessage().contains("Auth cancel"))
        {
            error = parent.getString(R.string.message_authcancel);
        }
        
        else if (paramJSchException.getMessage().contains("socket is not established"))
        {
            error = parent.getString(R.string.message_badconn);
        }
        
        else if (paramJSchException.getMessage().contains("Too many authentication"))
        {
            error = parent.getString(R.string.message_authincorrect);
        }
        
        else if (paramJSchException.getMessage().contains("Connection refused"))
        {
            error = parent.getString(R.string.message_refusedconn);
        }
        
        else if (paramJSchException.getMessage().contains("Unable to resolve host") || paramJSchException.getMessage().contains("Network is unreachable"))
        {
            error = parent.getString(R.string.message_retrybadconn);
            if(conn != null)
            {
                AlertDialog alert = new AlertDialog(parent, error, this);
                BlockingOnUIRunnable actionRunnable = new BlockingOnUIRunnable(parent, alert);
                actionRunnable.startOnUiAndWait();
                if( alert.getDialogResponse().responseBoolean )
                {
                    conn.connect();
                }

                else
                {
                    parent.finish();
                }

                return;
            }

            else
            {
                error = parent.getString(R.string.message_badconnwhost);
            }
        }
        showMessage(parent.getString(R.string.title_error), error);
    }

    @Override
    public String getPassphrase()
    {
        Log.d(log, "getPassphrase");
        return mPassword;
    }

    private void showMessage(String title, String message)
    {
        AlertBox alert = new AlertBox(parent, title, message);
        BlockingOnUIRunnable actionRunnable = new BlockingOnUIRunnable(parent, alert);
        actionRunnable.startOnUiAndWait();
    }

    @Override
    public void showMessage(String message)
    {
        Log.d(log, "showMessage:" + message);
        showMessage("", message);
    }

    public boolean promptYesNo(final String message)
    {
        AlertDialog alert = new AlertDialog(parent, message, this);
        BlockingOnUIRunnable actionRunnable = new BlockingOnUIRunnable(parent, alert);
        actionRunnable.startOnUiAndWait();

        return alert.getDialogResponse().responseBoolean;
    }

    @Override
    public boolean promptPassword(String message)
    {
        if(mPassword != null)
        {
            return true;
        }

        AlertInput alert = new AlertInput(parent, parent.getString(R.string.hint_loginpassword), message);
        BlockingOnUIRunnable actionRunnable = new BlockingOnUIRunnable(parent, alert);
        actionRunnable.startOnUiAndWait();

        if (alert._userResponse.responseBoolean)
        {
            mPassword = alert._userResponse.responseString;
        }
        return alert._userResponse.responseBoolean;
    }

    @Override
    public boolean promptPassphrase(String message)
    {
        Log.d(log, "promptPassphrase" + message);
        return true;
    }

    String getUser()
    {
        return mUser;
    }

    public String getHost()
    {
        return mHost;
    }

    public String getPassword()
    {
        return mPassword;
    }
}
