package com.cesarandres.ps2link.fragments.outfitpager.members

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.fragment.app.viewModels
import com.cesarandres.ps2link.base.BaseComposePS2Fragment
import com.cramsan.ps2link.appfrontend.outfitpager.members.MemberListCompose
import com.cramsan.ps2link.core.models.Namespace
import dagger.hilt.android.AndroidEntryPoint
import kotlin.time.ExperimentalTime

/**
 * Fragment to display the list of locally stored profiles.
 */
@AndroidEntryPoint
class FragmentComposeMembers : BaseComposePS2Fragment<MembersAndroidViewModel>() {

    override val logTag = "FragmentComposeMembers"
    override val viewModel: MembersAndroidViewModel by viewModels()

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val outfitId = arguments?.getString(OUTFIT_ID_KEY)
        val namespace = arguments?.getSerializable(NAMESPACE_KEY) as Namespace?

        viewModel.setUp(outfitId, namespace)
    }

    @OptIn(ExperimentalTime::class)
    @Composable
    override fun CreateComposeContent() {
        val memberList = viewModel.memberList.collectAsState()
        val isLoading = viewModel.isLoading.collectAsState()
        val isError = viewModel.isError.collectAsState()
        MemberListCompose(
            memberList = memberList.value,
            isLoading = isLoading.value,
            isError = isError.value,
            eventHandler = viewModel,
        )
    }

    companion object {

        private const val OUTFIT_ID_KEY = "outfitId"
        private const val NAMESPACE_KEY = "namespace"

        fun instance(outfitId: String, namespace: Namespace): FragmentComposeMembers {
            val bundle = Bundle().apply {
                putString(OUTFIT_ID_KEY, outfitId)
                putSerializable(NAMESPACE_KEY, namespace)
            }
            return FragmentComposeMembers().apply {
                arguments = bundle
            }
        }
    }
}
