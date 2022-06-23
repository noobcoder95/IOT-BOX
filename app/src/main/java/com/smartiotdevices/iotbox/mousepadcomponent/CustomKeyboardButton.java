package com.smartiotdevices.iotbox.mousepadcomponent;

public class CustomKeyboardButton
{
	private int spans;
	private String title;
	private String command;
	private String color="#FFFFFF";

	public CustomKeyboardButton(int _spans, String _title, String _command, String _color)
	{
		spans = _spans;
		title =  _title;
		command = _command;
		if(_color != null)
		{
			color = _color;
		}
	}

	public String getmColor()
	{
		return color;
	}

	public int getmSpans()
	{
		return spans;
	}

	public String getmTitle()
	{
		return title;
	}

	public String getmCommand()
	{
		return command;
	}

	public void setName(String n)
	{
		this.title = n;
	}
}
