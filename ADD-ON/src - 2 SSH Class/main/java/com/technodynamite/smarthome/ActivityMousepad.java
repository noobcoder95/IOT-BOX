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
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smartiotdevices.iotbox.mousepadcomponent.CoolDragAndDropGridView;
import com.smartiotdevices.iotbox.mousepadcomponent.CustomKeyboardButton;
import com.smartiotdevices.iotbox.mousepadcomponent.CustomKeyboardButtonAdapter;
import com.smartiotdevices.iotbox.mousepadcomponent.MyConnectionHandler;
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

public class ActivityMousepad extends AppCompatActivity implements CoolDragAndDropGridView.DragAndDropListener, SpanVariableGridView.OnItemClickListener, SpanVariableGridView.OnItemLongClickListener, SensorEventListener
{
	MenuItem conDiscButton = null;
	MenuItem KeyLockButton = null;
	MenuItem InvScrollButton= null;

	@SuppressLint("StaticFieldLeak")
    public static MyConnectionHandler conn;
	LinearLayout mouseLayout;
	LinearLayout keyboardLayout;

	public static String setting_host;
	public static String setting_user;
	public static int setting_port;
	public static String setting_pass;
	public static float setting_sensitivity=1.5f;
	private static boolean setting_keyboard_locked=false;
	public static String setting_xdotool_initial;
	private static boolean setting_keyboard_batch=true;
	private boolean setting_keyboard_autoclear=true;
	public static boolean setting_invert_scroll = false;

    private EditText ET;
	private CustomKeyboardButtonAdapter mItemAdapter;
	private CoolDragAndDropGridView mCoolDragAndDropGridView;
	private List<CustomKeyboardButton> mItems = new LinkedList<>();
	private String KEYLOAYOUTFILENAME = "keyLayoutFile.csv";
	private RelativeLayout EditKeyboardButtonsLayout;
	private LinearLayout ETLayout;
	private int potentialDeletePosition;
	private int startPosition;
	private FloatingActionButton fab;
	@SuppressLint("StaticFieldLeak")
    public static TextView recentCmdTextView;

	static
    {
		Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
	}

	public void xMouseClickMouse(View v)
    {
		String cmd ="";
		switch(v.getId())
        {
			case R.id.firstMouseButton:
				cmd ="xdotool click 1";
				break;
			case R.id.secondMouseButton:
				cmd ="xdotool click 2";
				break;
			case R.id.thirdMouseButton:
				cmd ="xdotool click 3";
				break;
			case R.id.fourthMouseButton:
				cmd ="xdotool click 4";
				break;
			case R.id.fifthMouseButton:
				cmd ="xdotool click 5";
				break;
			default:
				break;
		}
		conn.executeShellCommand(cmd);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
    {
		getMenuInflater().inflate(R.menu.menu_mousepad_settings, menu);
		KeyLockButton = menu.findItem(R.id.action_lock_keys);
		InvScrollButton = menu.findItem(R.id.action_invert_scroll);
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mousepad);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		if (sensorManager != null)
		{
			Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
			sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
		}
		fab = findViewById(R.id.fab);
		fab.setOnClickListener((v) ->
        {
            if (keyboardLayout.getVisibility() == View.VISIBLE)
            {
                mouseLayout.setVisibility(View.VISIBLE);
                keyboardLayout.setVisibility(View.INVISIBLE);
                fab.setImageResource(R.drawable.ic_action_hardware_keyboard);
            }
            else
            {
                keyboardLayout.setVisibility(View.VISIBLE);
                mouseLayout.setVisibility(View.INVISIBLE);
                fab.setImageResource(R.drawable.ic_action_hardware_mouse);
            }
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null)
			{
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			}
		});

		mouseLayout = findViewById(R.id.mouse);
		keyboardLayout = findViewById(R.id.keyboard);
		mouseLayout.addView(new MyMouseView(getBaseContext()));
		ScrollView scrollView = findViewById(R.id.key_drag_scrollView);
		mCoolDragAndDropGridView = findViewById(R.id.key_drag_DragAndDropGridView);
		mItems = loadKeyboardLayout(this,false);
		mItemAdapter = new CustomKeyboardButtonAdapter(this, mItems);
		mCoolDragAndDropGridView.setAdapter(mItemAdapter);
		mCoolDragAndDropGridView.setScrollingStrategy(new SimpleScrollingStrategy(scrollView));
		mCoolDragAndDropGridView.setDragAndDropListener(this);
		mCoolDragAndDropGridView.setOnItemLongClickListener(this);
		ET = findViewById(R.id.keyboard_input);
		ET.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void afterTextChanged(Editable s)
			{
				if(!ActivityMousepad.setting_keyboard_batch)
				{
					useKeyboardSendText();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start,  int before, int count) {}
		});

		EditKeyboardButtonsLayout = findViewById(R.id.editKeyboardButtonsLayout);
		EditKeyboardButtonsLayout.setVisibility(View.GONE);
		ETLayout = findViewById(R.id.keyboard_send_layout);
		recentCmdTextView = findViewById(R.id.recentCmd);
		conn = new MyConnectionHandler(this);
		//getPreferences();
		//conn.xMouseTryConnect();
	}

	public void getPreferences()
	{
		try
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			setting_host= prefs.getString("setting_host", SessionController.getSessionController().getSessionUserInfo().getHost());
			setting_user= prefs.getString("setting_user", SessionController.getSessionController().getSession().getUserName());
			setting_port= Integer.parseInt(prefs.getString("setting_port", String.valueOf(SessionController.getSessionController().getSession().getPort())));
			setting_pass= prefs.getString("setting_pass", SessionController.getSessionController().getSessionUserInfo().getPassword());

			setting_xdotool_initial=prefs.getString("setting_xdotool_initial", "export DISPLAY=':0' && unset HISTFILE");
			setting_sensitivity = Float.parseFloat(prefs.getString("sensitivity_list", "1.0f"));
			setting_keyboard_autoclear=prefs.getBoolean("keyboard_autoclear", true);
			setting_keyboard_batch=prefs.getBoolean("setting_keyboard_batch",false);
			setting_keyboard_locked=prefs.getBoolean("keyboard_layout_locked",true);
			setting_invert_scroll=prefs.getBoolean("mouse_invert_scroll",false);

		}
		catch(Exception e)
		{
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "There was a problem retrieving your settings: "+e.getMessage(), Toast.LENGTH_LONG).show();
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
		conn.xMouseTryConnect();
	}
	@Override
	protected void onResume()
	{
		super.onResume();
		getPreferences();

		conn.xMouseTryConnect();

		if(mCoolDragAndDropGridView!=null)
		{
			mItemAdapter = new CustomKeyboardButtonAdapter(ActivityMousepad.this, mItems);
			mCoolDragAndDropGridView.setAdapter(mItemAdapter);
			mItemAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onPause()
	{
		KEYLOAYOUTFILENAME = "keyLayoutFile.csv";
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
		conn.xMouseDisconnect();
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
					conn.executeShellCommand("xdotool key XF86AudioRaiseVolume");
				}
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				if(event.isTracking() && !event.isCanceled())
				{
					conn.executeShellCommand("xdotool key XF86AudioLowerVolume");
				}
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		finish();
	}
	public void confirmLayoutReload(String title, String msg,final boolean def)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setPositiveButton("OK", (dialog, which) ->
		{
			mItems = loadKeyboardLayout(ActivityMousepad.this,def);
			mItemAdapter = new CustomKeyboardButtonAdapter(ActivityMousepad.this, mItems);
			mCoolDragAndDropGridView.setAdapter(mItemAdapter);
			mItemAdapter.notifyDataSetChanged();
		});
		builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		if(conDiscButton!=null)
		{
			conDiscButton.setChecked(setting_keyboard_locked);
		}

		if(KeyLockButton!=null)
		{
			KeyLockButton.setChecked(setting_keyboard_locked);
		}

		if(InvScrollButton!=null)
		{
			InvScrollButton.setChecked(setting_invert_scroll);
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
				KEYLOAYOUTFILENAME = "keyLayoutFile.csv";
				confirmLayoutReload("Restore default Keyboard?","Any unsaved buttons will be lost",true);
				break;
			case R.id.action_lock_keys:
				setting_keyboard_locked=!setting_keyboard_locked;
				item.setChecked(setting_keyboard_locked);
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean("keyboard_layout_locked", setting_keyboard_locked);
				editor.apply();
				break;
			case R.id.action_invert_scroll:
				setting_invert_scroll=!setting_invert_scroll;
				item.setChecked(setting_invert_scroll);
				SharedPreferences prefs2 = PreferenceManager.getDefaultSharedPreferences(this);
				SharedPreferences.Editor editor2 = prefs2.edit();
				editor2.putBoolean("mouse_invert_scroll", setting_invert_scroll);
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
		if(!ActivityMousepad.setting_keyboard_locked)
		{
			mCoolDragAndDropGridView.startDragAndDrop();
			EditKeyboardButtonsLayout.setVisibility(View.VISIBLE);
			ETLayout.setVisibility(View.GONE);
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
			mItems.add(to, mItems.remove(from));
			mItemAdapter.notifyDataSetChanged();
		}

		potentialDeletePosition = to;
		startPosition = from;
		EditKeyboardButtonsLayout.setVisibility(View.GONE);
		fab.setVisibility(View.VISIBLE);
		ETLayout.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean isDragAndDropEnabled(int position)
	{
		return true;
	}

	@Override
	public void onEditItem(int mDragPosition)
	{
		onDropItem(potentialDeletePosition,startPosition);
	}
	@Override
	public void onDeleteItem(int mDragPosition)
	{
		try
		{
			mItems.remove(potentialDeletePosition);
			mItemAdapter.notifyDataSetChanged();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Toast.makeText(getBaseContext(),"Error during delete",Toast.LENGTH_LONG).show();
			mItemAdapter.notifyDataSetChanged();
		}

		Toast.makeText(getBaseContext(),"Remove item "+potentialDeletePosition,Toast.LENGTH_LONG).show();
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
			fis = mCont.openFileInput(KEYLOAYOUTFILENAME);
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
		views.add(new CustomKeyboardButton(2,"Esc","xdotool key Escape","#FF9999"));
		views.add(new CustomKeyboardButton(3,"Minimize","xdotool windowminimize $(xdotool getactivewindow)","#9FFF80"));
		views.add(new CustomKeyboardButton(3,"Maximize","xdotool windowsize $(xdotool getactivewindow) 100% 100%","#FFCC66"));
		views.add(new CustomKeyboardButton(2,"X","xdotool key alt+F4","#FF9999"));
		views.add(new CustomKeyboardButton(2,"F1","xdotool key F1","#FFFFFF"));
		views.add(new CustomKeyboardButton(2,"F2","xdotool key F2","#FFFFFF"));
		views.add(new CustomKeyboardButton(2,"F3","xdotool key F3","#FFFFFF"));
		views.add(new CustomKeyboardButton(2,"F4","xdotool key F4","#FFFFFF"));
		views.add(new CustomKeyboardButton(2,"F5","xdotool key F5","#FFFFFF"));
		views.add(new CustomKeyboardButton(2,"F6","xdotool key F6","#FFFFFF"));
		views.add(new CustomKeyboardButton(2,"F7","xdotool key F7","#FFFFFF"));
		views.add(new CustomKeyboardButton(2,"F8","xdotool key F8","#FFFFFF"));
		views.add(new CustomKeyboardButton(2,"F9","xdotool key F9","#FFFFFF"));
		views.add(new CustomKeyboardButton(2,"F10","xdotool key F10","#FFFFFF"));
		views.add(new CustomKeyboardButton(2,"F11","xdotool key F11","#FFFFFF"));
		views.add(new CustomKeyboardButton(2,"F12","xdotool key F12","#FFFFFF"));
		views.add(new CustomKeyboardButton(3,"Bright +","xdotool key XF86MonBrightnessUp","#e6f7ff"));
		views.add(new CustomKeyboardButton(3,"Bright -","xdotool key XF86MonBrightnessDown","#e6f7ff"));
		views.add(new CustomKeyboardButton(2,"Super","xdotool key super","#80d4ff"));
		views.add(new CustomKeyboardButton(3,"Vol +","xdotool key XF86AudioRaiseVolume","#B3E6FF"));
		views.add(new CustomKeyboardButton(3,"Vol -","xdotool key XF86AudioLowerVolume","#B3E6FF"));
		views.add(new CustomKeyboardButton(2,"Mute","xdotool key XF86AudioMute","#B3E6FF"));
		views.add(new CustomKeyboardButton(2,"Alt|Tab","xdotool key alt+Tab","#80d4ff"));
		views.add(new CustomKeyboardButton(3,"[Prev]","xdotool key XF86AudioPrev","#B3E6FF"));
		views.add(new CustomKeyboardButton(3,"[Next]","xdotool key XF86AudioNext","#B3E6FF"));
		views.add(new CustomKeyboardButton(2,"[Play]","xdotool key XF86AudioPlay","#B3E6FF"));
		views.add(new CustomKeyboardButton(2,"New","xdotool key ctrl+n","#E6B3FF"));
		views.add(new CustomKeyboardButton(2,"Open","xdotool key ctrl+o","#E6B3FF"));
		views.add(new CustomKeyboardButton(2,"Save","xdotool key ctrl+s","#E6B3FF"));
		views.add(new CustomKeyboardButton(2,"Find","xdotool key ctrl+f","#E6B3FF"));
		views.add(new CustomKeyboardButton(2,"Print","xdotool key ctrl+p","#E6B3FF"));
		views.add(new CustomKeyboardButton(4,"Select all","xdotool key ctrl+a","#E6B3FF"));
		views.add(new CustomKeyboardButton(3,"Zoom +","xdotool key ctrl+plus","#E6B3FF"));
		views.add(new CustomKeyboardButton(3,"Zoom -","xdotool key ctrl+minus","#E6B3FF"));
		views.add(new CustomKeyboardButton(2,"Undo","xdotool key ctrl+z","#F7E6FF"));
		views.add(new CustomKeyboardButton(2,"Redo","xdotool key ctrl+y","#F7E6FF"));
		views.add(new CustomKeyboardButton(2,"Copy","xdotool key ctrl+c","#F7E6FF"));
		views.add(new CustomKeyboardButton(2,"Crop","xdotool key ctrl+x","#F7E6FF"));
		views.add(new CustomKeyboardButton(2,"Paste","xdotool key ctrl+v","#F7E6FF"));
		views.add(new CustomKeyboardButton(3,"PgUp","xdotool key Prior","#FFFFFF"));
		views.add(new CustomKeyboardButton(3,"PgDown","xdotool key Next","#FFFFFF"));
		views.add(new CustomKeyboardButton(4,"Delete","xdotool key Delete","#FFFFFF"));
		views.add(new CustomKeyboardButton(2,"Home","xdotool key Home","#FFFFFF"));
		views.add(new CustomKeyboardButton(2,"Up","xdotool key Up","#FFFFB3"));
		views.add(new CustomKeyboardButton(2,"End","xdotool key End","#FFFFFF"));
		views.add(new CustomKeyboardButton(4,"Backspace","xdotool key BackSpace","#FFFFFF"));
		views.add(new CustomKeyboardButton(2,"Left","xdotool key Left","#FFFFB3"));
		views.add(new CustomKeyboardButton(2,"Down","xdotool key Down","#FFFFB3"));
		views.add(new CustomKeyboardButton(2,"Right","xdotool key Right","#FFFFB3"));
		views.add(new CustomKeyboardButton(4,"Enter","xdotool key Return","#FFFFFF"));
		views.add(new CustomKeyboardButton(5,"<Back","xdotool key XF86Back","#f2e5d9"));
		views.add(new CustomKeyboardButton(5,"Forward>","xdotool key XF86Forward","#f2e5d9"));
		views.add(new CustomKeyboardButton(1,"+","xdotool key ctrl+t","#e6ccb3"));
		views.add(new CustomKeyboardButton(1,"-","xdotool key ctrl+w","#e6ccb3"));
		views.add(new CustomKeyboardButton(4,"Reopen Last Tab","xdotool key ctrl+shift+t","#e6ccb3"));
		views.add(new CustomKeyboardButton(2,"<-","xdotool key ctrl+shift+Tab","#e6ccb3"));
		views.add(new CustomKeyboardButton(2,"->","xdotool key ctrl+Tab","#e6ccb3"));
		return views;
	}

	public String getKeyboardTextValue()
	{
		StringBuilder sb = new StringBuilder();
		for(int x=0; x<mItems.size(); x++)
		{
			sb.append(mItems.get(x).getmTitle()).append("<xmousesep>").append(mItems.get(x).getmCommand()).append("<xmousesep>").append(mItems.get(x).getmSpans()).append("<xmousesep>").append(mItems.get(x).getmColor());
			if(x!=mItems.size()-1)
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
			FileOutputStream fos = openFileOutput(KEYLOAYOUTFILENAME, Context.MODE_PRIVATE);
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
		CharSequence contents = ET.getText();
		String t = contents.toString();
		if(!t.isEmpty()){
			t = t.replaceAll("'", "'\\\"'\\\"'");
			t = t.replaceAll("\"", "\\\"");

			conn.executeShellCommand("xdotool type '" + t + "'");
			if(setting_keyboard_autoclear)
			{
				ET.setText("");
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

			Switch mouseSwitch=findViewById(R.id.mouseSwitch);
			if (mouseSwitch.isChecked())
			{
				if (Math.abs(gyroscopeX) >= 2 || Math.abs(gyroscopeY) >= 2)
				{
					if (gyroscopeX < 0 || gyroscopeY < 0)
					{
						cmd = "xdotool mousemove_relative -- " + (gyroscopeX) * 5 + " " + (gyroscopeY) * 5;
					}
					else
					{
						cmd = "xdotool mousemove_relative " + (gyroscopeX) * 5 + " " + (gyroscopeY) * 5;
					}
					conn.executeShellCommand(cmd);
				}
				if (gyroscopeZ >= 3)
				{
					cmd = "xdotool click 4";
					conn.executeShellCommand(cmd);
				}
				else if (gyroscopeZ <= -3)
				{
					cmd = "xdotool click 5";
					conn.executeShellCommand(cmd);
				}
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i)
	{

	}
}
