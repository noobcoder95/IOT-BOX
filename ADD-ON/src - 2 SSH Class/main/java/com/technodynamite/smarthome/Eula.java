package com.smartiotdevices.iotbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

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
            builder.setPositiveButton(activity.getResources().getString(R.string.button_accept), (dialog, which) -> accept(preferences));
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
        final Uri packageUri = Uri.parse("package:com.technodynamite.iotbox");
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
        activity.startActivity(uninstallIntent);
        activity.finish();
    }
}
