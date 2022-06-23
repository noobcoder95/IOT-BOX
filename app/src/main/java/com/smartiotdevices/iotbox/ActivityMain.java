package com.smartiotdevices.iotbox;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.smartiotdevices.iotbox.sshutils.ConnectionStatusListener;
import com.smartiotdevices.iotbox.sshutils.SessionController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class ActivityMain extends AppCompatActivity
{
	Toolbar toolbar,toolbar_tab;
	CustomViewPager view_pager;
	TabLayout tab_layout;
	PageAdapter page_adapter;
	AppBarLayout appbar_layout;
    ConnectionStatusListener cs_listener;
    Thread thread_disconnected, thread_connected;

    static ActivityMain ACTIVITYMAIN;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ACTIVITYMAIN = this;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        appbar_layout = findViewById(R.id.appbar_main);
        view_pager= findViewById(R.id.viewpager);
        tab_layout= findViewById(R.id.tablayout);
		page_adapter=new PageAdapter(getSupportFragmentManager());
        page_adapter.addFragment(new FrgDevice(), getString(R.string.title_device));
        page_adapter.addFragment(new FrgController(), getString(R.string.title_remote));
        page_adapter.addFragment(new FrgSecurityCam(), getString(R.string.title_securitycam_live));
        toolbar_tab= findViewById(R.id.toolbartab);
		toolbar= findViewById(R.id.toolbar);

		toolbar.setOnLongClickListener(v ->
        {
            LayoutInflater layoutInflater = LayoutInflater.from(ActivityMain.this);
            @SuppressLint("InflateParams")
            View view = layoutInflater.inflate(R.layout.popup_about, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityMain.this);
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setView(view);
            alertDialogBuilder.setTitle(R.string.about);
            alertDialogBuilder.setPositiveButton(getString(R.string.button_ok), (dialog, which) -> dialog.dismiss());
            alertDialogBuilder.setNeutralButton(R.string.button_show_eula, (dialog, which) ->
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMain.this);
                builder.setTitle(getString(R.string.title_eula));
                builder.setCancelable(false);
                builder.setPositiveButton(getString(R.string.button_accept), (a, which1) -> a.dismiss());
                builder.setNeutralButton(getString(R.string.button_decline), (a, which12) -> Eula.refuse(ACTIVITYMAIN));
                builder.setMessage(getString(R.string.description_eula));
                builder.create().show();
            });
            alertDialogBuilder.create().show();
            return false;
        });

        Eula.show(this);
        checkPermission();
        //checkUpdate();
		setSupportActionBar(toolbar);
        view_pager.setAdapter(page_adapter);
        view_pager.setPagingEnabled(true);
        tab_layout.setupWithViewPager(view_pager);

        cs_listener = new ConnectionStatusListener()
        {
            @Override
            public void onDisconnected()
            {
                thread_disconnected = new Thread(() -> runOnUiThread(() ->
                {
                    if (ActivityMousepad.getInstance() != null)
                    {
                        ActivityMousepad.getInstance().finish();
                    }
                    if (ActivityAdmin.getInstance() != null)
                    {
                        ActivityAdmin.getInstance().finish();
                    }

                    if (FrgDevice.getInstance() != null)
                    {
                        if (FrgDevice.getInstance().state_onoff.getText().toString().contains(getString(R.string.title_onconnected)) || FrgDevice.getInstance().btn_connect.getText().equals(getString(R.string.button_onconnect)))
                        {
                            FrgDevice.getInstance().state_onoff.setText(getString(R.string.message_disconnected));
                            FrgDevice.getInstance().btn_connect.setText(getString(R.string.button_ondisconnect));
                            Toast.makeText(ActivityMain.this, getString(R.string.message_disconnected), Toast.LENGTH_SHORT).show();
                        }

                        else if (SessionController.getSessionController().isFailed())
                        {
                            if (FrgDevice.getInstance().thread_progress != null && FrgDevice.getInstance().progress_bar !=null && FrgDevice.getInstance().progress_text != null && FrgDevice.getInstance().ok != null)
                            {
                                FrgDevice.getInstance().thread_progress.interrupt();
                                FrgDevice.getInstance().progress_bar.setProgress(FrgDevice.getInstance().progress_bar.getMax());
                                FrgDevice.getInstance().progress_text.setText(getString(R.string.message_bad_connection));
                                FrgDevice.getInstance().ok.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    if (FrgController.getInstance() != null)
                    {
                        FrgController.getInstance().reloadFragment();
                    }

                    if (FrgSecurityCam.getInstance() != null)
                    {
                        FrgSecurityCam.getInstance().reloadFragment();
                    }
                }));

                thread_disconnected.start();
                if (thread_connected != null)
                {
                    thread_connected.interrupt();
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onConnected()
            {
                thread_connected = new Thread(() -> runOnUiThread(() ->
                {
                    if (FrgDevice.getInstance().state_onoff.getText().equals(getString(R.string.message_disconnected)) || FrgDevice.getInstance().btn_connect.getText().equals(getString(R.string.button_ondisconnect)))
                    {
                        FrgDevice.getInstance().state_onoff.setText(getString(R.string.title_onconnected) + FrgDevice.getInstance().selected_connection.getName());
                        FrgDevice.getInstance().btn_connect.setText(getString(R.string.button_onconnect));
                        FrgDevice.getInstance().thread_progress.interrupt();
                        FrgDevice.getInstance().progress_bar.setProgress(FrgDevice.getInstance().progress_bar.getMax());
                        FrgDevice.getInstance().progress_text.setText(getString(R.string.title_established));
                        FrgDevice.getInstance().ok.setVisibility(View.VISIBLE);
                    }

                    else if (FrgDevice.getInstance().state_onoff.getText().equals(getString(R.string.title_status)))
                    {
                        FrgDevice.getInstance().state_onoff.setText(getString(R.string.title_onconnected) + FrgDevice.getInstance().selected_connection.getName());
                        FrgDevice.getInstance().btn_connect.setText(getString(R.string.button_onconnect));
                        FrgDevice.getInstance().thread_progress.interrupt();
                        FrgDevice.getInstance().progress_bar.setProgress(FrgDevice.getInstance().progress_bar.getMax());
                        FrgDevice.getInstance().progress_text.setText(getString(R.string.title_established));
                        FrgDevice.getInstance().ok.setVisibility(View.VISIBLE);
                    }
                }));
                thread_connected.start();
                if (thread_disconnected != null)
                {
                    thread_disconnected.interrupt();
                }
            }
        };

    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    public void onBackPressed()
    {
        if (getWindow().getDecorView().getSystemUiVisibility() != 0)
        {
            if (FrgSecurityCam.getInstance() != null)
            {
                FrgSecurityCam.getInstance().onHideView();
            }
        }

        else
        {
            AlertDialog.Builder ab=new AlertDialog.Builder(ActivityMain.this);
            ab.setTitle(getString(R.string.title_exit));
            ab.setMessage(getString(R.string.message_exit));
            ab.setPositiveButton(getString(R.string.button_yes), (dialog, which) ->
            {
                if (SessionController.isConnected())
                {
                    SessionController.getSessionController().cmdExec(null, null, getString(R.string.cmd_clear_history));

                    if (FrgSecurityCam.getInstance() != null)
                    {
                        FrgSecurityCam.getInstance().onHideView();
                    }

                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException ignored)
                    {

                    }

                    try
                    {
                        SessionController.getSessionController().disconnect();
                    }

                    catch (IOException ignore)
                    {

                    }

                    Toast.makeText(this, getString(R.string.message_disconnected), Toast.LENGTH_SHORT).show();
                }

                finishAndRemoveTask();
            });
            ab.setNegativeButton(getString(R.string.button_no), (dialog, which) -> dialog.dismiss());
            ab.show();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_setting)
        {
            Intent intent = new Intent(this, ActivitySettings.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            overridePendingTransition(0, 0);
            startActivity(intent);
        }

        if (id == R.id.action_mousepad)
        {
            if (!SessionController.isConnected())
            {
                Toast.makeText(this, getString(R.string.message_plugin), Toast.LENGTH_SHORT).show();
            }

            else
            {
                Intent intent = new Intent(this, ActivityMousepad.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                overridePendingTransition(0, 0);
                startActivity(intent);
            }
        }

        if (id == R.id.action_admin)
        {
            if (!SessionController.isConnected())
            {
                Toast.makeText(this, getString(R.string.message_plugin), Toast.LENGTH_SHORT).show();
            }

            else
            {
                Intent intent = new Intent(this, ActivityAdmin.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                overridePendingTransition(0, 0);
                startActivity(intent);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public static ActivityMain getInstance()
    {
        return ACTIVITYMAIN;
    }

    private void checkPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED))
            {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }

    private void checkUpdate()
    {
        new Thread(() -> runOnUiThread(() ->
        {
            try
            {
                URL  url = new URL("http://technodynamite.ddns.net/iotbox_notif.txt");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
                String output;
                while ((output = bufferedReader.readLine()) != null)
                {
                    AlertDialog.Builder ab=new AlertDialog.Builder(ActivityMain.this);
                    ab.setTitle(R.string.title_notif);
                    ab.setMessage(output);
                    ab.setCancelable(false);
                    ab.setPositiveButton(getString(R.string.button_update), (dialog, which) ->
                    {
                        Uri uri = Uri.parse("http://technodynamite.ddns.net/iotbox_update.html");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    });
                    ab.setNegativeButton(getString(R.string.button_dismiss), (dialog, which) -> dialog.dismiss());
                    ab.show();
                }
                bufferedReader.close();
            }

            catch (IOException io)
            {
                io.printStackTrace();
            }
        })).start();
    }
}
