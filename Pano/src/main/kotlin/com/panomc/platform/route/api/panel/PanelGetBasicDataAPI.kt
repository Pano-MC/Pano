package com.panomc.platform.route.api.panel

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.auth.PanelPermission
import com.panomc.platform.config.ConfigManager
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.Permission
import com.panomc.platform.db.model.Server
import com.panomc.platform.model.*
import com.panomc.platform.server.PlatformCodeManager
import io.vertx.ext.web.RoutingContext
import io.vertx.json.schema.SchemaParser

@Endpoint
class PanelGetBasicDataAPI(
    private val authProvider: AuthProvider,
    private val databaseManager: DatabaseManager,
    private val platformCodeManager: PlatformCodeManager,
    private val configManager: ConfigManager
) : PanelApi() {
    override val paths = listOf(Path("/api/panel/basicData", RouteType.GET))

    override fun getValidationHandler(schemaParser: SchemaParser) = null

    override suspend fun handle(context: RoutingContext): Result {
        val userId = authProvider.getUserIdFromRoutingContext(context)

        val sqlClient = getSqlClient()

        val user = databaseManager.userDao.getById(
            userId,
            sqlClient
        )!!

        val count = databaseManager.panelNotificationDao.getCountOfNotReadByUserId(userId, sqlClient)
        val connectedServerCount = databaseManager.serverDao.countOfPermissionGranted(sqlClient)

//        Since it's a panel API, it calls AuthProvider#hasAccessPanel method and these context fields are created
        val isAdmin = context.get<Boolean>("isAdmin") ?: false
        val permissions = context.get<List<Permission>>("permissions") ?: listOf()

        val result: MutableMap<String, Any?> = mutableMapOf(
            "user" to mapOf(
                "username" to user.username,
                "email" to user.email,
                "permissions" to permissions.map { it.name },
                "admin" to isAdmin
            ),
            "website" to mapOf(
                "name" to configManager.config.websiteName,
                "description" to configManager.config.websiteDescription
            ),
            "notificationCount" to count,
            "locale" to configManager.config.locale,
            "connectedServerCount" to connectedServerCount
        )

        if (authProvider.hasPermission(userId, PanelPermission.MANAGE_SERVERS, context)) {
            val mainServerId = databaseManager.systemPropertyDao.getByOption(
                "main_server",
                sqlClient
            )?.value?.toLong()
            var mainServer: Server? = null

            if (mainServerId != null && mainServerId != -1L) {
                mainServer = databaseManager.serverDao.getById(mainServerId, sqlClient)
            }

            val selectedServerPanelConfig =
                databaseManager.panelConfigDao.byUserIdAndOption(userId, "selected_server", sqlClient)
            var selectedServer: Server? = null

            if (selectedServerPanelConfig != null) {
                val selectedServerId = selectedServerPanelConfig.value.toLong()

                selectedServer = databaseManager.serverDao.getById(selectedServerId, sqlClient)
            }

            result["platformServerMatchKey"] = platformCodeManager.getPlatformKey()
            result["platformServerMatchKeyTimeStarted"] = platformCodeManager.getTimeStarted()
            result["platformHostAddress"] = context.request().authority().host()

            result["mainServer"] = mainServer
            result["selectedServer"] = selectedServer
        }

        return Successful(result)
    }
}