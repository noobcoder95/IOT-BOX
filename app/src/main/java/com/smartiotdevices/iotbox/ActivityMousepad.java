package com.smartiotdevices.iotbox;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smartiotdevices.iotbox.mousepadcomponent.CustomKeyboardButton;
import com.smartiotdevices.iotbox.mousepadcomponent.CustomKeyboardButtonAdapter;
import com.smartiotdevices.iotbox.mousepadcomponent.DragAndDropGridView;
import com.smartiotdevices.iotbox.mousepadcomponent.MyMouseView;
import com.smartiotdevices.iotbox.mousepadcomponent.SimpleScrollingStrategy;
import com.smartiotdevices.iotbox.mousepadcomponent.SpanVariableGridView;
import com.smartiotdevices.iotbox.sshutils.SessionController;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ActivityMousepad extends AppCompatActivity implements DragAndDropGridView.DragAndDropListener, SpanVariableGridView.OnItemClickListener, SpanVariableGridView.OnItemLongClickListener, SensorEventListener
{
	MenuItem key_lock_button = null;
	MenuItem invert_scroll_button = null;

	RelativeLayout mouse_layout;
	LinearLayout keyboard_layout;

	@SuppressLint("StaticFieldLeak")
    static ActivityMousepad ACTIVITY_MOUSEPAD;
	public static float SETTING_SENSITIVITY;
	private static boolean SETTING_KEYBOARD_LOCKED;
	private static boolean SETTING_KEYBOARD_BATCH;
	private boolean SETTING_KEYBOARD_AUTOCLEAR;
	public static boolean SETTING_INVERT_SCROLL;

    private EditText edit_text;
	private CustomKeyboardButtonAdapter item_adapter;
	private DragAndDropGridView drag_drop_grid_view;
	private List<CustomKeyboardButton> list_item = new LinkedList<>();
	private String key_layout_filename = "keyLayoutFile.csv";
	private RelativeLayout edit_keyboard_layout;
	private LinearLayout edit_text_layout;
	private int potential_delete_position;
	private int start_position;
	private FloatingActionButton fab;

	static
    {
		Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
	}

	public void xMouseClickMouse(View v)
    {
		String cmd ="";
		switch(v.getId())
        {
			case R.id.first_mouse_button:
				cmd = getString(R.string.cmd_mousepad_click) + " " + "1";
				break;
			case R.id.second_mouse_button:
				cmd = getString(R.string.cmd_mousepad_click) + " " + "2";
				break;
			case R.id.third_mouse_button:
				cmd = getString(R.string.cmd_mousepad_click) + " " + "3";
				break;
			case R.id.fourth_mouse_button:
				cmd = getString(R.string.cmd_mousepad_click) + " " + "4";
				break;
			case R.id.fifth_mouse_button:
				cmd = getString(R.string.cmd_mousepad_click) + " " + "5";
				break;
			default:
				break;
		}
        SessionController.getSessionController().x11Shell(cmd);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
    {
		getMenuInflater().inflate(R.menu.menu_mousepad_settings, menu);
		key_lock_button = menu.findItem(R.id.action_lock_keys);
		invert_scroll_button = menu.findItem(R.id.action_invert_scroll);
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mousepad);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

		SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		if (sensorManager != null)
		{
			Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
			sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
		}
		fab = findViewById(R.id.fab);
		fab.setOnClickListener((v) ->
        {
            if (keyboard_layout.getVisibility() == View.VISIBLE)
            {
                mouse_layout.setVisibility(View.VISIBLE);
                keyboard_layout.setVisibility(View.INVISIBLE);
                fab.setImageResource(R.drawable.ic_action_hardware_keyboard);
            }
            else
            {
                keyboard_layout.setVisibility(View.VISIBLE);
                mouse_layout.setVisibility(View.INVISIBLE);
                fab.setImageResource(R.drawable.ic_action_hardware_mouse);
            }
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null)
			{
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			}
		});

		mouse_layout = findViewById(R.id.mouse);
		keyboard_layout = findViewById(R.id.keyboard);
		mouse_layout.addView(new MyMouseView(getBaseContext()));
		ScrollView scrollView = findViewById(R.id.key_drag_scrollView);
		drag_drop_grid_view = findViewById(R.id.key_drag_DragAndDropGridView);
		list_item = loadKeyboardLayout(this,false);
		item_adapter = new CustomKeyboardButtonAdapter(this, list_item);
		drag_drop_grid_view.setAdapter(item_adapter);
		drag_drop_grid_view.setScrollingStrategy(new SimpleScrollingStrategy(scrollView));
		drag_drop_grid_view.setDragAndDropListener(this);
		drag_drop_grid_view.setOnItemLongClickListener(this);
		edit_text = findViewById(R.id.keyboard_input);
		edit_text.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void afterTextChanged(Editable s)
			{
				if(!ActivityMousepad.SETTING_KEYBOARD_BATCH)
				{
					useKeyboardSendText();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start,  int before, int count) {}
		});

		edit_keyboard_layout = findViewById(R.id.editKeyboardButtonsLayout);
		edit_keyboard_layout.setVisibility(View.GONE);
		edit_text_layout = findViewById(R.id.keyboard_send_layout);

		ACTIVITY_MOUSEPAD = this;
	}

	public void getPreferences()
	{
		try
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			SETTING_SENSITIVITY = Float.parseFloat(Objects.requireNonNull(prefs.getString("sensitivity_list", "1.0f")));
			SETTING_KEYBOARD_AUTOCLEAR = prefs.getBoolean("keyboard_autoclear", true);
			SETTING_KEYBOARD_BATCH = prefs.getBoolean("setting_keyboard_batch",false);
			SETTING_KEYBOARD_LOCKED = prefs.getBoolean("keyboard_layout_locked",true);
			SETTING_INVERT_SCROLL = prefs.getBoolean("mouse_invert_scroll",true);

		}

		catch(Exception e)
		{
			e.printStackTrace();
			Toast.makeText(this, "There was a problem retrieving your settings: "+e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	public void xMouseKeyboardSend(View v)
	{
		useKeyboardSendText();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		getPreferences();
        SessionController.getSessionController().openX11Shell();
    }

    protected void onResume()
    {
        super.onResume();
        getPreferences();

        if(drag_drop_grid_view !=null)
        {
            item_adapter = new CustomKeyboardButtonAdapter(ActivityMousepad.this, list_item);
            drag_drop_grid_view.setAdapter(item_adapter);
            item_adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause()
    {
        key_layout_filename = "keyLayoutFile.csv";
        saveKeyboardLayout();
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch(keyCode)
        {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                event.startTracking();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        switch(keyCode)
        {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if(event.isTracking() && !event.isCanceled())
                {
                    SessionController.getSessionController().x11Shell(getString(R.string.cmd_mousepad_key) + " " + "XF86AudioRaiseVolume");
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if(event.isTracking() && !event.isCanceled())
                {
                    SessionController.getSessionController().x11Shell(getString(R.string.cmd_mousepad_key) + " " + "XF86AudioLowerVolume");
                }
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

	@Override
	public void onBackPressed()
	{
		ExitDialog();
	}

    private void ExitDialog()
    {
        AlertDialog.Builder ab = new AlertDialog.Builder(ActivityMousepad.this);
        ab.setCancelable(false);
        ab.setTitle(getString(R.string.title_exit));
        ab.setMessage(getString(R.string.message_exit));
        ab.setPositiveButton(getString(R.string.button_yes), (dialog, which) -> finish());
        ab.setNegativeButton(getString(R.string.button_no), (dialog, which) -> dialog.dismiss());
        ab.show();
    }

    public void confirmLayoutReload(String title, String msg,final boolean def)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("OK", (dialog, which) ->
        {
            list_item = loadKeyboardLayout(ActivityMousepad.this,def);
            item_adapter = new CustomKeyboardButtonAdapter(ActivityMousepad.this, list_item);
            drag_drop_grid_view.setAdapter(item_adapter);
            item_adapter.notifyDataSetChanged();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        if(key_lock_button!=null)
        {
            key_lock_button.setChecked(SETTING_KEYBOARD_LOCKED);
        }

        if(invert_scroll_button!=null)
        {
            invert_scroll_button.setChecked(SETTING_INVERT_SCROLL);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_settings:
                Intent intent = new Intent(this, ActivityMousepadSettings.class);
                startActivity(intent);
                break;
            case R.id.action_restore_default_keys:
                key_layout_filename = "keyLayoutFile.csv";
                confirmLayoutReload(getString(R.string.mousepad_restore_title),getString(R.string.mousepad_restore_msg),true);
                break;
            case R.id.action_lock_keys:
                SETTING_KEYBOARD_LOCKED = !SETTING_KEYBOARD_LOCKED;
                item.setChecked(SETTING_KEYBOARD_LOCKED);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("keyboard_layout_locked", SETTING_KEYBOARD_LOCKED);
                editor.apply();
                break;
            case R.id.action_invert_scroll:
                SETTING_INVERT_SCROLL=!SETTING_INVERT_SCROLL;
                item.setChecked(SETTING_INVERT_SCROLL);
                SharedPreferences prefs2 = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor2 = prefs2.edit();
                editor2.putBoolean("mouse_invert_scroll", SETTING_INVERT_SCROLL);
                editor2.apply();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
    {
        if(!ActivityMousepad.SETTING_KEYBOARD_LOCKED)
        {
            drag_drop_grid_view.startDragAndDrop();
            edit_keyboard_layout.setVisibility(View.VISIBLE);
            edit_text_layout.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
    {

    }

    @Override
    public void onDragItem(int from)
    {

    }

    @Override
    public void onDraggingItem(int from, int to)
    {

    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onDropItem(int from, int to)
    {
        if (from != to)
        {
            list_item.add(to, list_item.remove(from));
            item_adapter.notifyDataSetChanged();
        }

        potential_delete_position = to;
        start_position = from;
        edit_keyboard_layout.setVisibility(View.GONE);
        fab.setVisibility(View.VISIBLE);
        edit_text_layout.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean isDragAndDropEnabled(int position)
    {
        return true;
    }

    @Override
    public void onEditItem(int mDragPosition)
    {
        onDropItem(potential_delete_position,start_position);
    }
    @Override
    public void onDeleteItem(int mDragPosition)
    {
        try
        {
            list_item.remove(potential_delete_position);
            item_adapter.notifyDataSetChanged();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getBaseContext(),"Error during delete",Toast.LENGTH_LONG).show();
            item_adapter.notifyDataSetChanged();
        }

        Toast.makeText(getBaseContext(),"Remove item "+potential_delete_position,Toast.LENGTH_LONG).show();
    }

    public LinkedList<CustomKeyboardButton> loadKeyboardLayout(Context mCont,boolean def)
    {
        if(def)
        {
            return loadDefaultKeyboardLayout();
        }

        LinkedList<CustomKeyboardButton> views = new LinkedList<>();
        FileInputStream fis;
        try
        {
            fis = mCont.openFileInput(key_layout_filename);
            try
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null)
                {
                    if(line.contains("<xmousesep>"))
                    {
                        String[] temp =line.split("<xmousesep>");
                        if(temp.length>3)
                        {
                            views.add(new CustomKeyboardButton(Integer.parseInt(temp[2]), temp[0], temp[1],temp[3]));
                        }
                        else
                        {
                            views.add(new CustomKeyboardButton(Integer.parseInt(temp[2]), temp[0], temp[1],"#FFFFFF"));
                        }
                    }
                    else
                    {
                        String[] temp =line.split(",");
                        views.add(new CustomKeyboardButton(Integer.parseInt(temp[2]), temp[0], temp[1],"#FFFFFF"));
                    }
                }
                fis.close();
            }
            catch(OutOfMemoryError | Exception om)
            {
                om.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if(views.size()<1)
        {
            views = loadDefaultKeyboardLayout();
        }

        return views;
    }

    public LinkedList<CustomKeyboardButton> loadDefaultKeyboardLayout()
    {
        LinkedList<CustomKeyboardButton> views = new LinkedList<>();
        views.add(new CustomKeyboardButton(2,"Esc",getString(R.string.cmd_mousepad_key) + " " + "Escape","#FF9999"));
        views.add(new CustomKeyboardButton(3,"Minimize",getString(R.string.cmd_mousepad_windowmin),"#9FFF80"));
        views.add(new CustomKeyboardButton(3,"Maximize",getString(R.string.cmd_mousepad_window_max),"#FFCC66"));
        views.add(new CustomKeyboardButton(2,"X",getString(R.string.cmd_mousepad_key) + " " + "alt+F4","#FF9999"));
        views.add(new CustomKeyboardButton(2,"F1",getString(R.string.cmd_mousepad_key) + " " + "F1","#FFFFFF"));
        views.add(new CustomKeyboardButton(2,"F2",getString(R.string.cmd_mousepad_key) + " " + "F2","#FFFFFF"));
        views.add(new CustomKeyboardButton(2,"F3",getString(R.string.cmd_mousepad_key) + " " + "F3","#FFFFFF"));
        views.add(new CustomKeyboardButton(2,"F4",getString(R.string.cmd_mousepad_key) + " " + "F4","#FFFFFF"));
        views.add(new CustomKeyboardButton(2,"F5",getString(R.string.cmd_mousepad_key) + " " + "F5","#FFFFFF"));
        views.add(new CustomKeyboardButton(2,"F6",getString(R.string.cmd_mousepad_key) + " " + "F6","#FFFFFF"));
        views.add(new CustomKeyboardButton(2,"F7",getString(R.string.cmd_mousepad_key) + " " + "F7","#FFFFFF"));
        views.add(new CustomKeyboardButton(2,"F8",getString(R.string.cmd_mousepad_key) + " " + "F8","#FFFFFF"));
        views.add(new CustomKeyboardButton(2,"F9",getString(R.string.cmd_mousepad_key) + " " + "F9","#FFFFFF"));
        views.add(new CustomKeyboardButton(2,"F10",getString(R.string.cmd_mousepad_key) + " " + "F10","#FFFFFF"));
        views.add(new CustomKeyboardButton(2,"F11",getString(R.string.cmd_mousepad_key) + " " + "F11","#FFFFFF"));
        views.add(new CustomKeyboardButton(2,"F12",getString(R.string.cmd_mousepad_key) + " " + "F12","#FFFFFF"));
        views.add(new CustomKeyboardButton(3,"Bright +",getString(R.string.cmd_mousepad_key) + " " + "XF86MonBrightnessUp","#e6f7ff"));
        views.add(new CustomKeyboardButton(3,"Bright -",getString(R.string.cmd_mousepad_key) + " " + "XF86MonBrightnessDown","#e6f7ff"));
        views.add(new CustomKeyboardButton(2,"Super",getString(R.string.cmd_mousepad_key) + " " + "super","#80d4ff"));
        views.add(new CustomKeyboardButton(3,"Vol +",getString(R.string.cmd_mousepad_key) + " " + "XF86AudioRaiseVolume","#B3E6FF"));
        views.add(new CustomKeyboardButton(3,"Vol -",getString(R.string.cmd_mousepad_key) + " " + "XF86AudioLowerVolume","#B3E6FF"));
        views.add(new CustomKeyboardButton(2,"Mute",getString(R.string.cmd_mousepad_key) + " " + "XF86AudioMute","#B3E6FF"));
        views.add(new CustomKeyboardButton(2,"Alt|Tab",getString(R.string.cmd_mousepad_key) + " " + "alt+Tab","#80d4ff"));
        views.add(new CustomKeyboardButton(3,"[Prev]",getString(R.string.cmd_mousepad_key) + " " + "XF86AudioPrev","#B3E6FF"));
        views.add(new CustomKeyboardButton(3,"[Next]",getString(R.string.cmd_mousepad_key) + " " + "XF86AudioNext","#B3E6FF"));
        views.add(new CustomKeyboardButton(2,"[Play]",getString(R.string.cmd_mousepad_key) + " " + "XF86AudioPlay","#B3E6FF"));
        views.add(new CustomKeyboardButton(2,"New",getString(R.string.cmd_mousepad_key) + " " + "ctrl+n","#E6B3FF"));
        views.add(new CustomKeyboardButton(2,"Open",getString(R.string.cmd_mousepad_key) + " " + "ctrl+o","#E6B3FF"));
        views.add(new CustomKeyboardButton(2,"Save",getString(R.string.cmd_mousepad_key) + " " + "ctrl+s","#E6B3FF"));
        views.add(new CustomKeyboardButton(2,"Find",getString(R.string.cmd_mousepad_key) + " " + "ctrl+f","#E6B3FF"));
        views.add(new CustomKeyboardButton(2,"Print",getString(R.string.cmd_mousepad_key) + " " + "ctrl+p","#E6B3FF"));
        views.add(new CustomKeyboardButton(4,"Select all",getString(R.string.cmd_mousepad_key) + " " + "ctrl+a","#E6B3FF"));
        views.add(new CustomKeyboardButton(3,"Zoom +",getString(R.string.cmd_mousepad_key) + " " + "ctrl+plus","#E6B3FF"));
        views.add(new CustomKeyboardButton(3,"Zoom -",getString(R.string.cmd_mousepad_key) + " " + "ctrl+minus","#E6B3FF"));
        views.add(new CustomKeyboardButton(2,"Undo",getString(R.string.cmd_mousepad_key) + " " + "ctrl+z","#F7E6FF"));
        views.add(new CustomKeyboardButton(2,"Redo",getString(R.string.cmd_mousepad_key) + " " + "ctrl+y","#F7E6FF"));
        views.add(new CustomKeyboardButton(2,"Copy",getString(R.string.cmd_mousepad_key) + " " + "ctrl+c","#F7E6FF"));
        views.add(new CustomKeyboardButton(2,"Crop",getString(R.string.cmd_mousepad_key) + " " + "ctrl+x","#F7E6FF"));
        views.add(new CustomKeyboardButton(2,"Paste",getString(R.string.cmd_mousepad_key) + " " + "ctrl+v","#F7E6FF"));
        views.add(new CustomKeyboardButton(3,"PgUp",getString(R.string.cmd_mousepad_key) + " " + "Prior","#FFFFFF"));
        views.add(new CustomKeyboardButton(3,"PgDown",getString(R.string.cmd_mousepad_key) + " " + "Next","#FFFFFF"));
        views.add(new CustomKeyboardButton(4,"Delete",getString(R.string.cmd_mousepad_key) + " " + "Delete","#FFFFFF"));
        views.add(new CustomKeyboardButton(2,"Home",getString(R.string.cmd_mousepad_key) + " " + "Home","#FFFFFF"));
        views.add(new CustomKeyboardButton(2,"Up",getString(R.string.cmd_mousepad_key) + " " + "Up","#FFFFB3"));
        views.add(new CustomKeyboardButton(2,"End",getString(R.string.cmd_mousepad_key) + " " + "End","#FFFFFF"));
        views.add(new CustomKeyboardButton(4,"Backspace",getString(R.string.cmd_mousepad_key) + " " + "BackSpace","#FFFFFF"));
        views.add(new CustomKeyboardButton(2,"Left",getString(R.string.cmd_mousepad_key) + " " + "Left","#FFFFB3"));
        views.add(new CustomKeyboardButton(2,"Down",getString(R.string.cmd_mousepad_key) + " " + "Down","#FFFFB3"));
        views.add(new CustomKeyboardButton(2,"Right",getString(R.string.cmd_mousepad_key) + " " + "Right","#FFFFB3"));
        views.add(new CustomKeyboardButton(4,"Enter",getString(R.string.cmd_mousepad_key) + " " + "Return","#FFFFFF"));
        views.add(new CustomKeyboardButton(5,"<Back",getString(R.string.cmd_mousepad_key) + " " + "XF86Back","#f2e5d9"));
        views.add(new CustomKeyboardButton(5,"Forward>",getString(R.string.cmd_mousepad_key) + " " + "XF86Forward","#f2e5d9"));
        views.add(new CustomKeyboardButton(1,"+",getString(R.string.cmd_mousepad_key) + " " + "ctrl+t","#e6ccb3"));
        views.add(new CustomKeyboardButton(1,"-",getString(R.string.cmd_mousepad_key) + " " + "ctrl+w","#e6ccb3"));
        views.add(new CustomKeyboardButton(4,"Reopen Last Tab",getString(R.string.cmd_mousepad_key) + " " + "ctrl+shift+t","#e6ccb3"));
        views.add(new CustomKeyboardButton(2,"<-",getString(R.string.cmd_mousepad_key) + " " + "ctrl+shift+Tab","#e6ccb3"));
        views.add(new CustomKeyboardButton(2,"->",getString(R.string.cmd_mousepad_key) + " " + "ctrl+Tab","#e6ccb3"));
        return views;
    }

    public String getKeyboardTextValue()
    {
        StringBuilder sb = new StringBuilder();
        for(int x=0; x<list_item.size(); x++)
        {
            sb.append(list_item.get(x).getmTitle()).append("<xmousesep>").append(list_item.get(x).getmCommand()).append("<xmousesep>").append(list_item.get(x).getmSpans()).append("<xmousesep>").append(list_item.get(x).getmColor());
            if(x!=list_item.size()-1)
            {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public void saveKeyboardLayout()
    {
        try
        {
            FileOutputStream fos = openFileOutput(key_layout_filename, Context.MODE_PRIVATE);
            fos.write(getKeyboardTextValue().getBytes());
            fos.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void useKeyboardSendText()
    {
        CharSequence contents = edit_text.getText();
        String text = contents.toString();
        if(!text.isEmpty()){
            text = text.replaceAll("'", "'\\\"'\\\"'");
            text = text.replaceAll("\"", "\\\"");

            SessionController.getSessionController().x11Shell(getString(R.string.cmd_mousepad_type) + " " + "'" + text + "'");
            if(SETTING_KEYBOARD_AUTOCLEAR)
            {
                edit_text.setText("");
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        if(sensorEvent.sensor.getType()==Sensor.TYPE_GYROSCOPE)
        {
            float gyroscopeX=sensorEvent.values[1];
            float gyroscopeY=sensorEvent.values[0];
            float gyroscopeZ=sensorEvent.values[2];

            String cmd;

            @SuppressLint("UseSwitchCompatOrMaterialCode") Switch mouseSwitch=findViewById(R.id.mouse_switch);
            if (mouseSwitch.isChecked())
            {
                if (Math.abs(gyroscopeX) >= 2 || Math.abs(gyroscopeY) >= 2)
                {
                    if (gyroscopeX < 0 || gyroscopeY < 0)
                    {
                        cmd = getString(R.string.cmd_mousepad_move) + " -- " + (gyroscopeX) * 5 + " " + (gyroscopeY) * 5;
                    }
                    else
                    {
                        cmd = getString(R.string.cmd_mousepad_move) + " " + (gyroscopeX) * 5 + " " + (gyroscopeY) * 5;
                    }
                    SessionController.getSessionController().x11Shell(cmd);
                }
                if (gyroscopeZ >= 3)
                {
                    cmd = getString(R.string.cmd_mousepad_click) + " " + "4";
                    SessionController.getSessionController().x11Shell(cmd);
                }
                else if (gyroscopeZ <= -3)
                {
                    cmd = getString(R.string.cmd_mousepad_click) + " " + "5";
                    SessionController.getSessionController().x11Shell(cmd);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {

    }

    public static ActivityMousepad getInstance()
    {
        return ACTIVITY_MOUSEPAD;
    }
}
