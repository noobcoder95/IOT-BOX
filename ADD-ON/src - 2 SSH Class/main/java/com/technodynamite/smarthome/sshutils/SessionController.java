package com.smartiotdevices.iotbox.sshutils;

import android.os.Handler;
import android.util.Log;
import android.widget.EditText;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class SessionController
{
    private static final String TAG = "SessionController";
    private Session mSession;
    private SessionUserInfo mSessionUserInfo;
    private Thread mThread;
    private SftpController mSftpController;
    private ShellController mShellController;
    private ConnectionStatusListener mConnectStatusListener;
    private static SessionController sSessionController;

    private SessionController()
    {

    }

    public static SessionController getSessionController()
    {
        if (sSessionController == null)
        {
            sSessionController = new SessionController();
        }
        return sSessionController;
    }

    public Session getSession()
    {
        return mSession;
    }

    private SessionController(SessionUserInfo sessionUserInfo)
    {
        mSessionUserInfo = sessionUserInfo;
        connect();
    }

    private static boolean exists()
    {
        return sSessionController != null;
    }

    public static boolean isConnected()
    {
        Log.v(TAG, "Session controller exists... " + exists());
        if (exists())
        {
            return getSessionController().getSession().isConnected();
        }
        return false;
    }

    public void setUserInfo(SessionUserInfo sessionUserInfo)
    {
        mSessionUserInfo = sessionUserInfo;
    }

    public SessionUserInfo getSessionUserInfo()
    {
        return mSessionUserInfo;
    }

    public void connect()
    {
        if (mSession == null)
        {
            mThread = new Thread(new SshRunnable());
            mThread.start();
        }
        else if (!mSession.isConnected())
        {
            mThread = new Thread(new SshRunnable());
            mThread.start();
        }
    }

    public SftpController getSftpController()
    {
        return mSftpController;
    }

    public void setConnectionStatusListener(ConnectionStatusListener csl)
    {
        mConnectStatusListener = csl;
    }

    public void uploadFiles(File[] files, SftpProgressMonitor spm)
    {
        if (mSftpController == null)
        {
            mSftpController = new SftpController();
        }
        mSftpController.new UploadTask(mSession, files, spm).execute();
    }

    public boolean downloadFile(String srcPath, String out, SftpProgressMonitor spm) throws JSchException, SftpException
    {
        if (mSftpController == null)
        {
            mSftpController = new SftpController();
        }
        mSftpController.new DownloadTask(mSession, srcPath, out, spm).execute();
        return true;
    }

    public void listRemoteFiles(TaskCallbackHandler taskCallbackHandler, String path) throws JSchException, SftpException
    {
        if (mSession == null || !mSession.isConnected())
        {
            return;
        }

        if (mSftpController == null)
        {
            mSftpController = new SftpController();
        }
        mSftpController.lsRemoteFiles(mSession, taskCallbackHandler, path);
    }

    public void disconnect() throws IOException
    {
        if (mSession != null)
        {
            if (mSftpController != null)
            {

                mSftpController.disconnect();
            }

            if (mShellController != null)
            {
                mShellController.disconnect();
            }

            synchronized (mConnectStatusListener)
            {
                if (mConnectStatusListener != null)
                {
                    mConnectStatusListener.onDisconnected();
                }
            }
            mSession.disconnect();
        }

        if (mThread != null && mThread.isAlive())
        {
            try
            {
                mThread.join();
            }
            catch (Exception ignored)
            {

            }
        }

        mSftpController = null;
        mShellController = null;
        Log.e(TAG, "Session Disconnected");
    }
	
    public void cmdExec(Handler handler, EditText editText, String command)
    {
        if (mShellController == null)
        {
            mShellController = new ShellController();

            try
            {
                mShellController.startShell(getSession(), handler, editText);
            }

            catch (Exception e)
            {
                Log.e(TAG, "Shell open exception " + e.getMessage());
            }
        }

        synchronized (mShellController)
        {
            mShellController.writeToOutput(command);
        }

    }

	public boolean isFailed()
	{
		if (mThread != null && mThread.isAlive())
		{
		    if (isConnected())
		    {
                return false;
            }
		}

		else
        {
            Log.e(TAG, "Failed to Connect");
		    return true;
        }

		return false;
	}

    public class SshRunnable implements Runnable
    {
        public void run()
        {
            JSch jsch = new JSch();
            mSession = null;
            try
            {
                mSession = jsch.getSession(mSessionUserInfo.getUser(), mSessionUserInfo.getHost(), mSessionUserInfo.getPort());

                mSession.setUserInfo(mSessionUserInfo);

                Properties properties = new Properties();
                properties.setProperty("StrictHostKeyChecking", "no");
                mSession.setConfig(properties);
				String xhost = "127.0.0.1";
                mSession.setX11Host(xhost);
                int xport = 0;
                mSession.setX11Port(xport + 6000);
                mSession.connect();

            }
            catch (JSchException jex)
            {
                Log.e(TAG, "JschException: " + jex.getMessage() +  ", Fail to get session " + mSessionUserInfo.getUser() + ", " + mSessionUserInfo.getHost());
                mSessionUserInfo.handleException(jex);
            }

            catch (Exception ex)
            {
                Log.e(TAG, "Exception:" + ex.getMessage());
            }

            Log.d("SessionController", "Session connected? " + mSession.isConnected());

            new Thread(() ->
            {
                while (true)
                {
                    try
                    {
                        Thread.sleep(2000);
                        if (mConnectStatusListener != null)
                        {
                            if (mSession.isConnected())
                            {
                                mConnectStatusListener.onConnected();
                            }
                            else mConnectStatusListener.onDisconnected();
                        }
                    }
                    catch (InterruptedException e)
                    {
						Log.e (TAG, "SessionController Interrupted: " + e.getMessage());
                    }
                }
            }).start();
        }
    }
}
