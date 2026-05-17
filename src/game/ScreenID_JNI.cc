#ifdef __ANDROID__

#include <jni.h>
#include "JAScreens.h"
#include "UILayout.h"

extern "C" JNIEXPORT jint JNICALL
Java_org_libsdl_app_SDLActivity_getJa2ScreenId(JNIEnv* env, jclass cls)
{
    return static_cast<jint>(guiCurrentScreen);
}

extern "C" JNIEXPORT jint JNICALL
Java_org_libsdl_app_SDLActivity_getJa2ScreenWidth(JNIEnv* env, jclass cls)
{
    return static_cast<jint>(SCREEN_WIDTH);
}

extern "C" JNIEXPORT jint JNICALL
Java_org_libsdl_app_SDLActivity_getJa2ScreenHeight(JNIEnv* env, jclass cls)
{
    return static_cast<jint>(SCREEN_HEIGHT);
}

#endif
