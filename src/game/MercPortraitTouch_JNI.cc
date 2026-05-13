#ifdef __ANDROID__

#include <jni.h>
#include "JAScreens.h"
#include "ScreenIDs.h"
#include "Strategic/MapScreen.h"
#include "Tactical/Interface.h"
#include "Tactical/Interface_Items.h"
#include "Tactical/Interface_Panels.h"
#include "UILayout.h"

extern "C" JNIEXPORT jboolean JNICALL
Java_org_libsdl_app_SDLActivity_selectTeamPanelMercPortraitAt(JNIEnv* env, jclass cls, jfloat xNorm, jfloat yNorm)
{
	const INT16 x = static_cast<INT16>(xNorm * SCREEN_WIDTH);
	const INT16 y = static_cast<INT16>(yNorm * SCREEN_HEIGHT);
	return HandleTeamPanelMercPortraitTouch(x, y) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_org_libsdl_app_SDLActivity_isTeamPanelMercPortraitAt(JNIEnv* env, jclass cls, jfloat xNorm, jfloat yNorm)
{
	const INT16 x = static_cast<INT16>(xNorm * SCREEN_WIDTH);
	const INT16 y = static_cast<INT16>(yNorm * SCREEN_HEIGHT);
	return IsTeamPanelMercPortraitAt(x, y) ? JNI_TRUE : JNI_FALSE;
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
	switch (gsCurInterfacePanel)
	{
		case SM_PANEL:
			return static_cast<jfloat>(INV_INTERFACE_START_Y) / static_cast<jfloat>(SCREEN_HEIGHT);
		case TEAM_PANEL:
			return static_cast<jfloat>(INTERFACE_START_Y) / static_cast<jfloat>(SCREEN_HEIGHT);
		default:
			return 1.0f;
	}
}

#endif
