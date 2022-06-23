package com.smartiotdevices.iotbox;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
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

import java.util.ArrayList;

public class FrgDeviceSetup extends Fragment
{
    private PreferenceConnetion selected_host, setup = null, add_connection = null;
    private View add_dialog_view = null;
    private Button save = null;
    private EditText edit;

    public FrgDeviceSetup()
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
        final View rootView = inflater.inflate(R.layout.fragment_list_host, container,false);
        ListView listView = rootView.findViewById(R.id.host_list);
        setupListView(listView);
        Button btnAdd = rootView.findViewById(R.id.button_add);
        btnAdd.setOnClickListener(view -> setupAddButton());
        Button btnDeleteList = rootView.findViewById(R.id.button_delete);
        setupDeleteButton(btnDeleteList);
        selected_host = null;

        return rootView;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    private void setupListView(final ListView lv)
    {
        if (getActivity() != null)
        {
            final ArrayList<String> connname = new ArrayList<>();
            @SuppressLint("Recycle") final Cursor cursor= DBHelper.getInstance(getActivity()).getReadableDatabase().rawQuery("select * from " + DBHelper.CONNECTION_TABLE,null);
            while (cursor.moveToNext())
            {
                String cname= KeyGenerator.decrypt(cursor.getString(3));
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
                selected_host = new PreferenceConnetion(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
            });
        }
    }

    private void setupAddButton()
    {
        if (getActivity() != null)
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(getString(R.string.title_device_setup));
            add_host_dialog();
            alertDialogBuilder.setView(add_dialog_view);
            final AlertDialog ad = alertDialogBuilder.create();
            ad.show();
            save.setOnClickListener(v ->
            {
                edit = add_dialog_view.findViewById(R.id.usernameField);
                String userName = edit.getText().toString();
                edit = add_dialog_view.findViewById(R.id.hostNameField);
                String host = edit.getText().toString();
                edit = add_dialog_view.findViewById(R.id.portField);
                String port = edit.getText().toString();
                edit = add_dialog_view.findViewById(R.id.connectionNameField);
                String name = edit.getText().toString();

                if (userName.isEmpty() || host.isEmpty() || port.isEmpty() || name.isEmpty())
                {
                    findError(userName, host, port, name);
                }
                else
                {
                    add_connection = new PreferenceConnetion(KeyGenerator.encrypt(userName), KeyGenerator.encrypt(host), KeyGenerator.encrypt(port), KeyGenerator.encrypt(name));
                    createNewPreference(add_connection);
                    if (getFragmentManager() != null)
                    {
                        getFragmentManager().beginTransaction().detach(FrgDeviceSetup.this).attach(FrgDeviceSetup.this).commit();
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
                if (selected_host != null)
                {
                    ab.setMessage(getString(R.string.message_delete_device));
                    ab.setTitle(getString(R.string.title_warning));
                    ab.setPositiveButton(getString(R.string.button_ok), (dialog, which) ->
                    {
                        dialog.dismiss();
                        deletePreference(selected_host);
                    });
                    ab.setNegativeButton(getString(R.string.button_no), (dialog, which) -> dialog.dismiss());
                }
                else
                {
                    ab.setMessage(getString(R.string.message_empty_selection));
                    ab.setTitle(getString(R.string.title_error));
                    ab.setPositiveButton(getString(R.string.button_ok), (dialog, which) -> dialog.dismiss());
                }
                ab.show();
            }
        });
    }

    private void createNewPreference(PreferenceConnetion c)
    {
        DBHelper.getInstance(getActivity()).addConnection(c);
    }

    private void deletePreference(PreferenceConnetion c)
    {
        DBHelper.getInstance(getActivity()).deleteConnection(c);
        if (getFragmentManager() != null)
        {
            getFragmentManager().beginTransaction().detach(FrgDeviceSetup.this).attach(FrgDeviceSetup.this).commit();
        }
    }

    @SuppressLint("InflateParams")
    private void add_host_dialog()
    {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        add_dialog_view = layoutInflater.inflate(R.layout.popup_addhost, null);
        save = add_dialog_view.findViewById(R.id.saveHost);

        if (setup != null)
        {
            fillForm();
        }
    }

    private void fillForm()
    {
        EditText edit;

        edit = add_dialog_view.findViewById(R.id.usernameField);
        edit.setText(setup.getUsername());
        edit = add_dialog_view.findViewById(R.id.hostNameField);
        edit.setText(setup.getHostName());
        edit = add_dialog_view.findViewById(R.id.portField);
        edit.setText(String.valueOf(setup.getPort()));
        edit = add_dialog_view.findViewById(R.id.connectionNameField);
        edit.setText(setup.getName());
    }

    private void findError(String userName,String host,String port,String name)
    {
        if (getActivity() != null)
        {
            AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
            ab.setTitle(getString(R.string.title_alert));
            ab.setPositiveButton(getString(R.string.button_ok), (dialog, which) -> dialog.dismiss());
            if (userName.isEmpty())
            {
                ab.setMessage(getString(R.string.message_fill_username));
                ab.show();
            }
            else if (host.isEmpty())
            {
                ab.setMessage(getString(R.string.message_fill_device_address));
                ab.show();
            }
            else if (port.isEmpty())
            {
                ab.setMessage(getString(R.string.message_fill_port));
                ab.show();
            }
            else if (name.isEmpty())
            {
                ab.setMessage(getString(R.string.message_fill_device_name));
                ab.show();
            }
        }
    }
}