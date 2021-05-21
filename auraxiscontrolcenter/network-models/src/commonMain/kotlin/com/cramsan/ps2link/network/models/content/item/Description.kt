package com.cramsan.ps2link.network.models.content.item

import kotlinx.serialization.Serializable

@Serializable
data class Description(
    var de: String? = null,
    var en: String? = null,
    var es: String? = null,
    var fr: String? = null,
    var it: String? = null,
    var tr: String? = null,
)
