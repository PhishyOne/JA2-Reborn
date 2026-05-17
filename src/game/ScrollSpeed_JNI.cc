#ifdef __ANDROID__

#include <jni.h>
#include "Utils/Timer_Control.h"
#include "TileEngine/RenderWorld.h"

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

extern "C" JNIEXPORT void JNICALL
Java_org_libsdl_app_SDLActivity_setMouseScrollingDisabled(JNIEnv* env, jclass cls, jboolean disabled)
{
	SetMouseScrollingDisabled(disabled == JNI_TRUE);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_org_libsdl_app_SDLActivity_isMouseScrollingDisabled(JNIEnv* env, jclass cls)
{
	return IsMouseScrollingDisabled() ? JNI_TRUE : JNI_FALSE;
}

#endif
