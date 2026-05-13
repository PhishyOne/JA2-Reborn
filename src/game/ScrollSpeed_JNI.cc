#ifdef __ANDROID__

#include <jni.h>
#include "Utils/Timer_Control.h"

extern "C" JNIEXPORT void JNICALL
Java_org_libsdl_app_SDLActivity_setScrollSpeed(JNIEnv* env, jclass cls, jint ms)
{
	SetScrollSpeed(ms);
}

extern "C" JNIEXPORT jint JNICALL
Java_org_libsdl_app_SDLActivity_getScrollSpeed(JNIEnv* env, jclass cls)
{
	return GetScrollSpeed();
}

#endif
