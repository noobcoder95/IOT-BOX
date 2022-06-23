package com.smartiotdevices.iotbox.mousepadcomponent;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.smartiotdevices.iotbox.R;

public class SpanVariableGridView extends AdapterView<BaseAdapter>
{
	private static final int NOT_DEFINED_VALUE = -1;
	private static final int INVALID_POSITION = NOT_DEFINED_VALUE;
	private static final int TOUCH_STATE_RESTING = 0;
	private static final int TOUCH_STATE_CLICK = 1;
	private static final int TOUCH_STATE_LONG_CLICK = 2;
	private int touch_start_x;
	private int touch_start_y;
	private int touch_start_item_position;
	private Runnable long_press_runnable;
	private int column_count = 2;
	private int item_margin = 0;
	private int control_height = 0;
	private Rect rec = new Rect();
	private boolean populating = false;
	private int touch_state = TOUCH_STATE_RESTING;
	private BaseAdapter adapter = null;
	private TransitionDrawable item_transition_drawable = null;
	//private List<CalculateChildrenPosition> mCalculateChildrenPositionList = new LinkedList<>();

	private final DataSetObserver observer = new DataSetObserver()
	{
		@Override
		public void onChanged()
		{
			populating = false;
			removeAllViewsInLayout();
			requestLayout();
		}

		@Override
		public void onInvalidated()
		{

		}
	};

	static class LayoutParams extends AdapterView.LayoutParams
	{
		static final int ALL_COLUMNS = NOT_DEFINED_VALUE;
		int span = 1;
		int position = NOT_DEFINED_VALUE;
		int row = NOT_DEFINED_VALUE;
		int column = NOT_DEFINED_VALUE;

		LayoutParams(AdapterView.LayoutParams other)
		{
			super(other);
			setupWidthAndHeight();
		}

		private void setupWidthAndHeight()
		{
			if (this.width != MATCH_PARENT)
			{
				this.width = MATCH_PARENT;
			}

			if (this.height == MATCH_PARENT)
			{
				this.height = WRAP_CONTENT;
			}
		}
	}

	public SpanVariableGridView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initialize(attrs);
	}

	public SpanVariableGridView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initialize(attrs);
	}

	public SpanVariableGridView(Context context)
	{
		super(context);
	}

	private void initialize(final AttributeSet attrs)
	{
		if (attrs != null)
		{
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SpanVariableGridView);
			try
			{
				column_count = a.getInteger(R.styleable.SpanVariableGridView_numColumns, 2);
				item_margin = a.getDimensionPixelSize(R.styleable.SpanVariableGridView_itemMargin, 0);

			}
			finally
			{
				a.recycle();
			}

		}
		else
		{
			column_count = 2;
			item_margin = 0;
		}
	}

	@Override
	public BaseAdapter getAdapter()
	{
		return adapter;
	}

	@Override
	public View getSelectedView()
	{
		return null;
	}

	@Override
	public void setAdapter(BaseAdapter _adapter)
	{
		if (adapter != null)
		{
			adapter.unregisterDataSetObserver(observer);
		}

		adapter = _adapter;

		if (adapter != null)
		{
			adapter.registerDataSetObserver(observer);
		}
		removeAllViewsInLayout();
		requestLayout();
	}

	@Override
	public void setSelection(int position)
	{

	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		if (adapter == null)
		{
			return;
		}

		layoutChildrens(INVALID_POSITION, false);
	}

	protected void performDragAndDropSwapping(int from, int to)
	{
		populating = true;

		if (from != to)
		{
			final View removedChild = getChildAt(from);
			removedChild.clearAnimation();
			removeViewInLayout(removedChild);
			addViewInLayout(removedChild, to, removedChild.getLayoutParams());
		}
		control_height = measureChildrens();
		layoutChildrens(to, true);
		populating = false;
	}

	@Override
	public void requestLayout()
	{
		if (!populating)
		{
			super.requestLayout();
		}
	}

	protected void layoutChildrens(final int draggedChild, final boolean animate)
	{
		int row = 0;
		int rowHeight = 0;
		int fullHeight = item_margin;
		int width = getMeasuredWidth() - 2 * item_margin;
		final int colWidth = (width - (column_count - 1) * item_margin) / column_count;
		//Rect draggedChildRect = null;
		for (int position = 0; position < adapter.getCount(); position++)
		{
			final View childView = getChildAt(position);
			final Point prev = new Point(childView.getLeft(), childView.getTop());
			final LayoutParams lp = (LayoutParams) childView.getLayoutParams();
			final int column = lp.column;
			if (row != lp.row)
			{
				fullHeight += (rowHeight + item_margin);
				rowHeight = 0;
			}
			rowHeight = Math.max(rowHeight, childView.getMeasuredHeight());
			row = lp.row;
			final int width_ = column == LayoutParams.ALL_COLUMNS ? width : (lp.span * (colWidth + item_margin) - item_margin);
			final int left_ = item_margin + (column == LayoutParams.ALL_COLUMNS ? 0 : column * (colWidth + item_margin));
			final int top_ = fullHeight;
			final int right_ = left_ + width_;
			final int bottom_ = top_ + childView.getMeasuredHeight();
			measureChildren(childView, width_, lp.height);
			if (position != draggedChild)
			{
				final Point now = new Point(left_, top_);
				childView.layout(left_, top_, right_, bottom_);
				if (animate)
				{
					translateChild(childView, prev, now);
				}
			}
		}
	}

	protected final void translateChild(View v, Point prev, Point now)
	{
		TranslateAnimation translate = new TranslateAnimation(Animation.ABSOLUTE, -now.x + prev.x, Animation.ABSOLUTE, 0, Animation.ABSOLUTE, -now.y + prev.y, Animation.ABSOLUTE, 0);
		translate.setInterpolator(new AccelerateInterpolator(4f));
		translate.setDuration(350);
		translate.setFillEnabled(false);
		translate.setFillAfter(false);
		v.clearAnimation();
		v.startAnimation(translate);
	}

	protected void measureChildren(View child, final int width, final int height)
	{
		final int heightSpec;
		if (height == LayoutParams.WRAP_CONTENT)
		{
			heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		else
		{
			heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
		}
		final int widthSpec;
		if (width == LayoutParams.WRAP_CONTENT)
		{
			widthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		else
		{
			widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
		}
		child.measure(widthSpec, heightSpec);
	}

	protected int pointToPosition(final int draggedChild, final int x, final int y)
	{
		for (int index = 0; index < getChildCount(); index++)
		{
			if (index == draggedChild)
			{
				continue;
			}

			getChildAt(index).getHitRect(rec);

			if (rec.contains(x, y))
			{
				return index;
			}
		}
		return INVALID_POSITION;
	}

	private void clickChildAt()
	{
		final int index = pointToPosition(INVALID_POSITION, touch_start_x, touch_start_y);
		if (index != INVALID_POSITION && index == touch_start_item_position)
		{
			final View itemView = getChildAt(index);
			final long id = adapter.getItemId(index);
			performItemClick(itemView, index, id);
		}
	}

	private void longClickChild(final int index)
	{
		final View itemView = getChildAt(index);
		final long id = adapter.getItemId(index);
		final OnItemLongClickListener listener = getOnItemLongClickListener();
		if (listener != null)
		{
			listener.onItemLongClick(this, itemView, index, id);
		}
	}

	private void startLongPressCheck()
	{
		if (long_press_runnable == null)
		{
			long_press_runnable = () ->
			{
				if (touch_state == TOUCH_STATE_CLICK)
				{
					final int index = pointToPosition(INVALID_POSITION, touch_start_x, touch_start_y);
					if (index != INVALID_POSITION && index == touch_start_item_position)
					{
						longClickChild(index);
						touch_state = TOUCH_STATE_LONG_CLICK;
					}
				}
			};
		}
		postDelayed(long_press_runnable, ViewConfiguration.getLongPressTimeout());
	}

	protected void startLongClickTransition(final View clickedChild)
	{
		if (clickedChild != null && item_transition_drawable == null)
		{
			if (clickedChild.getBackground().getCurrent() instanceof TransitionDrawable)
			{
				item_transition_drawable = (TransitionDrawable) clickedChild.getBackground().getCurrent();
				item_transition_drawable.startTransition(ViewConfiguration.getLongPressTimeout());
			}
		}
	}

	protected void resetLongClickTransition()
	{
		if (item_transition_drawable != null)
		{
			item_transition_drawable.resetTransition();
			item_transition_drawable = null;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		if (isInEditMode() || adapter == null)
		{
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}

		control_height = measureChildrens();
		final int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
		setMeasuredDimension(measuredWidth, control_height);
	}

	private int measureChildrens()
	{
		int row = 0;
		int col = 0;
		int rowHeight = 0;
		int spansFilled = 0;
		int fullHeight = item_margin;
		for (int position = 0; position < adapter.getCount(); position++)
		{
			View childView = getChildAt(position);
			if (childView == null)
			{
				childView = adapter.getView(position, null, this);
				LayoutParams params = (LayoutParams) childView.getLayoutParams();
				if (params == null)
				{
					params = new LayoutParams(new AdapterView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				}

				addViewInLayout(childView, NOT_DEFINED_VALUE, params);
			}
			final LayoutParams lp = (LayoutParams) childView.getLayoutParams();
			measureChildren(childView, lp.width, lp.height);
			lp.position = position;
			spansFilled += lp.span;
			while (true)
			{
				if (spansFilled <= column_count)
				{
					lp.row = row;
					lp.column = lp.span == column_count ? LayoutParams.ALL_COLUMNS : col;
					col = spansFilled;
					childView.setLayoutParams(lp);
					rowHeight = Math.max(rowHeight, item_margin + childView.getMeasuredHeight());
				}

				if (spansFilled >= column_count)
				{
					fullHeight += rowHeight;
					row++;
					col = 0;
					rowHeight = 0;
					if (spansFilled != column_count)
					{
						spansFilled = lp.span;
						continue;
					}
					spansFilled = 0;
				}
				break;
			}
		}
		fullHeight += rowHeight;
		return fullHeight;
	}

	private void startTouch(final MotionEvent event)
	{
		touch_start_x = (int) event.getX();
		touch_start_y = (int) event.getY();
		touch_start_item_position = pointToPosition(INVALID_POSITION, touch_start_x, touch_start_y);
		startLongPressCheck();
		touch_state = TOUCH_STATE_CLICK;
	}

	@Override
	public void childDrawableStateChanged(View child)
	{
		startLongClickTransition(child);
		super.childDrawableStateChanged(child);
	}

	private void endTouch()
	{
		resetLongClickTransition();
		removeCallbacks(long_press_runnable);
		touch_state = TOUCH_STATE_RESTING;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event)
	{
		final boolean result = super.dispatchTouchEvent(event);
		if (getChildCount() == 0)
		{
			return result;
		}
		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN: startTouch(event);
				break;

			case MotionEvent.ACTION_MOVE:
				break;

			case MotionEvent.ACTION_UP:
				if (touch_state == TOUCH_STATE_CLICK)
				{
					clickChildAt();
				}

				endTouch();
				break;

			default:
				endTouch();
				break;
		}
		return result;
	}
}
