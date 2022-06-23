package com.smartiotdevices.iotbox;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.smartiotdevices.iotbox.sshutils.SessionController;

public class ActivityAdmin extends AppCompatActivity
{
    AppBarLayout appbar_layout;
    Toolbar toolbar_tab;
    CustomViewPager view_pager;
    TabLayout tab_layout;
    PageAdapter page_adapter;

    static ActivityAdmin ACTIVITYADMIN;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        appbar_layout= findViewById(R.id.appbar_cctv);
        toolbar_tab= findViewById(R.id.cctv_toolbartab);
        view_pager= findViewById(R.id.cctv_viewpager);
        tab_layout= findViewById(R.id.cctv_tablayout);
        page_adapter=new PageAdapter(getSupportFragmentManager());
        page_adapter.addFragment(new FrgDesktopViewer(), getString(R.string.title_desktop_live));
        page_adapter.addFragment(new FrgRouterManagement(), getString(R.string.title_router_management));
        page_adapter.addFragment(new FrgHotspotManagement(), getString(R.string.title_hotspot_management));
        page_adapter.addFragment(new FrgAvaTalk(), getString(R.string.title_ava));
        view_pager.setAdapter(page_adapter);
        view_pager.setPagingEnabled(false);
        tab_layout.setupWithViewPager(view_pager);

        ACTIVITYADMIN = this;

        checkPermission();
        SessionController.getSessionController().openX11Shell();
    }

    @Override
    public void onBackPressed()
    {
        if (getWindow().getDecorView().getSystemUiVisibility() != 0)
        {
            if (FrgDesktopViewer.getInstance() != null)
            {
                FrgDesktopViewer.getInstance().onHideView();
            }
        }

        else if (FrgDesktopViewer.getInstance() != null && FrgDesktopViewer.getInstance().clicked)
        {
            AlertDialog.Builder ab=new AlertDialog.Builder(ActivityAdmin.this);
            ab.setTitle(getString(R.string.title_warning));
            ab.setMessage(R.string.message_desktop_recorder);
            ab.setPositiveButton(getString(R.string.button_yes), (dialog, which) ->
            {
                FrgDesktopViewer.getInstance().onHideView();
                SessionController.getSessionController().cmdExec(null, null, getString(R.string.cmd_stop_desktop_live));
                overridePendingTransition(0, 0);
                finish();
            });

            ab.setNegativeButton(getString(R.string.button_no), (dialog, which) ->
            {
                dialog.dismiss();
                overridePendingTransition(0, 0);
                finish();
            });
            ab.show();
        }

        else
        {
            overridePendingTransition(0, 0);
            finish();
        }
    }

    private void checkPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
            {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }
    public static ActivityAdmin getInstance()
    {
        return ACTIVITYADMIN;
    }
}
