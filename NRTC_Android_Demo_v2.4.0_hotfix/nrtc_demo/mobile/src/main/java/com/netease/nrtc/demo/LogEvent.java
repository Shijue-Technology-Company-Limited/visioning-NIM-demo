package com.netease.nrtc.demo;

import android.content.Context;
import android.util.Log;
import java.util.Map;

/**
 * Created by liuqijun on 6/2/16.
 */
public class LogEvent {

    public static void logEvent(Context context, String tag, Map<String, Object> event) {
        
        Log.i(tag, event.toString());
    }
}
