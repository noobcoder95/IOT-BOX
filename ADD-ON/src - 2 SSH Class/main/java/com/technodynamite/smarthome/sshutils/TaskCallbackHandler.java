package com.smartiotdevices.iotbox.sshutils;

import com.jcraft.jsch.ChannelSftp;

import java.util.Vector;

public interface TaskCallbackHandler
{
    void OnBegin();
    void onFail();
    void onTaskFinished(Vector<ChannelSftp.LsEntry> lsEntries);
}
