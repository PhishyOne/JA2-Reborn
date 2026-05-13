package com.ja2.reborn

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheatConfig(
    @SerialName("enabled")
    val enabled: Boolean = false,
    @SerialName("god_mode")
    val godMode: Boolean = false,
    @SerialName("non_lethal_player_damage")
    val nonLethalPlayerDamage: Boolean = false,
    @SerialName("full_medical_healing")
    val fullMedicalHealing: Boolean = false,
    @SerialName("unlimited_ammo")
    val unlimitedAmmo: Boolean = false,
    @SerialName("no_weapon_jam")
    val noWeaponJam: Boolean = false,
    @SerialName("unlimited_ap")
    val unlimitedAP: Boolean = false,
    @SerialName("unlimited_breath")
    val unlimitedBreath: Boolean = false,
    @SerialName("reveal_enemies")
    val revealEnemies: Boolean = false,
    @SerialName("reveal_items")
    val revealItems: Boolean = false,
    @SerialName("one_hit_kill")
    val oneHitKill: Boolean = false,
    @SerialName("perfect_hit_chance")
    val perfectHitChance: Boolean = false
) {
    companion object {
        val DEFAULT = CheatConfig()
    }
}
