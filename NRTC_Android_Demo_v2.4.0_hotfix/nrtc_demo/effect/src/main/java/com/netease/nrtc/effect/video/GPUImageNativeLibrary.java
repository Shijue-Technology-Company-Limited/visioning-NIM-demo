package com.netease.nrtc.effect.video;

import java.nio.ByteBuffer;

/**
 * Created by liuqijun on 9/6/16.
 * libyuv实现
 */
public class GPUImageNativeLibrary {


    static {
        System.loadLibrary("nrtc_effect_video");
    }


    public static native void NV21ToARGB(ByteBuffer nv21, int width, int height, ByteBuffer argb);
    public static native void ARGBToI420(ByteBuffer argb, int width, int height, ByteBuffer i420);
    public static native void RGBAToI420(ByteBuffer rgba, int width, int height, ByteBuffer i420);
    public static native void ABGRToI420(ByteBuffer abgr, int width, int height, ByteBuffer i420);
    public static native void NV21ToRBGA(ByteBuffer nv21, int width, int height, int[] out);


}
