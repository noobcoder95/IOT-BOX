package com.smartiotdevices.iotbox.alertdialogs;

import android.app.Activity;

public class AlertDialog implements BlockingOnUIRunnableListener
{
    private Activity            _activity_parent;
    private String              _message;
    private DialogResponse      _user_response;

    public AlertDialog(Activity parent, String msg)
    {
        _message = msg;
        _activity_parent = parent;
        _user_response = new DialogResponse();
    }

    public DialogResponse getDialogResponse()
    {
        return _user_response;
    }

    public void onRunOnUIThread(final Runnable runnable)
    {
        android.app.AlertDialog alert =  new android.app.AlertDialog.Builder(_activity_parent).setMessage(_message).setNegativeButton("No", (dialog, which) ->
        {
            dialog.dismiss();
            _user_response.response_boolean = false;
            synchronized ( runnable )
            {
                runnable.notifyAll();
            }
        }).setPositiveButton("Yes", (dialog, which) ->
        {
            dialog.dismiss();
            _user_response.response_boolean = true;
            synchronized ( runnable )
            {
                runnable.notifyAll();
            }
        }).create();
        alert.setCancelable(false);
        alert.show();
    }
}
