package com.smartiotdevices.iotbox;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.smartiotdevices.iotbox.helpers.DBHelper;
import com.smartiotdevices.iotbox.helpers.SecurityHelpers.KeyGenerator;
import com.smartiotdevices.iotbox.preference.PreferenceConnetion;
import com.smartiotdevices.iotbox.preference.PreferenceConstants;

import java.util.ArrayList;

public class FrgListHost extends Fragment
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String PRECONFIGURED_CLICKED = "preconfig_host.clicked";
    private static final String PRECONFIGURED = "preconfig_host";
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    private PreferenceConnetion selected_host, delete_host, setup;
    private EditText edit;
    private SharedPreferences preconfigured;
    private View addDialogView = null;
    private Button save = null;
    private PreferenceConnetion add_connection=null;
    private DBHelper dbh;

    public FrgListHost()
    {

    }

    public static FrgListHost newInstance(String param1, String param2)
    {
        FrgListHost fragment = new FrgListHost();
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
        final View rootView = inflater.inflate(R.layout.fragment_list_host, container,false);
        ListView listView = rootView.findViewById(R.id.host_list);
        setupListView(listView);
        Button btnAdd = rootView.findViewById(R.id.button_add);
        btnAdd.setOnClickListener(view -> setupAddButton());

        if (getActivity() != null)
        {
            preconfigured = getActivity().getSharedPreferences(PRECONFIGURED, Activity.MODE_PRIVATE);
        }
        Button preconfig = rootView.findViewById(R.id.preconfigured_host);
        preconfig.setOnClickListener(view ->
        {
            // Set Preconfigured Host Here
            dbh.addConnection(new PreferenceConnetion(KeyGenerator.encrypt("0o)O-0-0"), KeyGenerator.encrypt("SmartHome"), KeyGenerator.encrypt("192.168.43.205"), KeyGenerator.encrypt("technodynamite"), KeyGenerator.encrypt("22")));
            if (getFragmentManager() != null)
            {
                getFragmentManager().beginTransaction().detach(FrgListHost.this).attach(FrgListHost.this).commit();
            }
            preconfigured.edit().putBoolean(PRECONFIGURED_CLICKED, true).apply();
        });

        if (!preconfigured.getBoolean(PRECONFIGURED_CLICKED, false))
        {
            preconfig.setVisibility(View.VISIBLE);
        }
        else
        {
            preconfig.setVisibility(View.GONE);
        }

        Button btnDeleteList = rootView.findViewById(R.id.button_delete);
        setupDeleteButton(btnDeleteList);
        selected_host = null;
        delete_host = null;

        return rootView;
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

    private void setupListView(final ListView lv)
    {
        if (getActivity() != null)
        {
            dbh = new DBHelper(getActivity());
            final ArrayList<String> connname = new ArrayList<>();
            final Cursor cursor= dbh.getReadableDatabase().rawQuery("select * from " + DBHelper.tableconnection,null);
            while (cursor.moveToNext())
            {
                String cname= KeyGenerator.decrypt(cursor.getString(0));
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
                selected_host = new PreferenceConnetion(KeyGenerator.decrypt(cursor.getString(3)), KeyGenerator.decrypt(cursor.getString(0)), KeyGenerator.decrypt(cursor.getString(1)), KeyGenerator.decrypt(cursor.getString(2)), KeyGenerator.decrypt(cursor.getString(4)));
                delete_host = new PreferenceConnetion(cursor.getString(3), cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(4));
            });
        }
    }

    private void setupAddButton()
    {
        if (getActivity() != null)
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(getString(R.string.title_connectionsetup));
            alertDialogBuilder.setIcon(R.mipmap.tdx);
            add_host_dialog();
            alertDialogBuilder.setView(addDialogView);
            final AlertDialog ad = alertDialogBuilder.create();
            ad.show();
            save.setOnClickListener(v ->
            {
                edit = addDialogView.findViewById(R.id.connectionNameField);
                String name = edit.getText().toString();
                edit = addDialogView.findViewById(R.id.hostNameField);
                String host = edit.getText().toString();
                edit = addDialogView.findViewById(R.id.usernameField);
                String userName = edit.getText().toString();
                edit = addDialogView.findViewById(R.id.portField);
                String port = edit.getText().toString();
                edit = addDialogView.findViewById(R.id.passwordField);
                String passwordOrKey = edit.getText().toString();

                if (name.isEmpty() || host.isEmpty() || userName.isEmpty() || port.isEmpty() || passwordOrKey.isEmpty()) {
                    findError(name, host, userName, port, passwordOrKey);
                }
                else
                {
                    add_connection = new PreferenceConnetion(KeyGenerator.encrypt(passwordOrKey), KeyGenerator.encrypt(name), KeyGenerator.encrypt(host), KeyGenerator.encrypt(userName), KeyGenerator.encrypt(port));
                    createNewPreference(add_connection);
                    if (getFragmentManager() != null)
                    {
                        getFragmentManager().beginTransaction().detach(FrgListHost.this).attach(FrgListHost.this).commit();
                    }
                    ad.dismiss();
                }
            });
        }
    }

    private void setupDeleteButton(Button btnDeleteList)
    {
        btnDeleteList.setOnClickListener(v ->
        {
            if (getActivity() != null)
            {
                AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
                if (delete_host != null)
                {
                    ab.setMessage(getString(R.string.message_deleteconnection));
                    ab.setTitle(getString(R.string.title_warning));
                    ab.setPositiveButton(getString(R.string.button_ok), (dialog, which) ->
                    {
                        dialog.dismiss();
                        deletePreference(delete_host);
                    });
                    ab.setNegativeButton(getString(R.string.button_no), (dialog, which) -> dialog.dismiss());
                    ab.show();
                }
                else
                {
                    ab.setMessage(getString(R.string.message_connectionnotselected));
                    ab.setTitle(getString(R.string.title_noselect));
                    ab.setPositiveButton(getString(R.string.button_ok), (dialog, which) -> dialog.dismiss());
                    ab.show();
                }
            }
        });
    }

    private void createNewPreference(PreferenceConnetion c)
    {
        dbh = new DBHelper(getActivity());
        dbh.addConnection(c);
    }

    private void deletePreference(PreferenceConnetion c)
    {
        dbh = new DBHelper(getActivity());
        dbh.deleteConnection(c);
        if (getFragmentManager() != null)
        {
            getFragmentManager().beginTransaction().detach(FrgListHost.this).attach(FrgListHost.this).commit();
        }
    }

    @SuppressLint("InflateParams")
    private void add_host_dialog()
    {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        addDialogView = layoutInflater.inflate(R.layout.popup_addhost, null);
        save = addDialogView.findViewById(R.id.saveHost);
        if (getActivity() != null)
        {
            setup = getActivity().getIntent().getParcelableExtra(PreferenceConstants.PREFERENCE_PARCEABLE);
        }

        if (setup != null)
        {
            fillForm();
        }
    }

    private void fillForm()
    {
        EditText edit;

        edit = addDialogView.findViewById(R.id.hostNameField);
        edit.setText(setup.getHostName());
        edit = addDialogView.findViewById(R.id.usernameField);
        edit.setText(setup.getUsername());
        edit = addDialogView.findViewById(R.id.connectionNameField);
        edit.setText(setup.getName());
        edit = addDialogView.findViewById(R.id.passwordField);
        edit.setText(setup.getPassword());
        edit = addDialogView.findViewById(R.id.portField);
        edit.setText(String.valueOf(setup.getPort()));
    }

    private void findError(String name,String host,String userName,String port,String passwordOrKey)
    {
        if (getActivity() != null)
        {
            AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
            ab.setTitle(getString(R.string.title_alert));
            ab.setPositiveButton(getString(R.string.button_ok), (dialog, which) -> dialog.dismiss());
            if (name.isEmpty())
            {
                ab.setMessage(getString(R.string.message_fillconnectionname));
                ab.show();
            }
            else if (host.isEmpty())
            {
                ab.setMessage(getString(R.string.message_fillhostname));
                ab.show();
            }
            else if (userName.isEmpty())
            {
                ab.setMessage(getString(R.string.message_filluname));
                ab.show();
            }
            else if (port.isEmpty())
            {
                ab.setMessage(getString(R.string.message_fillport));
                ab.show();
            }
            else if (passwordOrKey.isEmpty())
            {
                ab.setMessage(getString(R.string.message_fillpass));
                ab.show();
            }
        }
    }
}