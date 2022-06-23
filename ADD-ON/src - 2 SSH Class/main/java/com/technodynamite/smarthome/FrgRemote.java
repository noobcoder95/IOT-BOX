package com.smartiotdevices.iotbox;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.smartiotdevices.iotbox.helpers.DBHelper;
import com.smartiotdevices.iotbox.helpers.SecurityHelpers.KeyGenerator;
import com.smartiotdevices.iotbox.sshutils.SessionController;

import java.util.ArrayList;

public class FrgRemote extends Fragment
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private ResultText mServerStatus;
    private View cmdDialog = null;
    private Handler mHandler = new Handler();

    private OnFragmentInteractionListener mListener;

    public FrgRemote()
    {

    }

    public static FrgRemote newInstance(String param1, String param2)
    {
        FrgRemote fragment = new FrgRemote();
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
        View rootView = inflater.inflate(R.layout.fragment_remote, container, false);
        mServerStatus = rootView.findViewById(R.id.status_result);
        mServerStatus.setEnabled(false);
        Button showcmdlist = rootView.findViewById(R.id.btn_showCmd);

        showcmdlist.setOnClickListener(v ->
        {
            if (getActivity() != null) {
                if (SessionController.isConnected())
                {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    cmd_list();
                    alertDialogBuilder.setView(cmdDialog);
                    alertDialogBuilder.setTitle(getString(R.string.title_cmdlist));
                    alertDialogBuilder.setPositiveButton(getString(R.string.button_dismiss), (dialog, which) -> dialog.dismiss());
                    alertDialogBuilder.create().show();
                }
                else
                {
                    Toast.makeText(getActivity(), getString(R.string.message_cmdlist), Toast.LENGTH_SHORT).show();
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
        inflater.inflate(R.menu.menu_mousepad,menu);
    }

    public void onButtonPressed(Uri uri)
    {
        if (mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
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

    @SuppressLint("InflateParams")
    private void cmd_list()
    {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        cmdDialog = layoutInflater.inflate(R.layout.popup_remote, null);
        ListView listView = cmdDialog.findViewById(R.id.remote_list);
        setupCMD(listView);
    }

    private void setupCMD(ListView lv)
    {
        if (getActivity() != null)
        {
            DBHelper dbh = new DBHelper(getActivity());
            final ArrayList<String> cmdList = new ArrayList<>();
            final Cursor cursor = dbh.getReadableDatabase().rawQuery("select * from " + DBHelper.userCommand, null);
            while (cursor.moveToNext())
            {
                String cmdName = KeyGenerator.decrypt(cursor.getString(0));
                cmdList.add(cmdName);
            }
            ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, cmdList);
            lv.setAdapter(arrayAdapter);
            lv.setOnItemClickListener((parent, view, position, id) ->
            {
                cursor.moveToPosition(position);
                SessionController.getSessionController().cmdExec(mHandler, mServerStatus, KeyGenerator.decrypt(cursor.getString(1)));
            });
        }
    }
}
