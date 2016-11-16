/*
*
* @Author: LiuQijun <liuqijun@corp.netease.com>
* @Date: 2017/09/07
*/

#include <jni.h>
#include "../libyuv/include/libyuv.h"



bool CheckException(JNIEnv* jni) {
    if (jni->ExceptionCheck()) {
        jni->ExceptionDescribe();
        jni->ExceptionClear();
        return true;
    }
    return false;
}



extern "C" jint JNIEXPORT JNICALL 
Java_com_netease_nrtc_effect_video_GPUImageNativeLibrary_NV21ToARGB(JNIEnv *jni,
                                                     jclass,
                                                     jobject nv21,
                                                     jint width,
                                                     jint height,
                                                     jobject argb) {
    
    uint8_t *nv21_buffer = reinterpret_cast<uint8_t *>(jni->GetDirectBufferAddress(nv21));
    
    if(CheckException(jni)) return -1;
    
    uint8_t *argb_buffer = reinterpret_cast<uint8_t *>(jni->GetDirectBufferAddress(argb));
    
    if(CheckException(jni)) return -1;
    
 
    const uint8_t* src_y = nv21_buffer;
    const uint8_t* src_vu = nv21_buffer + (width * height);
    int src_stride_y = width;
    int src_stride_vu = (width + 1) >> 1;
    
    return libyuv::NV12ToARGB(src_y, src_stride_y,
                   src_vu, src_stride_vu,
                   argb_buffer, width << 2,
                   width, height);
    
}




extern "C" jint JNIEXPORT JNICALL 
Java_com_netease_nrtc_effect_video_GPUImageNativeLibrary_NV21ToRBGA(JNIEnv *jni,
                                                     jclass,
                                                     jobject nv21,
                                                     jint width,
                                                     jint height,
                                                     jintArray rbga) {
    
    uint8_t *nv21_buffer = reinterpret_cast<uint8_t *>(jni->GetDirectBufferAddress(nv21));
    
    if(CheckException(jni)) return -1;
    
    uint32_t *rbga_buffer = reinterpret_cast<uint32_t *>(jni->GetPrimitiveArrayCritical(rbga, 0));
    
    if(CheckException(jni)) return -1;
    
    
    int             sz;
    int             i;
    int             j;
    int             Y;
    int             Cr = 0;
    int             Cb = 0;
    int             pixPtr = 0;
    int             jDiv2 = 0;
    int             R = 0;
    int             G = 0;
    int             B = 0;
    int             cOff;
    int w = width;
    int h = height;
    sz = w * h;
    
    for(j = 0; j < h; j++) {
        pixPtr = j * w;
        jDiv2 = j >> 1;
        for(i = 0; i < w; i++) {
            Y = nv21_buffer[pixPtr];
            if(Y < 0) Y += 255;
                if((i & 0x1) != 1) {
                    cOff = sz + jDiv2 * w + (i >> 1) * 2;
                    Cb = nv21_buffer[cOff];
                    if(Cb < 0) Cb += 127; else Cb -= 128;
                        Cr = nv21_buffer[cOff + 1];
                        if(Cr < 0) Cr += 127; else Cr -= 128;
                            }
            

            Y = Y + (Y >> 3) + (Y >> 5) + (Y >> 7);
            R = Y + (Cr << 1) + (Cr >> 6);
            if(R < 0) R = 0; else if(R > 255) R = 255;
                G = Y - Cb + (Cb >> 3) + (Cb >> 4) - (Cr >> 1) + (Cr >> 3);
                if(G < 0) G = 0; else if(G > 255) G = 255;
                    B = Y + Cb + (Cb >> 1) + (Cb >> 4) + (Cb >> 5);
                    if(B < 0) B = 0; else if(B > 255) B = 255;
                        rbga_buffer[pixPtr++] = 0xff000000 + (R << 16) + (G << 8) + B;
                        }
    }
    
    jni->ReleasePrimitiveArrayCritical(rbga, rbga_buffer, 0);
    
    return 0;
    
}

extern "C" jint JNIEXPORT JNICALL 
Java_com_netease_nrtc_effect_video_GPUImageNativeLibrary_ABGRToI420(JNIEnv *jni,
                                                     jclass,
                                                     jobject abgr,
                                                     jint width,
                                                     jint height,
                                                     jobject i420) {
    
    uint8_t *i420_buffer = reinterpret_cast<uint8_t *>(jni->GetDirectBufferAddress(i420));
    
    if(CheckException(jni)) return -1;
    
    uint8_t *abgr_buffer = reinterpret_cast<uint8_t *>(jni->GetDirectBufferAddress(abgr));
    
    if(CheckException(jni)) return -1;
    
    
    int i420_stride_y = width;
    int i420_stride_vu = (width + 1) >> 1;
    uint8_t* i420_y = i420_buffer;
    uint8_t* i420_u = i420_y + i420_stride_y * height;
    uint8_t* i420_v = i420_u + i420_stride_vu * ((height + 1) >> 1);
    
    return libyuv::ABGRToI420(abgr_buffer, width << 2,
                              i420_y, i420_stride_y,
                              i420_u, i420_stride_vu,
                              i420_v, i420_stride_vu,
                              width, height);
    
    
}


extern "C" jint JNIEXPORT JNICALL 
Java_com_netease_nrtc_effect_video_GPUImageNativeLibrary_RGBAToI420(JNIEnv *jni,
                                                     jclass,
                                                     jobject rgba,
                                                     jint width,
                                                     jint height,
                                                     jobject i420) {
    
    uint8_t *i420_buffer = reinterpret_cast<uint8_t *>(jni->GetDirectBufferAddress(i420));
    
    if(CheckException(jni)) return -1;
    
    uint8_t *rgba_buffer = reinterpret_cast<uint8_t *>(jni->GetDirectBufferAddress(rgba));
    
    if(CheckException(jni)) return -1;
    
    
    int i420_stride_y = width;
    int i420_stride_vu = (width + 1) >> 1;
    uint8_t* i420_y = i420_buffer;
    uint8_t* i420_u = i420_y + i420_stride_y * height;
    uint8_t* i420_v = i420_u + i420_stride_vu * ((height + 1) >> 1);
    
    return libyuv::RGBAToI420(rgba_buffer, width << 2,
                              i420_y, i420_stride_y,
                              i420_u, i420_stride_vu,
                              i420_v, i420_stride_vu,
                              width, height);
    
    
}



extern "C" jint JNIEXPORT JNICALL 
Java_com_netease_nrtc_effect_video_GPUImageNativeLibrary_ARGBToI420(JNIEnv *jni,
                                                     jclass,
                                                     jobject argb,
                                                     jint width,
                                                     jint height,
                                                     jobject i420) {
    
    uint8_t *i420_buffer = reinterpret_cast<uint8_t *>(jni->GetDirectBufferAddress(i420));
    
    if(CheckException(jni)) return -1;
    
    uint8_t *argb_buffer = reinterpret_cast<uint8_t *>(jni->GetDirectBufferAddress(argb));
    
    if(CheckException(jni)) return -1;
    
    
    int i420_stride_y = width;
    int i420_stride_vu = (width + 1) >> 1;
    uint8_t* i420_y = i420_buffer;
    uint8_t* i420_u = i420_y + i420_stride_y * height;
    uint8_t* i420_v = i420_u + i420_stride_vu * ((height + 1) >> 1);
    
    return libyuv::ARGBToI420(argb_buffer, width << 2,
                              i420_y, i420_stride_y,
                              i420_u, i420_stride_vu,
                              i420_v, i420_stride_vu,
                              width, height);
    
 
}


