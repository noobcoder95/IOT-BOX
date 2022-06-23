package com.smartiotdevices.iotbox.alertdialogs;

import android.app.Activity;

public class BlockingOnUIRunnable
{
    private Activity activity;

    private BlockingOnUIRunnableListener listener;

    private final Runnable ui_runnable;

    public BlockingOnUIRunnable( Activity activity, BlockingOnUIRunnableListener listener )
    {
        this.activity = activity;
        this.listener = listener;

        ui_runnable = new Runnable()
        {
            public void run()
            {
                if ( BlockingOnUIRunnable.this.listener != null )
                {
                    BlockingOnUIRunnable.this.listener.onRunOnUIThread(this);
                }
            }
        };
    }

    public void startOnUiAndWait()
    {
        synchronized ( ui_runnable )
        {
            activity.runOnUiThread( ui_runnable );

            try
            {
                ui_runnable.wait();
            }
            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }
        }
    }
}
