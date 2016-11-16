package com.netease.nrtc.effect.audio;

/**
 * Created by liuqijun on 9/12/16.
 */
public class AudioEffect {

    long handle = 0;

    public AudioEffect() {
        handle = AudioNativeLibrary.newInstance();
    }


    public void close() {
        AudioNativeLibrary.deleteInstance(handle);
        handle = 0;
    }


    public void setTempo(float tempo) {
        AudioNativeLibrary.setTempo(handle, tempo);
    }


    public void setPitchSemiTones(float pitch) {
        AudioNativeLibrary.setPitchSemiTones(handle, pitch);
    }


    public void setSpeed(float speed) {
        AudioNativeLibrary.setSpeed(handle, speed);
    }


    public int process(byte[] data,
                       int dataLen,
                       int channels,
                       int sampleRate) {
        return AudioNativeLibrary.process(handle, data, dataLen, channels, sampleRate);
    }

}
