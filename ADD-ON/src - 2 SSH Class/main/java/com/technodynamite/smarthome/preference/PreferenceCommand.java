package com.smartiotdevices.iotbox.preference;

import android.os.Parcel;
import android.os.Parcelable;

public class PreferenceCommand implements Parcelable
{
    private String cmdName;

    private String cmdExec;

    public PreferenceCommand(String cmd, String exec)
    {
        cmdName=cmd;
        cmdExec=exec;
    }

    public String getCmd()
    {
        return cmdName;
    }

    public String getExec()
    {
        return cmdExec;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeString(cmdName);
        out.writeString(cmdExec);
    }

    public static final Creator<PreferenceCommand> CREATOR = new Creator<PreferenceCommand>()
    {
        public PreferenceCommand createFromParcel(Parcel in)
        {
            return new PreferenceCommand(in);
        }

        public PreferenceCommand[] newArray(int size)
        {
            return new PreferenceCommand[size];
        }
    };

    public PreferenceCommand(Parcel in)
    {
        cmdName = in.readString();
        cmdExec = in.readString();
    }
}
