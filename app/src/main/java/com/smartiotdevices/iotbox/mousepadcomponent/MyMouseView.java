package com.smartiotdevices.iotbox.mousepadcomponent;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.MotionEventCompat;

import com.smartiotdevices.iotbox.ActivityMousepad;
import com.smartiotdevices.iotbox.R;
import com.smartiotdevices.iotbox.sshutils.SessionController;

public class MyMouseView extends View
{
    private Paint paint;
    private Bitmap bitmap;
    private Path path;
    private Paint bitmap_paint;
    private float scroll_start = 120f;
    final int CLICK = 3;
    private int w=0;
    private int h=0;
    Canvas canvas;
    PointF start = new PointF();
    long down_start = 0;
    boolean dragging = false;
    boolean draggable = true;
    boolean touching = false;

    enum ClickType
    {
        Left_click,
        Right_click,
        Drag_Down,
        Drag_up,
        Zoom_in,
        Zoom_out
    }

    public MyMouseView(Context c)
    {
        super(c);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        path = new Path();
        bitmap_paint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        this.w=w;
        this.h=h;
    }

    @Override
    protected void onDraw(Canvas c)
    {
        canvas=c;
        canvas.drawBitmap(bitmap, 0, 0, bitmap_paint);
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(3);
        canvas.drawLine(w-scroll_start, h*0.2f, w-scroll_start, h*1.0f, paint);
        if (dragging)
        {
            paint.setColor(Color.YELLOW);
        }

        else
        {
            paint.setColor(0xFFFF0000);
        }

        paint.setStrokeWidth(10);
        canvas.drawPath(path, paint);
        if (touching)
        {
            paint.setColor(Color.BLACK);
            if (dragging)
            {
                paint.setStrokeWidth(10);
            }

            else
            {
                paint.setStrokeWidth(3);
            }

            if (!scrolling && !zooming)
            {
                canvas.drawCircle(mX, mY, 80, paint);
            }

            if (zooming || scrolling)
            {
                canvas.drawCircle(curr.x, curr.y, 80, paint);
                if (yX<scroll_start || twoFingerScroll)
                {
                    canvas.drawCircle(yX, yY, 80, paint);
                }
            }
        }
    }

    private boolean twoFingerScroll = false;
    private int zoomCounter = 0;
    private float mX=0, mY=0, yX=scroll_start, yY=0;
    private boolean scrolling = false;
    private boolean firstTouch= true;
    private boolean zooming =false;
    private double dist = 0;
    private PointF curr;

    private void touch_start(float x, float y)
    {
        touching=true;
        if ((w-x)<=scroll_start)
        {
            scrolling=true;
            x = w-scroll_start/2;
            paint.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));
        }

        else
        {
            scrolling=false;
            paint.setPathEffect(null);
        }

        path.reset();
        path.moveTo(x, y);
        mX = x;
        mY = y;
    }
    private void touch_move(float x, float y)
    {
        if (scrolling)
        {
            x = w - scroll_start / 2;
        }

        float rx = x - mX;
        float ry = y - mY;
        float dx = Math.abs(rx);
        float dy = Math.abs(ry);

        if (!zooming)
        {
            float TOUCH_TOLERANCE = 4;
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE)
            {
                path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
                OnXMouseMoved(rx, ry, scrolling);
            }
        }

        else
        {
            path.reset();
            path.setLastPoint(curr.x,curr.y);
            path.lineTo(yX,yY);
        }
    }
    private void touch_up()
    {
        path.lineTo(mX, mY);
        path.reset();
        scrolling=false;
        touching=false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @SuppressWarnings("deprecation")
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float x = event.getX();
        float y = event.getY();
        curr = new PointF(event.getX(), event.getY());
        double newDist;
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                start.set(curr);
                down_start = System.currentTimeMillis();
                touch_start(x, y);
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                int xDiff1 = (int) Math.abs(curr.x - start.x);
                int yDiff1 = (int) Math.abs(curr.y - start.y);
                if (event.getPointerCount() > 1)
                {
                    yX = MotionEventCompat.getX(event, 1);
                    yY = MotionEventCompat.getY(event, 1);
                    newDist = Math.sqrt(Math.pow(yX - x, 2) + Math.pow(yY - y, 2));
                    if (firstTouch)
                    {
                        dist=newDist;
                        firstTouch=false;
                    }

                    double scaleFactor = (newDist - dist) / dist;
                    if (Math.abs(scaleFactor)>0.5)
                    {
                        zooming=true;
                        zoomCounter++;
                        int zoomOverFlow = 10;
                        if (zoomCounter> zoomOverFlow)
                        {
                            if (scaleFactor > 0)
                            {
                                OnXMouseClicked(ClickType.Zoom_in);
                            }

                            else
                            {
                                OnXMouseClicked(ClickType.Zoom_out);
                            }
                            zoomCounter=0;
                        }

                    }
                    else if (!zooming)
                    {
                        twoFingerScroll=true;
                        scrolling=true;
                    }
                }

                long thisTime = System.currentTimeMillis()-down_start;
                if (xDiff1 < CLICK*6 && yDiff1 < CLICK*6)
                {
                    if (draggable && !dragging && thisTime>350)
                    {
                        OnXMouseClicked(ClickType.Drag_Down);
                        dragging=true;
                    }
                }
                else
                {
                    draggable=false;
                }
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                int xDiff = (int) Math.abs(curr.x - start.x);
                int yDiff = (int) Math.abs(curr.y - start.y);
                if (xDiff < CLICK && yDiff < CLICK)
                {
                    if (scrolling)
                    {
                        OnXMouseClicked(ClickType.Right_click);
                    }
                    else
                    {
                        OnXMouseClicked(ClickType.Left_click);
                    }
                }

                if (dragging)
                {
                    dragging=false;
                    OnXMouseClicked(ClickType.Drag_up);
                }

                firstTouch=true;
                zooming=false;
                draggable=true;
                twoFingerScroll=false;
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    public void OnXMouseMoved(float dx, float dy,boolean scroll)
    {
        dx = dx* ActivityMousepad.SETTING_SENSITIVITY;
        dy = dy* ActivityMousepad.SETTING_SENSITIVITY;
        String cmd;
        if (dx <0 || dy <0)
        {
            cmd = getContext().getString(R.string.cmd_mousepad_move) + " -- " + dx + " " + dy;
            if (scroll)
            {
                if (ActivityMousepad.SETTING_INVERT_SCROLL)
                {
                    cmd = getContext().getString(R.string.cmd_mousepad_click) + " " + "4";
                }
                else
                {
                    cmd = getContext().getString(R.string.cmd_mousepad_click) + " " + "5";
                }
            }
        }
        else
        {
            cmd = getContext().getString(R.string.cmd_mousepad_move) + " " + dx + " " + dy;
            if (scroll)
            {
                if (ActivityMousepad.SETTING_INVERT_SCROLL)
                {
                    cmd = getContext().getString(R.string.cmd_mousepad_click) + " " + "5";
                }
                else
                {
                    cmd = getContext().getString(R.string.cmd_mousepad_click) + " " + "4";
                }
            }
        }
        SessionController.getSessionController().x11Shell(cmd);
    }

    public void OnXMouseClicked(ClickType type)
    {
        String cmd = "";
        switch(type)
        {
            case Left_click:
                cmd = getContext().getString(R.string.cmd_mousepad_click) + " " + "1";
                break;
            case Right_click:
                cmd = getContext().getString(R.string.cmd_mousepad_click) + " " + "3";
                break;
            case Drag_Down:
                cmd = getContext().getString(R.string.cmd_mousepad_mouse_down) + " " + "1";
                break;
            case Drag_up:
                cmd = getContext().getString(R.string.cmd_mousepad_mouse_up) + " " + "1";
                break;
            case Zoom_in:
                cmd = getContext().getString(R.string.cmd_mousepad_key) + " " + "Ctrl+plus";
                break;
            case Zoom_out:
                cmd = getContext().getString(R.string.cmd_mousepad_key) + " " + "Ctrl+minus";
                break;
            default:
                break;
        }
        SessionController.getSessionController().x11Shell(cmd);
    }
}
