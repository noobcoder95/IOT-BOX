package com.smartiotdevices.iotbox.mousepadcomponent;

public interface ScrollingStrategy
{
	boolean performScrolling(final int x, final int y, final CoolDragAndDropGridView view);
}
