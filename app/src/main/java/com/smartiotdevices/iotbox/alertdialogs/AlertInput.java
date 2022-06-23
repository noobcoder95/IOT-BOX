package com.smartiotdevices.iotbox.alertdialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.EditText;

public class AlertInput implements BlockingOnUIRunnableListener
{
    private Activity        _activity_parent;
    private String          _message;
    private String          _title;
    public DialogResponse   _user_response;

    public AlertInput(Activity parent, String box_title, String msg)
    {
        _message = msg;
        _activity_parent = parent;
        _title = box_title;
        _user_response = new DialogResponse();
    }

    public void onRunOnUIThread(final Runnable runnable)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(_activity_parent);
        alert.setMessage(_message);
        alert.setTitle(_title);
        alert.setCancelable(false);
        final EditText input = new EditText(_activity_parent);
        alert.setView(input);
        alert.setPositiveButton("Submit", (dialog, which) ->
        {
            String data = input.getText().toString();
            _user_response.response_boolean = true;
            _user_response.response_string = data;
            synchronized ( runnable )
            {
                runnable.notifyAll();
            }
        });

        alert.setNegativeButton("Cancel", (dialog, which) ->
        {
            _user_response.response_boolean = false;
            synchronized ( runnable )
            {
                runnable.notifyAll();
            }
        });
        alert.show();
    }
}
