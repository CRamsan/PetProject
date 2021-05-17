package com.cesarandres.ps2link.fragments.mainmenu

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.cesarandres.ps2link.base.BasePS2ViewModel
import com.cesarandres.ps2link.fragments.OpenAbout
import com.cesarandres.ps2link.fragments.OpenOutfit
import com.cesarandres.ps2link.fragments.OpenOutfitList
import com.cesarandres.ps2link.fragments.OpenProfile
import com.cesarandres.ps2link.fragments.OpenProfileList
import com.cesarandres.ps2link.fragments.OpenReddit
import com.cesarandres.ps2link.fragments.OpenServerList
import com.cesarandres.ps2link.fragments.OpenTwitter
import com.cramsan.framework.assert.assertNotNull
import com.cramsan.framework.core.DispatcherProvider
import com.cramsan.ps2link.appcore.preferences.PS2Settings
import com.cramsan.ps2link.appcore.repository.PS2LinkRepository
import com.cramsan.ps2link.core.models.CensusLang
import com.cramsan.ps2link.core.models.Character
import com.cramsan.ps2link.core.models.Namespace
import com.cramsan.ps2link.core.models.Outfit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainMenuViewModel @Inject constructor(
    application: Application,
    pS2LinkRepository: PS2LinkRepository,
    pS2Settings: PS2Settings,
    dispatcherProvider: DispatcherProvider,
    savedStateHandle: SavedStateHandle
) : BasePS2ViewModel(
    application,
    pS2LinkRepository,
    pS2Settings,
    dispatcherProvider,
    savedStateHandle
),
    MainMenuEventHandler {

    override val logTag: String
        get() = "MainMenuViewModel"

    // State
    private val _preferredProfileId = MutableStateFlow<String?>(null)
    private val _preferredOutfitId = MutableStateFlow<String?>(null)

    val preferredProfile: Flow<Character?> = _preferredProfileId.transform { profileId ->
        if (profileId.isNullOrBlank()) {
            emit(null)
            return@transform
        }

        emit(Character(profileId, cached = false))
        val namespace = ps2Settings.getPreferredProfileNamespace()

        assertNotNull(namespace, logTag, "Namespace cannot be null")

        if (namespace != null) {
            // TODO: Fix wrong language
            emit(pS2LinkRepository.getCharacter(profileId, namespace, CensusLang.EN))
        } else {
            emit(null)
        }
    }.flowOn(dispatcherProvider.ioDispatcher())

    val preferredOutfit: Flow<Outfit?> = _preferredOutfitId.transform { outfitId ->
        if (outfitId.isNullOrBlank()) {
            emit(null)
            return@transform
        }

        emit(Outfit(outfitId))
        val namespace = ps2Settings.getPreferredOutfitNamespace()

        assertNotNull(namespace, logTag, "Namespace cannot be null")

        if (namespace != null) {
            // TODO: Fix wrong language
            emit(pS2LinkRepository.getOutfit(outfitId, namespace, CensusLang.EN))
        } else {
            emit(null)
        }
    }.flowOn(dispatcherProvider.ioDispatcher())

    fun updateUI() {
        viewModelScope.launch {
            _preferredProfileId.value = ps2Settings.getPreferredCharacterId()
            _preferredOutfitId.value = ps2Settings.getPreferredOutfitId()
        }
    }

    override fun onPreferredProfileClick(characterId: String, namespace: Namespace) {
        events.value = OpenProfile(characterId, namespace)
    }

    override fun onPreferredOutfitClick(outfitId: String, namespace: Namespace) {
        events.value = OpenOutfit(outfitId, namespace)
    }

    override fun onProfileClick() {
        events.value = OpenProfileList
    }

    override fun onServersClick() {
        events.value = OpenServerList
    }

    override fun onOutfitsClick() {
        events.value = OpenOutfitList
    }

    override fun onTwitterClick() {
        events.value = OpenTwitter
    }

    override fun onRedditClick() {
        events.value = OpenReddit
    }

    override fun onAboutClick() {
        events.value = OpenAbout
    }
}
