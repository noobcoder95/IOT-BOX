package com.smartiotdevices.iotbox;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.smartiotdevices.iotbox.sshutils.SessionController;

import java.util.Objects;

public class FrgAvaTalk extends Fragment
{
    EditText message;

    public FrgAvaTalk()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint({"ClickableViewAccessibility", "ShowToast"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_ava, container, false);
        message = rootView.findViewById(R.id.edittext_message);
        Button input_msg = rootView.findViewById(R.id.button_talk_ava);
        Button repeat_msg = rootView.findViewById(R.id.button_repeat_ava);
        Button stop_ava = rootView.findViewById(R.id.button_stop_ava);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        Objects.requireNonNull(getActivity()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        message.setOnClickListener(v ->
        {
            if (!message.getText().toString().isEmpty())
            {
                message.setText("");
            }
        });

        input_msg.setOnClickListener(v ->
        {
            if (!message.getText().toString().isEmpty())
            {
                processInput();
                message.setText("");
            }

            else
            {
                try
                {
                    startActivityForResult(intent, 1666);
                }

                catch (ActivityNotFoundException a)
                {
                    Toast.makeText(getActivity(), R.string.message_doesnt_support_stt, Toast.LENGTH_LONG);
                }
            }
        });

        repeat_msg.setOnClickListener(v ->
        {
            if (!message.getText().toString().isEmpty())
            {
                SessionController.getSessionController().x11Shell(getString(R.string.cmd_tts_engine) + message.getText().toString().replaceAll("\n", " ") + " &");
                message.setText("");
            }

            else
            {
                try
                {
                    startActivityForResult(intent, 2666);
                }

                catch (ActivityNotFoundException a)
                {
                    Toast.makeText(getActivity(), R.string.message_doesnt_support_stt, Toast.LENGTH_LONG);
                }
            }
        });

        stop_ava.setOnClickListener(v ->
        {
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_kill_voice_engine));
            if (!message.getText().toString().isEmpty())
            {
                message.setText("");
            }
        });

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1666)
        {
            if (resultCode == ActivityMain.RESULT_OK && data != null)
            {
                message.setText("");
                String result = Objects.requireNonNull(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)).get(0);
                message.setText(result);
                processInput();
            }
        }

        else if (requestCode == 2666)
        {
            if (resultCode == ActivityMain.RESULT_OK && data != null)
            {
                message.setText("");
                String result = Objects.requireNonNull(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)).get(0);
                message.setText(result.toLowerCase());
                SessionController.getSessionController().x11Shell(getString(R.string.cmd_tts_engine) + message.getText().toString().replaceAll("\n", " ") + " &");
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_admin,menu);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    private void processInput()
    {
        if (message.getText().toString().toLowerCase().contains("turn on") && message.getText().toString().toLowerCase().contains("channel a") || message.getText().toString().toLowerCase().contains("channel a") && message.getText().toString().toLowerCase().contains("turn on") || message.getText().toString().toLowerCase().contains("turn on") && message.getText().toString().toLowerCase().contains("channel number 1") || message.getText().toString().toLowerCase().contains("channel number 1") && message.getText().toString().toLowerCase().contains("turn on"))
        {
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_tts_engine) + "Okay. Right away" + " &");
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_relay) + "on-a " + SessionController.getSessionController().getSessionUserInfo().getPassword());
        }

        else if (message.getText().toString().toLowerCase().contains("turn on") && message.getText().toString().toLowerCase().contains("channel b") || message.getText().toString().toLowerCase().contains("channel b") && message.getText().toString().toLowerCase().contains("turn on") || message.getText().toString().toLowerCase().contains("turn on") && message.getText().toString().toLowerCase().contains("channel number 2") || message.getText().toString().toLowerCase().contains("channel number 2") && message.getText().toString().toLowerCase().contains("turn on"))
        {
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_tts_engine) + "Okay. Right away" + " &");
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_relay) + "on-b " + SessionController.getSessionController().getSessionUserInfo().getPassword());
        }

        else if (message.getText().toString().toLowerCase().contains("turn on") && message.getText().toString().toLowerCase().contains("channel c") || message.getText().toString().toLowerCase().contains("channel c") && message.getText().toString().toLowerCase().contains("turn on") || message.getText().toString().toLowerCase().contains("turn on") && message.getText().toString().toLowerCase().contains("channel number 3") || message.getText().toString().toLowerCase().contains("channel number 3") && message.getText().toString().toLowerCase().contains("turn on"))
        {
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_tts_engine) + "Okay. Right away" + " &");
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_relay) + "on-c " + SessionController.getSessionController().getSessionUserInfo().getPassword());
        }

        else if (message.getText().toString().toLowerCase().contains("turn on") && message.getText().toString().toLowerCase().contains("channel d") || message.getText().toString().toLowerCase().contains("channel d") && message.getText().toString().toLowerCase().contains("turn on") || message.getText().toString().toLowerCase().contains("turn on") && message.getText().toString().toLowerCase().contains("channel number 4") || message.getText().toString().toLowerCase().contains("channel number 4") && message.getText().toString().toLowerCase().contains("turn on"))
        {
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_tts_engine) + "Okay. Right away" + " &");
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_relay) + "on-d " + SessionController.getSessionController().getSessionUserInfo().getPassword());
        }

        else if (message.getText().toString().toLowerCase().contains("turn on") && message.getText().toString().toLowerCase().contains("channel e") || message.getText().toString().toLowerCase().contains("channel e") && message.getText().toString().toLowerCase().contains("turn on") || message.getText().toString().toLowerCase().contains("turn on") && message.getText().toString().toLowerCase().contains("channel number 5") || message.getText().toString().toLowerCase().contains("channel number 5") && message.getText().toString().toLowerCase().contains("turn on"))
        {
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_tts_engine) + "Okay. Right away" + " &");
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_relay) + "on-e " + SessionController.getSessionController().getSessionUserInfo().getPassword());
        }

        else if (message.getText().toString().toLowerCase().contains("turn on") && message.getText().toString().toLowerCase().contains("channel f") || message.getText().toString().toLowerCase().contains("channel f") && message.getText().toString().toLowerCase().contains("turn on") || message.getText().toString().toLowerCase().contains("turn on") && message.getText().toString().toLowerCase().contains("channel number 6") || message.getText().toString().toLowerCase().contains("channel number 6") && message.getText().toString().toLowerCase().contains("turn on"))
        {
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_tts_engine) + "Okay. Right away" + " &");
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_relay) + "on-f " + SessionController.getSessionController().getSessionUserInfo().getPassword());
        }

        else if (message.getText().toString().toLowerCase().contains("turn on") && message.getText().toString().toLowerCase().contains("channel g") || message.getText().toString().toLowerCase().contains("channel g") && message.getText().toString().toLowerCase().contains("turn on") || message.getText().toString().toLowerCase().contains("turn on") && message.getText().toString().toLowerCase().contains("channel number 7") || message.getText().toString().toLowerCase().contains("channel number 7") && message.getText().toString().toLowerCase().contains("turn on"))
        {
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_tts_engine) + "Okay. Right away" + " &");
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_relay) + "on-g " + SessionController.getSessionController().getSessionUserInfo().getPassword());
        }

        else if (message.getText().toString().toLowerCase().contains("turn on") && message.getText().toString().toLowerCase().contains("channel h") || message.getText().toString().toLowerCase().contains("channel h") && message.getText().toString().toLowerCase().contains("turn on") || message.getText().toString().toLowerCase().contains("turn on") && message.getText().toString().toLowerCase().contains("channel number 8") || message.getText().toString().toLowerCase().contains("channel number 8") && message.getText().toString().toLowerCase().contains("turn on"))
        {
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_tts_engine) + "Okay. Right away" + " &");
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_relay) + "on-h " + SessionController.getSessionController().getSessionUserInfo().getPassword());
        }

        else if (message.getText().toString().toLowerCase().contains("turn on") && message.getText().toString().toLowerCase().contains("all channel"))
        {
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_tts_engine) + "Okay. Right away" + " &");
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_relay) + "on " + SessionController.getSessionController().getSessionUserInfo().getPassword());
        }

        else if (message.getText().toString().toLowerCase().contains("turn off") && message.getText().toString().toLowerCase().contains("channel a") || message.getText().toString().toLowerCase().contains("channel a") && message.getText().toString().toLowerCase().contains("turn off") || message.getText().toString().toLowerCase().contains("turn off") && message.getText().toString().toLowerCase().contains("channel number 1") || message.getText().toString().toLowerCase().contains("channel number 1") && message.getText().toString().toLowerCase().contains("turn off"))
        {
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_tts_engine) + "Okay. Right away" + " &");
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_relay) + "off-a " + SessionController.getSessionController().getSessionUserInfo().getPassword());
        }

        else if (message.getText().toString().toLowerCase().contains("turn off") && message.getText().toString().toLowerCase().contains("channel b") || message.getText().toString().toLowerCase().contains("channel b") && message.getText().toString().toLowerCase().contains("turn off") || message.getText().toString().toLowerCase().contains("turn off") && message.getText().toString().toLowerCase().contains("channel number 2") || message.getText().toString().toLowerCase().contains("channel number 2") && message.getText().toString().toLowerCase().contains("turn off"))
        {
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_tts_engine) + "Okay. Right away" + " &");
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_relay) + "off-b " + SessionController.getSessionController().getSessionUserInfo().getPassword());
        }

        else if (message.getText().toString().toLowerCase().contains("turn off") && message.getText().toString().toLowerCase().contains("channel c") || message.getText().toString().toLowerCase().contains("channel c") && message.getText().toString().toLowerCase().contains("turn off") || message.getText().toString().toLowerCase().contains("turn off") && message.getText().toString().toLowerCase().contains("channel number 3") || message.getText().toString().toLowerCase().contains("channel number 3") && message.getText().toString().toLowerCase().contains("turn off"))
        {
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_tts_engine) + "Okay. Right away" + " &");
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_relay) + "off-c " + SessionController.getSessionController().getSessionUserInfo().getPassword());
        }

        else if (message.getText().toString().toLowerCase().contains("turn off") && message.getText().toString().toLowerCase().contains("channel d") || message.getText().toString().toLowerCase().contains("channel d") && message.getText().toString().toLowerCase().contains("turn off") || message.getText().toString().toLowerCase().contains("turn off") && message.getText().toString().toLowerCase().contains("channel number 4") || message.getText().toString().toLowerCase().contains("channel number 4") && message.getText().toString().toLowerCase().contains("turn off"))
        {
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_tts_engine) + "Okay. Right away" + " &");
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_relay) + "off-d " + SessionController.getSessionController().getSessionUserInfo().getPassword());
        }

        else if (message.getText().toString().toLowerCase().contains("turn off") && message.getText().toString().toLowerCase().contains("channel e") || message.getText().toString().toLowerCase().contains("channel e") && message.getText().toString().toLowerCase().contains("turn off") || message.getText().toString().toLowerCase().contains("turn off") && message.getText().toString().toLowerCase().contains("channel number 5") || message.getText().toString().toLowerCase().contains("channel number 5") && message.getText().toString().toLowerCase().contains("turn off"))
        {
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_tts_engine) + "Okay. Right away" + " &");
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_relay) + "off-e " + SessionController.getSessionController().getSessionUserInfo().getPassword());
        }

        else if (message.getText().toString().toLowerCase().contains("turn off") && message.getText().toString().toLowerCase().contains("channel f") || message.getText().toString().toLowerCase().contains("channel f") && message.getText().toString().toLowerCase().contains("turn off") || message.getText().toString().toLowerCase().contains("turn off") && message.getText().toString().toLowerCase().contains("channel number 6") || message.getText().toString().toLowerCase().contains("channel number 6") && message.getText().toString().toLowerCase().contains("turn off"))
        {
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_tts_engine) + "Okay. Right away" + " &");
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_relay) + "off-f " + SessionController.getSessionController().getSessionUserInfo().getPassword());
        }

        else if (message.getText().toString().toLowerCase().contains("turn off") && message.getText().toString().toLowerCase().contains("channel g") || message.getText().toString().toLowerCase().contains("channel g") && message.getText().toString().toLowerCase().contains("turn off") || message.getText().toString().toLowerCase().contains("turn off") && message.getText().toString().toLowerCase().contains("channel number 7") || message.getText().toString().toLowerCase().contains("channel number 7") && message.getText().toString().toLowerCase().contains("turn off"))
        {
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_tts_engine) + "Okay. Right away" + " &");
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_relay) + "off-g " + SessionController.getSessionController().getSessionUserInfo().getPassword());
        }

        else if (message.getText().toString().toLowerCase().contains("turn off") && message.getText().toString().toLowerCase().contains("channel h") || message.getText().toString().toLowerCase().contains("channel h") && message.getText().toString().toLowerCase().contains("turn off") || message.getText().toString().toLowerCase().contains("turn off") && message.getText().toString().toLowerCase().contains("channel number 8") || message.getText().toString().toLowerCase().contains("channel number 8") && message.getText().toString().toLowerCase().contains("turn off"))
        {
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_tts_engine) + "Okay. Right away" + " &");
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_relay) + "off-h " + SessionController.getSessionController().getSessionUserInfo().getPassword());
        }

        else if (message.getText().toString().toLowerCase().contains("turn off") && message.getText().toString().toLowerCase().contains("all channel"))
        {
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_tts_engine) + "Okay. Right away" + " &");
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_relay) + "off " + SessionController.getSessionController().getSessionUserInfo().getPassword());
        }

        else
        {
            SessionController.getSessionController().x11Shell(getString(R.string.cmd_ava_engine) + message.getText().toString().replaceAll("\n", " ") + " &");
        }
    }
}
