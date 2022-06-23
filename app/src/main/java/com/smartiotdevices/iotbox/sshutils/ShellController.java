package com.smartiotdevices.iotbox.sshutils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.widget.EditText;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

class ShellController
{
    private BufferedReader buffered_reader;
    private DataOutputStream data_output_stream;
    private Channel channel;

    ShellController()
    {

    }

    synchronized void disconnect() throws IOException
    {
        if (channel != null)
            channel.disconnect();

        data_output_stream.flush();
        data_output_stream.close();
        buffered_reader.close();

    }

    void writeToOutput(String command)
    {
        if (data_output_stream != null)
        {
            try
            {
                data_output_stream.writeBytes(command + "\r\n");
                data_output_stream.flush();
            }

            catch (IOException ignore)
            {

            }
        }
    }

    @SuppressLint("SetTextI18n")
    void startShell(Session session, Handler handler, EditText edit_text) throws JSchException, IOException
    {
        if (session == null) throw new NullPointerException("Session cannot be null!");
        if (!session.isConnected()) throw new IllegalStateException("Session must be connected.");
        final Handler newHandler = handler;
        final EditText customEditText = edit_text;
        if (channel == null)
        {
            channel = session.openChannel("shell");
            channel.connect();
        }
        buffered_reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
        data_output_stream = new DataOutputStream(channel.getOutputStream());
        new Thread(() ->
        {
            try
            {
                String line;
                while (true)
                {
                    while ((line = buffered_reader.readLine()) != null)
                    {
                        final String result = line;
                        if (customEditText != null)
                        {
                            newHandler.post(() -> customEditText.setText(customEditText.getText().toString() + "\r\n" + result));
                        }
                    }
                }
            }

            catch (Exception ignore)
            {

            }
        }).start();
    }
}