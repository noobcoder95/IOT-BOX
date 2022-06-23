package com.smartiotdevices.iotbox.alertdialogs;

import android.app.Activity;
import android.app.AlertDialog;

public class AlertBox implements BlockingOnUIRunnableListener
{
    private Activity            _activityParent;
    private String              _message;
    private String              _title;
    private DialogResponse       _userResponse;

    public AlertBox(Activity parent, String boxTitle, String msg)
    {
        _message = msg;
        _activityParent = parent;
        _title = boxTitle;
        _userResponse = new DialogResponse();
    }

    public DialogResponse getUserResponse()
    {
        return _userResponse;
    }

    public void onRunOnUIThread (final Runnable runnable)
    {
        new AlertDialog.Builder (_activityParent).setMessage(_message).setTitle(_title).setCancelable(false).setPositiveButton(android.R.string.ok, (dialog, whichButton) ->
        {
            dialog.dismiss();
        }).show();

        synchronized ( runnable )
        {
            runnable.notifyAll();
        }
    }
}
