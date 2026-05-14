#pragma once

#include "JA2Types.h"

enum CheatId : int
{
	CHEAT_ENABLED                 = 0,
	CHEAT_GOD_MODE                = 1,
	CHEAT_NON_LETHAL_PLAYER_DAMAGE = 2,
	CHEAT_FULL_MEDICAL_HEALING    = 3,
	CHEAT_UNLIMITED_AMMO          = 4,
	CHEAT_NO_WEAPON_JAM           = 5,
	CHEAT_UNLIMITED_AP            = 6,
	CHEAT_UNLIMITED_BREATH        = 7,
	CHEAT_REVEAL_ENEMIES          = 8,
	CHEAT_REVEAL_ITEMS            = 9,
	CHEAT_ONE_HIT_KILL            = 10,
	CHEAT_PERFECT_HIT_CHANCE      = 11
};

struct CheatSettings
{
	bool enabled              = false;
	bool godMode              = false;
	bool nonLethalPlayerDamage = false;
	bool fullMedicalHealing   = false;
	bool unlimitedAmmo        = false;
	bool noWeaponJam          = false;
	bool unlimitedAP          = false;
	bool unlimitedBreath      = false;
	bool autoHealTeam         = false;
	bool revealEnemies        = false;
	bool revealItems          = false;
	bool oneHitKill           = false;
	bool perfectHitChance     = false;
};

namespace CheatSystem
{

void setSettings(const CheatSettings& s);
const CheatSettings& settings();
void setCheat(CheatId id, bool value);
void refreshRuntimeFlags();

// Master
bool enabled();

// Passive cheats
bool godMode();
bool nonLethalPlayerDamage();
bool fullMedicalHealing();
bool unlimitedAmmo();
bool noWeaponJam();
bool unlimitedAP();
bool unlimitedBreath();
bool autoHealTeam();
bool revealEnemies();
bool revealItems();
bool oneHitKill();
bool perfectHitChance();

// Persistence
bool loadFromFile(const char* configFolderPath);

// One-shot actions
void healPlayerTeam();
void reloadPlayerTeamWeapons();
void reloadSelectedMercWeapon(SOLDIERTYPE* s);
void grantMoney(INT32 amount);

// Helper
bool appliesToPlayerSoldier(const SOLDIERTYPE* s);
bool appliesToEnemyTarget(const SOLDIERTYPE* s);

}
