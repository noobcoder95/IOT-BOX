package com.smartiotdevices.iotbox.mousepadcomponent;

public class CustomKeyboardButton
{
	private int mSpans;
	private String mTitle;
	private String mCommand;
	private String mColor="#FFFFFF";

	public CustomKeyboardButton(int spans, String title, String desc, String color)
	{
		mSpans=spans;
		mTitle= title;
		mCommand=desc;
		if(color!=null)
		{
			mColor=color;
		}
	}

	public String getmColor()
	{
		return mColor;
	}

	public int getmSpans()
	{
		return mSpans;
	}

	public String getmTitle()
	{
		return mTitle;
	}

	public String getmCommand()
	{
		return mCommand;
	}

	public void setName(String n)
	{
		this.mTitle = n;
	}
}
