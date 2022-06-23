package com.smartiotdevices.iotbox.mousepadcomponent;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;

public class DragAndDropGridView extends SpanVariableGridView implements View.OnTouchListener
{

	private static final int ITEM_HOVER_DELAY = 450;

	private int drag_point_x;
	private int drag_point_y;
	private int drag_offset_x;
	private int drag_offset_y;
	private int drag_to_x;
	private int drag_to_y;
	private int drag_position = AdapterView.INVALID_POSITION;
	private int drop_position = AdapterView.INVALID_POSITION;
	private int current_position = AdapterView.INVALID_POSITION;
	private boolean drag_and_drop_started = false;

	private ImageView drag_image_view = null;
	private DragAndDropListener drag_drop_listener = null;
	private OnTrackTouchEventsListener on_track_touch_events_listener = null;
	private Runnable delayed_on_drag_runnable = null;

	ScrollingStrategy scroll_strategy = null;
	WindowManager window_manager = null;
	WindowManager.LayoutParams window_params = null;

	public interface OnTrackTouchEventsListener
	{
		void trackTouchEvents(final MotionEvent motionEvent);
	}

	public interface DragAndDropListener
	{
		void onDragItem(int from);
		void onDraggingItem(int from, int to);
		void onDropItem(int from, int to);
		boolean isDragAndDropEnabled(int position);
		void onDeleteItem(int mDragPosition);
		void onEditItem(int mDragPosition);
	}


	public DragAndDropGridView(Context context)
	{
		super(context);
		initialize();
	}

	public DragAndDropGridView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initialize();
	}

	public DragAndDropGridView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initialize();
	}

	private void initialize()
	{
		setOnTouchListener(this);
		setChildrenDrawingOrderEnabled(true);
	}

	public void startDragAndDrop()
	{
		drag_and_drop_started = true;
	}

	public void setDragAndDropListener(DragAndDropListener drag_and_drop_listener)
	{
		drag_drop_listener = drag_and_drop_listener;
	}

	private void destroyDragImageView()
	{
		if (drag_image_view != null)
		{
			window_manager.removeView(drag_image_view);
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drag_image_view.getDrawable();
			if (bitmapDrawable != null)
			{
				final Bitmap bitmap = bitmapDrawable.getBitmap();
				if (bitmap != null && !bitmap.isRecycled())
				{
					bitmap.recycle();
				}
			}

			drag_image_view.setImageDrawable(null);
			drag_image_view = null;
		}
	}

	@SuppressLint("RtlHardcoded")
	private ImageView createDragImageView(final View v, final int x, final int y)
	{
		v.destroyDrawingCache();
		v.setDrawingCacheEnabled(true);
		Bitmap bm = Bitmap.createBitmap(v.getDrawingCache());

		drag_point_x = x - v.getLeft();
		drag_point_y = y - v.getTop();
		window_params = new WindowManager.LayoutParams();
		window_params.gravity = Gravity.TOP | Gravity.LEFT;
		window_params.x = x - drag_point_x + drag_offset_x;
		window_params.y = y - drag_point_y + drag_offset_y;
		window_params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		window_params.width = WindowManager.LayoutParams.WRAP_CONTENT;
		window_params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		window_params.format = PixelFormat.TRANSLUCENT;
		window_params.alpha = 0.7f;
		window_params.windowAnimations = 0;

		ImageView iv = new ImageView(getContext());
		iv.setBackgroundColor(Color.parseColor("#ff555555"));
		iv.setImageBitmap(bm);

		window_manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		if (window_manager != null)
		{
			window_manager.addView(iv, window_params);
		}
		return iv;
	}

	private void startDrag(final int x, final int y)
	{
		final View v = getChildAt(drag_position);
		destroyDragImageView();
		drag_image_view = createDragImageView(v, x, y);
		v.setVisibility(View.INVISIBLE);
		if (drag_drop_listener != null)
		{
			drag_drop_listener.onDragItem(drag_position);
		}
	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i)
	{
		if (current_position == -1)
			return i;
		else if (i == childCount - 1)
			return current_position;
		else if (i >= current_position)
			return i + 1;
		return i;
	}

	private void onDrop()
	{
		destroyDragImageView();
		removeCallbacks(delayed_on_drag_runnable);
		View v = getChildAt(drop_position);
		v.setVisibility(View.VISIBLE);
		v.clearAnimation();
		if (drag_drop_listener != null && drop_position != AdapterView.INVALID_POSITION)
		{
			drag_drop_listener.onDropItem(drag_position, drop_position);
		}
		drag_position = drop_position = current_position = AdapterView.INVALID_POSITION;
		drag_and_drop_started = false;
		if(drag_to_y > this.getHeight())
		{
			if (drag_to_x > this.getWidth()/2)
			{
				drag_drop_listener.onEditItem(drag_position);
			}
			else
			{
				drag_drop_listener.onDeleteItem(drag_position);
			}
		}
	}

	public void setScrollingStrategy(ScrollingStrategy scrolling_strategy)
	{
		scroll_strategy = scrolling_strategy;
	}

	private void onDrag(final int x, final int y)
	{
		if (scroll_strategy != null && scroll_strategy.performScrolling(x, y, this))
		{
			removeCallbacks(delayed_on_drag_runnable);
			return;
		}

		final int tempDropPosition = pointToPosition(current_position, x, y);

		if (drag_drop_listener != null && drop_position != tempDropPosition && tempDropPosition != AdapterView.INVALID_POSITION)
		{
			removeCallbacks(delayed_on_drag_runnable);

			if (drag_drop_listener.isDragAndDropEnabled(tempDropPosition))
			{
				drop_position = tempDropPosition;
				delayed_on_drag_runnable = () ->
				{
					drag_drop_listener.onDraggingItem(current_position, tempDropPosition);
					performDragAndDropSwapping(current_position, tempDropPosition);
					final int nextDropPosition = pointToPosition(tempDropPosition, x, y);
					if (nextDropPosition == AdapterView.INVALID_POSITION)
					{
						current_position = drop_position = tempDropPosition;
					}
				};
				postDelayed(delayed_on_drag_runnable, ITEM_HOVER_DELAY);
			}
			else
			{
				drop_position = drag_position;
			}
		}
		if (drag_image_view != null)
		{
			window_params.x = x - drag_point_x + drag_offset_x;
			window_params.y = y - drag_point_y + drag_offset_y;
			window_manager.updateViewLayout(drag_image_view, window_params);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event)
	{
		if (on_track_touch_events_listener != null)
		{
			on_track_touch_events_listener.trackTouchEvents(event);
		}
		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:

				if (drag_drop_listener != null && drag_and_drop_started)
				{
					drag_and_drop_started = false;
					getParent().requestDisallowInterceptTouchEvent(true);
					return launchDragAndDrop(event);
				}
				break;
			default:
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				drag_and_drop_started = false;
				getParent().requestDisallowInterceptTouchEvent(false);
				break;
		}
		return super.onInterceptTouchEvent(event);
	}

	private boolean launchDragAndDrop(final MotionEvent event)
	{
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		current_position = drag_position = drop_position = pointToPosition(drag_position, x, y);
		if (drag_position != AdapterView.INVALID_POSITION && drag_drop_listener.isDragAndDropEnabled(drag_position))
		{
			drag_offset_x = (int) (event.getRawX() - x);
			drag_offset_y = (int) (event.getRawY() - y);
			startDrag(x, y);
			return true;
		}
		return false;
	}

	@Override
	public boolean onTouch(View view, MotionEvent event)
	{
		if (drag_position != AdapterView.INVALID_POSITION && drag_image_view != null)
		{
			final int x = (int) event.getX();
			final int y = (int) event.getY();
			switch (event.getAction())
			{
				case MotionEvent.ACTION_MOVE:
					drag_offset_y = (int) (event.getRawX() - x);
					drag_offset_y = (int) (event.getRawY() - y);
					drag_to_y = y;
					drag_to_x = x;
					onDrag(x, y);
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					onDrop();
					resetLongClickTransition();
					getParent().requestDisallowInterceptTouchEvent(false);
					return false;
				default:
			}
			return true;
		}
		return false;
	}
}