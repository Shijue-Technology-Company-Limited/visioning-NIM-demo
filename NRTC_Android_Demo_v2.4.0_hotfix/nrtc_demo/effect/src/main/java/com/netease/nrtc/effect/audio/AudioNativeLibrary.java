package com.netease.nrtc.effect.audio;

/**
 * Created by liuqijun on 9/11/16.
 */
public class AudioNativeLibrary {


    public native final static String getVersionString();

    public native final static void setTempo(long handle, float tempo);

    public native final static void setPitchSemiTones(long handle, float pitch);

    public native final static void setSpeed(long handle, float speed);

    public native final static int process(long handle,
                                     byte[] data,
                                     int dataLen,
                                     int channels,
                                     int sampleRate);

    public native final static String getErrorString();

    public native final static long newInstance();

    public native final static void deleteInstance(long handle);


    static {
        System.loadLibrary("nrtc_effect_audio");
    }

}
