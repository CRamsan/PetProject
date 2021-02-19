package com.cesarandres.ps2link.fragments.outfitlist

import android.app.Application
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.cesarandres.ps2link.base.BasePS2ViewModel
import com.cesarandres.ps2link.fragments.OpenProfile
import com.cesarandres.ps2link.fragments.OpenProfileSearch
import com.cramsan.framework.core.DispatcherProvider
import com.cramsan.ps2link.appcore.preferences.PS2Settings
import com.cramsan.ps2link.appcore.repository.PS2LinkRepository
import com.cramsan.ps2link.core.models.Namespace

class OutfitListViewModel @ViewModelInject constructor(
    application: Application,
    pS2LinkRepository: PS2LinkRepository,
    pS2Settings: PS2Settings,
    dispatcherProvider: DispatcherProvider,
    @Assisted savedStateHandle: SavedStateHandle,
) : BasePS2ViewModel(
        application,
        pS2LinkRepository,
        pS2Settings,
        dispatcherProvider,
        savedStateHandle
    ),
    OutfitListEventHandler {

    override val logTag: String
        get() = "OutfitListViewModel"

    // State
    private val _outfitList = pS2LinkRepository.getAllOutfitsAsFlow()
    val outfitList = _outfitList.asLiveData()

    override fun onSearchOutfitClick() {
        events.value = OpenProfileSearch
    }

    override fun onOutfitSelected(outfitId: String, namespace: Namespace) {
        events.value = OpenProfile(outfitId, namespace)
    }
}
