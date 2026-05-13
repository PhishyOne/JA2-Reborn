#include "CheatSystem.h"
#include "Json.h"
#include "FileMan.h"
#include "Logger.h"
#include "Tactical/Overhead.h"
#include "Tactical/Soldier_Control.h"
#include "Tactical/Weapons.h"
#include "Tactical/Interface.h"
#include "Laptop/Finances.h"
#include "Strategic/Game_Clock.h"
#include "TileEngine/RenderWorld.h"

static CheatSettings gCheatSettings;

static void applyRevealFlags()
{
	if (CheatSystem::revealEnemies())
		gTacticalStatus.uiFlags |= SHOW_ALL_MERCS;
	else
		gTacticalStatus.uiFlags &= ~SHOW_ALL_MERCS;

	if (CheatSystem::revealItems())
		gTacticalStatus.uiFlags |= SHOW_ALL_ITEMS;
	else
		gTacticalStatus.uiFlags &= ~SHOW_ALL_ITEMS;

	SetRenderFlags(RENDER_FLAG_FULL);
}

void CheatSystem::refreshRuntimeFlags()
{
	applyRevealFlags();
}

void CheatSystem::setSettings(const CheatSettings& s)
{
	gCheatSettings = s;
	applyRevealFlags();
}

void CheatSystem::setCheat(CheatId id, bool value)
{
	bool revealStateMayHaveChanged = false;
	switch (id)
	{
		case CHEAT_ENABLED:
			gCheatSettings.enabled = value;
			revealStateMayHaveChanged = true;
			break;
		case CHEAT_GOD_MODE:                gCheatSettings.godMode               = value; break;
		case CHEAT_NON_LETHAL_PLAYER_DAMAGE: gCheatSettings.nonLethalPlayerDamage = value; break;
		case CHEAT_FULL_MEDICAL_HEALING:    gCheatSettings.fullMedicalHealing    = value; break;
		case CHEAT_UNLIMITED_AMMO:          gCheatSettings.unlimitedAmmo         = value; break;
		case CHEAT_NO_WEAPON_JAM:           gCheatSettings.noWeaponJam           = value; break;
		case CHEAT_UNLIMITED_AP:            gCheatSettings.unlimitedAP           = value; break;
		case CHEAT_UNLIMITED_BREATH:        gCheatSettings.unlimitedBreath       = value; break;
		case CHEAT_REVEAL_ENEMIES:
			gCheatSettings.revealEnemies = value;
			revealStateMayHaveChanged = true;
			break;
		case CHEAT_REVEAL_ITEMS:
			gCheatSettings.revealItems = value;
			revealStateMayHaveChanged = true;
			break;
		case CHEAT_ONE_HIT_KILL:            gCheatSettings.oneHitKill            = value; break;
		case CHEAT_PERFECT_HIT_CHANCE:      gCheatSettings.perfectHitChance      = value; break;
		default: break;
	}
	if (revealStateMayHaveChanged)
	{
		applyRevealFlags();
	}
}

const CheatSettings& CheatSystem::settings()
{
	return gCheatSettings;
}

bool CheatSystem::enabled()
{
	return gCheatSettings.enabled;
}

bool CheatSystem::godMode()
{
	return gCheatSettings.enabled && gCheatSettings.godMode;
}

bool CheatSystem::nonLethalPlayerDamage()
{
	return gCheatSettings.enabled && !gCheatSettings.godMode && gCheatSettings.nonLethalPlayerDamage;
}

bool CheatSystem::fullMedicalHealing()
{
	return gCheatSettings.enabled && gCheatSettings.fullMedicalHealing;
}

bool CheatSystem::unlimitedAmmo()
{
	return gCheatSettings.enabled && gCheatSettings.unlimitedAmmo;
}

bool CheatSystem::noWeaponJam()
{
	return gCheatSettings.enabled && gCheatSettings.noWeaponJam;
}

bool CheatSystem::unlimitedAP()
{
	return gCheatSettings.enabled && gCheatSettings.unlimitedAP;
}

bool CheatSystem::unlimitedBreath()
{
	return gCheatSettings.enabled && gCheatSettings.unlimitedBreath;
}

bool CheatSystem::autoHealTeam()
{
	return gCheatSettings.enabled && gCheatSettings.autoHealTeam;
}

bool CheatSystem::revealEnemies()
{
	return gCheatSettings.enabled && gCheatSettings.revealEnemies;
}

bool CheatSystem::revealItems()
{
	return gCheatSettings.enabled && gCheatSettings.revealItems;
}

bool CheatSystem::oneHitKill()
{
	return gCheatSettings.enabled && gCheatSettings.oneHitKill;
}

bool CheatSystem::perfectHitChance()
{
	return gCheatSettings.enabled && gCheatSettings.perfectHitChance;
}

bool CheatSystem::appliesToPlayerSoldier(const SOLDIERTYPE* s)
{
	return s != nullptr && s->bTeam == OUR_TEAM;
}

bool CheatSystem::appliesToEnemyTarget(const SOLDIERTYPE* s)
{
	return s != nullptr && s->bTeam != OUR_TEAM;
}

void CheatSystem::healPlayerTeam()
{
	FOR_EACH_IN_TEAM(s, OUR_TEAM)
	{
		if (s->bLife <= 0) continue;

		s->bBreath    = s->bBreathMax;
		s->bLife      = s->bLifeMax;
		s->bBleeding  = 0;
		s->sBreathRed = 0;

		DirtyMercPanelInterface(s, DIRTYLEVEL2);
	}
}

void CheatSystem::reloadPlayerTeamWeapons()
{
	FOR_EACH_IN_TEAM(s, OUR_TEAM)
	{
		if (s->bLife <= 0) continue;
		ReloadWeapon(s, s->ubAttackingHand);
	}
}

void CheatSystem::reloadSelectedMercWeapon(SOLDIERTYPE* s)
{
	if (!s || s->bLife <= 0) return;
	ReloadWeapon(s, s->ubAttackingHand);
}

void CheatSystem::grantMoney(INT32 amount)
{
	AddTransactionToPlayersBook(ANONYMOUS_DEPOSIT, 0, GetWorldTotalMin(), amount);
}

bool CheatSystem::loadFromFile(const char* configFolderPath)
{
	ST::string path = FileMan::joinPaths(ST::string(configFolderPath), "cheats.json");
	if (!FileMan::isFile(path))
	{
		return false;
	}

	try
	{
		AutoSGPFile f(FileMan::openForReading(path));
		ST::string content = f->readStringToEnd();
		JsonValue root = JsonValue::deserialize(content);
		JsonObject obj = root.toObject();

		CheatSettings s;
		s.enabled               = obj.getOptionalBool("enabled", false);
		s.godMode               = obj.getOptionalBool("god_mode", false);
		s.nonLethalPlayerDamage = obj.getOptionalBool("non_lethal_player_damage", false);
		s.fullMedicalHealing    = obj.getOptionalBool("full_medical_healing", false);
		s.unlimitedAmmo         = obj.getOptionalBool("unlimited_ammo", false);
		s.noWeaponJam           = obj.getOptionalBool("no_weapon_jam", false);
		s.unlimitedAP           = obj.getOptionalBool("unlimited_ap", false);
		s.unlimitedBreath       = obj.getOptionalBool("unlimited_breath", false);
		s.autoHealTeam          = obj.getOptionalBool("auto_heal_team", false);
		s.revealEnemies         = obj.getOptionalBool("reveal_enemies", false);
		s.revealItems           = obj.getOptionalBool("reveal_items", false);
		s.oneHitKill            = obj.getOptionalBool("one_hit_kill", false);
		s.perfectHitChance      = obj.getOptionalBool("perfect_hit_chance", false);

		setSettings(s);
		return true;
	}
	catch (...)
	{
		SLOGW("Failed to parse cheats.json, using defaults");
		return false;
	}
}
