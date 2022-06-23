package com.smartiotdevices.iotbox.alertdialogs;

import android.app.Activity;
import android.app.AlertDialog;

public class AlertBox implements BlockingOnUIRunnableListener
{
    private Activity            _activity_parent;
    private String              _message;
    private String              _title;

    public AlertBox(Activity parent, String box_title, String msg)
    {
        _message = msg;
        _activity_parent = parent;
        _title = box_title;
    }

    public void onRunOnUIThread (final Runnable runnable)
    {
        new AlertDialog.Builder (_activity_parent).setMessage(_message).setTitle(_title).setCancelable(false).setPositiveButton(android.R.string.ok, (dialog, whichButton) -> dialog.dismiss()).show();

        synchronized ( runnable )
        {
            runnable.notifyAll();
        }
    }
}
