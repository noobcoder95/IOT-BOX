package com.smartiotdevices.iotbox.sshutils;

import android.os.Handler;
import android.widget.EditText;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Properties;

public class SessionController
{
    private Session session;
    private SessionUserInfo session_user_info;
    private Thread thread;
    private ShellController shell_controller;
    private ConnectionStatusListener connect_status_listener;
    private static SessionController SESSION_CONTROLLER;
    private PipedOutputStream pos = null;
    private Channel channel;

    private SessionController()
    {

    }

    public static SessionController getSessionController()
    {
        if (SESSION_CONTROLLER == null)
        {
            SESSION_CONTROLLER = new SessionController();
        }
        return SESSION_CONTROLLER;
    }

    public Session getSession()
    {
        return session;
    }

    private static boolean exists()
    {
        return SESSION_CONTROLLER != null;
    }

    public static boolean isConnected()
    {
        if (exists())
        {
            return getSessionController().getSession().isConnected();
        }
        return false;
    }

    public void setUserInfo(SessionUserInfo sessionUserInfo)
    {
        session_user_info = sessionUserInfo;
    }

    public SessionUserInfo getSessionUserInfo()
    {
        return session_user_info;
    }

    public void connect()
    {
        if (session == null)
        {
            thread = new Thread(new SshRunnable());
            thread.start();
        }
        else if (!session.isConnected())
        {
            thread = new Thread(new SshRunnable());
            thread.start();
        }
    }

    public void setConnectionStatusListener(ConnectionStatusListener csl)
    {
        connect_status_listener = csl;
    }

    public void disconnect() throws IOException
    {
        if (session != null)
        {
            if (shell_controller != null)
            {
                shell_controller.disconnect();
            }

            if (channel != null)
            {
                pos.flush();
                pos.close();
                channel.disconnect();
            }

            synchronized (connect_status_listener)
            {
                if (connect_status_listener != null)
                {
                    connect_status_listener.onDisconnected();
                }
            }
            session.disconnect();
        }

        if (thread != null && thread.isAlive())
        {
            try
            {
                thread.join();
            }
            catch (Exception ignored)
            {}
        }

        shell_controller = null;
        channel = null;
        pos = null;
    }
	
    public void cmdExec(Handler handler, EditText editText, String command)
    {
        if (shell_controller == null)
        {
            shell_controller = new ShellController();
        }

        try
        { shell_controller.startShell(getSession(), handler, editText);
        }

        catch (Exception ignore)
        {}

        synchronized (shell_controller)
        {
            shell_controller.writeToOutput(command);
        }
    }

	public boolean isFailed()
	{
		if (thread != null && thread.isAlive())
		{
		    if (isConnected())
		    {
                return false;
            }
		}

		else
        {
		    return true;
        }

		return false;
	}

    public class SshRunnable implements Runnable
    {
        public void run()
        {
            JSch jsch = new JSch();
            session = null;
            try
            {
                session = jsch.getSession(session_user_info.getUser(), session_user_info.getHost(), session_user_info.getPort());

                session.setUserInfo(session_user_info);

                Properties properties = new Properties();
                properties.setProperty("StrictHostKeyChecking", "no");
                session.setConfig(properties);
				String xhost = "127.0.0.1";
                session.setX11Host(xhost);
                int xport = 0;
                session.setX11Port(xport + 6000);
                session.connect();

            }

            catch (JSchException jex)
            {
                jex.printStackTrace();
                session_user_info.handleException(jex);
            }

            catch (Exception ignore)
            {}

            new Thread(() ->
            {
                while (true)
                {
                    try
                    {
                        Thread.sleep(2000);
                        if (connect_status_listener != null)
                        {
                            if (session.isConnected())
                            {
                                connect_status_listener.onConnected();
                            }
                            else connect_status_listener.onDisconnected();
                        }
                    }
                    catch (InterruptedException ignore)
                    {}
                }
            }).start();
        }
    }

    public void openX11Shell()
    {
        if (session == null) throw new NullPointerException("Session cannot be null!");
        if (!session.isConnected()) throw new IllegalStateException("Session must be connected.");
        if (channel == null)
        {
            try
            {
                channel = session.openChannel("shell");
            }

            catch (JSchException ignore)
            {}

            PipedInputStream pis = new PipedInputStream();

            try
            {
                pos = new PipedOutputStream(pis);
            }

            catch (IOException ignore)
            {}

            channel.setInputStream(pis);

            try
            {
                channel.connect();
            }

            catch (JSchException ignore)
            {}
        }
        x11Shell("export DISPLAY=':0' && unset HISTFILE");
    }

    public void x11Shell(String command)
    {
        if (pos != null)
        {
            try
            {
                command=command+"\r\n";
                pos.write(command.getBytes());
                pos.flush();
            }
            catch (IOException ignore)
            {}
        }
    }
}
