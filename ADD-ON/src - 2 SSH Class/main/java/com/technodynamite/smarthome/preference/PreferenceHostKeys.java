package com.smartiotdevices.iotbox.preference;

public class PreferenceHostKeys
{
    private String fingerprint;

    private String key;

    private String type;

    private String hostName;

    public PreferenceHostKeys(String host, String fp, String k, String t)
    {
        fingerprint = fp;
        key = k;
        type = t;
        hostName = host;
    }

    public String getKey()
    {
        return key;
    }

    public String getFingerprint()
    {
        return fingerprint;
    }

    public String getType()
    {
        return type;
    }

    public String getHostName()
    {
        return hostName;
    }
}
