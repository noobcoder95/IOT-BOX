package com.smartiotdevices.iotbox;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

public class ActivitySettings extends AppCompatActivity
{
    AppBarLayout app_bar_layout;
    Toolbar toolbar_tab;
    ViewPager view_pager;
    TabLayout tab_layout;
    PageAdapter page_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        app_bar_layout = findViewById(R.id.appbar_settings);
        toolbar_tab = findViewById(R.id.setting_toolbartab);
        view_pager = findViewById(R.id.setting_viewpager);
        tab_layout = findViewById(R.id.setting_tablayout);
        page_adapter = new PageAdapter(getSupportFragmentManager());
        page_adapter.addFragment(new FrgDeviceSetup(), getString(R.string.title_device_list));
        view_pager.setAdapter(page_adapter);
        tab_layout.setupWithViewPager(view_pager);
    }

    @Override
    public void onBackPressed()
    {
        ExitDialog();
    }

    private void ExitDialog()
    {
        AlertDialog.Builder ab=new AlertDialog.Builder(ActivitySettings.this);
        ab.setTitle(getString(R.string.title_exit));
        ab.setMessage(R.string.message_settings_exit);
        ab.setPositiveButton(getString(R.string.button_yes), (dialog, which) ->
        {
            overridePendingTransition(0,0);
            Intent intent = new Intent(this, ActivityMain.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();

            overridePendingTransition(0,0);
            startActivity(intent);
        });
        ab.setNegativeButton(getString(R.string.button_no), (dialog, which) -> dialog.dismiss());
        ab.show();
    }
}
