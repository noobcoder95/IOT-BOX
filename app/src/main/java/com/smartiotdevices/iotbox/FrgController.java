package com.smartiotdevices.iotbox;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.smartiotdevices.iotbox.helpers.DBHelper;
import com.smartiotdevices.iotbox.helpers.SecurityHelpers.KeyGenerator;
import com.smartiotdevices.iotbox.sshutils.SessionController;

import java.util.ArrayList;
import java.util.Objects;

public class FrgController extends Fragment
{

    @SuppressLint("StaticFieldLeak")
    static FrgController FRGCONTROLLER;
    private ResultText result, state;
    private EditText edittext_days, edittext_hours, edittext_minutes;
    private View dialog_timer, dialog_powerstrip, dialog_smartlight;
    private Handler handler = new Handler();
    private CheckBox checkbox_ch1, checkbox_ch2, checkbox_ch3, checkbox_ch4, checkbox_ch5, checkbox_ch6, checkbox_ch7, checkbox_ch8, smlight_ch1, smlight_ch2, smlight_ch3, smlight_ch4, smlight_ch5, smlight_ch6, smlight_ch7, smlight_ch8;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switch_ch1, switch_ch2, switch_ch3, switch_ch4, switch_ch5, switch_ch6, switch_ch7, switch_ch8, onoff_ch1, onoff_ch2, onoff_ch3, onoff_ch4, onoff_ch5, onoff_ch6, onoff_ch7, onoff_ch8;

    public FrgController()
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
        View rootView = inflater.inflate(R.layout.fragment_controller, container, false);
        ListView cmd_list = rootView.findViewById(R.id.cmd_list);
        EditText manual_cmd = rootView.findViewById(R.id.manualCmd);
        Button send = rootView.findViewById(R.id.btn_sendCmd);
        FRGCONTROLLER = this;

        if (SessionController.isConnected())
        {
            cmdSetup(cmd_list);
        }

        send.setOnClickListener(v ->
        {
            if (SessionController.isConnected())
            {
                if (!manual_cmd.getText().toString().isEmpty())
                {
                    if (manual_cmd.getText().toString().contains(getString(R.string.cmd_system_sudo)) || manual_cmd.getText().toString().contains(getString(R.string.cmd_system_reboot)) || manual_cmd.getText().toString().contains(getString(R.string.cmd_system_halt)) || manual_cmd.getText().toString().contains(getString(R.string.cmd_system_poweroff)) || manual_cmd.getText().toString().contains(" " + getString(R.string.cmd_system_su)) || manual_cmd.getText().toString().contains(getString(R.string.cmd_system_su) + " ") || manual_cmd.getText().toString().equals(getString(R.string.cmd_system_su)))
                    {
                        Toast.makeText(getActivity(), R.string.message_blocked, Toast.LENGTH_SHORT).show();
                    }

                    else
                    {
                        showResult();
                        SessionController.getSessionController().cmdExec(handler, result, manual_cmd.getText().toString());
                    }
                }

                else
                {
                    Toast.makeText(getActivity(), R.string.message_empty_cmd, Toast.LENGTH_SHORT).show();
                }
            }

            else
            {
                Toast.makeText(getActivity(), R.string.message_cmd_failed, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    private void cmdSetup(ListView lv)
    {
        if (getActivity() != null)
        {
            final ArrayList<String> cmdList = new ArrayList<>();
            @SuppressLint("Recycle") final Cursor cursor = DBHelper.getInstance(getActivity()).getReadableDatabase().rawQuery("select * from " + DBHelper.USER_COMMAND, null);
            while (cursor.moveToNext())
            {
                String cmdName = KeyGenerator.decrypt(cursor.getString(0));
                cmdList.add(cmdName);
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, cmdList);
            lv.setAdapter(arrayAdapter);
            lv.setOnItemClickListener((parent, view, position, id) ->
            {
                cursor.moveToPosition(position);

                if (KeyGenerator.decrypt(cursor.getString(0)).equals(getString(R.string.cmd_label_system_status)))
                {
                    showResult();
                    SessionController.getSessionController().cmdExec(handler, result, KeyGenerator.decrypt(cursor.getString(1)));
                }

                else if (KeyGenerator.decrypt(cursor.getString(0)).equals(getString(R.string.cmd_label_system_clean)))
                {
                    AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
                    db.setCancelable(false);
                    db.setTitle(R.string.title_warning);
                    db.setMessage(R.string.message_system_clean);
                    db.setPositiveButton(getString(R.string.button_yes), (dialog, which) ->
                    {
                        showResult();
                        SessionController.getSessionController().cmdExec(handler, result, KeyGenerator.decrypt(cursor.getString(1)) + SessionController.getSessionController().getSessionUserInfo().getPassword());
                    });
                    db.setNegativeButton(getString(R.string.button_no), (dialog, which) -> dialog.dismiss());
                    db.show();
                }

                else if (KeyGenerator.decrypt(cursor.getString(0)).equals(getString(R.string.cmd_label_powerstrip_controller)))
                {
                    AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
                    popupPowerstrip();

                    onoff_ch1.setOnClickListener(v ->
                    {
                        if (!onoff_ch1.isChecked())
                        {
                            SessionController.getSessionController().cmdExec(handler, state, KeyGenerator.decrypt(cursor.getString(1)) + "off-a " + SessionController.getSessionController().getSessionUserInfo().getPassword());
                        }

                        else
                        {
                            SessionController.getSessionController().cmdExec(handler, state, KeyGenerator.decrypt(cursor.getString(1)) + "on-a " + SessionController.getSessionController().getSessionUserInfo().getPassword());
                        }
                        SessionController.getSessionController().cmdExec(handler, state, getString(R.string.cmd_relay_state));
                    });

                    onoff_ch2.setOnClickListener(v ->
                    {
                        if (!onoff_ch2.isChecked())
                        {
                            SessionController.getSessionController().cmdExec(handler, state, KeyGenerator.decrypt(cursor.getString(1)) + "off-b " + SessionController.getSessionController().getSessionUserInfo().getPassword());
                        }

                        else
                        {
                            SessionController.getSessionController().cmdExec(handler, state, KeyGenerator.decrypt(cursor.getString(1)) + "on-b " + SessionController.getSessionController().getSessionUserInfo().getPassword());
                        }
                        SessionController.getSessionController().cmdExec(handler, state, getString(R.string.cmd_relay_state));
                    });

                    onoff_ch3.setOnClickListener(v ->
                    {
                        if (!onoff_ch3.isChecked())
                        {
                            SessionController.getSessionController().cmdExec(handler, state, KeyGenerator.decrypt(cursor.getString(1)) + "off-c " + SessionController.getSessionController().getSessionUserInfo().getPassword());
                        }

                        else
                        {
                            SessionController.getSessionController().cmdExec(handler, state, KeyGenerator.decrypt(cursor.getString(1)) + "on-c " + SessionController.getSessionController().getSessionUserInfo().getPassword());
                        }
                        SessionController.getSessionController().cmdExec(handler, state, getString(R.string.cmd_relay_state));
                    });

                    onoff_ch4.setOnClickListener(v ->
                    {
                        if (!onoff_ch4.isChecked())
                        {
                            SessionController.getSessionController().cmdExec(handler, state, KeyGenerator.decrypt(cursor.getString(1)) + "off-d " + SessionController.getSessionController().getSessionUserInfo().getPassword());
                        }

                        else
                        {
                            SessionController.getSessionController().cmdExec(handler, state, KeyGenerator.decrypt(cursor.getString(1)) + "on-d " + SessionController.getSessionController().getSessionUserInfo().getPassword());
                        }
                        SessionController.getSessionController().cmdExec(handler, state, getString(R.string.cmd_relay_state));
                    });

                    onoff_ch5.setOnClickListener(v ->
                    {
                        if (!onoff_ch5.isChecked())
                        {
                            SessionController.getSessionController().cmdExec(handler, state, KeyGenerator.decrypt(cursor.getString(1)) + "off-e " + SessionController.getSessionController().getSessionUserInfo().getPassword());
                        }

                        else
                        {
                            SessionController.getSessionController().cmdExec(handler, state, KeyGenerator.decrypt(cursor.getString(1)) + "on-e " + SessionController.getSessionController().getSessionUserInfo().getPassword());
                        }
                        SessionController.getSessionController().cmdExec(handler, state, getString(R.string.cmd_relay_state));
                    });

                    onoff_ch6.setOnClickListener(v ->
                    {
                        if (!onoff_ch6.isChecked())
                        {
                            SessionController.getSessionController().cmdExec(handler, state, KeyGenerator.decrypt(cursor.getString(1)) + "off-f " + SessionController.getSessionController().getSessionUserInfo().getPassword());
                        }

                        else
                        {
                            SessionController.getSessionController().cmdExec(handler, state, KeyGenerator.decrypt(cursor.getString(1)) + "on-f " + SessionController.getSessionController().getSessionUserInfo().getPassword());
                        }
                        SessionController.getSessionController().cmdExec(handler, state, getString(R.string.cmd_relay_state));
                    });

                    onoff_ch7.setOnClickListener(v ->
                    {
                        if (!onoff_ch7.isChecked())
                        {
                            SessionController.getSessionController().cmdExec(handler, state, KeyGenerator.decrypt(cursor.getString(1)) + "off-g " + SessionController.getSessionController().getSessionUserInfo().getPassword());
                        }

                        else
                        {
                            SessionController.getSessionController().cmdExec(handler, state, KeyGenerator.decrypt(cursor.getString(1)) + "on-g " + SessionController.getSessionController().getSessionUserInfo().getPassword());
                        }
                        SessionController.getSessionController().cmdExec(handler, state, getString(R.string.cmd_relay_state));
                    });

                    onoff_ch8.setOnClickListener(v ->
                    {
                        if (!onoff_ch8.isChecked())
                        {
                            SessionController.getSessionController().cmdExec(handler, state, KeyGenerator.decrypt(cursor.getString(1)) + "off-h " + SessionController.getSessionController().getSessionUserInfo().getPassword());
                        }

                        else
                        {
                            SessionController.getSessionController().cmdExec(handler, state, KeyGenerator.decrypt(cursor.getString(1)) + "on-h " + SessionController.getSessionController().getSessionUserInfo().getPassword());
                        }
                        SessionController.getSessionController().cmdExec(handler, state, getString(R.string.cmd_relay_state));
                    });

                    db.setCancelable(false);
                    db.setView(dialog_powerstrip);
                    db.setPositiveButton(getString(R.string.button_dismiss), (dialog, which) -> dialog.dismiss());
                    db.show();
                }

                else if (KeyGenerator.decrypt(cursor.getString(0)).equals(getString(R.string.cmd_label_powerstrip_timer)))
                {
                    AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
                    popupTimer();
                    db.setCancelable(false);
                    db.setView(dialog_timer);
                    db.setPositiveButton(getString(R.string.button_ok), (dialog, which) ->
                    {
                        if (!checkbox_ch1.isChecked() && !checkbox_ch2.isChecked() && !checkbox_ch3.isChecked() && !checkbox_ch4.isChecked() && !checkbox_ch5.isChecked() && !checkbox_ch6.isChecked() && !checkbox_ch7.isChecked() && !checkbox_ch8.isChecked())
                        {
                            Toast.makeText(getActivity(), R.string.message_option_null, Toast.LENGTH_SHORT).show();
                        }

                        else
                        {
                            int days = 0, hours = 0, minutes = 0, value;
                            if (!edittext_days.getText().toString().isEmpty())
                            {
                                days = Integer.parseInt(edittext_days.getText().toString()) * 24 * 60 * 60;
                            }

                            if (!edittext_hours.getText().toString().isEmpty())
                            {
                                hours = Integer.parseInt(edittext_hours.getText().toString()) * 60 * 60;
                            }

                            if (!edittext_minutes.getText().toString().isEmpty())
                            {
                                minutes = Integer.parseInt(edittext_minutes.getText().toString()) * 60;
                            }

                            value = minutes + hours + days;

                            if (value != 0)
                            {
                                showResult();
                                if (checkbox_ch1.isChecked())
                                {
                                    if (switch_ch1.isChecked())
                                    {
                                        SessionController.getSessionController().cmdExec(handler, result, KeyGenerator.decrypt(cursor.getString(1)) + "ontime-a " + SessionController.getSessionController().getSessionUserInfo().getPassword() + " " + value);
                                    }

                                    else
                                    {
                                        SessionController.getSessionController().cmdExec(handler, result, KeyGenerator.decrypt(cursor.getString(1)) + "offtime-a " + SessionController.getSessionController().getSessionUserInfo().getPassword() + " " + value);
                                    }
                                }

                                if (checkbox_ch2.isChecked())
                                {
                                    if (switch_ch2.isChecked())
                                    {
                                        SessionController.getSessionController().cmdExec(handler, result, KeyGenerator.decrypt(cursor.getString(1)) + "ontime-b " + SessionController.getSessionController().getSessionUserInfo().getPassword() + " " + value);
                                    }

                                    else
                                    {
                                        SessionController.getSessionController().cmdExec(handler, result, KeyGenerator.decrypt(cursor.getString(1)) + "offtime-b " + SessionController.getSessionController().getSessionUserInfo().getPassword() + " " + value);
                                    }
                                }

                                if (checkbox_ch3.isChecked())
                                {
                                    if (switch_ch3.isChecked())
                                    {
                                        SessionController.getSessionController().cmdExec(handler, result, KeyGenerator.decrypt(cursor.getString(1)) + "ontime-c " + SessionController.getSessionController().getSessionUserInfo().getPassword() + " " + value);
                                    }

                                    else
                                    {
                                        SessionController.getSessionController().cmdExec(handler, result, KeyGenerator.decrypt(cursor.getString(1)) + "offtime-c " + SessionController.getSessionController().getSessionUserInfo().getPassword() + " " + value);
                                    }
                                }

                                if (checkbox_ch4.isChecked())
                                {
                                    if (switch_ch4.isChecked())
                                    {
                                        SessionController.getSessionController().cmdExec(handler, result, KeyGenerator.decrypt(cursor.getString(1)) + "ontime-d " + SessionController.getSessionController().getSessionUserInfo().getPassword() + " " + value);
                                    }

                                    else
                                    {
                                        SessionController.getSessionController().cmdExec(handler, result, KeyGenerator.decrypt(cursor.getString(1)) + "offtime-d " + SessionController.getSessionController().getSessionUserInfo().getPassword() + " " + value);
                                    }
                                }

                                if (checkbox_ch5.isChecked())
                                {
                                    if (switch_ch5.isChecked())
                                    {
                                        SessionController.getSessionController().cmdExec(handler, result, KeyGenerator.decrypt(cursor.getString(1)) + "ontime-e " + SessionController.getSessionController().getSessionUserInfo().getPassword() + " " + value);
                                    }

                                    else
                                    {
                                        SessionController.getSessionController().cmdExec(handler, result, KeyGenerator.decrypt(cursor.getString(1)) + "offtime-e " + SessionController.getSessionController().getSessionUserInfo().getPassword() + " " + value);
                                    }
                                }

                                if (checkbox_ch6.isChecked())
                                {
                                    if (switch_ch6.isChecked())
                                    {
                                        SessionController.getSessionController().cmdExec(handler, result, KeyGenerator.decrypt(cursor.getString(1)) + "ontime-f " + SessionController.getSessionController().getSessionUserInfo().getPassword() + " " + value);
                                    }

                                    else
                                    {
                                        SessionController.getSessionController().cmdExec(handler, result, KeyGenerator.decrypt(cursor.getString(1)) + "offtime-f " + SessionController.getSessionController().getSessionUserInfo().getPassword() + " " + value);
                                    }
                                }

                                if (checkbox_ch7.isChecked())
                                {
                                    if (switch_ch7.isChecked())
                                    {
                                        SessionController.getSessionController().cmdExec(handler, result, KeyGenerator.decrypt(cursor.getString(1)) + "ontime-g " + SessionController.getSessionController().getSessionUserInfo().getPassword() + " " + value);
                                    }

                                    else
                                    {
                                        SessionController.getSessionController().cmdExec(handler, result, KeyGenerator.decrypt(cursor.getString(1)) + "offtime-g " + SessionController.getSessionController().getSessionUserInfo().getPassword() + " " + value);
                                    }
                                }

                                if (checkbox_ch8.isChecked())
                                {
                                    if (switch_ch8.isChecked())
                                    {
                                        SessionController.getSessionController().cmdExec(handler, result, KeyGenerator.decrypt(cursor.getString(1)) + "ontime-h " + SessionController.getSessionController().getSessionUserInfo().getPassword() + " " + value);
                                    }

                                    else
                                    {
                                        SessionController.getSessionController().cmdExec(handler, result, KeyGenerator.decrypt(cursor.getString(1)) + "offtime-h " + SessionController.getSessionController().getSessionUserInfo().getPassword() + " " + value);
                                    }
                                }
                            }

                            else
                            {
                                Toast.makeText(getActivity(), R.string.message_time_null, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    db.setNegativeButton(getString(R.string.button_cancel), (dialog, which) -> dialog.dismiss());
                    db.show();
                }

                else if (KeyGenerator.decrypt(cursor.getString(0)).equals(getString(R.string.cmd_label_smartlight)))
                {
                    AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                    popupSmartlight();
                    adb.setView(dialog_smartlight);
                    adb.setCancelable(false);
                    adb.setPositiveButton(R.string.button_active, (ad, w) ->
                    {
                        if (!smlight_ch1.isChecked() && !smlight_ch2.isChecked() && !smlight_ch3.isChecked() && !smlight_ch4.isChecked() && !smlight_ch5.isChecked() && !smlight_ch6.isChecked() && !smlight_ch7.isChecked() && !smlight_ch8.isChecked())
                        {
                            Toast.makeText(getActivity(), R.string.message_option_null, Toast.LENGTH_SHORT).show();
                        }

                        else
                        {
                            showResult();
                            String a = " n ", b = "n ", c = "n ", d = "n ", e = "n ", f = "n ", g = "n ", h = "n";
                            if (smlight_ch1.isChecked())
                            {
                                a = " y ";
                            }

                            if (smlight_ch2.isChecked()) {
                                b = "y ";
                            }

                            if (smlight_ch3.isChecked())
                            {
                                c = "y ";
                            }

                            if (smlight_ch4.isChecked())
                            {
                                d = "y ";
                            }

                            if (smlight_ch5.isChecked())
                            {
                                e = "y ";
                            }

                            if (smlight_ch6.isChecked())
                            {
                                f = "y ";
                            }

                            if (smlight_ch7.isChecked())
                            {
                                g = "y ";
                            }

                            if (smlight_ch8.isChecked())
                            {
                                h = "y";
                            }
                            SessionController.getSessionController().cmdExec(handler, result, KeyGenerator.decrypt(cursor.getString(1)) + SessionController.getSessionController().getSessionUserInfo().getPassword() + a + b + c + d + e + f + g + h);
                        }
                    });

                    adb.setNegativeButton(R.string.button_deactive, (ad, w) ->
                    {
                        showResult();
                        SessionController.getSessionController().cmdExec(handler, result, getString(R.string.cmd_smartlight_off) + SessionController.getSessionController().getSessionUserInfo().getPassword());
                    });
                    adb.show();
                }

                else
                {
                    showResult();
                    SessionController.getSessionController().cmdExec(handler, result, KeyGenerator.decrypt(cursor.getString(1)) + SessionController.getSessionController().getSessionUserInfo().getPassword());
                }
            });
        }
    }

    @SuppressLint("InflateParams")
    private void showResult()
    {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View dialogResult = layoutInflater.inflate(R.layout.popup_resulttext, null);
        result = dialogResult.findViewById(R.id.result_text);
        if (getActivity() != null)
        {
            AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
            db.setCancelable(false);
            db.setView(dialogResult);
            db.setTitle(R.string.title_result);
            db.setPositiveButton(getString(R.string.button_dismiss), (dialog, which) -> dialog.dismiss());
            db.show();
        }
    }

    @SuppressLint("InflateParams")
    private void popupPowerstrip()
    {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        dialog_powerstrip = layoutInflater.inflate(R.layout.popup_powerstrip, null);
        state = dialog_powerstrip.findViewById(R.id.powerstrip_state);
        SessionController.getSessionController().cmdExec(handler, state, getString(R.string.cmd_relay_state));
        onoff_ch1 = dialog_powerstrip.findViewById(R.id.onoff_ch1);
        onoff_ch2 = dialog_powerstrip.findViewById(R.id.onoff_ch2);
        onoff_ch3 = dialog_powerstrip.findViewById(R.id.onoff_ch3);
        onoff_ch4 = dialog_powerstrip.findViewById(R.id.onoff_ch4);
        onoff_ch5 = dialog_powerstrip.findViewById(R.id.onoff_ch5);
        onoff_ch6 = dialog_powerstrip.findViewById(R.id.onoff_ch6);
        onoff_ch7 = dialog_powerstrip.findViewById(R.id.onoff_ch7);
        onoff_ch8 = dialog_powerstrip.findViewById(R.id.onoff_ch8);

        new Thread(() ->
        {
            int count = 0;
            while (count <= state.getLineCount())
            {
                count += 1;
                if (Objects.requireNonNull(state.getText()).toString().contains("Channel A - ON"))
                {
                    handler.post(() -> onoff_ch1.setChecked(true));
                }

                if (Objects.requireNonNull(state.getText()).toString().contains("Channel B - ON"))
                {
                    handler.post(() -> onoff_ch2.setChecked(true));
                }

                if (Objects.requireNonNull(state.getText()).toString().contains("Channel C - ON"))
                {
                    handler.post(() -> onoff_ch3.setChecked(true));
                }

                if (Objects.requireNonNull(state.getText()).toString().contains("Channel D - ON"))
                {
                    handler.post(() -> onoff_ch4.setChecked(true));
                }

                if (Objects.requireNonNull(state.getText()).toString().contains("Channel E - ON"))
                {
                    handler.post(() -> onoff_ch5.setChecked(true));
                }

                if (Objects.requireNonNull(state.getText()).toString().contains("Channel F - ON"))
                {
                    handler.post(() -> onoff_ch6.setChecked(true));
                }

                if (Objects.requireNonNull(state.getText()).toString().contains("Channel G - ON"))
                {
                    handler.post(() -> onoff_ch7.setChecked(true));
                }

                if (Objects.requireNonNull(state.getText()).toString().contains("Channel H - ON"))
                {
                    handler.post(() -> onoff_ch8.setChecked(true));
                }

                try
                {
                    Thread.sleep(300);
                }

                catch (InterruptedException ignore)
                {

                }
            }
        }).start();
    }

    @SuppressLint("InflateParams")
    private void popupTimer()
    {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        dialog_timer = layoutInflater.inflate(R.layout.popup_timer, null);
        checkbox_ch1 = dialog_timer.findViewById(R.id.checkbox_ch1);
        checkbox_ch2 = dialog_timer.findViewById(R.id.checkbox_ch2);
        checkbox_ch3 = dialog_timer.findViewById(R.id.checkbox_ch3);
        checkbox_ch4 = dialog_timer.findViewById(R.id.checkbox_ch4);
        checkbox_ch5 = dialog_timer.findViewById(R.id.checkbox_ch5);
        checkbox_ch6 = dialog_timer.findViewById(R.id.checkbox_ch6);
        checkbox_ch7 = dialog_timer.findViewById(R.id.checkbox_ch7);
        checkbox_ch8 = dialog_timer.findViewById(R.id.checkbox_ch8);
        switch_ch1 = dialog_timer.findViewById(R.id.switch_ch1);
        switch_ch2 = dialog_timer.findViewById(R.id.switch_ch2);
        switch_ch3 = dialog_timer.findViewById(R.id.switch_ch3);
        switch_ch4 = dialog_timer.findViewById(R.id.switch_ch4);
        switch_ch5 = dialog_timer.findViewById(R.id.switch_ch5);
        switch_ch6 = dialog_timer.findViewById(R.id.switch_ch6);
        switch_ch7 = dialog_timer.findViewById(R.id.switch_ch7);
        switch_ch8 = dialog_timer.findViewById(R.id.switch_ch8);
        edittext_days = dialog_timer.findViewById(R.id.edittext_days);
        edittext_hours = dialog_timer.findViewById(R.id.edittext_hours);
        edittext_minutes = dialog_timer.findViewById(R.id.edittext_minutes);
    }

    @SuppressLint("InflateParams")
    private void popupSmartlight()
    {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        dialog_smartlight = layoutInflater.inflate(R.layout.popup_smartlight, null);
        smlight_ch1 = dialog_smartlight.findViewById(R.id.checkbox_slch1);
        smlight_ch2 = dialog_smartlight.findViewById(R.id.checkbox_slch2);
        smlight_ch3 = dialog_smartlight.findViewById(R.id.checkbox_slch3);
        smlight_ch4 = dialog_smartlight.findViewById(R.id.checkbox_slch4);
        smlight_ch5 = dialog_smartlight.findViewById(R.id.checkbox_slch5);
        smlight_ch6 = dialog_smartlight.findViewById(R.id.checkbox_slch6);
        smlight_ch7 = dialog_smartlight.findViewById(R.id.checkbox_slch7);
        smlight_ch8 = dialog_smartlight.findViewById(R.id.checkbox_slch8);
    }

    public static FrgController getInstance()
    {
        return FRGCONTROLLER;
    }

    public void reloadFragment()
    {
        FrgController frg = this;
        if (getFragmentManager() != null)
        {
            getFragmentManager().beginTransaction().detach(frg).attach(frg).commitNowAllowingStateLoss();
        }
    }
}
