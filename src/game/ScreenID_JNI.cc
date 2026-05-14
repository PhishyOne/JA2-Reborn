#ifdef __ANDROID__

#include <jni.h>
#include "JAScreens.h"

extern "C" JNIEXPORT jint JNICALL
Java_org_libsdl_app_SDLActivity_getJa2ScreenId(JNIEnv* env, jclass cls)
{
    return static_cast<jint>(guiCurrentScreen);
}

#endif
