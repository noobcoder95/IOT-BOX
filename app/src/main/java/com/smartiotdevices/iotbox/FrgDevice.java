package com.smartiotdevices.iotbox;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.smartiotdevices.iotbox.helpers.DBHelper;
import com.smartiotdevices.iotbox.helpers.SecurityHelpers.KeyGenerator;
import com.smartiotdevices.iotbox.preference.PreferenceConnetion;
import com.smartiotdevices.iotbox.sshutils.SessionController;
import com.smartiotdevices.iotbox.sshutils.SessionUserInfo;

import java.io.IOException;
import java.util.ArrayList;

public class FrgDevice extends Fragment
{
    private int progress_state = 0;

    public PreferenceConnetion selected_connection = null;
    public ProgressBar progress_bar;
    private View progress_dialog, passwd_dialog;
    private EditText input_passwd;
    public Button ok, btn_connect;
    public TextView progress_text, state_onoff;
	public Thread thread_progress;

	@SuppressLint("StaticFieldLeak")
    static FrgDevice FRGDEVICE;

    public FrgDevice()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View rootView = inflater.inflate(R.layout.fragment_connection, container, false);
        ListView listView = rootView.findViewById(R.id.connections_list);
        setupConnectionsList(listView);

        state_onoff = rootView.findViewById(R.id.tv_connectionStatus);
        state_onoff.setText(getString(R.string.title_status));
        btn_connect = rootView.findViewById(R.id.btn_connect);
        FRGDEVICE = this;

        btn_connect.setOnClickListener(v ->
        {
            if (getActivity() != null)
            {
                AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
                ab.setCancelable(false);

                if (SessionController.isConnected())
                {
                    ab.setMessage(getString(R.string.message_disconnect_confirmation));
                    ab.setTitle(getString(R.string.button_disconnect));
                    ab.setPositiveButton(getString(R.string.button_yes), (dialog, which) ->
                    {
                        SessionController.getSessionController().cmdExec(null, null, getString(R.string.cmd_clear_history));

                        if (FrgSecurityCam.getInstance() != null)
                        {
                            FrgSecurityCam.getInstance().onHideView();
                            FrgSecurityCam.getInstance().clicked = false;
                            FrgSecurityCam.getInstance().reloadFragment();
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

                        dialog.dismiss();
                    });
                    ab.setNegativeButton(getString(R.string.button_no), (dialog, which) -> dialog.cancel());
                    ab.show();
                }

                else if (selected_connection != null)
                {
                    AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
                    getPasswd();
                    db.setCancelable(false);
                    db.setView(passwd_dialog);
                    db.setTitle(getString(R.string.hint_password));
                    db.setPositiveButton(getString(R.string.button_ok), (dialog, which) ->
                    {
                        if (input_passwd.getText().toString().isEmpty())
                        {
                            dialog.dismiss();
                            Toast.makeText(getActivity(), getString(R.string.message_empty_pass), Toast.LENGTH_SHORT).show();
                        }

                        else
                        {
                            dialog.dismiss();
                            startConnection();
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                            alertDialogBuilder.setCancelable(false);
                            loadingProgress();
                            alertDialogBuilder.setView(progress_dialog);
                            final AlertDialog ad = alertDialogBuilder.create();
                            ad.show();
                            ok.setOnClickListener(view ->
                            {
                                ad.dismiss();
                                FrgController.getInstance().reloadFragment();
                                progress_state = 0;
                            });
                        }
                    });
                    db.show();
                }
                else
                {
                    ab.setMessage(getString(R.string.message_empty_selection));
                    ab.setTitle(getString(R.string.title_error));
                    ab.setPositiveButton(getString(R.string.button_ok), (dialog, which) -> dialog.dismiss());
                    ab.show();
                }
            }
        });

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_technical_settings,menu);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    private void startConnection()
    {
        SessionUserInfo user = new SessionUserInfo(selected_connection.getUsername(), selected_connection.getHostName(), Integer.parseInt(selected_connection.getPort()), getActivity());
        user.setPassword(input_passwd.getText().toString());
        SessionController.getSessionController().setUserInfo(user);
        SessionController.getSessionController().connect();
        if (ActivityMain.getInstance().cs_listener != null)
        {
            SessionController.getSessionController().setConnectionStatusListener(ActivityMain.getInstance().cs_listener);
        }
    }

    private void setupConnectionsList(ListView lv)
    {
        if (getActivity() != null)
        {
            final ArrayList<String> connname = new ArrayList<>();
            @SuppressLint("Recycle") final Cursor cursor = DBHelper.getInstance(getActivity()).getReadableDatabase().rawQuery("select * from " + DBHelper.CONNECTION_TABLE, null);
            while (cursor.moveToNext())
            {
                String cname = KeyGenerator.decrypt(cursor.getString(3));
                connname.add(cname);
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, connname);
            lv.setAdapter(arrayAdapter);
            lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            lv.setOnItemClickListener((parent, view, position, id) ->
            {
                cursor.moveToPosition(position);
                for (int i = 0; i < lv.getChildCount(); i++)
                {
                    if (position == i)
                    {
                        lv.getChildAt(i).setBackgroundColor(Color.LTGRAY);
                    }
                    else
                    {
                        lv.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                    }
                }
                selected_connection = new PreferenceConnetion(KeyGenerator.decrypt(cursor.getString(0)), KeyGenerator.decrypt(cursor.getString(1)), KeyGenerator.decrypt(cursor.getString(2)), KeyGenerator.decrypt(cursor.getString(3)));
            });
        }
    }

    @SuppressLint("InflateParams")
    private void loadingProgress()
    {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        progress_dialog = layoutInflater.inflate(R.layout.popup_progressbar, null);
        progress_bar = progress_dialog.findViewById(R.id.progressBar);
        progress_text = progress_dialog.findViewById(R.id.progressText);
        ok = progress_dialog.findViewById(R.id.ok);
        ok.setVisibility(View.INVISIBLE);
        thread_progress = new Thread(() ->
        {
            while (progress_state < progress_bar.getMax())
            {
                progress_state += 1;
                if (getActivity() != null)
                {
                    getActivity().runOnUiThread(() -> progress_bar.setProgress(progress_state));
                }
                try
                {
                    Thread.sleep(100);
                }

                catch (InterruptedException ignore)
                {
                    progress_state = progress_bar.getMax();
                }
            }
        });
		thread_progress.start();
    }

    @SuppressLint("InflateParams")
    private void getPasswd()
    {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        passwd_dialog = layoutInflater.inflate(R.layout.popup_passwd, null);
        input_passwd = passwd_dialog.findViewById(R.id.passwd);
    }

    public static FrgDevice getInstance()
    {
        return FRGDEVICE;
    }
}
