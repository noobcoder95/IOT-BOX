package com.smartiotdevices.iotbox;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.smartiotdevices.iotbox.sshutils.SessionController;

public class ActivityMain extends AppCompatActivity
{
	Toolbar toolbar,toolbartab;
	ViewPager viewPager;
	TabLayout tabLayout;
	PageAdapter pageAdapter;
	AppBarLayout appBarLayout;

    static ActivityMain mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;

        appBarLayout = findViewById(R.id.appbar_main);
        appBarLayout.setBackgroundColor(Color.GRAY);
        viewPager= findViewById(R.id.viewpager);
        tabLayout= findViewById(R.id.tablayout);
        tabLayout.setBackgroundColor(Color.GRAY);
		pageAdapter=new PageAdapter(getSupportFragmentManager());
        pageAdapter.addFragment(new FrgConnection(), getString(R.string.title_connection));
        pageAdapter.addFragment(new FrgRemote(), getString(R.string.title_remote));
        pageAdapter.addFragment(new FrgCctv_main(), getString(R.string.title_cctvlive));
        toolbartab= findViewById(R.id.toolbartab);
		toolbar= findViewById(R.id.toolbar);
        toolbar.setOnLongClickListener(view ->
        {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            @SuppressLint("InflateParams")
            View v = layoutInflater.inflate(R.layout.popup_about, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setView(v);
            alertDialogBuilder.setPositiveButton(getString(R.string.button_ok), (dialog, which) -> dialog.dismiss());
            alertDialogBuilder.setNeutralButton(R.string.button_showeula, (dialog, which) ->
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.title_eula));
                builder.setCancelable(false);
                builder.setPositiveButton(getString(R.string.button_accept), (a, which1) -> a.dismiss());
                builder.setNegativeButton(getString(R.string.button_decline), (a, which12) -> Eula.refuse(mainActivity));
                builder.setMessage(getString(R.string.description_eula));
                builder.create().show();
            });
            alertDialogBuilder.create().show();
            return false;
        });

        Eula.show(this);
        checkPermission();
		setSupportActionBar(toolbar);
        viewPager.setAdapter(pageAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    public void onBackPressed()
    {
        ExitDialog();
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

        if (id == R.id.action_extra)
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

        if (id == R.id.action_cctv)
        {
            if (!SessionController.isConnected())
            {
                Toast.makeText(this, getString(R.string.message_plugin), Toast.LENGTH_SHORT).show();
            }

            else
            {
                Intent intent = new Intent(this, ActivityCctv.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                overridePendingTransition(0, 0);
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public static ActivityMain getInstance()
    {
        return mainActivity;
    }

    private void checkPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED))
            {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }
    
    private void ExitDialog()
    {
        AlertDialog.Builder ab=new AlertDialog.Builder(ActivityMain.this);
        ab.setTitle(getString(R.string.title_exit));
        ab.setMessage(getString(R.string.message_exit));
        ab.setPositiveButton(getString(R.string.button_yes), (dialog, which) -> {
            if (SessionController.isConnected())
            {
                SessionController.getSessionController().getSession().disconnect();
                finish();
            }

            else
            {
                finish();
            }
        });
        ab.setNegativeButton(getString(R.string.button_no), (dialog, which) -> dialog.dismiss());
        ab.show();
    }
}
