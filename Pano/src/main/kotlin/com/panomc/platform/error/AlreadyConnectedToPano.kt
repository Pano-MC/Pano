package com.panomc.platform.error

import com.panomc.platform.model.Error

class AlreadyConnectedToPano(
    statusMessage: String = "",
    extras: Map<String, Any?> = mapOf()
) : Error(400, statusMessage, extras)