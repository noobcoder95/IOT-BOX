package com.smartiotdevices.iotbox;

import android.content.Context;
import android.text.InputType;
import android.text.Layout;
import android.text.Selection;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

import java.util.Objects;

public class ResultText extends androidx.appcompat.widget.AppCompatEditText
{

    public ResultText(Context context)
    {
        super(context);
        setup();
    }

    public ResultText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setup();
    }

    public ResultText(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        setup();
    }

    public void setup()
    {
        this.setRawInputType(InputType.TYPE_CLASS_TEXT);
        this.setImeOptions(EditorInfo.IME_ACTION_GO);
    }

    @Override
    protected void onSelectionChanged(int s, int e)
    {
        setSelection(this.length());
    }

    public int getCurrentCursorLine()
    {
        int selectionStart = Selection.getSelectionStart(this.getText());
        Layout layout = this.getLayout();

        if (!(selectionStart == -1))
        {
            return layout.getLineForOffset(selectionStart);
        }
        return -1;
    }

    public boolean isNewLine()
    {
        int i = Objects.requireNonNull(this.getText()).toString().toCharArray().length;
        if(i == 0)
            return true;

        char s = this.getText().toString().toCharArray()[i - 1];
        return s == '\n' || s == '\r';
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs)
    {
        InputConnection ic = super.onCreateInputConnection(outAttrs);
        if (ic == null)
        {
            return null;
        }
        return new SshConnectionWrapper(super.onCreateInputConnection(outAttrs), true);
    }

    private class SshConnectionWrapper extends InputConnectionWrapper
    {
        SshConnectionWrapper(InputConnection target, boolean mutable)
        {
            super(target, mutable);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event)
        {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DEL)
            {
                if(isNewLine())
                {
                    return false;
                }

                else if(getCurrentCursorLine() < getLineCount() - 1)
                {
                    return false;
                }
            }
            return super.sendKeyEvent(event);
        }

        @Override
        public boolean deleteSurroundingText (int beforeLength, int afterLength)
        {
            if(isNewLine())
            {
                return false;
            }

            else if(getCurrentCursorLine() < getLineCount() - 1)
            {
                return false;
            }

            else
            {
                return super.deleteSurroundingText(beforeLength, afterLength);
            }
        }
    }
}
