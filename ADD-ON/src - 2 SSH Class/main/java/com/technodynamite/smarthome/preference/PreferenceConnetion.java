package com.smartiotdevices.iotbox.preference;

import android.os.Parcel;
import android.os.Parcelable;

public class PreferenceConnetion implements Parcelable
{
    private String connectionName;

    private String hostName;

    private String portNumber;

    private String username;

    private String password;

    public PreferenceConnetion(String pass, String name, String host, String user, String port)
    {
        connectionName = name;
        hostName = host;
        username = user;
        portNumber = port;
        password = pass;
    }

    public String getName()
    {
        return connectionName;
    }

    public String getHostName()
    {
        return hostName;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public String getPort()
    {
        return portNumber;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeString(connectionName);
        out.writeString(hostName);
        out.writeString(portNumber);
        out.writeString(username);
        out.writeString(password);
    }

    public static final Creator<PreferenceConnetion> CREATOR = new Creator<PreferenceConnetion>()
    {
        public PreferenceConnetion createFromParcel(Parcel in)
        {
            return new PreferenceConnetion(in);
        }

        public PreferenceConnetion[] newArray(int size)
        {
            return new PreferenceConnetion[size];
        }
    };

    private PreferenceConnetion(Parcel in)
    {
        connectionName = in.readString();
        hostName = in.readString();
        portNumber = in.readString();
        username = in.readString();
        password = in.readString();
    }
}
