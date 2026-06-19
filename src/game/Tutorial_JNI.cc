#ifdef __ANDROID__

#include <jni.h>
#include "JAScreens.h"
#include "TutorialSystem.h"

extern "C" JNIEXPORT void JNICALL
Java_org_libsdl_app_SDLActivity_showTutorial(JNIEnv* env, jclass cls)
{
    if (guiCurrentScreen == GAME_SCREEN && !gTutorial.fVisible)
    {
        EnterTutorial();
    }
}

extern "C" JNIEXPORT void JNICALL
Java_org_libsdl_app_SDLActivity_setTutorialLanguage(JNIEnv* env, jclass cls, jboolean german)
{
    SetTutorialLanguageGerman(german == JNI_TRUE);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_org_libsdl_app_SDLActivity_isTutorialVisible(JNIEnv* env, jclass cls)
{
    return gTutorial.fVisible ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_org_libsdl_app_SDLActivity_requestTouchPresetUpdateNotice(JNIEnv* env, jclass cls, jint version)
{
    RequestTouchPresetUpdateNotice(static_cast<int>(version));
}

#endif
