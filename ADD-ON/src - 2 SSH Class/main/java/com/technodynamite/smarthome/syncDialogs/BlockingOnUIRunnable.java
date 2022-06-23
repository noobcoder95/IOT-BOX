package com.smartiotdevices.iotbox.alertdialogs;

import android.app.Activity;

public class BlockingOnUIRunnable
{
    private Activity activity;

    private BlockingOnUIRunnableListener listener;

    private final Runnable uiRunnable;

    public BlockingOnUIRunnable( Activity activity, BlockingOnUIRunnableListener listener )
    {
        this.activity = activity;
        this.listener = listener;

        uiRunnable = new Runnable()
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
        synchronized ( uiRunnable )
        {
            activity.runOnUiThread( uiRunnable );

            try
            {
                uiRunnable.wait();
            }
            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }
        }
    }
}
