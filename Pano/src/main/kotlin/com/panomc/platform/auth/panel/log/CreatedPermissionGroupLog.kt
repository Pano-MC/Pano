package com.panomc.platform.auth.panel.log

import com.panomc.platform.db.model.PanelActivityLog
import io.vertx.core.json.JsonObject

class CreatedPermissionGroupLog(
    userId: Long,
    username: String,
    permissionGroupName: String,
) : PanelActivityLog(
    userId = userId,
    details = JsonObject().put("username", username).put("permissionGroupName", permissionGroupName)
)