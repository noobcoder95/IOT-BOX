package com.smartiotdevices.iotbox.mousepadcomponent;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smartiotdevices.iotbox.R;
import com.smartiotdevices.iotbox.mousepadcomponent.SpanVariableGridView.LayoutParams;
import com.smartiotdevices.iotbox.sshutils.SessionController;

import java.util.List;

public class CustomKeyboardButtonAdapter extends ArrayAdapter<CustomKeyboardButton>
{

	private static final class ItemViewHolder
	{
		TextView item_title;
	}

	private Context context;
	private LayoutInflater layout_inflater;

	public CustomKeyboardButtonAdapter(Context _context, List<CustomKeyboardButton> plugins)
	{
		super(_context, R.layout.button_mousepad_keyboard, plugins);

		context = _context;
		layout_inflater = LayoutInflater.from(_context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final ItemViewHolder itemViewHolder;

		if (convertView == null) {
			convertView = layout_inflater.inflate(R.layout.button_mousepad_keyboard, parent, false);

			itemViewHolder = new ItemViewHolder();
			itemViewHolder.item_title = convertView.findViewById(R.id.textViewTitle);
			convertView.setTag(itemViewHolder);
		}

		else
		{
			itemViewHolder = (ItemViewHolder) convertView.getTag();
		}

		final CustomKeyboardButton item = getItem(position);
		SpanVariableGridView.LayoutParams lp = new LayoutParams(convertView.getLayoutParams());
		if (item != null)
		{
			lp.span = item.getmSpans();
		}
		convertView.setLayoutParams(lp);
		if (item != null)
		{
			itemViewHolder.item_title.setText(item.getmTitle());
		}

		final RelativeLayout layoutHolder = convertView.findViewById(R.id.textViewHolderLayout);
		try
		{
			if (item != null)
			{
				layoutHolder.setBackgroundColor(Color.parseColor(item.getmColor()));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Toast.makeText(context, "Error parsing new layout: " + e.getMessage() + " " + item.getmColor(), Toast.LENGTH_SHORT).show();
		}
		convertView.setOnClickListener(v ->
		{
			if (item != null)
			{
				SessionController.getSessionController().x11Shell(item.getmCommand());
			}
		});
		return convertView;
	}
}