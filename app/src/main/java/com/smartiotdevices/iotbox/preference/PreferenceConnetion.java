package com.smartiotdevices.iotbox.preference;

import android.os.Parcel;
import android.os.Parcelable;

public class PreferenceConnetion implements Parcelable
{
    private String connection_name;

    private String host_name;

    private String port_number;

    private String username;

    public PreferenceConnetion(String user, String host, String port, String name)
    {
        connection_name = name;
        host_name = host;
        username = user;
        port_number = port;
    }

    public String getName()
    {
        return connection_name;
    }

    public String getHostName()
    {
        return host_name;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPort()
    {
        return port_number;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeString(connection_name);
        out.writeString(host_name);
        out.writeString(port_number);
        out.writeString(username);
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
        connection_name = in.readString();
        host_name = in.readString();
        port_number = in.readString();
        username = in.readString();
    }
}
