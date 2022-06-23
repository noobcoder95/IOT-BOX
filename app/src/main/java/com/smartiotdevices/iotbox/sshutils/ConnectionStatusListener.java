package com.smartiotdevices.iotbox.sshutils;

public interface ConnectionStatusListener
{
   void onDisconnected();
   void onConnected();
}
