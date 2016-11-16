package com.netease.nrtc.effect.video;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;

import com.netease.nrtc.effect.utils.ThreadUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;


/**
 * Created by liuqijun on 9/6/16.
 * <p>
 * 仅仅进行美颜处理, 不裁剪和旋转图像
 */
public class GPUImage {

    private Handler mHandler;
    private final Object mHandlerLock = new Object();
    private GPUImageFilter mFilter;
    private GPUImageRenderer mRenderer;
    private PixelBuffer mPixelBuffer;
    private ByteBuffer mRgbBuffer;
    private IntBuffer mRgbIntBuffer;
    private int mWidth;
    private int mHeight;


    public static GPUImage create(final Context context) {
        final HandlerThread thread = new HandlerThread("nrtc_effect");
        thread.start();
        final Handler handler = new Handler(thread.getLooper());

        return ThreadUtils.invokeAtFrontUninterruptibly(handler, new Callable<GPUImage>() {
            @Override
            public GPUImage call() {
                try {
                    return new GPUImage(context, handler);
                } catch (RuntimeException e) {
                    return null;
                }
            }
        });
    }

    private boolean maybePostOnEffectThread(Runnable runnable) {
        return maybePostDelayedOnEffectThread(0, runnable);
    }

    private boolean maybePostDelayedOnEffectThread(int delayMs, Runnable runnable) {
        synchronized (mHandlerLock) {
            return mHandler != null
                    && mHandler.postAtTime(
                    runnable, this, SystemClock.uptimeMillis() + delayMs);
        }
    }


    private GPUImage(final Context context, Handler handler) {
        if (!isEffectSupported(context)) {
            throw new IllegalStateException("OpenGL ES 2.0 is not supported on this phone.");
        }

        synchronized (mHandlerLock) {
            mFilter = new GPUImageFilter();
            mRenderer = new GPUImageRenderer(mFilter);
            mHandler = handler;
            mWidth = -1;
            mHeight = -1;
        }
    }


    private boolean isEffectSupported(final Context context) {
        final ActivityManager activityManager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 0x20000;
    }


    public void setFilter(final GPUImageFilter filter) {
        maybePostOnEffectThread(new Runnable() {
            @Override
            public void run() {
                mFilter = filter;
                mRenderer.setFilter(mFilter);
            }
        });
    }

    public void dispose() {
        maybePostOnEffectThread(new Runnable() {
            @Override
            public void run() {
                if (mFilter != null) {
                    mFilter.destroy();
                    mFilter = null;
                }

                if (mRenderer != null) {
                    mRenderer.deleteImage();
                }

                if (mPixelBuffer != null) {
                    mPixelBuffer.destroy();
                    mPixelBuffer = null;
                }

            }
        });

        synchronized (mHandlerLock) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mHandler.getLooper().quitSafely();
            } else {
                mHandler.getLooper().quit();
            }
            mHandler = null;
        }
    }

    //简单示例, 目前输入NV21, 尺寸不改变
    public int apply(final ByteBuffer data, final int width, final int height) {

        final CountDownLatch barrier = new CountDownLatch(1);
        final boolean didPost = maybePostOnEffectThread(new Runnable() {
            @Override
            public void run() {

                if (width != mWidth || height != mHeight) {

                    if (mPixelBuffer != null) {
                        mPixelBuffer.destroy();
                        mPixelBuffer = null;
                    }

                    if (mRenderer != null) {
                        mRenderer.deleteImage();
                        mRenderer = null;
                    }

                    if (mRenderer == null) {
                        mRenderer = new GPUImageRenderer(mFilter);
                    }

                    if (mPixelBuffer == null) {
                        mPixelBuffer = new PixelBuffer(width, height);
                        mPixelBuffer.setRenderer(mRenderer);
                    }

                    mRgbIntBuffer = IntBuffer.allocate(width * height);

                    mWidth = width;
                    mHeight = height;

                }
                Log.d("GPUProcess", data.limit() + "/start");
                GPUImageNativeLibrary.NV21ToRBGA(data, width, height, mRgbIntBuffer.array());
                mRenderer.uploadData(mRgbIntBuffer, width, height);
                ByteBuffer rgba = mPixelBuffer.getPixels();
                GPUImageNativeLibrary.ABGRToI420(rgba, width, height, data);
                Log.d("GPUProcess", data.limit() + "/end");
                barrier.countDown();
            }
        });

        if (didPost) {
            ThreadUtils.awaitUninterruptibly(barrier);
            return 0;
        }
        return -1;
    }


}
