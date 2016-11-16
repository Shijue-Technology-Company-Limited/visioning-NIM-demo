////////////////////////////////////////////////////////////////////////////////
///
/// Example Interface class for SoundTouch native compilation
///
/// Author        : Copyright (c) Olli Parviainen
/// Author e-mail : oparviai 'at' iki.fi
/// WWW           : http://www.surina.net
///
////////////////////////////////////////////////////////////////////////////////
//
// $Id: soundtouch-jni.cpp 212 2015-05-15 10:22:36Z oparviai $
//
////////////////////////////////////////////////////////////////////////////////

#include <jni.h>
#include <android/log.h>
#include <stdexcept>
#include <string>

using namespace std;

#include "include/SoundTouch.h"

#define LOGV(...)   __android_log_print((int)ANDROID_LOG_INFO, "SOUNDTOUCH", __VA_ARGS__)
//#define LOGV(...)


// String for keeping possible c++ exception error messages. Notice that this isn't
// thread-safe but it's expected that exceptions are special situations that won't
// occur in several threads in parallel.
static string _errMsg = "";


#define DLL_PUBLIC __attribute__ ((visibility ("default")))
#define BUFF_SIZE 4096


using namespace soundtouch;


// Set error message to return
static void _setErrmsg(const char *msg)
{
	_errMsg = msg;
}


#ifdef _OPENMP

#include <pthread.h>
extern pthread_key_t gomp_tls_key;
static void * _p_gomp_tls = NULL;

/// Function to initialize threading for OpenMP.
///
/// This is a workaround for bug in Android NDK v10 regarding OpenMP: OpenMP works only if
/// called from the Android App main thread because in the main thread the gomp_tls storage is
/// properly set, however, Android does not properly initialize gomp_tls storage for other threads.
/// Thus if OpenMP routines are invoked from some other thread than the main thread,
/// the OpenMP routine will crash the application due to NULL pointer access on uninitialized storage.
///
/// This workaround stores the gomp_tls storage from main thread, and copies to other threads.
/// In order this to work, the Application main thread needws to call at least "getVersionString"
/// routine.
static int _init_threading(bool warn)
{
	void *ptr = pthread_getspecific(gomp_tls_key);
	LOGV("JNI thread-specific TLS storage %ld", (long)ptr);
	if (ptr == NULL)
	{
		LOGV("JNI set missing TLS storage to %ld", (long)_p_gomp_tls);
		pthread_setspecific(gomp_tls_key, _p_gomp_tls);
	}
	else
	{
		LOGV("JNI store this TLS storage");
		_p_gomp_tls = ptr;
	}
	// Where critical, show warning if storage still not properly initialized
	if ((warn) && (_p_gomp_tls == NULL))
	{
		_setErrmsg("Error - OpenMP threading not properly initialized: Call SoundTouch.getVersionString() from the App main thread!");
		return -1;
	}
	return 0;
}

#else
static int _init_threading(bool warn)
{
	// do nothing if not OpenMP build
	return 0;
}
#endif






extern "C" DLL_PUBLIC jstring Java_com_netease_nrtc_effect_audio_AudioNativeLibrary_getVersionString(JNIEnv *env, jobject thiz)
{
    const char *verStr;

    LOGV("JNI call SoundTouch.getVersionString");

    // Call example SoundTouch routine
    verStr = SoundTouch::getVersionString();

    /// gomp_tls storage bug workaround - see comments in _init_threading() function!
    _init_threading(false);

    int threads = 0;
	#pragma omp parallel
    {
		#pragma omp atomic
    	threads ++;
    }
    LOGV("JNI thread count %d", threads);

    // return version as string
    return env->NewStringUTF(verStr);
}



extern "C" DLL_PUBLIC jlong Java_com_netease_nrtc_effect_audio_AudioNativeLibrary_newInstance(JNIEnv *env, jobject thiz)
{
    SoundTouch* pSoundTouch = new SoundTouch();
    pSoundTouch->setSetting(SETTING_SEQUENCE_MS, 10);
    pSoundTouch->setSetting(SETTING_SEEKWINDOW_MS, 15);
    pSoundTouch->setSetting(SETTING_OVERLAP_MS, 6);
    pSoundTouch->setSetting(SETTING_USE_AA_FILTER, 0);
	return (jlong)(pSoundTouch);
}


extern "C" DLL_PUBLIC void Java_com_netease_nrtc_effect_audio_AudioNativeLibrary_deleteInstance(JNIEnv *env, jobject thiz, jlong handle)
{
	SoundTouch *ptr = (SoundTouch*)handle;
	delete ptr;
}


extern "C" DLL_PUBLIC void Java_com_netease_nrtc_effect_audio_AudioNativeLibrary_setTempo(JNIEnv *env, jobject thiz, jlong handle, jfloat tempo)
{
	SoundTouch *ptr = (SoundTouch*)handle;
	ptr->setTempo(tempo);
}


extern "C" DLL_PUBLIC void Java_com_netease_nrtc_effect_audio_AudioNativeLibrary_setPitchSemiTones(JNIEnv *env, jobject thiz, jlong handle, jfloat pitch)
{
	SoundTouch *ptr = (SoundTouch*)handle;
	ptr->setPitchSemiTones(pitch);
}


extern "C" DLL_PUBLIC void Java_com_netease_nrtc_effect_audio_AudioNativeLibrary_setSpeed(JNIEnv *env, jobject thiz, jlong handle, jfloat speed)
{
	SoundTouch *ptr = (SoundTouch*)handle;
	ptr->setRate(speed);
}


extern "C" DLL_PUBLIC jstring Java_com_netease_nrtc_effect_audio_AudioNativeLibrary_getErrorString(JNIEnv *env, jobject thiz)
{
	jstring result = env->NewStringUTF(_errMsg.c_str());
	_errMsg.clear();

	return result;
}


// Processes the sound file
static void _process(SoundTouch *pSoundTouch, uint8_t* data, int data_len, int channels, int sample_rate)
{
    pSoundTouch->setSampleRate(sample_rate);
    pSoundTouch->setChannels(channels);

    int samples = channels * data_len / sizeof(int16_t);

    pSoundTouch->putSamples((int16_t*)data, samples);

    int total = 0;
    int sample = 0;
    do
    {
       sample = pSoundTouch->receiveSamples((int16_t*)data + total, samples - total);
       total += sample;
    } while (sample != 0);

}

extern "C" DLL_PUBLIC int Java_com_netease_nrtc_effect_audio_AudioNativeLibrary_process(JNIEnv *env,
                                                                        jobject thiz, 
                                                                        jlong handle, 
                                                                        jbyteArray data, 
                                                                        jint dataLen, 
                                                                        jint channels, 
                                                                        jint sampleRate)
{
	SoundTouch *ptr = (SoundTouch*)handle;

	uint8_t *data_bytes = (uint8_t*)env->GetByteArrayElements(data, 0);


    /// gomp_tls storage bug workaround - see comments in _init_threading() function!
    if (_init_threading(true)) return -1;

	try
	{
		_process(ptr, data_bytes, dataLen, channels, sampleRate);
	}
	catch (const runtime_error &e)
    {
		const char *err = e.what();
        // An exception occurred during processing, return the error message
    	LOGV("JNI exception in SoundTouch::processFile: %s", err);
        _setErrmsg(err);
        return -1;
    }
    
    env->ReleaseByteArrayElements(data, (jbyte*)data_bytes, JNI_COMMIT);

	return 0;
}
