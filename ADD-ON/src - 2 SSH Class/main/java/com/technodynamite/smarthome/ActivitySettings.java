package com.smartiotdevices.iotbox;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.smartiotdevices.iotbox.helpers.SecurityHelpers.KeyGenerator;
import com.smartiotdevices.iotbox.sshutils.SessionController;

public class ActivitySettings extends AppCompatActivity
{
    private View popupInputDialogView = null;
    private EditText pwdTS = null;
    private Button loginTS = null;
    private Button cancelTS = null;

    AppBarLayout appBarLayout;
    Toolbar toolbartab;
    ViewPager viewPager;
    TabLayout tabLayout;
    PageAdapter pageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        appBarLayout= findViewById(R.id.appbar_settings);
        appBarLayout.setBackgroundColor(Color.GRAY);
        toolbartab= findViewById(R.id.setting_toolbartab);
        viewPager= findViewById(R.id.setting_viewpager);
        tabLayout= findViewById(R.id.setting_tablayout);
        tabLayout.setBackgroundColor(Color.GRAY);
        pageAdapter=new PageAdapter(getSupportFragmentManager());
        pageAdapter.addFragment(new FrgListHost(), getString(R.string.title_hostlist));
        pageAdapter.addFragment(new FrgListCmd(), getString(R.string.title_cmdlist));
        viewPager.setAdapter(pageAdapter);
        tabLayout.setupWithViewPager(viewPager);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.title_supportlogin));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setIcon(R.mipmap.tdx);

        popupLogin();
        alertDialogBuilder.setView(popupInputDialogView);
        final AlertDialog ad = alertDialogBuilder.create();
        ad.show();
        loginTS.setOnClickListener(view ->
        {
            if (pwdTS.getText().toString().contentEquals(KeyGenerator.decrypt(getString(R.string.encryption_key_verdi))) || pwdTS.getText().toString().contentEquals(KeyGenerator.decrypt(getString(R.string.encryption_key_jaka))) || pwdTS.getText().toString().contentEquals(KeyGenerator.decrypt(getString(R.string.encryption_key_fajar))))
            {
                if (SessionController.isConnected() && SessionController.getSessionController() != null)
                {
                    Toast.makeText(this, getString(R.string.message_prevent_settings),Toast.LENGTH_LONG).show();
                    ad.dismiss();
                    finish();
                }

                else
                {
                    ActivityMain.getInstance().finish();
                    ad.dismiss();
                }
            }

            else
            {
                Toast.makeText(ActivitySettings.this, getString(R.string.message_wrongpass), Toast.LENGTH_SHORT).show();
            }
        });

        cancelTS.setOnClickListener(view ->
        {
            ad.dismiss();
            finish();
        });
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();

        overridePendingTransition(0,0);
        Intent intent = new Intent(this, ActivityMain.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0,0);
        startActivity(intent);
    }

    @SuppressLint("InflateParams")
    private void popupLogin()
    {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        popupInputDialogView = layoutInflater.inflate(R.layout.popup_login, null);
        pwdTS = popupInputDialogView.findViewById(R.id.ts_password);
        loginTS = popupInputDialogView.findViewById(R.id.ts_btnlogin);
        cancelTS = popupInputDialogView.findViewById(R.id.ts_btncancel);
    }
}
