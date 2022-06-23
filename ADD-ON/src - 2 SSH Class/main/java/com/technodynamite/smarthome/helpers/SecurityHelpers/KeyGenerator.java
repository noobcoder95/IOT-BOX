package com.smartiotdevices.iotbox.helpers.SecurityHelpers;

import android.annotation.SuppressLint;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class KeyGenerator
{

    private static final byte[] keyValue =
            new byte[]{'T', '3', 'c', 'h', 'n', '0', 'd', 'y', 'n', '4', 'm', '1', 'T', '3', '2', '0'};


    public static String encrypt(String cleartext)
    {
        byte[] rawKey = getRawKey();
        byte[] result = new byte[0];
        try
        {
            result = encrypt(rawKey, cleartext.getBytes());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return toHex(result);
    }

    public static String int_encrypt(Integer cleartext)
    {
        byte[] rawKey = getRawKey();
        byte[] result = new byte[0];
        try
        {
            result = encrypt(rawKey, cleartext.toString().getBytes());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return toHex(result);
    }

    public static String decrypt(String encrypted)
    {
        byte[] enc = toByte(encrypted);
        byte[] result = new byte[0];
        try {
            result = decrypt(enc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(result);
    }

    private static byte[] getRawKey() {
        SecretKey key = new SecretKeySpec(keyValue, "AES");
        return key.getEncoded();
    }

    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception
    {
        SecretKey skeySpec = new SecretKeySpec(raw, "AES");
        @SuppressLint("GetInstance")
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        return cipher.doFinal(clear);
    }

    private static byte[] decrypt(byte[] encrypted) throws Exception
    {
        SecretKey skeySpec = new SecretKeySpec(keyValue, "AES");
        @SuppressLint("GetInstance")
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        return cipher.doFinal(encrypted);
    }

    private static byte[] toByte(String hexString)
    {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        return result;
    }

    private static String toHex(byte[] buf)
    {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (byte b : buf) {
            appendHex(result, b);
        }
        return result.toString();
    }

    private final static String HEX = "0123456789ABCDEF";

    private static void appendHex(StringBuffer sb, byte b)
    {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }
}
