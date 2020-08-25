package com.cramsan.awslib.entitymanager.implementation

import com.cramsan.awslib.ai.`interface`.AIRepo
import com.cramsan.awslib.ai.implementation.DummyAIRepoImpl
import com.cramsan.awslib.entity.GameEntityInterface
import com.cramsan.awslib.entitymanager.EntityManagerEventListener
import com.cramsan.awslib.entitymanager.EntityManagerInteractionReceiver
import com.cramsan.awslib.entitymanager.EntityManagerInterface
import com.cramsan.awslib.enums.Direction
import com.cramsan.awslib.enums.EntityType
import com.cramsan.awslib.enums.TerrainType
import com.cramsan.awslib.enums.TurnActionType
import com.cramsan.awslib.eventsystem.GameEntityTrigger
import com.cramsan.awslib.eventsystem.events.BaseEvent
import com.cramsan.awslib.eventsystem.events.ChangeTriggerEvent
import com.cramsan.awslib.eventsystem.events.EventType
import com.cramsan.awslib.eventsystem.events.InteractiveEvent
import com.cramsan.awslib.eventsystem.events.InteractiveEventOption
import com.cramsan.awslib.eventsystem.events.NoopEvent
import com.cramsan.awslib.eventsystem.events.SwapEntityEvent
import com.cramsan.awslib.eventsystem.triggers.CellTrigger
import com.cramsan.awslib.eventsystem.triggers.Trigger
import com.cramsan.awslib.map.GameMap
import com.cramsan.awslib.scene.SceneEventsCallback
import com.cramsan.awslib.utils.constants.InitialValues
import com.cramsan.framework.logging.EventLoggerInterface
import kotlinx.coroutines.channels.Channel
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class EntityManager(
    private val map: GameMap,
    private val triggerList: List<Trigger>,
    private val eventList: List<BaseEvent>,
    private var eventListener: EntityManagerEventListener?,
    override val di: DI,
    private val aiRepo: AIRepo = DummyAIRepoImpl(di)
) : EntityManagerInterface, EntityManagerInteractionReceiver, DIAware {

    private val log: EventLoggerInterface by instance()

    val entityTriggerMap: HashMap<Int, GameEntityTrigger> = HashMap()

    var triggerMap: Array<Array<CellTrigger?>> = Array(map.width) { arrayOfNulls<CellTrigger?>(map.height) }
    val triggerIdMap: HashMap<Int, Trigger> = HashMap()

    val eventMap: HashMap<Int, BaseEvent> = HashMap()
    var nextEvent: BaseEvent? = null
    val channel = Channel<BaseEvent>()
    private val tag = "EntityManager"

    init {
        triggerList.forEach {
            log.d(tag, "Trigger: $it")

            triggerIdMap.put(it.id, it)
            if (it is GameEntityTrigger) {
                entityTriggerMap.put(it.targetId, it)
            } else if (it is CellTrigger) {
                triggerMap[it.posX][it.posY] = it
            }
        }
        eventList.forEach {
            log.d(tag, "Event: $it")
            eventMap.put(it.id, it)
        }
    }

    val entitySet = mutableSetOf<GameEntityInterface>()
    val disabledEntitySet = mutableSetOf<GameEntityInterface>()

    val queue = mutableListOf<GameEntityInterface>()

    val removeSet = mutableSetOf<GameEntityInterface>()
    val addSet = mutableSetOf<GameEntityInterface>()

    var entityMap: Array<Array<GameEntityInterface?>> = Array(map.width) { arrayOfNulls<GameEntityInterface?>(map.height) }
    var entityIdMap: MutableMap<Int, GameEntityInterface> = mutableMapOf()

    override fun register(entity: GameEntityInterface): Boolean {
        if (!entity.enabled) {
            disabledEntitySet.add(entity)
            return true
        }

        if (isBlocked(entity.posX, entity.posY)) {
            return false
        }

        if (entitySet.contains(entity)) {
            return false
        }

        if (entityIdMap.containsKey(entity.id)) {
            return false
        }

        entitySet.add(entity)
        entityIdMap[entity.id] = entity
        queue.add(entity)
        setPosition(entity, entity.posX, entity.posY)
        return true
    }

    override fun deregister(entity: GameEntityInterface): Boolean {
        if (!entitySet.contains(entity)) {
            return false
        }
        entityIdMap.remove(entity.id)
        queue.remove(entity)
        entityMap[entity.posX][entity.posY] = null
        entitySet.remove(entity)
        disabledEntitySet.add(entity)
        entity.enabled = false
        return true
    }

    override fun setEntityState(entity: GameEntityInterface, enabled: Boolean) {
        if (enabled) {
            prepareForAdd(entity)
        } else {
            prepareForRemove(entity)
        }
    }

    override suspend fun runTurns(callback: SceneEventsCallback?) {
        queue.forEach {
            log.i(tag, "Entity: $it")
            if (it.type != EntityType.PLAYER) {
                if (it.shouldMove) {
                    it.nextTurnAction = aiRepo.getNextTurnAction(it, this@EntityManager, map)
                } else {
                    it.nextTurnAction = TurnAction.NOOP
                }
            }
            if (it.nextTurnAction.turnActionType == TurnActionType.MOVE) {
                move(it, callback)
            } else if (it.nextTurnAction.turnActionType == TurnActionType.ATTACK) {
                act(it, callback)
            } else if (it.nextTurnAction.turnActionType == TurnActionType.NONE) {
                log.i(tag, "Noop action")
            } else {
                TODO("Unexpected TUrnActionType")
            }
        }
    }

    private suspend fun move(entity: GameEntityInterface, callback: SceneEventsCallback?): Boolean {
        log.i(tag, "Moving")
        var x = entity.posX
        var y = entity.posY

        entity.nextTurnAction.direction.apply {
            when (this) {
                Direction.NORTH -> {
                    y--
                }
                Direction.SOUTH -> {
                    y++
                }
                Direction.WEST -> {
                    x--
                }
                Direction.EAST -> {
                    x++
                }
                Direction.KEEP -> {
                }
            }
        }
        log.d(tag, "Trying to move from x:${entity.posX},y:${entity.posY} -> x:$x,y:$y")

        if (map.isBlocked(x, y)) {
            log.d(tag, "Map location is blocked with ${map.cellAt(x, y)}")
            return false
        }

        if (isBlocked(x, y)) {
            log.d(tag, "Map location is blocked with ${getEntity(x, y)}")
            return false
        }

        setPosition(entity, x, y)
        doTileAction(x, y, callback)
        entity.nextTurnAction = TurnAction.NOOP

        callback?.onEntityChanged(entity)

        // TODO: Game end should be done as a trigger
        if (map.cellAt(x, y).terrain == TerrainType.END) {
            callback?.onSceneEnded(true)
        }

        return true
    }

    private suspend fun act(entity: GameEntityInterface, callback: SceneEventsCallback?): Boolean {
        log.i(tag, "Performing action")
        var x = entity.posX
        var y = entity.posY

        when (entity.heading) {
            Direction.NORTH -> {
                y--
            }
            Direction.SOUTH -> {
                y++
            }
            Direction.WEST -> {
                x--
            }
            Direction.EAST -> {
                x++
            }
            Direction.KEEP -> {
            }
        }
        log.d(tag, "Trying to target from x:${entity.posX},y:${entity.posY} -> x:$x,y:$y")

        val targetEntity = getEntity(x, y)
        if (targetEntity != null) {
            log.d(tag, "Target entity: $targetEntity")
            if (targetEntity.group != entity.group) {
                doDamage(entity, targetEntity, callback)
            } else {
                doAction(targetEntity, callback)
            }
        } else {
            val cell = map.cellAt(x, y)
            log.d(tag, "Target cell: $cell")
            cell.onActionTaken()
            callback?.onCellChanged(cell)
        }

        entity.nextTurnAction = TurnAction.NOOP
        return true
    }

    override fun setPosition(entity: GameEntityInterface, poxX: Int, posY: Int): Boolean {
        if (isBlocked(poxX, posY)) {
            return false
        }
        entityMap[entity.posX][entity.posY] = null
        entityMap[poxX][posY] = entity
        entity.posX = poxX
        entity.posY = posY
        return true
    }

    override fun prepareForAdd(entity: GameEntityInterface) {
        entity.enabled = true
        addSet.add(entity)
    }

    override fun prepareForRemove(entity: GameEntityInterface) {
        entity.enabled = false
        removeSet.add(entity)
        entityIdMap.remove(entity.id)
        entityMap[entity.posX][entity.posY] = null
        entitySet.remove(entity)
    }

    override fun doDamage(attacker: GameEntityInterface, defender: GameEntityInterface, callback: SceneEventsCallback?) {
        log.i(tag, "Performing damage")
        defender.health -= attacker.health
        if (defender.health <= 0) {
            log.i(tag, "Entity is dead: $defender")
            defender.health = -1
            prepareForRemove(defender)
            callback?.onEntityChanged(defender)
        }
    }

    private suspend fun doAction(receiver: GameEntityInterface, callback: SceneEventsCallback?) {
        val trigger = entityTriggerMap.getValue(receiver.id)
        log.i(tag, "Performing action to trigger: $trigger")
        if (!trigger.enabled) {
            log.i(tag, "Trigger disabled")
            return
        }

        executeTrigger(trigger, callback)
    }

    private suspend fun doTileAction(posX: Int, posY: Int, callback: SceneEventsCallback?) {
        val trigger = triggerMap[posX][posY]
        if (trigger == null || !trigger.enabled) {
            return
        }

        log.i(tag, "Executing trigger: $trigger")
        executeTrigger(trigger, callback)
    }

    override fun handleSwapEntityEvent(event: SwapEntityEvent, callback: SceneEventsCallback?): BaseEvent? {
        val disableId = event.disableId
        val enableId = event.enableId

        val toDisableEntity = entityIdMap[disableId]
        var toEnableEntity: GameEntityInterface? = null
        toDisableEntity?.let { setEntityState(it, false) }
        disabledEntitySet.forEach {
            if (it.id == enableId) {
                setEntityState(it, true)
                toEnableEntity = it
            }
        }
        if (toEnableEntity != null && toDisableEntity != null) {
            toEnableEntity!!.posX = toDisableEntity.posX
            toEnableEntity!!.posY = toDisableEntity.posY
        }
        toDisableEntity?.let { callback?.onEntityChanged(it) }
        toEnableEntity?.let { callback?.onEntityChanged(it) }
        return eventMap[event.nextEventId]
    }

    private suspend fun handleInteractiveEntityEvent(event: InteractiveEvent): BaseEvent? {
        log.i(tag, "Handling Interactive Event: $event")
        log.d(tag, "Text: ${event.text}")
        event.options.forEach {
            log.d(tag, "Option: $it")
        }

        // TODO: Shouldn't this be a non-nullable?
        log.i(tag, "Calling Callback: $eventListener")
        eventListener?.onInteractionRequired(event.text, event.options, this@EntityManager)

        log.d(tag, "Waiting for response from Event: $event")
        val receivedEvent = channel.receive()
        log.d(tag, "Got response from Event: $event")
        log.i(tag, "Received Event: $receivedEvent")
        return if (receivedEvent.type == EventType.NOOP) {
            null
        } else {
            receivedEvent
        }
    }

    override fun handleChangeTriggerEvent(event: ChangeTriggerEvent): BaseEvent? {
        val disableId = event.disableId
        val enableId = event.enableId

        triggerIdMap[enableId]?.enabled = true
        triggerIdMap[disableId]?.enabled = false

        return eventMap[event.nextEventId]
    }

    private suspend fun executeTrigger(trigger: Trigger, callback: SceneEventsCallback?) {
        if (nextEvent != null) {
            throw RuntimeException("Cannot overwrite event")
        }

        nextEvent = eventMap[trigger.eventId]
        while (nextEvent != null) {
            var localNextEvent = nextEvent
            log.i(tag, "Event: $localNextEvent")
            when (localNextEvent) {
                is SwapEntityEvent -> {
                    log.d(tag, "Swap event")
                    nextEvent = handleSwapEntityEvent(localNextEvent, callback)
                }
                is InteractiveEvent -> {
                    log.d(tag, "Interactive event")
                    nextEvent = handleInteractiveEntityEvent(localNextEvent)
                }
                is ChangeTriggerEvent -> {
                    log.d(tag, "Change trigger event")
                    nextEvent = handleChangeTriggerEvent(localNextEvent)
                }
                else -> {
                    TODO("Unexpected Event type")
                }
            }
        }
    }
    override suspend fun selectOption(option: InteractiveEventOption?) {
        log.i(tag, "Selection: $option")
        if (option == null) {
            channel.send(NoopEvent())
            return
        }

        if (option.eventId == InitialValues.INVALID_ID) {
            TODO()
        }

        if (option.eventId == InitialValues.NOOP_ID) {
            log.i(tag, "SelectIotuin INVALID")
            channel.send(NoopEvent())
            return
        }

        val eEvent = eventMap.getValue(option.eventId)
        log.i(tag, "Event: $eEvent")
        channel.send(eEvent)
    }

    override fun processGameEntityState() {
        removeSet.forEach {
            deregister(it)
        }
        removeSet.clear()

        addSet.forEach {
            register(it)
        }
        addSet.clear()
    }

    override fun getEntity(posX: Int, posY: Int): GameEntityInterface? {
        return entityMap[posX][posY]
    }

    override fun isBlocked(posX: Int, posY: Int): Boolean {
        if (posX < 0 || posX > entityMap.lastIndex || posY < 0 || posY > entityMap.first().lastIndex)
            return true

        return entityMap[posX][posY] != null
    }
}
