package com.cesarandres.ps2link.fragments.addoutfit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.fragment.app.viewModels
import com.cesarandres.ps2link.R
import com.cesarandres.ps2link.base.BaseComposePS2Fragment
import com.cramsan.framework.core.requireAppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment to display the list of locally stored outfits.
 */
@AndroidEntryPoint
class FragmentComposeOutfitAdd : BaseComposePS2Fragment<OutfitAddViewModel>() {

    override val logTag = "FragmentComposeOutfitAdd"
    override val viewModel: OutfitAddViewModel by viewModels()

    @Composable
    override fun CreateComposeContent() {
        val tagSearchQueryState = viewModel.tagSearchQuery.collectAsState()
        val nameSearchQueryState = viewModel.nameSearchQuery.collectAsState()
        val profileList = viewModel.outfitList.collectAsState()
        val isLoading = viewModel.isLoading.collectAsState()
        OutfitAddCompose(
            tagSearchField = tagSearchQueryState.value,
            nameSearchField = nameSearchQueryState.value,
            outfitItems = profileList.value,
            isLoading = isLoading.value,
            eventHandler = viewModel
        )
    }

    override fun onResume() {
        super.onResume()
        requireAppCompatActivity().supportActionBar?.title = getString(R.string.title_outfits)
    }
}
