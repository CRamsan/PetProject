package com.cramsan.ps2link.network.ws.testgui.ui.lib.items

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.cramsan.ps2link.core.models.Faction
import com.cramsan.ps2link.core.models.Namespace
import com.cramsan.ps2link.network.ws.testgui.ui.lib.SlimButton
import com.cramsan.ps2link.network.ws.testgui.ui.lib.theme.Padding
import com.cramsan.ps2link.network.ws.testgui.ui.lib.theme.Size
import com.cramsan.ps2link.network.ws.testgui.ui.lib.widgets.BR
import com.cramsan.ps2link.network.ws.testgui.ui.lib.widgets.FactionIcon
import com.cramsan.ps2link.network.ws.testgui.ui.lib.widgets.NamespaceIcon

@Composable
fun ProfileItem(
    modifier: Modifier = Modifier,
    label: String,
    faction: Faction,
    namespace: Namespace,
    level: Int,
    onClick: () -> Unit = {},
) {
    SlimButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FactionIcon(modifier = Modifier.size(Size.xlarge), faction = faction)
            Spacer(modifier = Modifier.width(Padding.medium))
            NamespaceIcon(modifier = Modifier.size(Size.large), namespace = namespace)
            Spacer(modifier = Modifier.weight(5f))
            Text(
                text = label,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.weight(5f))
            BR(
                modifier = Modifier.height(Size.large).width(Size.xlarge),
                level = level,
            )
        }
    }
}
