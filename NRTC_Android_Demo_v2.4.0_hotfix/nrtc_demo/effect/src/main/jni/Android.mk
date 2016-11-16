
ROOT_PATH:= $(call my-dir)
CPP_PATH:= $(ROOT_PATH)/../cpp
include $(CPP_PATH)/libyuv/Android.mk

include $(CLEAR_VARS)
LOCAL_PATH := $(ROOT_PATH)

LOCAL_ARM_MODE := arm
LOCAL_MODULE    := nrtc_effect_video

LOCAL_SRC_FILES := $(CPP_PATH)/video_effect_jni.cpp

LOCAL_STATIC_LIBRARIES := libyuv_static

LOCAL_C_INCLUDES := $(CPP_PATH)/libyuv

include $(BUILD_SHARED_LIBRARY)




include $(CLEAR_VARS)

LOCAL_MODULE    := nrtc_effect_audio
LOCAL_SRC_FILES :=  $(CPP_PATH)/audio_effect_jni.cpp \
                    $(CPP_PATH)/soundtouch/source/SoundTouch/AAFilter.cpp \
                    $(CPP_PATH)/soundtouch/source/SoundTouch/FIFOSampleBuffer.cpp \
                    $(CPP_PATH)/soundtouch/source/SoundTouch/FIRFilter.cpp \
                    $(CPP_PATH)/soundtouch/source/SoundTouch/cpu_detect_x86.cpp \
                    $(CPP_PATH)/soundtouch/source/SoundTouch/sse_optimized.cpp \
                    $(CPP_PATH)/soundtouch/source/SoundStretch/WavFile.cpp \
                    $(CPP_PATH)/soundtouch/source/SoundTouch/RateTransposer.cpp \
                    $(CPP_PATH)/soundtouch/source/SoundTouch/SoundTouch.cpp \
                    $(CPP_PATH)/soundtouch/source/SoundTouch/InterpolateCubic.cpp \
                    $(CPP_PATH)/soundtouch/source/SoundTouch/InterpolateLinear.cpp \
                    $(CPP_PATH)/soundtouch/source/SoundTouch/InterpolateShannon.cpp \
                    $(CPP_PATH)/soundtouch/source/SoundTouch/TDStretch.cpp \
                    $(CPP_PATH)/soundtouch/source/SoundTouch/BPMDetect.cpp \
                    $(CPP_PATH)/soundtouch/source/SoundTouch/PeakFinder.cpp


LOCAL_C_INCLUDES := $(CPP_PATH)/soundtouch \
                    $(CPP_PATH)/soundtouch/include

# for native audio
LOCAL_SHARED_LIBRARIES += -lgcc
# --whole-archive -lgcc
# for logging
LOCAL_LDLIBS    += -llog
# for native asset manager
#LOCAL_LDLIBS    += -landroid

# Custom Flags:
# -fvisibility=hidden : don't export all symbols
LOCAL_CFLAGS += -fvisibility=hidden -fdata-sections -ffunction-sections -DSOUNDTOUCH_INTEGER_SAMPLES -DSOUNDTOUCH_DISABLE_X86_OPTIMIZATIONS

# OpenMP mode : enable these flags to enable using OpenMP for parallel computation
#LOCAL_CFLAGS += -fopenmp
#LOCAL_LDFLAGS += -fopenmp


# Use ARM instruction set instead of Thumb for improved calculation performance in ARM CPUs
LOCAL_ARM_MODE := arm

include $(BUILD_SHARED_LIBRARY)