#ifdef __ANDROID__

#include <jni.h>
#include "CheatSystem.h"
#include "Tactical/Overhead.h"

enum CheatActionId : int
{
	CHEAT_ACTION_HEAL_TEAM = 0,
	CHEAT_ACTION_RELOAD_TEAM = 1,
	CHEAT_ACTION_RELOAD_SELECTED = 2,
	CHEAT_ACTION_GRANT_MONEY = 3
};

extern "C" JNIEXPORT void JNICALL
Java_org_libsdl_app_SDLActivity_setCheat(JNIEnv* env, jclass cls, jint cheatId, jboolean enabled)
{
	CheatSystem::setCheat(static_cast<CheatId>(cheatId), enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_org_libsdl_app_SDLActivity_runCheatAction(JNIEnv* env, jclass cls, jint actionId, jint amount)
{
	switch (static_cast<CheatActionId>(actionId))
	{
		case CHEAT_ACTION_HEAL_TEAM:
			CheatSystem::healPlayerTeam();
			break;
		case CHEAT_ACTION_RELOAD_TEAM:
			CheatSystem::reloadPlayerTeamWeapons();
			break;
		case CHEAT_ACTION_RELOAD_SELECTED:
			CheatSystem::reloadSelectedMercWeapon(GetSelectedMan());
			break;
		case CHEAT_ACTION_GRANT_MONEY:
			CheatSystem::grantMoney(amount);
			break;
		default:
			break;
	}
}

#endif
