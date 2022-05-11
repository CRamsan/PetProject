package com.cramsan.awslib.entity.implementation

import com.cramsan.awslib.utils.constants.InitialValues

/**
 * This class represents the single player of this game. The [id] is hardcoded to be
 * [com.cramsan.awslib.utils.constants.InitialValues.PLAYER_ID]. The [type] is set to [com.cramsan.awslib.enums.EntityType.PLAYER]
 */
class Player(
    posX: Int,
    posY: Int,
    speed: Int,
) :
    Character(
        InitialValues.PLAYER_ID,
        InitialValues.HEALTH_PLAYER,
        InitialValues.GROUP_PLAYER,
        posX,
        posY,
        speed,
        true,
        true,
    ) {
    val equipableItemList = mutableListOf<EquippableItem>()
    val keyItemList = mutableListOf<KeyItem>()
}
