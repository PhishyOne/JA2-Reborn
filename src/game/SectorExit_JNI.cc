#ifdef __ANDROID__

#include <jni.h>
#include "JAScreens.h"
#include "Strategic/Campaign_Types.h"
#include "Strategic/Strategic_Movement.h"
#include "Strategic/StrategicMap.h"
#include "Tactical/Overhead_Types.h"
#include "Tactical/Strategic_Exit_GUI.h"

enum AndroidSectorExitDirection : int
{
	ANDROID_SECTOR_EXIT_NORTH = 0,
	ANDROID_SECTOR_EXIT_EAST = 1,
	ANDROID_SECTOR_EXIT_SOUTH = 2,
	ANDROID_SECTOR_EXIT_WEST = 3
};

static bool IsValidAndroidSectorExit(AndroidSectorExitDirection const direction, UINT8& ja2Direction)
{
	if (!gWorldSector.IsValid())
	{
		return false;
	}

	SGPSector target = gWorldSector;
	INT8 strategicMove = -1;
	switch (direction)
	{
		case ANDROID_SECTOR_EXIT_NORTH:
			target.y -= 1;
			ja2Direction = NORTH;
			strategicMove = NORTH_STRATEGIC_MOVE;
			break;
		case ANDROID_SECTOR_EXIT_EAST:
			target.x += 1;
			ja2Direction = EAST;
			strategicMove = EAST_STRATEGIC_MOVE;
			break;
		case ANDROID_SECTOR_EXIT_SOUTH:
			target.y += 1;
			ja2Direction = SOUTH;
			strategicMove = SOUTH_STRATEGIC_MOVE;
			break;
		case ANDROID_SECTOR_EXIT_WEST:
			target.x -= 1;
			ja2Direction = WEST;
			strategicMove = WEST_STRATEGIC_MOVE;
			break;
		default:
			return false;
	}

	if (!target.IsValid())
	{
		return false;
	}

	if (gWorldSector.z == 0 && SectorInfo[gWorldSector.AsByte()].ubTraversability[strategicMove] == EDGEOFWORLD)
	{
		return false;
	}

	return true;
}

extern "C" JNIEXPORT void JNICALL
Java_org_libsdl_app_SDLActivity_showSectorExitMenu(JNIEnv* env, jclass cls, jint direction)
{
	if (guiCurrentScreen != GAME_SCREEN || gfInSectorExitMenu)
	{
		return;
	}

	UINT8 ja2Direction = 0;
	if (!IsValidAndroidSectorExit(static_cast<AndroidSectorExitDirection>(direction), ja2Direction))
	{
		return;
	}

	InitSectorExitMenu(ja2Direction, 0);
}

#endif
