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
import com.smartiotdevices.iotbox.preference.PreferenceCommand;
import com.smartiotdevices.iotbox.preference.PreferenceConstants;

import java.util.ArrayList;

public class FrgListCmd extends Fragment
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String PRECONFIGURED_CLICKED = "preconfig_cmd.clicked";
    private static final String PRECONFIGURED = "preconfig_cmd";
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    private PreferenceCommand selected_cmd, delete_cmd, setup;
    private SharedPreferences preconfigured;
    private View addDialogView = null;
    private Button save = null;
    private PreferenceCommand add_cmd=null;
    private EditText edit;
    private DBHelper dbh;

    public FrgListCmd()
    {

    }

    public static FrgListCmd newInstance(String param1, String param2)
    {
        FrgListCmd fragment = new FrgListCmd();
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
        final View rootView = inflater.inflate(R.layout.fragment_list_cmd, container,false);
        ListView listView = rootView.findViewById(R.id.cmd_list);
        Button btnAdd = rootView.findViewById(R.id.add_cmd);
        btnAdd.setOnClickListener(view -> setupAddButton());
        Button btnDeleteList = rootView.findViewById(R.id.del_cmd);

        if (getActivity() != null)
        {
            preconfigured = getActivity().getSharedPreferences(PRECONFIGURED, Activity.MODE_PRIVATE);
        }
        Button preconfig = rootView.findViewById(R.id.preconfigured_cmd);
        preconfig.setOnClickListener(view ->
        {
            // Set Preconfigured Command Here
            dbh.addCmd(new PreferenceCommand(KeyGenerator.encrypt("Version"), KeyGenerator.encrypt("echo && echo && echo && echo && echo && echo && echo && echo && echo && echo && echo && echo && uname -a")));
            if (getFragmentManager() != null)
            {
                getFragmentManager().beginTransaction().detach(FrgListCmd.this).attach(FrgListCmd.this).commit();
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

        setupDeleteButton(btnDeleteList);
        setupListView(listView);

        selected_cmd = null;
        delete_cmd = null;
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
            final ArrayList<String> connname=new ArrayList<>();
            final Cursor cursor= dbh.getReadableDatabase().rawQuery("select * from "+ DBHelper.userCommand,null);
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
                selected_cmd = new PreferenceCommand(KeyGenerator.decrypt(cursor.getString(0)), KeyGenerator.decrypt(cursor.getString(1)));
                delete_cmd = new PreferenceCommand(cursor.getString(0), cursor.getString(1));
            });
        }
    }

    private void setupAddButton()
    {
        if (getActivity() != null)
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(getString(R.string.title_cmdsetup));
            alertDialogBuilder.setIcon(R.mipmap.tdx);
            add_cmd_dialog();
            alertDialogBuilder.setView(addDialogView);
            final AlertDialog ad = alertDialogBuilder.create();
            ad.show();
            save.setOnClickListener(v ->
            {
                edit = addDialogView.findViewById(R.id.editcmd);
                String cmd = edit.getText().toString();
                edit = addDialogView.findViewById(R.id.editexec);
                String exec = edit.getText().toString();

                if (cmd.isEmpty() || exec.isEmpty())
                {
                    findError(cmd, exec);
                }
                else
                {
                    add_cmd = new PreferenceCommand(KeyGenerator.encrypt(cmd), KeyGenerator.encrypt(exec));
                    createNewPreference(add_cmd);
                    if (getFragmentManager() != null)
                    {
                        getFragmentManager().beginTransaction().detach(FrgListCmd.this).attach(FrgListCmd.this).commit();
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
                if (delete_cmd != null)
                {
                    ab.setMessage(getString(R.string.message_deletecmd));
                    ab.setTitle(getString(R.string.title_warning));
                    ab.setPositiveButton(getString(R.string.button_ok), (dialog, which) ->
                    {
                        dialog.dismiss();
                        deletePreference(delete_cmd);
                    });
                    ab.setNegativeButton(getString(R.string.button_no), (dialog, which) -> dialog.dismiss());
                    ab.show();
                }
                else
                {
                    ab.setMessage(getString(R.string.message_cmdnotselected));
                    ab.setTitle(getString(R.string.title_noselect));
                    ab.setPositiveButton(getString(R.string.button_ok), (dialog, which) -> dialog.dismiss());
                    ab.show();
                }
            }
        });
    }

    private void createNewPreference(PreferenceCommand c)
    {
        dbh.addCmd(c);
    }

    private void deletePreference(PreferenceCommand c)
    {
        dbh.delCmd(c);
        if (getFragmentManager() != null)
        {
            getFragmentManager().beginTransaction().detach(FrgListCmd.this).attach(FrgListCmd.this).commit();
        }
    }

    @SuppressLint("InflateParams")
    private void add_cmd_dialog()
    {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        addDialogView = layoutInflater.inflate(R.layout.popup_addcmd, null);
        save = addDialogView.findViewById(R.id.saveCmd);
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

        edit = addDialogView.findViewById(R.id.editcmd);
        edit.setText(setup.getCmd());
        edit = addDialogView.findViewById(R.id.editexec);
        edit.setText(setup.getExec());
    }

    private void findError(String cmd,String exec)
    {
        if (getActivity() != null)
        {
            AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
            ab.setTitle(getString(R.string.title_alert));
            ab.setPositiveButton(getString(R.string.button_ok), (dialog, which) -> dialog.dismiss());

            if ((cmd.isEmpty()) | exec.isEmpty())
            {
                ab.setMessage(getString(R.string.message_fillform));
                ab.show();
            }
        }
    }
}
