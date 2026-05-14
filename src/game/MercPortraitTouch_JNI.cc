#ifdef __ANDROID__

#include <SDL.h>
#include <jni.h>
#include "JAScreens.h"
#include "ScreenIDs.h"
#include "Strategic/MapScreen.h"
#include "Tactical/Interface.h"
#include "Tactical/Interface_Items.h"
#include "Tactical/Interface_Panels.h"
#include "UILayout.h"
#include "TacticalScaling.h"

extern "C" JNIEXPORT jboolean JNICALL
Java_org_libsdl_app_SDLActivity_selectTeamPanelMercPortraitAt(JNIEnv* env, jclass cls, jfloat xNorm, jfloat yNorm)
{
	float x = xNorm * SCREEN_WIDTH;
	float y = yNorm * SCREEN_HEIGHT;
	TacticalScaling::ScreenToTacticalLogicalPoint(x, y);
	return HandleTeamPanelMercPortraitTouch(static_cast<INT16>(x), static_cast<INT16>(y)) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_org_libsdl_app_SDLActivity_isTeamPanelMercPortraitAt(JNIEnv* env, jclass cls, jfloat xNorm, jfloat yNorm)
{
	float x = xNorm * SCREEN_WIDTH;
	float y = yNorm * SCREEN_HEIGHT;
	TacticalScaling::ScreenToTacticalLogicalPoint(x, y);
	return IsTeamPanelMercPortraitAt(static_cast<INT16>(x), static_cast<INT16>(y)) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_org_libsdl_app_SDLActivity_selectAllTeamPanelMercs(JNIEnv* env, jclass cls)
{
	return HandleTeamPanelMercPortraitLongPress() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_org_libsdl_app_SDLActivity_toggleTacticalPanels(JNIEnv* env, jclass cls)
{
	if (guiCurrentScreen != GAME_SCREEN) return;
	if (fInMapMode || gfInItemPickupMenu || gpItemPointer != NULL) return;
	ToggleTacticalPanels();
}

extern "C" JNIEXPORT jfloat JNICALL
Java_org_libsdl_app_SDLActivity_getTacticalBottomPanelTopRatio(JNIEnv* env, jclass cls)
{
	if (guiCurrentScreen != GAME_SCREEN || SCREEN_HEIGHT <= 0) return 1.0f;

	SDL_Rect panelDest;
	if (!TacticalScaling::GetPanelDestinationRect(panelDest))
	{
		return 1.0f;
	}

	return static_cast<jfloat>(panelDest.y) / static_cast<jfloat>(SCREEN_HEIGHT);
}

#endif
