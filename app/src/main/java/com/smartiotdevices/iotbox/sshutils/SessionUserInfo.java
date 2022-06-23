package com.smartiotdevices.iotbox.sshutils;

import android.app.Activity;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import com.smartiotdevices.iotbox.R;
import com.smartiotdevices.iotbox.alertdialogs.AlertBox;
import com.smartiotdevices.iotbox.alertdialogs.AlertDialog;
import com.smartiotdevices.iotbox.alertdialogs.AlertInput;
import com.smartiotdevices.iotbox.alertdialogs.BlockingOnUIRunnable;

import java.util.Objects;

public class SessionUserInfo implements UserInfo, UIKeyboardInteractive
{

    private String password;
    private final String user;
    private final String host;
    private final int port;

    private Activity parent;
    private SessionController connection;

    public SessionUserInfo(String _user, String _host, int _port, Activity activity)
    {
        host = _host;
        user = _user;
        port = _port;
        parent = activity;
        connection = null;
        password = null;
    }

    public void setPassword(String pass)
    {
        password = pass;
    }

    int getPort()
    {
        return port;
    }

    @Override
    public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo)
    {
        return null;
    }

    void handleException(JSchException paramJSchException)
    {
        String error = paramJSchException.getMessage();

        if (Objects.requireNonNull(paramJSchException.getMessage()).contains("reject HostKey"))
        {
            error = parent.getString(R.string.message_hostkey_reject);
        }
        
        else if (paramJSchException.getMessage().contains("UnknownHostKey"))
        {
            error = parent.getString(R.string.message_hostkey_unknown);
        }
        
        else if (paramJSchException.getMessage().contains("HostKey has been changed"))
        {
            error = parent.getString(R.string.message_hostkey_changed);
        }
        
        else if (paramJSchException.getMessage().contains("Auth fail"))
        {
            error = parent.getString(R.string.message_auth_fail);
        }
        
        else if (paramJSchException.getMessage().contains("Auth cancel"))
        {
            error = parent.getString(R.string.message_auth_cancel);
        }
        
        else if (paramJSchException.getMessage().contains("socket is not established"))
        {
            error = parent.getString(R.string.message_unknown_connection);
        }
        
        else if (paramJSchException.getMessage().contains("Too many authentication"))
        {
            error = parent.getString(R.string.message_auth_incorrect);
        }
        
        else if (paramJSchException.getMessage().contains("Connection refused"))
        {
            error = parent.getString(R.string.message_refused_connection);
        }
        
        else if (paramJSchException.getMessage().contains("Unable to resolve host") || paramJSchException.getMessage().contains("Network is unreachable"))
        {
            error = parent.getString(R.string.message_retry_bad_connection);
            if(connection != null)
            {
                AlertDialog alert = new AlertDialog(parent, error);
                BlockingOnUIRunnable actionRunnable = new BlockingOnUIRunnable(parent, alert);
                actionRunnable.startOnUiAndWait();
                if( alert.getDialogResponse().response_boolean )
                {
                    connection.connect();
                }

                else
                {
                    parent.finish();
                }

                return;
            }

            else
            {
                error = parent.getString(R.string.message_bad_connection);
            }
        }
        showMessage(parent.getString(R.string.title_error), error);
    }

    @Override
    public String getPassphrase()
    {
        return password;
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
        showMessage("", message);
    }

    public boolean promptYesNo(final String message)
    {
        AlertDialog alert = new AlertDialog(parent, message);
        BlockingOnUIRunnable actionRunnable = new BlockingOnUIRunnable(parent, alert);
        actionRunnable.startOnUiAndWait();

        return alert.getDialogResponse().response_boolean;
    }

    @Override
    public boolean promptPassword(String message)
    {
        if(password != null)
        {
            return true;
        }

        AlertInput alert = new AlertInput(parent, parent.getString(R.string.hint_login_password), message);
        BlockingOnUIRunnable actionRunnable = new BlockingOnUIRunnable(parent, alert);
        actionRunnable.startOnUiAndWait();

        if (alert._user_response.response_boolean)
        {
            password = alert._user_response.response_string;
        }
        return alert._user_response.response_boolean;
    }

    @Override
    public boolean promptPassphrase(String message)
    {
        return true;
    }

    String getUser()
    {
        return user;
    }

    public String getHost()
    {
        return host;
    }

    public String getPassword()
    {
        return password;
    }
}
