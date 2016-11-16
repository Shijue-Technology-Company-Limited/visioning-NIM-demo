package com.netease.nrtc.demo.utils;


import android.graphics.Paint;
import android.util.TypedValue;
import android.widget.TextView;

import com.netease.nrtc.demo.R;

public class Util {

    public static void resizeText(TextView textView, int originalTextSize, int minTextSize) {

        final Paint paint = textView.getPaint();
        final int width = textView.getWidth();
        if (width == 0) return;
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalTextSize);
        float ratio = width / paint.measureText(textView.getText().toString());
        if (ratio <= 1.0f) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    Math.max(minTextSize, originalTextSize * ratio));
        }

    }


}
