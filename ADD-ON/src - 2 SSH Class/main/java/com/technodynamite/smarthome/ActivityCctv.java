package com.smartiotdevices.iotbox;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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

public class ActivityCctv extends AppCompatActivity
{
    AppBarLayout appBarLayout;
    Toolbar toolbartab;
    CustomViewPager viewPager;
    TabLayout tabLayout;
    PageAdapter pageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cctv);

        appBarLayout= findViewById(R.id.appbar_cctv);
        appBarLayout.setBackgroundColor(Color.GRAY);
        toolbartab= findViewById(R.id.cctv_toolbartab);
        viewPager= findViewById(R.id.cctv_viewpager);
        tabLayout= findViewById(R.id.cctv_tablayout);
        tabLayout.setBackgroundColor(Color.GRAY);
        pageAdapter=new PageAdapter(getSupportFragmentManager());
        pageAdapter.addFragment(new FrgVideoList(), getString(R.string.title_listrecorded));
        pageAdapter.addFragment(new FrgCctv_main(), getString(R.string.title_maincctv));
        pageAdapter.addFragment(new FrgCctv_add1(), getString(R.string.title_secondcctv));
        pageAdapter.addFragment(new FrgCctv_add2(), getString(R.string.title_thirdcctv));
        viewPager.setAdapter(pageAdapter);
        viewPager.setPagingEnabled(false);
        tabLayout.setupWithViewPager(viewPager);

        checkPermission();

        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle(getString(R.string.title_info));
        ab.setMessage(getString(R.string.message_cctvuserinfo));
        ab.setPositiveButton(getString(R.string.button_ok), (dialog, which) -> dialog.dismiss());
        ab.setCancelable(false);
        ab.show();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition(0, 0);
        finish();
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
}
