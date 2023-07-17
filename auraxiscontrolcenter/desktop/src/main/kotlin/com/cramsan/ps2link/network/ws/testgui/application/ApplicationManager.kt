package com.cramsan.ps2link.network.ws.testgui.application

import com.cramsan.framework.logging.logD
import com.cramsan.framework.logging.logE
import com.cramsan.framework.logging.logI
import com.cramsan.framework.logging.logW
import com.cramsan.framework.preferences.Preferences
import com.cramsan.ps2link.appcore.preferences.PS2Settings
import com.cramsan.ps2link.appcore.repository.PS2LinkRepository
import com.cramsan.ps2link.core.models.Namespace
import com.cramsan.ps2link.network.ws.StreamingClient
import com.cramsan.ps2link.network.ws.StreamingClientEventHandler
import com.cramsan.ps2link.network.ws.messages.AchievementEarned
import com.cramsan.ps2link.network.ws.messages.BattleRankUp
import com.cramsan.ps2link.network.ws.messages.CharacterSubscribe
import com.cramsan.ps2link.network.ws.messages.ConnectionStateChanged
import com.cramsan.ps2link.network.ws.messages.ContinentLock
import com.cramsan.ps2link.network.ws.messages.ContinentUnlock
import com.cramsan.ps2link.network.ws.messages.Death
import com.cramsan.ps2link.network.ws.messages.EventType
import com.cramsan.ps2link.network.ws.messages.FacilityControl
import com.cramsan.ps2link.network.ws.messages.GainExperience
import com.cramsan.ps2link.network.ws.messages.Heartbeat
import com.cramsan.ps2link.network.ws.messages.ItemAdded
import com.cramsan.ps2link.network.ws.messages.MetagameEvent
import com.cramsan.ps2link.network.ws.messages.PlayerFacilityCapture
import com.cramsan.ps2link.network.ws.messages.PlayerFacilityDefend
import com.cramsan.ps2link.network.ws.messages.PlayerLogin
import com.cramsan.ps2link.network.ws.messages.PlayerLogout
import com.cramsan.ps2link.network.ws.messages.ServerEvent
import com.cramsan.ps2link.network.ws.messages.ServerEventPayload
import com.cramsan.ps2link.network.ws.messages.ServerEventPayloadV2
import com.cramsan.ps2link.network.ws.messages.ServiceMessage
import com.cramsan.ps2link.network.ws.messages.ServiceStateChanged
import com.cramsan.ps2link.network.ws.messages.SkillAdded
import com.cramsan.ps2link.network.ws.messages.SubscriptionConfirmation
import com.cramsan.ps2link.network.ws.messages.UnhandledEvent
import com.cramsan.ps2link.network.ws.messages.VehicleDestroy
import com.cramsan.ps2link.network.ws.testgui.Constants
import com.cramsan.ps2link.network.ws.testgui.filelogger.BufferedFileLog
import com.cramsan.ps2link.network.ws.testgui.filelogger.FileLog
import com.cramsan.ps2link.network.ws.testgui.hoykeys.HotKeyManager
import com.cramsan.ps2link.network.ws.testgui.ui.ApplicationUIModel
import com.cramsan.ps2link.network.ws.testgui.ui.dialogs.PS2DialogType
import com.cramsan.ps2link.network.ws.testgui.ui.screens.tracker.PlayerEvent
import com.cramsan.ps2link.network.ws.testgui.ui.tabs.ApplicationTabUIModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.io.File
import javax.swing.JFrame
import kotlin.system.exitProcess

/**
 * Manager class that provides high level access to all the functionalities of the application.
 *
 * @author cramsan
 */
@Suppress("TooManyFunctions")
class ApplicationManager(
    private val streamingClient: StreamingClient,
    private val hotKeyManager: HotKeyManager,
    private val gameSessionManager: GameSessionManager,
    private val ps2Preferences: PS2Settings,
    private val preferences: Preferences,
    private val ps2Repository: PS2LinkRepository,
    private val coroutineScope: CoroutineScope,
) : StreamingClientEventHandler {

    private var isClientReady = false

    private var window: JFrame? = null

    private val eventHandlers = mutableSetOf<ApplicationManagerCallback>()

    private val applicationState = MutableStateFlow(
        ApplicationUIModel.State(
            programMode = ProgramMode.NOT_CONFIGURED,
            debugModeEnabled = false,
            selectedTab = ApplicationTabUIModel.Profile(null, null, true),
            profileTab = ApplicationTabUIModel.Profile(null, null, true),
            outfitTab = ApplicationTabUIModel.Outfit(null, null, true),
            trackerTab = ApplicationTabUIModel.Tracker(null, null, true)
        ),
    )

    private val trayUIModel = MutableStateFlow(
        ApplicationUIModel.TrayUIModel(
            statusLabel = applicationState.value.programMode.toFriendlyString(),
            actionLabel = applicationState.value.programMode.toActionLabel(),
            iconPath = pathForStatus(ProgramMode.NOT_CONFIGURED),
        ),
    )

    private val windowUIModel = MutableStateFlow(
        ApplicationUIModel.WindowUIModel(
            isVisible = true,
            iconPath = "icon_large.png",
            dialogUIModel = null,
            title = "",
            showAddButton = false,
        ),
    )

    val uiModel = combine(
        windowUIModel,
        trayUIModel,
        applicationState,
    ) { t1, t2, t3, ->
        ApplicationUIModel(t1, t2, t3)
    }.stateIn(
        coroutineScope,
        SharingStarted.Companion.Eagerly,
        ApplicationUIModel(
            windowUIModel = windowUIModel.value,
            trayUIModel = trayUIModel.value,
            state = applicationState.value,
        ),
    )

    private var observeJob: Job? = null

    private var fileLog: FileLog? = null

    /**
     * Start the application.
     */
    fun startApplication() {
        initialize()
        // Load all hotkeys
        hotKeyManager.loadFromPreferences()
        // Register for events from the WS client
        streamingClient.registerListener(this)
        // Load the character if stored from a previous session.
    }

    private fun initialize() {
        val inDebugMode = preferences.loadString(Constants.DEBUG_MODE_PREF_KEY).toBoolean()
        changeDebugMode(inDebugMode)
        setProgramMode(ProgramMode.PAUSED)

        coroutineScope.launch {
            val cachedOutfitId = ps2Preferences.getPreferredOutfitId()
            val cachedOutfitNamespace = ps2Preferences.getPreferredOutfitNamespace()
            val cachedCharacterId = ps2Preferences.getPreferredCharacterId()
            val cachedCharacterNamespace = ps2Preferences.getPreferredProfileNamespace()
            if (cachedOutfitId != null && cachedOutfitNamespace != null) {
                openOutfit(cachedOutfitId, cachedOutfitNamespace)
            }
            if (cachedCharacterId != null && cachedCharacterNamespace != null) {
                openProfile(cachedCharacterId, cachedCharacterNamespace)
            }
        }
    }

    private fun setProgramMode(programMode: ProgramMode) {
        logD(TAG, "Setting program mode: $programMode")
        applicationState.value = applicationState.value.copy(
            programMode = programMode,
        )
        trayUIModel.value = trayUIModel.value.copy(
            statusLabel = programMode.toFriendlyString(),
            actionLabel = programMode.toActionLabel(),
            iconPath = pathForStatus(programMode),
        )
        eventHandlers.forEach { it.onProgramModeChanged(programMode) }
    }

    fun registerCallback(callback: ApplicationManagerCallback) {
        eventHandlers.add(callback)
    }

    fun deregisterCallback(callback: ApplicationManagerCallback) {
        eventHandlers.remove(callback)
    }

    /**
     * Start listening for events for the selected character.
     */
    private fun startListening() {
        val characterId = uiModel.value.state.trackerTab.characterId
        val namespace = uiModel.value.state.trackerTab.namespace

        if (characterId == null || namespace == null) {
            isClientReady = false
            setProgramMode(ProgramMode.NOT_CONFIGURED)
            return
        }

        isClientReady = false
        setProgramMode(ProgramMode.LOADING)
        coroutineScope.launch {
            try {
                streamingClient.start()

                for (i in 0..5) {
                    delay(1000)
                    logI(TAG, "Waiting for client to be ready to receive events.")
                    if (isClientReady) {
                        break
                    }
                }

                val fileLog = getLatestLogFile(characterId, namespace) ?: return@launch
                this@ApplicationManager.fileLog = fileLog
                eventHandlers.forEach { it.onFileLogActive(fileLog) }

                streamingClient.sendMessage(
                    CharacterSubscribe(
                        characters = listOf(characterId),
                        eventNames = listOf(
                            EventType.ACHIEVEMENT_EARNED,
                            EventType.DEATH,
                            EventType.BATTLE_RANK_UP,
                        ),
                    ),
                )
                setProgramMode(ProgramMode.RUNNING)
            } catch (throwable: Throwable) {
                pauseListening()
                logE(TAG, "Unexpected error in main loop for WS client.", throwable)
            }
        }
    }

    private fun getLatestLogFile(characterId: String, namespace: Namespace): FileLog? {
        return BufferedFileLog("$characterId-$namespace.log")
    }

    /**
     * Pause from listening for events.
     */
    private fun pauseListening() {
        streamingClient.stop()
        setProgramMode(ProgramMode.PAUSED)
    }

    override fun onServerEventReceived(serverEvent: ServerEvent) {
        when (serverEvent) {
            is ConnectionStateChanged -> Unit
            is Heartbeat -> Unit
            is ServiceMessage<*> -> { handleServerEventPayload(serverEvent.payload) }
            is ServiceStateChanged -> { isClientReady = true }
            is SubscriptionConfirmation -> Unit
            is UnhandledEvent -> Unit
        }
    }

    @Suppress("ComplexMethod")
    private fun handleServerEventPayload(payload: ServerEventPayload?) {
        when (payload) {
            is AchievementEarned -> Unit
            is BattleRankUp -> Unit
            is ContinentLock -> Unit
            is ContinentUnlock -> Unit
            is Death -> Unit
            is FacilityControl -> Unit
            is GainExperience -> {
                gameSessionManager.onExperienceGained(payload)
            }
            is ItemAdded -> Unit
            is MetagameEvent -> Unit
            is PlayerFacilityCapture -> Unit
            is PlayerFacilityDefend -> Unit
            is PlayerLogin -> Unit
            is PlayerLogout -> Unit
            is SkillAdded -> Unit
            is VehicleDestroy -> Unit
            null -> Unit
            is ServerEventPayloadV2.DeathV2 -> {
                val characterId = uiModel.value.state.trackerTab.characterId
                if (payload.characterId != characterId) {
                    gameSessionManager.onPlayerDeathEvent(payload)
                }
            }
        }

        if (payload != null) {
            eventHandlers.forEach { it.onServerEventPayload(payload) }
            coroutineScope.launch {
                val playerEvent = payload.toPlayerEvent()
                playerEvent?.let { fileLog?.addLine(it) }
            }
        }
    }

    /**
     * Exit the application.
     */
    fun exitApplication() {
        logI(TAG, "Closing program.")
        exitProcess(0)
    }

    /**
     * Close the main window.
     */
    fun closeWindow() {
        windowUIModel.value = windowUIModel.value.copy(
            isVisible = false,
        )
    }

    /**
     * Open the main window.
     */
    fun openWindow() {
        logD(TAG, "Trying to open window")
        windowUIModel.value = windowUIModel.value.copy(
            isVisible = true,
        )
        window?.toFront()
    }

    /**
     * Execute the single tray action available to the user.
     */
    fun onTrayAction() {
        when (uiModel.value.state.programMode) {
            ProgramMode.NOT_CONFIGURED -> openWindow()
            ProgramMode.LOADING -> openWindow()
            ProgramMode.RUNNING -> pauseListening()
            ProgramMode.PAUSED -> startListening()
        }
    }

    /**
     * Register a [window] so we can execute some operations based on the applications state.
     */
    fun registerWindow(window: JFrame) {
        this.window = window
    }

    /**
     * Deregister the referenced window.
     */
    fun deregisterWindow() {
        window = null
    }

    fun openFolder(path: String) {
        val directory = File(path)
        if (!directory.exists() || !directory.isDirectory) {
            logW(TAG, "Cannot open directory $path")
            return
        }
        Desktop.getDesktop().open(directory)
    }

    fun changeDebugMode(debugEnabled: Boolean) {
        preferences.saveString(Constants.DEBUG_MODE_PREF_KEY, debugEnabled.toString())
        applicationState.value = applicationState.value.copy(
            debugModeEnabled = debugEnabled,
        )
    }

    companion object {
        private const val TAG = "ApplicationManager"
    }

    fun openProfile(newCharacterId: String?, newNamespace: Namespace?) {
        val existingTab = applicationState.value.profileTab

        val characterId = newCharacterId ?: existingTab.characterId
        val namespace = newNamespace ?: existingTab.namespace
        val showFTE = characterId == null || namespace == null

        coroutineScope.launch {
            ps2Preferences.updatePreferredCharacterId(characterId)
            ps2Preferences.updatePreferredProfileNamespace(namespace)
        }

        val newSelectedTab = ApplicationTabUIModel.Profile(characterId, namespace, showFTE)
        applicationState.value = applicationState.value.copy(
            profileTab = newSelectedTab,
            selectedTab = newSelectedTab,
        )
        windowUIModel.value = windowUIModel.value.copy(
            dialogUIModel = null,
            title = "",
            showAddButton = true
        )
        loadLightweightCharacter(characterId, namespace)
        if (characterId != existingTab.characterId || namespace != existingTab.namespace) {
            eventHandlers.forEach { it.onCharacterSelected(characterId, namespace) }
        }
    }

    private fun loadLightweightCharacter(characterId: String?, namespace: Namespace?) {
        observeJob?.cancel()
        if (characterId == null || namespace == null) {
            windowUIModel.value = windowUIModel.value.copy(title = "")
            return
        }

        observeJob = coroutineScope.launch {
            ps2Repository.getCharacterAsFlow(characterId, namespace).onEach {
                windowUIModel.value = windowUIModel.value.copy(
                    title = it?.name ?: ""
                )
            }.collect()
        }
    }

    fun openOutfit(newOutfitId: String?, newNamespace: Namespace?) {
        val existingTab = applicationState.value.outfitTab

        val outfitId = newOutfitId ?: existingTab.outfitId
        val namespace = newNamespace ?: existingTab.namespace

        val showFTE = outfitId == null || namespace == null

        val newSelectedTab = ApplicationTabUIModel.Outfit(outfitId, namespace, showFTE)
        coroutineScope.launch {
            ps2Preferences.updatePreferredOutfitId(outfitId)
            ps2Preferences.updatePreferredOutfitNamespace(namespace)
        }

        applicationState.value = applicationState.value.copy(
            outfitTab = newSelectedTab,
            selectedTab = newSelectedTab,
        )
        windowUIModel.value = windowUIModel.value.copy(
            showAddButton = true,
            title = "",
            dialogUIModel = null,
        )
        loadLightweightOutfit(outfitId, namespace)
        if (outfitId != existingTab.outfitId || namespace != existingTab.namespace) {
            eventHandlers.forEach { it.onOutfitSelected(outfitId, namespace) }
        }
    }

    fun openTracker(newCharacterId: String?, newNamespace: Namespace?) {
        val existingTab = applicationState.value.trackerTab

        val characterId = newCharacterId ?: existingTab.characterId
        val namespace = newNamespace ?: existingTab.namespace

        val showFTE = characterId == null || namespace == null

        val newTab = ApplicationTabUIModel.Tracker(
            characterId = characterId,
            namespace = namespace,
            showFTE = showFTE,
        )

        val modifiableState = isModifiableProgramMode(applicationState.value.programMode)
        if (applicationState.value.trackerTab != newTab) {
            if (!modifiableState) {
                pauseListening()
            }
            applicationState.value = applicationState.value.copy(
                selectedTab = newTab,
                trackerTab = newTab
            )
            eventHandlers.forEach {
                it.onTrackedCharacterSelected(
                    newTab.characterId ?: "",
                    newTab.namespace ?: Namespace.UNDETERMINED,
                )
            }
        } else {
            applicationState.value = applicationState.value.copy(
                selectedTab = newTab,
            )
        }

        windowUIModel.value = windowUIModel.value.copy(
            dialogUIModel = null,
            title = "",
            showAddButton = true,
        )
    }

    fun openSettings() {
        applicationState.value = applicationState.value.copy(
            selectedTab = ApplicationTabUIModel.Settings,
        )

        windowUIModel.value = windowUIModel.value.copy(
            dialogUIModel = null,
            title = "",
            showAddButton = false,
        )
    }

    private fun loadLightweightOutfit(outfitId: String?, namespace: Namespace?) {
        observeJob?.cancel()
        if (outfitId == null || namespace == null) {
            windowUIModel.value = windowUIModel.value.copy(title = "")
            return
        }

        observeJob = coroutineScope.launch {
            ps2Repository.getOutfitAsFlow(outfitId, namespace).onEach {
                windowUIModel.value = windowUIModel.value.copy(
                    title = it?.name ?: ""
                )
            }.collect()
        }
    }

    fun minimizeWindow() {
        closeWindow()
    }

    fun closeProgram() {
        exitApplication()
    }

    fun dismissDialog() {
        windowUIModel.value = windowUIModel.value.copy(
            dialogUIModel = null
        )
    }

    fun openSearch() {
        when (applicationState.value.selectedTab) {
            is ApplicationTabUIModel.Profile -> {
                windowUIModel.value = windowUIModel.value.copy(
                    dialogUIModel = ApplicationUIModel.DialogUIModel(PS2DialogType.ADD_PROFILE)
                )
            }
            is ApplicationTabUIModel.Outfit -> {
                windowUIModel.value = windowUIModel.value.copy(
                    dialogUIModel = ApplicationUIModel.DialogUIModel(PS2DialogType.ADD_OUTFIT)
                )
            }
            is ApplicationTabUIModel.Settings -> Unit
            is ApplicationTabUIModel.Tracker -> {
                windowUIModel.value = windowUIModel.value.copy(
                    dialogUIModel = ApplicationUIModel.DialogUIModel(PS2DialogType.SEARCH_PROFILE_TRACKER)
                )
            }
        }
    }

    private fun isModifiableProgramMode(programMode: ProgramMode): Boolean {
        return programMode == ProgramMode.NOT_CONFIGURED || programMode == ProgramMode.PAUSED
    }
}

@Suppress("CyclomaticComplexMethod", "LongMethod")
private fun ServerEventPayload.toPlayerEvent(): PlayerEvent? {
    return when (this) {
        is BattleRankUp -> PlayerEvent.BattleRankUp(
            timestamp = timestamp,
            worldId = worldId,
            zoneId = zoneId,
            battleRank = battleRank,
            characterId = characterId,
        )
        is Death -> null
        is FacilityControl -> null
        is MetagameEvent -> null
        is ContinentLock -> null
        is ContinentUnlock -> null
        is ServerEventPayloadV2.DeathV2 -> PlayerEvent.Death(
            attackerCharacterId = attackerCharacterId,
            attackerCharacterName = attackerCharacterName,
            attackerCharacterRank = attackerCharacterRank,
            attackerCharacterFaction = attackerCharacterFaction,
            attackerFireModeId = attackerFireModeId,
            attackerLoadoutId = attackerLoadoutId,
            attackerVehicleId = attackerVehicleId,
            attackerWeaponId = attackerWeaponId,
            attackerWeaponName = attackerWeaponName,
            attackerWeaponImageUrl = attackerWeaponImageUrl,
            characterId = characterId,
            characterName = characterName,
            characterRank = characterRank,
            characterFaction = characterFaction,
            characterLoadoutId = characterLoadoutId,
            isCritical = isCritical,
            isHeadshot = isHeadshot,
            timestamp = timestamp,
            vehicleId = vehicleId,
            worldId = worldId,
            zoneId = zoneId,
        )
        is AchievementEarned -> null
        is GainExperience -> null
        is ItemAdded -> null
        is PlayerFacilityCapture -> null
        is PlayerFacilityDefend -> null
        is PlayerLogin -> null
        is PlayerLogout -> null
        is SkillAdded -> null
        is VehicleDestroy -> null
    }
}
