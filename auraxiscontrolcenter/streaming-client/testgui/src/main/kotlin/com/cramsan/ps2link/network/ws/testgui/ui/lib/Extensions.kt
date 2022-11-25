package com.cramsan.ps2link.network.ws.testgui.ui.lib

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.cramsan.ps2link.core.models.CharacterClass
import com.cramsan.ps2link.core.models.KillType
import com.cramsan.ps2link.core.models.LoginStatus
import com.cramsan.ps2link.core.models.MedalType
import com.cramsan.ps2link.core.models.Population
import com.cramsan.ps2link.core.models.ServerStatus
import com.cramsan.ps2link.network.ws.testgui.ui.lib.theme.negative
import com.cramsan.ps2link.network.ws.testgui.ui.lib.theme.positive
import com.cramsan.ps2link.network.ws.testgui.ui.lib.theme.undefined
import com.cramsan.ps2link.network.ws.testgui.ui.lib.theme.warning

/**
 * @Author cramsan
 * @created 1/17/2021
 */

fun Color.setAlpha(alpha: Float) = this.copy(alpha = alpha)

@Composable
fun LoginStatus.toStringResource(): String = when (this) {
    LoginStatus.ONLINE -> "Online"
    LoginStatus.OFFLINE -> "Offline"
    LoginStatus.UNKNOWN -> "Unknown"
}

@Composable
fun ServerStatus.toStringResource() = when (this) {
    ServerStatus.ONLINE -> "online"
    ServerStatus.OFFLINE -> "offline"
    ServerStatus.LOCKED -> "locked"
    ServerStatus.UNKNOWN -> "unknown"
}

fun LoginStatus?.toColor() = when (this) {
    LoginStatus.ONLINE -> positive
    LoginStatus.OFFLINE -> negative
    LoginStatus.UNKNOWN, null -> undefined
}

fun ServerStatus?.toColor() = when (this) {
    ServerStatus.ONLINE -> positive
    ServerStatus.OFFLINE -> negative
    ServerStatus.LOCKED -> warning
    ServerStatus.UNKNOWN, null -> undefined
}

fun KillType?.toColor() = when (this) {
    KillType.KILL -> positive
    KillType.KILLEDBY, KillType.SUICIDE, KillType.UNKNOWN, null -> negative
}

fun MedalType?.toImageRes() = when (this) {
    MedalType.AURAXIUM -> "medal_araxium.webp"
    MedalType.GOLD -> "medal_gold.webp"
    MedalType.SILVER -> "medal_silver.webp"
    MedalType.BRONCE -> "medal_copper.webp"
    MedalType.NONE, null -> "medal_empty.webp"
}

fun CharacterClass.toImageRes() = when (this) {
    CharacterClass.LIGHT_ASSAULT -> "icon_lia.webp"
    CharacterClass.ENGINEER -> "icon_eng.webp"
    CharacterClass.MEDIC -> "icon_med.webp"
    CharacterClass.INFILTRATOR -> "icon_inf.webp"
    CharacterClass.HEAVY_ASSAULT -> "icon_hea.webp"
    CharacterClass.MAX -> "icon_max.webp"
    CharacterClass.UNKNOWN -> "icon_lia.webp"
}

@Composable
fun Population.toStringResource(): String {
    return when (this) {
        Population.HIGH -> "high"
        Population.MEDIUM -> "medium"
        Population.LOW -> "low"
        Population.UNKNOWN -> "unknown"
    }
}

@Composable
fun Population.toColor() = when (this) {
    Population.HIGH -> positive
    Population.MEDIUM -> warning
    Population.LOW -> warning
    Population.UNKNOWN -> undefined
}
