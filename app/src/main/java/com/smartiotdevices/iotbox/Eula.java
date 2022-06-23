package com.smartiotdevices.iotbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.smartiotdevices.iotbox.helpers.DBHelper;
import com.smartiotdevices.iotbox.helpers.SecurityHelpers.KeyGenerator;
import com.smartiotdevices.iotbox.preference.PreferenceCommand;

public class Eula
{
    private static final String PREFERENCE_EULA_ACCEPTED = "eula.accepted";
    private static final String PREFERENCES_EULA = "eula";
    public static boolean show(final Activity activity)
    {
        final SharedPreferences preferences = activity.getSharedPreferences(PREFERENCES_EULA, Activity.MODE_PRIVATE);
        if (!preferences.getBoolean(PREFERENCE_EULA_ACCEPTED, false))
        {
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(activity.getResources().getString(R.string.title_eula));
            builder.setCancelable(false);
            builder.setPositiveButton(activity.getResources().getString(R.string.button_accept), (dialog, which) ->
            {
                cmdBuiltin(activity);
                accept(preferences);
            });
            builder.setNegativeButton(activity.getResources().getString(R.string.button_decline), (dialog, which) -> refuse(activity));
            builder.setOnCancelListener(dialog -> refuse(activity));
            builder.setMessage(activity.getResources().getString(R.string.description_eula));
            builder.create().show();
            return false;
        }
        return true;
    }

    private static void accept(SharedPreferences preferences)
    {
        preferences.edit().putBoolean(PREFERENCE_EULA_ACCEPTED, true).apply();
    }

    static void refuse(Activity activity)
    {
        final Uri packageUri = Uri.parse("package:com.smartiotdevices.iotbox");
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
        activity.startActivity(uninstallIntent);
    }

    private static void cmdBuiltin(Activity activity)
    {
        DBHelper.getInstance(activity).addCmd(new PreferenceCommand(KeyGenerator.encrypt(activity.getString(R.string.cmd_label_system_status)), KeyGenerator.encrypt(activity.getString(R.string.cmd_system_status))));
        DBHelper.getInstance(activity).addCmd(new PreferenceCommand(KeyGenerator.encrypt(activity.getString(R.string.cmd_label_system_clean)), KeyGenerator.encrypt(activity.getString(R.string.cmd_system_clean))));
        DBHelper.getInstance(activity).addCmd(new PreferenceCommand(KeyGenerator.encrypt(activity.getString(R.string.cmd_label_system_update)), KeyGenerator.encrypt(activity.getString(R.string.cmd_system_update))));
        DBHelper.getInstance(activity).addCmd(new PreferenceCommand(KeyGenerator.encrypt(activity.getString(R.string.cmd_label_restart_hotspot)), KeyGenerator.encrypt(activity.getString(R.string.cmd_restart_hotspot))));
        DBHelper.getInstance(activity).addCmd(new PreferenceCommand(KeyGenerator.encrypt(activity.getString(R.string.cmd_label_powerstrip_controller)), KeyGenerator.encrypt(activity.getString(R.string.cmd_relay))));
        DBHelper.getInstance(activity).addCmd(new PreferenceCommand(KeyGenerator.encrypt(activity.getString(R.string.cmd_label_powerstrip_timer)), KeyGenerator.encrypt(activity.getString(R.string.cmd_relay))));
        DBHelper.getInstance(activity).addCmd(new PreferenceCommand(KeyGenerator.encrypt(activity.getString(R.string.cmd_label_smartlight)), KeyGenerator.encrypt(activity.getString(R.string.cmd_smartlight_on))));
    }
}
