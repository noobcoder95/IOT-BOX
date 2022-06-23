package com.smartiotdevices.iotbox.preference;

import android.os.Parcel;
import android.os.Parcelable;

public class PreferenceCommand implements Parcelable
{
    private String cmd_name;

    private String cmd_exec;

    public PreferenceCommand(String cmd, String exec)
    {
        cmd_name = cmd;
        cmd_exec = exec;
    }

    public String getCmd()
    {
        return cmd_name;
    }

    public String getExec()
    {
        return cmd_exec;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeString(cmd_name);
        out.writeString(cmd_exec);
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

    private PreferenceCommand(Parcel in)
    {
        cmd_name = in.readString();
        cmd_exec = in.readString();
    }
}
