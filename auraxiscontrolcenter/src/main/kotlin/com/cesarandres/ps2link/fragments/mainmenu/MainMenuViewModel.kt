package com.cesarandres.ps2link.fragments.mainmenu

import android.app.Application
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
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
import com.cramsan.ps2link.appcore.DBGServiceClient
import com.cramsan.ps2link.appcore.dbg.Namespace
import com.cramsan.ps2link.appcore.preferences.PS2Settings
import com.cramsan.ps2link.appcore.sqldelight.DbgDAO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainMenuViewModel @ViewModelInject constructor(
    application: Application,
    dbgCensus: DBGServiceClient,
    dbgDAO: DbgDAO,
    pS2Settings: PS2Settings,
    dispatcherProvider: DispatcherProvider,
    @Assisted savedStateHandle: SavedStateHandle
) : BasePS2ViewModel(
        application,
        dbgCensus,
        dbgDAO,
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

    val preferredProfile = _preferredProfileId.map { profileId ->
        profileId?.let {
            val namespace = ps2Settings.getPreferredNamespace()

            assertNotNull(namespace, logTag, "Namespace cannot be null")

            if (namespace != null) {
                dbgDAO.getCharacter(it, namespace)
            } else {
                null
            }
        }
    }
    val preferredOutfit = _preferredOutfitId.map { outfitId ->
        outfitId?.let {
            val namespace = ps2Settings.getPreferredNamespace()

            assertNotNull(namespace, logTag, "Namespace cannot be null")

            if (namespace != null) {
                dbgDAO.getOutfit(it, namespace)
            } else {
                null
            }
        }
    }

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
