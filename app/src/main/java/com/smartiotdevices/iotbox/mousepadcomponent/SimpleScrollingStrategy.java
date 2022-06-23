package com.smartiotdevices.iotbox.mousepadcomponent;

import android.widget.ScrollView;

public class SimpleScrollingStrategy implements ScrollingStrategy
{
	private ScrollView scroll_view_container;

	public SimpleScrollingStrategy(ScrollView _scroll_view_container)
	{
		scroll_view_container = _scroll_view_container;
	}

	@Override
	public boolean performScrolling(final int x, final int y, final DragAndDropGridView view)
	{
		if (scroll_view_container != null)
		{
			int scrollY = scroll_view_container.getScrollY();
			int delta = scrollY - view.getTop();
			int maxDelta = Math.max(delta, 0);
			int dy = y - delta;
			int height = view.getHeight();
			int containerHeight = scroll_view_container.getHeight();
			int topThresshold = containerHeight / 10;
			int bottomThresshold = 9 * containerHeight / 10;

			if ((dy < topThresshold) && (maxDelta > 0))
			{
				scroll_view_container.scrollBy(0, -topThresshold / 8);
				return true;
			}

			else if ((dy > bottomThresshold) && ((delta + containerHeight) < height))
			{
				scroll_view_container.scrollBy(0, topThresshold / 8);
				return true;
			}
		}
		return false;
	}
}
