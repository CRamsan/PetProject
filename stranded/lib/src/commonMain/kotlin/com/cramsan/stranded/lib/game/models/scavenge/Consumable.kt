package com.cramsan.stranded.lib.game.models.scavenge

import com.cramsan.stranded.lib.game.models.common.Food
import com.cramsan.stranded.lib.game.models.common.Status
import kotlinx.serialization.Serializable

/**
 * This is a [ScavengeResult] of type [Food].
 */
@Serializable
class Consumable(
    val __title: String,
    override var remainingDays: Int,
    override var healthModifier: Int,
    override var statusModifier: Status,
    override var remainingUses: Int
) : Food, ScavengeResult(__title)
