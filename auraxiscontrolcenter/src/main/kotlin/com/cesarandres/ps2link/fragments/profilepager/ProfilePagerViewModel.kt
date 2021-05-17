package com.cesarandres.ps2link.fragments.profilepager

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.cesarandres.ps2link.base.BasePS2ViewModel
import com.cramsan.framework.core.DispatcherProvider
import com.cramsan.framework.logging.logE
import com.cramsan.ps2link.appcore.preferences.PS2Settings
import com.cramsan.ps2link.appcore.repository.PS2LinkRepository
import com.cramsan.ps2link.core.models.CensusLang
import com.cramsan.ps2link.core.models.Character
import com.cramsan.ps2link.core.models.Namespace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfilePagerViewModel @Inject constructor(
    application: Application,
    pS2LinkRepository: PS2LinkRepository,
    pS2Settings: PS2Settings,
    dispatcherProvider: DispatcherProvider,
    savedStateHandle: SavedStateHandle,
) : BasePS2ViewModel(
    application,
    pS2LinkRepository,
    pS2Settings,
    dispatcherProvider,
    savedStateHandle
) {

    override val logTag: String
        get() = "ProfilePagerViewModel"

    // State
    private val _profile: MutableStateFlow<Character?> = MutableStateFlow(null)
    val profile = _profile.asStateFlow()
    private val _preferredProfile: MutableStateFlow<String?> = MutableStateFlow(null)
    val preferredProfile = _preferredProfile.asStateFlow()
    private lateinit var characterId: String
    private lateinit var namespace: Namespace

    fun setUp(characterId: String?, namespace: Namespace?) {
        if (characterId == null || namespace == null) {
            logE(logTag, "Invalid arguments: characterId=$characterId namespace=$namespace")
            // TODO: Provide some event that can be handled by the UI
            return
        }
        this.characterId = characterId
        this.namespace = namespace
        ioScope.launch {
            val lang = ps2Settings.getCurrentLang() ?: CensusLang.EN
            _profile.value = pS2LinkRepository.getCharacter(characterId, namespace, lang, false)
        }
    }

    suspend fun addCharacter() = withContext(dispatcherProvider.ioDispatcher()) {
        val lang = ps2Settings.getCurrentLang() ?: CensusLang.EN
        val character = pS2LinkRepository.getCharacter(characterId, namespace, lang)
        if (character == null) {
            // TODO : Report error
            return@withContext
        }
        pS2LinkRepository.saveCharacter(character.copy(cached = true))
        _profile.value = _profile.value?.copy(cached = true)
    }

    suspend fun removeCharacter() = withContext(dispatcherProvider.ioDispatcher()) {
        val lang = ps2Settings.getCurrentLang() ?: CensusLang.EN
        val character = pS2LinkRepository.getCharacter(characterId, namespace, lang)
        if (character == null) {
            // TODO : Report error
            return@withContext
        }
        pS2LinkRepository.saveCharacter(character.copy(cached = false))
        _profile.value = _profile.value?.copy(cached = false)
    }

    suspend fun pinCharacter() = withContext(dispatcherProvider.ioDispatcher()) {
        ps2Settings.updatePreferredProfileNamespace(namespace)
        ps2Settings.updatePreferredCharacterId(characterId)
        _preferredProfile.value = characterId
    }

    suspend fun unpinCharacter() = withContext(dispatcherProvider.ioDispatcher()) {
        ps2Settings.updatePreferredProfileNamespace(null)
        ps2Settings.updatePreferredCharacterId(null)
        _preferredProfile.value = null
    }
}
