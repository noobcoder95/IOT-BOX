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
import com.smartiotdevices.iotbox.ActivityMousepad;
import com.smartiotdevices.iotbox.R;
import com.smartiotdevices.iotbox.mousepadcomponent.SpanVariableGridView.LayoutParams;

import java.util.List;

public class CustomKeyboardButtonAdapter extends ArrayAdapter<CustomKeyboardButton> implements SpanVariableGridView.CalculateChildrenPosition
{

	private static final class ItemViewHolder
	{
		TextView itemTitle;
	}

	private Context mContext;
	private LayoutInflater mLayoutInflater;

	public CustomKeyboardButtonAdapter(Context context, List<CustomKeyboardButton> plugins)
	{
		super(context, R.layout.button_mousepad_keyboard, plugins);

		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final ItemViewHolder itemViewHolder;

		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.button_mousepad_keyboard, parent, false);

			itemViewHolder = new ItemViewHolder();
			itemViewHolder.itemTitle = convertView.findViewById(R.id.textViewTitle);
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
			itemViewHolder.itemTitle.setText(item.getmTitle());
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
			Toast.makeText(mContext, "Error parsing new layout: " + e.getMessage() + " " + item.getmColor(), Toast.LENGTH_SHORT).show();
		}
		convertView.setOnClickListener(v ->
		{
			if (item != null)
			{
				ActivityMousepad.conn.executeShellCommand(item.getmCommand());
			}
		});
		return convertView;
	}

	@Override
	public void onCalculatePosition(View view, int position, int row, int column)
	{

	}
}