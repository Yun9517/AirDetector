package com.microjet.airqi2.CustomAPI;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.microjet.airqi2.R;

/**
 * Created by B00055 on 2018/3/27.
 */

public class TrebuchetTextView extends android.support.v7.widget.AppCompatTextView {

    public TrebuchetTextView(Context context) {
        super(context);

        init();
    }

    public TrebuchetTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public TrebuchetTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {
        if (!isInEditMode()) {
            final Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), getContext().getString(R.string.normal_font_path));
            setTypeface(typeface);
        }
    }
}
