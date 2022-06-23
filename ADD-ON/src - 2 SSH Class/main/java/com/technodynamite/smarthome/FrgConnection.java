package com.smartiotdevices.iotbox;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.smartiotdevices.iotbox.sshutils.ConnectionStatusListener;
import com.smartiotdevices.iotbox.sshutils.SessionController;
import com.smartiotdevices.iotbox.sshutils.SessionUserInfo;

import java.io.IOException;
import java.util.ArrayList;

public class FrgConnection extends Fragment
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private int progressStatus = 0;

    private PreferenceConnetion selected_connection = null;
    private ProgressBar progressBar;
    private View progressDialog = null;
    private Button ok = null;
    private Button btnConnect;
    private TextView progressText, stateOnOff;
	private Thread thread;
    private ConnectionStatusListener csListener;
    private OnFragmentInteractionListener mListener;

    public FrgConnection()
    {

    }

    public static FrgConnection newInstance(String param1, String param2)
    {
        FrgConnection fragment = new FrgConnection();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View rootView = inflater.inflate(R.layout.fragment_connection, container, false);
        ListView listView = rootView.findViewById(R.id.connections_list);
        setupConnectionsList(listView);

        stateOnOff = rootView.findViewById(R.id.tv_connectionStatus);
        stateOnOff.setText(getString(R.string.textview_status));
        btnConnect = rootView.findViewById(R.id.btn_connect);

        btnConnect.setOnClickListener(v ->
        {
            if (getActivity() != null)
            {
                AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
                ab.setCancelable(false);
                if (SessionController.isConnected())
                {
                    ab.setMessage(getString(R.string.message_disconnectconfirmation));
                    ab.setTitle(getString(R.string.button_disconnect));
                    ab.setPositiveButton(getString(R.string.button_yes), (dialog, which) ->
                    {
                        try
                        {
                            SessionController.getSessionController().disconnect();
                        }

                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    });
                    ab.setNegativeButton(getString(R.string.button_no), (dialog, which) -> dialog.cancel());
                    ab.show();
                }
                else if (selected_connection != null)
                {
                    startConnection();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setCancelable(false);
                    loadingProgress();
                    alertDialogBuilder.setView(progressDialog);
                    final AlertDialog ad = alertDialogBuilder.create();
                    ad.show();
                    ok.setOnClickListener(view ->
                    {
                        ad.dismiss();
                        progressStatus = 0;
                    });
                }
                else
                {
                    ab.setMessage(getString(R.string.message_noconnectionselected));
                    ab.setTitle(getString(R.string.title_noselect));
                    ab.setPositiveButton(getString(R.string.button_ok), (dialog, which) -> dialog.dismiss());
                    ab.show();
                }
            }
        });

        csListener = new ConnectionStatusListener()
        {
            @Override
            public void onDisconnected()
            {
                if (getActivity() != null)
                {
                    new Thread(() -> getActivity().runOnUiThread(() ->
                    {
                        if (stateOnOff.getText().toString().contains(getString(R.string.textview_onconnected)) || btnConnect.getText().equals(getString(R.string.button_onconnect)))
                        {
                            try
                            {
                                SessionController.getSessionController().disconnect();
                            }

                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                            stateOnOff.setText(getString(R.string.message_disconnected));
                            btnConnect.setText(getString(R.string.button_ondisconnect));
                            Toast.makeText(getActivity(), getString(R.string.message_disconnected), Toast.LENGTH_LONG).show();
                        }

                        else if (SessionController.getSessionController().isFailed())
                        {
                            thread.interrupt();
                            progressBar.setProgress(progressBar.getMax());
                            progressText.setText(getString(R.string.message_badconnwhost));
                            ok.setVisibility(View.VISIBLE);
                        }
                    })).start();
                }
            }

            @Override
            public void onConnected()
            {
                if (getActivity() != null)
                {
                    new Thread(() -> getActivity().runOnUiThread(() ->
                    {
                        if (stateOnOff.getText().equals(getString(R.string.message_disconnected)) || btnConnect.getText().equals(getString(R.string.button_ondisconnect)))
                        {
                            stateOnOff.setText(getString(R.string.textview_onconnected) + selected_connection.getName());
                            btnConnect.setText(getString(R.string.button_onconnect));
                            thread.interrupt();
                            progressBar.setProgress(progressBar.getMax());
                            progressText.setText(getString(R.string.title_established));
                            ok.setVisibility(View.VISIBLE);
                        }
                        else if (stateOnOff.getText().equals(getString(R.string.textview_status)))
                        {
                            stateOnOff.setText(getString(R.string.textview_onconnected) + selected_connection.getName());
                            btnConnect.setText(getString(R.string.button_onconnect));
                            thread.interrupt();
                            progressBar.setProgress(progressBar.getMax());
                            progressText.setText(getString(R.string.title_established));
                            ok.setVisibility(View.VISIBLE);
                        }
                    })).start();
                }
            }
        };

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
        mListener = null;
    }

    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(Uri uri);
    }

    public void onButtonPressed(Uri uri)
    {
        if (mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
    }

    private void startConnection()
    {
        SessionUserInfo user = new SessionUserInfo(selected_connection.getUsername(), selected_connection.getHostName(), Integer.parseInt(selected_connection.getPort()), getActivity());
        user.setPassword(selected_connection.getPassword());
        SessionController.getSessionController().setUserInfo(user);
        SessionController.getSessionController().connect();
        if (csListener != null)
            SessionController.getSessionController().setConnectionStatusListener(csListener);
    }

    private void setupConnectionsList(ListView lv)
    {
        if (getActivity() != null)
        {
            DBHelper dbh = new DBHelper(getActivity());
            final ArrayList<String> connname = new ArrayList<>();
            final Cursor cursor = dbh.getReadableDatabase().rawQuery("select * from " + DBHelper.tableconnection, null);
            while (cursor.moveToNext())
            {
                String cname = KeyGenerator.decrypt(cursor.getString(0));
                connname.add(cname);
            }
            ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, connname);
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
                selected_connection = new PreferenceConnetion(KeyGenerator.decrypt(cursor.getString(3)), KeyGenerator.decrypt(cursor.getString(0)), KeyGenerator.decrypt(cursor.getString(1)), KeyGenerator.decrypt(cursor.getString(2)), KeyGenerator.decrypt(cursor.getString(4)));
            });
        }
    }

    @SuppressLint("InflateParams")
    private void loadingProgress()
    {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        progressDialog = layoutInflater.inflate(R.layout.popup_progressbar, null);
        progressBar = progressDialog.findViewById(R.id.progressBar);
        progressText = progressDialog.findViewById(R.id.progressText);
        ok = progressDialog.findViewById(R.id.ok);
        ok.setVisibility(View.INVISIBLE);
        thread = new Thread(() ->
        {
            while (progressStatus < progressBar.getMax())
            {
                progressStatus += 1;
                if (getActivity() != null)
                {
                    getActivity().runOnUiThread(() -> progressBar.setProgress(progressStatus));
                }
                try
                {
                    Thread.sleep(1000);
                }

                catch (InterruptedException e)
                {
                    Log.e("Exception", "Thread Interrupted");
                    progressStatus = progressBar.getMax();
                }
            }
        });
		thread.start();
    }
}
